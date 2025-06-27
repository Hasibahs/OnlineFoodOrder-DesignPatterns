package com.nhlstenden.foodorder.persistence;

import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBToppingRepository {
    private static final Path DB_DIR  = Paths.get("database");
    private static final String URL = "jdbc:sqlite:" + DB_DIR.resolve("menu.db");

    public DBToppingRepository() {
        try {
            Files.createDirectories(DB_DIR);
            try (Connection con = DriverManager.getConnection(URL);
                 Statement st = con.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS toppings (
                        name TEXT PRIMARY KEY,
                        price REAL NOT NULL
                    )
                """);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not init toppings DB", e);
        }
    }

    public List<ToppingRecord> findAll() {
        var list = new ArrayList<ToppingRecord>();
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement("SELECT name, price FROM toppings");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ToppingRecord(rs.getString("name"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read toppings", e);
        }
        return list;
    }

    public void save(ToppingRecord r) {
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement("""
                INSERT INTO toppings(name, price)
                VALUES(?, ?)
                ON CONFLICT(name) DO UPDATE SET price=excluded.price
             """)) {
            ps.setString(1, r.getName());
            ps.setDouble(2, r.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save topping", e);
        }
    }

    public void delete(String name) {
        try (Connection con = DriverManager.getConnection(URL);
             PreparedStatement ps = con.prepareStatement("DELETE FROM toppings WHERE name = ?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete topping", e);
        }
    }
}
