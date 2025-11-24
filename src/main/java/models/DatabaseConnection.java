/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author DANNER
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestorv2";
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
        for (DatabaseChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onDatabaseChanged(tableChanged);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void notifyMantenimientoChanged() {
        notifyDatabaseChanged("Mantenimiento");
        notifyDatabaseChanged("Mantenimiento_Equipo");
        notifyDatabaseChanged("Mantenimiento_Material");
        notifyDatabaseChanged("Equipos");
        notifyDatabaseChanged("Material_Adicional");
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
    
    public static List<Equipo> obtenerTodosEquipos() {
        List<Equipo> equipos = new ArrayList<>();
        String query = "SELECT * FROM Equipos ORDER BY marca, modelo";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Equipo equipo = new Equipo();
                equipo.setIdEquipo(rs.getInt("Id_Equipo"));
                equipo.setMarca(rs.getString("Marca"));
                equipo.setModelo(rs.getString("Modelo"));
                equipo.setNumeroSerie(rs.getString("numero_de_serie"));
                equipo.setProcesador(rs.getString("Procesador"));
                equipo.setRam(rs.getString("RAM"));
                equipo.setAlmacenamiento(rs.getString("Almacenamiento"));
                equipo.setSo(rs.getString("SO"));
                equipo.setEstado(rs.getString("Estado"));
                
                int labId = rs.getInt("Id_Laboratorio");
                if (!rs.wasNull()) {
                    equipo.setIdLaboratorio(labId);
                }
                
                equipos.add(equipo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }
    
    public interface DatabaseChangeListener {
        void onDatabaseChanged(String tableChanged);
    }
}