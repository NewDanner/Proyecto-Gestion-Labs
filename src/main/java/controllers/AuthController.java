/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Andrei
 */
package controllers;

import models.User;
import models.DatabaseConnection;
import java.sql.*;

public class AuthController {
    public User login(String username, String password) {
        String query = "SELECT id_usuario, username, ci, rol FROM Usuarios WHERE username = ? AND password = ? AND estado = TRUE";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
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
            stmt.setString(2, password);
            stmt.setString(3, ci);
            stmt.setString(4, role);
            
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUser(int userId, String username, String password, String ci) {
        String query = "UPDATE Usuarios SET username = ?, password = ?, ci = ? WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, ci);
            stmt.setInt(4, userId);
            
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUserWithoutPassword(int userId, String username, String ci) {
        String query = "UPDATE Usuarios SET username = ?, ci = ? WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, ci);
            stmt.setInt(3, userId);
            
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public User getUserById(int userId) {
        String query = "SELECT id_usuario, username, ci, rol FROM Usuarios WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
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
    
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM Usuarios WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    DatabaseConnection.resetAutoIncrement("Usuarios");
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
