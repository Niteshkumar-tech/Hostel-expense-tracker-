package hostel.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – singleton helper that provides a live JDBC connection.
 *
 * Edit DB_URL / DB_USER / DB_PASS to match your MySQL setup.
 * The driver is loaded automatically via DriverManager (JDBC 4+).
 */
public class DBConnection {

    // ── Configuration ────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/hostel_tracker"
                                        + "?useSSL=false&allowPublicKeyRetrieval=true"
                                        + "&serverTimezone=UTC";
    private static final String DB_USER = "root";   // change if needed
    private static final String DB_PASS = "";   // change if needed

    // ── Singleton ────────────────────────────────────────────────────────────
    private static Connection connection = null;

    /** Returns a shared Connection, opening it lazily on first call. */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load the MySQL JDBC driver (auto-loaded in JDBC 4+, but explicit is safer)
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("[DB] Connected to MySQL successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL JDBC driver not found! Add mysql-connector-j to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /** Gracefully close the shared connection (call on app shutdown). */
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Prevent instantiation
    private DBConnection() {}
}
