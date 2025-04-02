/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

/**
 *
 * @author Andrei
 */

import models.User;
import models.DatabaseConnection;
import java.sql.*;

public class AuthController {
    public User login(String username, String password) {
        String query = "SELECT id_usuario, username, ci, rol FROM Usuarios WHERE username = ? AND password = ? AND estado = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); // En producción, usar hash de contraseña
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id_usuario"),
                    rs.getString("username"),
                    rs.getString("ci"),
                    rs.getString("rol")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean register(String username, String password, String ci, String role) {
        String query = "INSERT INTO Usuarios (username, password, ci, rol) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); // En producción, usar hash
            stmt.setString(3, ci);
            stmt.setString(4, role);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
