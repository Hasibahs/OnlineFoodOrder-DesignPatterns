package com.nhlstenden.foodorder.persistence;

import com.nhlstenden.foodorder.model.MenuEntry;

import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBMenuRepository implements MenuRepository
{
    private static final Path DB_DIR  = Paths.get("database");
    private static final String URL    = "jdbc:sqlite:" + DB_DIR.resolve("menu.db");
    private static final String CREATE =
            "CREATE TABLE IF NOT EXISTS menu (" +
                    " type       TEXT NOT NULL," +
                    " key        TEXT PRIMARY KEY," +
                    " name       TEXT NOT NULL," +
                    " price      REAL NOT NULL" +
                    ")";

    public DBMenuRepository()
    {
        try
        {
            Files.createDirectories(DB_DIR);
            try (Connection c = DriverManager.getConnection(URL);
                 Statement s = c.createStatement())
            {
                s.execute(CREATE);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Could not initialize menu DB", ex);
        }
    }

    @Override
    public List<MenuEntry> findAll()
    {
        var list = new ArrayList<MenuEntry>();
        String sql = "SELECT type, key, name, price FROM menu ORDER BY rowid";
        try (Connection c = DriverManager.getConnection(URL);
             Statement  s = c.createStatement();
             ResultSet  rs = s.executeQuery(sql))
        {
            while (rs.next())
            {
                list.add(new MenuEntry(
                        rs.getString("type"),
                        rs.getString("key"),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Could not read menu", ex);
        }
        return list;
    }

    @Override
    public void save(MenuEntry e)
    {
        String sql = """
            INSERT INTO menu(type,key,name,price)
            VALUES(?,?,?,?)
            ON CONFLICT(key) DO UPDATE
              SET name  = excluded.name,
                  price = excluded.price
            """;
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql))
        {
            p.setString(1, e.getType());
            p.setString(2, e.getKey());
            p.setString(3, e.getName());
            p.setDouble(4, e.getPrice());
            p.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Could not save menu entry", ex);
        }
    }

    @Override
    public void deleteByKey(String key)
    {
        String sql = "DELETE FROM menu WHERE key = ?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement p = c.prepareStatement(sql))
        {
            p.setString(1, key);
            p.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Could not delete menu entry", ex);
        }
    }
}
