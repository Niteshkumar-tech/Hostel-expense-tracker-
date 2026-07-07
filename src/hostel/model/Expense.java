package hostel.model;

import hostel.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Expense – model + DAO for the EXPENSE and EXPENSE_SPLIT tables.
 *
 * A 'personal' expense creates a single split row for the payer only.
 * A 'shared'   expense divides the amount equally among all chosen users.
 */
public class Expense {

    // ── Fields ───────────────────────────────────────────────────────────────
    private int    expenseId;
    private String title;
    private double amount;
    private String date;        // "YYYY-MM-DD"
    private int    paidBy;
    private String paidByName;  // resolved join value (display only)
    private String type;        // "personal" | "shared"

    // ── Constructors ─────────────────────────────────────────────────────────
    public Expense() {}

    public Expense(int expenseId, String title, double amount,
                   String date, int paidBy, String paidByName, String type) {
        this.expenseId  = expenseId;
        this.title      = title;
        this.amount     = amount;
        this.date       = date;
        this.paidBy     = paidBy;
        this.paidByName = paidByName;
        this.type       = type;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getExpenseId()  { return expenseId;  }
    public String getTitle()      { return title;      }
    public double getAmount()     { return amount;     }
    public String getDate()       { return date;       }
    public int    getPaidBy()     { return paidBy;     }
    public String getPaidByName() { return paidByName; }
    public String getType()       { return type;       }

    // ── DAO Methods ──────────────────────────────────────────────────────────

    /**
     * Save an expense and auto-create split rows.
     *
     * @param title         expense description
     * @param amount        total cost (must be > 0)
     * @param date          "YYYY-MM-DD"
     * @param paidBy        user_id of the payer
     * @param type          "personal" or "shared"
     * @param splitUserIds  list of user_ids who share the cost
     * @return true on success
     */
    public static boolean addExpense(String title, double amount, String date,
                                     int paidBy, String type, List<Integer> splitUserIds) {
        if (amount <= 0 || splitUserIds == null || splitUserIds.isEmpty()) return false;

        Connection conn = DBConnection.getConnection();
        String expSql   = "INSERT INTO EXPENSE (title,amount,date,paid_by,type) VALUES (?,?,?,?,?)";
        String splitSql = "INSERT INTO EXPENSE_SPLIT (expense_id,user_id,share_amount) VALUES (?,?,?)";

        try {
            conn.setAutoCommit(false); // use a transaction for atomicity

            // 1) Insert the expense header
            PreparedStatement expPs = conn.prepareStatement(expSql, Statement.RETURN_GENERATED_KEYS);
            expPs.setString(1, title.trim());
            expPs.setDouble(2, amount);
            expPs.setString(3, date);
            expPs.setInt   (4, paidBy);
            expPs.setString(5, type);
            expPs.executeUpdate();

            ResultSet keys = expPs.getGeneratedKeys();
            if (!keys.next()) { conn.rollback(); return false; }
            int newExpenseId = keys.getInt(1);

            // 2) Calculate each person's share
            double share = amount / splitUserIds.size();
            share = Math.round(share * 100.0) / 100.0; // round to 2 dp

            PreparedStatement splitPs = conn.prepareStatement(splitSql);
            for (int uid : splitUserIds) {
                splitPs.setInt   (1, newExpenseId);
                splitPs.setInt   (2, uid);
                splitPs.setDouble(3, share);
                splitPs.addBatch();
            }
            splitPs.executeBatch();

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.err.println("[Expense.addExpense] " + e.getMessage());
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return false;
        }
    }

    /** Delete an expense (splits removed by CASCADE). */
    public static boolean deleteExpense(int expenseId) {
        String sql = "DELETE FROM EXPENSE WHERE expense_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Expense.deleteExpense] " + e.getMessage());
            return false;
        }
    }

    /** All expenses with the payer's name, newest first. */
    public static List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = """
            SELECT e.expense_id, e.title, e.amount, e.date,
                   e.paid_by, u.name AS paid_by_name, e.type
            FROM EXPENSE e
            JOIN USER u ON e.paid_by = u.user_id
            ORDER BY e.date DESC, e.expense_id DESC
            """;
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Expense(
                    rs.getInt   ("expense_id"),
                    rs.getString("title"),
                    rs.getDouble("amount"),
                    rs.getString("date"),
                    rs.getInt   ("paid_by"),
                    rs.getString("paid_by_name"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Expense.getAllExpenses] " + e.getMessage());
        }
        return list;
    }

    /** Split details for a single expense. */
    public static List<Object[]> getSplits(int expenseId) {
        List<Object[]> rows = new ArrayList<>();
        String sql = """
            SELECT u.name, es.share_amount
            FROM EXPENSE_SPLIT es
            JOIN USER u ON es.user_id = u.user_id
            WHERE es.expense_id = ?
            """;
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{ rs.getString("name"), rs.getDouble("share_amount") });
            }
        } catch (SQLException e) {
            System.err.println("[Expense.getSplits] " + e.getMessage());
        }
        return rows;
    }
}
