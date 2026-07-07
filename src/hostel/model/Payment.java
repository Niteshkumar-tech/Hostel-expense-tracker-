package hostel.model;

import hostel.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Payment – model + DAO for the PAYMENT table.
 *
 * Represents a settlement where one resident pays another.
 */
public class Payment {

    // ── Fields ───────────────────────────────────────────────────────────────
    private int    paymentId;
    private int    fromUser;
    private int    toUser;
    private String fromName;   // resolved via JOIN (display)
    private String toName;     // resolved via JOIN (display)
    private double amount;
    private String date;       // "YYYY-MM-DD"

    // ── Constructor ──────────────────────────────────────────────────────────
    public Payment(int paymentId, int fromUser, String fromName,
                   int toUser, String toName, double amount, String date) {
        this.paymentId = paymentId;
        this.fromUser  = fromUser;
        this.fromName  = fromName;
        this.toUser    = toUser;
        this.toName    = toName;
        this.amount    = amount;
        this.date      = date;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getPaymentId() { return paymentId; }
    public int    getFromUser()  { return fromUser;  }
    public int    getToUser()    { return toUser;    }
    public String getFromName()  { return fromName;  }
    public String getToName()    { return toName;    }
    public double getAmount()    { return amount;    }
    public String getDate()      { return date;      }

    // ── DAO Methods ──────────────────────────────────────────────────────────

    /**
     * Record a payment from one user to another.
     *
     * @param fromUser  user_id of the person paying
     * @param toUser    user_id of the person receiving
     * @param amount    amount transferred (must be > 0)
     * @param date      "YYYY-MM-DD"
     * @return true on success
     */
    public static boolean addPayment(int fromUser, int toUser, double amount, String date) {
        if (amount <= 0 || fromUser == toUser) return false;

        String sql = "INSERT INTO PAYMENT (from_user, to_user, amount, date) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt   (1, fromUser);
            ps.setInt   (2, toUser);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Payment.addPayment] " + e.getMessage());
            return false;
        }
    }

    /** Delete a payment record. */
    public static boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM PAYMENT WHERE payment_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Payment.deletePayment] " + e.getMessage());
            return false;
        }
    }

    /** All payments, newest first, with payer and receiver names. */
    public static List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = """
            SELECT p.payment_id,
                   p.from_user, uf.name AS from_name,
                   p.to_user,   ut.name AS to_name,
                   p.amount, p.date
            FROM PAYMENT p
            JOIN USER uf ON p.from_user = uf.user_id
            JOIN USER ut ON p.to_user   = ut.user_id
            ORDER BY p.date DESC, p.payment_id DESC
            """;
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Payment(
                    rs.getInt   ("payment_id"),
                    rs.getInt   ("from_user"),
                    rs.getString("from_name"),
                    rs.getInt   ("to_user"),
                    rs.getString("to_name"),
                    rs.getDouble("amount"),
                    rs.getString("date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Payment.getAllPayments] " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns net balances from the USER_BALANCE view.
     * Each row: { user_id (int), name (String), room_no (String), balance (double) }
     */
    public static List<Object[]> getBalances() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT user_id, name, room_no, balance FROM USER_BALANCE ORDER BY name";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt   ("user_id"),
                    rs.getString("name"),
                    rs.getString("room_no"),
                    rs.getDouble("balance")
                });
            }
        } catch (SQLException e) {
            System.err.println("[Payment.getBalances] " + e.getMessage());
        }
        return rows;
    }
}
