/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Andrei
 */
package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/GESTOR";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";
    private static List<DatabaseChangeListener> listeners = new ArrayList<>();
    
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        conn.setNetworkTimeout(Executors.newFixedThreadPool(1), 5000);
        return conn;
    }
    
    public static void addListener(DatabaseChangeListener listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(DatabaseChangeListener listener) {
        listeners.remove(listener);
    }
    
    public static void notifyDatabaseChanged(String tableChanged) {
        // Crear copia para evitar ConcurrentModificationException
        for (DatabaseChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onDatabaseChanged(tableChanged);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void resetAutoIncrement(String tableName) {
        String sql = "ALTER TABLE " + tableName + " AUTO_INCREMENT = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.executeUpdate(sql);
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public interface DatabaseChangeListener {
        void onDatabaseChanged(String tableChanged);
    }
}
