package com.nhlstenden.foodorder.persistence;

import com.nhlstenden.foodorder.model.User;
import com.nhlstenden.foodorder.util.AppUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUserRepository implements UserRepository {
    private final Connection con;

    public DBUserRepository() {
        try {
            Path dbDir = Paths.get("database");
            Files.createDirectories(dbDir);
            con = DriverManager.getConnection("jdbc:sqlite:" + dbDir.resolve("orders.db"));

            try (Statement st = con.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        username TEXT PRIMARY KEY,
                        passwordHash TEXT NOT NULL,
                        role TEXT NOT NULL CHECK(role IN ('admin', 'user'))
                    )
                """);
            }

            try (PreparedStatement st = con.prepareStatement("DELETE FROM users WHERE username = 'admin'")) {
                st.executeUpdate();
                System.out.println("❗Deleted existing admin user (if existed)");
            }

            insertDefaultAdminIfNotExists();

        } catch (Exception e) {
            throw new RuntimeException("❌ Could not init users DB", e);
        }
    }

    private void insertDefaultAdminIfNotExists() {
        try (PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            st.setString(1, "admin");
            ResultSet rs = st.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                String hashedPassword = AppUtils.hashPassword("12345");
                try (PreparedStatement insertSt = con.prepareStatement(
                        "INSERT INTO users (username, passwordHash, role) VALUES (?, ?, ?)")) {
                    insertSt.setString(1, "admin");
                    insertSt.setString(2, hashedPassword);
                    insertSt.setString(3, "admin");
                    insertSt.executeUpdate();
                    System.out.println("✅ Admin user created with password '12345'");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to check or insert admin user:");
            e.printStackTrace();
        }
    }

    @Override
    public void save(User user) {
        try (PreparedStatement st = con.prepareStatement(
                "INSERT INTO users (username, passwordHash, role) VALUES (?, ?, ?)")) {
            st.setString(1, user.getUsername());
            st.setString(2, user.getPasswordHash());
            st.setString(3, user.getRole());
            st.executeUpdate();
            System.out.println("✅ User saved: " + user.getUsername());
        } catch (SQLException e) {
            System.err.println("❌ Could not save user: " + user.getUsername());
            e.printStackTrace();
        }
    }

    @Override
    public User findByUsername(String username) {
        try (PreparedStatement st = con.prepareStatement(
                "SELECT username, passwordHash, role FROM users WHERE username = ?")) {
            st.setString(1, username);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding user: " + username);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement st = con.prepareStatement("SELECT username, passwordHash, role FROM users")) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching all users");
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void update(User user) {
        try (PreparedStatement st = con.prepareStatement(
                "UPDATE users SET passwordHash = ?, role = ? WHERE username = ?")) {
            st.setString(1, user.getPasswordHash());
            st.setString(2, user.getRole());
            st.setString(3, user.getUsername());
            st.executeUpdate();
            System.out.println("✅ User updated: " + user.getUsername());
        } catch (SQLException e) {
            System.err.println("❌ Could not update user: " + user.getUsername());
            e.printStackTrace();
        }
    }


    @Override
    public void delete(String username) {
        try {
            // Command Pattern: encapsulate actions
            Runnable deleteOrdersCommand = () -> {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM orders WHERE username = ?")) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                    System.out.println("🗑️ Orders deleted for user: " + username);
                } catch (SQLException e) {
                    throw new RuntimeException("❌ Could not delete orders for user", e);
                }
            };

            Runnable deleteUserCommand = () -> {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE username = ?")) {
                    ps.setString(1, username);
                    ps.executeUpdate();
                    System.out.println("✅ User deleted: " + username);
                } catch (SQLException e) {
                    throw new RuntimeException("❌ Could not delete user", e);
                }
            };

            // Execute commands in order
            deleteOrdersCommand.run();
            deleteUserCommand.run();

        } catch (Exception e) {
            System.err.println("❌ Could not fully delete user and orders: " + username);
            e.printStackTrace();
        }
    }

}
