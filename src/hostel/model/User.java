package hostel.model;

import hostel.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User – model + DAO for the USER table.
 *
 * Encapsulates fields and all CRUD operations.
 */
public class User {

    // ── Fields ───────────────────────────────────────────────────────────────
    private int    userId;
    private String name;
    private String email;
    private String roomNo;

    // ── Constructors ─────────────────────────────────────────────────────────
    public User() {}

    public User(int userId, String name, String email, String roomNo) {
        this.userId = userId;
        this.name   = name;
        this.email  = email;
        this.roomNo = roomNo;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public int    getUserId() { return userId; }
    public String getName()   { return name;   }
    public String getEmail()  { return email;  }
    public String getRoomNo() { return roomNo; }

    public void setUserId(int userId)    { this.userId = userId; }
    public void setName(String name)     { this.name   = name;   }
    public void setEmail(String email)   { this.email  = email;  }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    @Override
    public String toString() { return name + " (Room " + roomNo + ")"; }

    // ── DAO Methods ──────────────────────────────────────────────────────────

    /** Insert a new user; returns true on success. */
    public static boolean addUser(String name, String email, String roomNo) {
        String sql = "INSERT INTO USER (name, email, room_no) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, roomNo.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[User.addUser] " + e.getMessage());
            return false;
        }
    }

    /** Update an existing user by user_id; returns true on success. */
    public static boolean updateUser(int userId, String name, String email, String roomNo) {
        String sql = "UPDATE USER SET name=?, email=?, room_no=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, roomNo.trim());
            ps.setInt   (4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[User.updateUser] " + e.getMessage());
            return false;
        }
    }

    /** Delete user and all related data (CASCADE handles FK cleanup). */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM USER WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[User.deleteUser] " + e.getMessage());
            return false;
        }
    }

    /** Fetch all users ordered by name. */
    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, name, email, room_no FROM USER ORDER BY name";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("room_no")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[User.getAllUsers] " + e.getMessage());
        }
        return list;
    }

    /** Fetch a single user by id; returns null if not found. */
    public static User getById(int userId) {
        String sql = "SELECT * FROM USER WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("room_no")
                );
            }
        } catch (SQLException e) {
            System.err.println("[User.getById] " + e.getMessage());
        }
        return null;
    }
}
