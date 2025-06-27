package com.nhlstenden.foodorder.persistence;

import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBOrderRepository implements OrderRepository {
    private static final Path DB_DIR  = Paths.get("database");
    private static final String URL = "jdbc:sqlite:" + DB_DIR.resolve("orders.db");

    public DBOrderRepository() {
        try {
            Files.createDirectories(DB_DIR);
            try (Connection con = DriverManager.getConnection(URL);
                 Statement st = con.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        datetime TEXT NOT NULL,
                        items TEXT NOT NULL,
                        raw_total REAL NOT NULL,
                        final_total REAL NOT NULL,
                        payment_method TEXT NOT NULL,
                        username TEXT NOT NULL,
                        deleted_by TEXT
                    )
                """);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not init orders DB", e);
        }
    }

    @Override
    public void save(OrderRecord record) {
        String sql = """
            INSERT INTO orders (datetime, items, raw_total, final_total, payment_method, username)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, record.getDateTime().toString());
            ps.setString(2, record.getItemsCsv());
            ps.setDouble(3, record.getRawTotal());
            ps.setDouble(4, record.getFinalTotal());
            ps.setString(5, record.getPaymentMethod());
            ps.setString(6, record.getUsername());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save order", e);
        }
    }

    @Override
    public List<OrderRecord> findAll() {
        return load("SELECT * FROM orders ORDER BY id DESC");
    }

    @Override
    public List<OrderRecord> findAllByUser(String username) {
        return load("SELECT * FROM orders WHERE username = ? ORDER BY id DESC", username);
    }

    @Override
    public List<OrderRecord> findAllVisibleToUser(String username) {
        // Admin sees all orders, regardless of deleted_by
        if ("admin".equalsIgnoreCase(username)) {
            return load("SELECT * FROM orders ORDER BY id DESC");
        }

        // Regular users see only their own orders, unless deleted_by = themselves
        String sql = """
        SELECT * FROM orders
        WHERE username = ?
        AND (deleted_by IS NULL OR deleted_by != ?)
        ORDER BY id DESC
    """;
        return load(sql, username, username);
    }

    @Override
    public void clearAll() {
        try (Connection con = DriverManager.getConnection(URL);
             Statement st = con.createStatement()) {
            st.execute("DELETE FROM orders");
        } catch (SQLException e) {
            throw new RuntimeException("Could not clear order history", e);
        }
    }

    @Override
    public void deleteById(int id) {
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete order", e);
        }
    }

    @Override
    public void softDeleteById(int id, String username) {
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement("UPDATE orders SET deleted_by = ? WHERE id = ?")) {
            ps.setString(1, username);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not soft delete order", e);
        }
    }

    private List<OrderRecord> load(String sql, String... params) {
        List<OrderRecord> list = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new OrderRecord(
                        rs.getInt("id"),
                        LocalDateTime.parse(rs.getString("datetime")),
                        rs.getString("items"),
                        rs.getDouble("raw_total"),
                        rs.getDouble("final_total"),
                        rs.getString("payment_method"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load orders", e);
        }
        return list;
    }
}
