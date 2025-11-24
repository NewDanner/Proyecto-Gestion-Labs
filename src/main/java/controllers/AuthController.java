/**
 *
 * @author Andrei/DANNER
 */
package controllers;

import models.User;
import models.DatabaseConnection;
import java.sql.*;

public class AuthController {
    //Camnbios en el LOGIN_QUERY "WHERE username = ? AND estado = TRUE", se eliminó "AND password = ?"
    private static final String LOGIN_QUERY = "SELECT id_usuario, nombre, segundo_nombre, primer_apellido, " +
            "segundo_apellido, turno, username, ci, rol, sexo, estado, correo, password " +
            "FROM Usuarios WHERE username = ? AND estado = TRUE";
    //*************************************************************************************************************
    
    private static final String REGISTER_QUERY = "INSERT INTO Usuarios " +
            "(nombre, segundo_nombre, primer_apellido, segundo_apellido, turno, " +
            "username, password, ci, rol, sexo, correo, password) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_USER_QUERY = "UPDATE Usuarios SET nombre = ?, username = ?, password = ?, ci = ? " +
            "WHERE id_usuario = ?";
    
    private static final String UPDATE_USER_NO_PASSWORD_QUERY = "UPDATE Usuarios SET nombre = ?, username = ?, ci = ? " +
            "WHERE id_usuario = ?";
    
    private static final String GET_USER_BY_ID_QUERY = "SELECT id_usuario, nombre, segundo_nombre, primer_apellido, " +
            "segundo_apellido, turno, username, ci, rol, sexo, estado " +
            "FROM Usuarios WHERE id_usuario = ?";
    
    private static final String DELETE_USER_QUERY = "UPDATE Usuarios SET estado = FALSE WHERE id_usuario = ?";
    
    private static final String GET_ADMIN_BY_PASSWORD_QUERY = "SELECT id_usuario, nombre, username, ci, rol, sexo, correo, password " +
        "FROM Usuarios WHERE password = ? AND rol = 'Administrador(a)' AND estado = TRUE";

    // Agrega estas constantes junto con las otras
private static final String UPDATE_USER_FULL_QUERY = "UPDATE Usuarios SET nombre = ?, segundo_nombre = ?, " +
        "primer_apellido = ?, segundo_apellido = ?, turno = ?, username = ?, password = ?, ci = ? " +
        "WHERE id_usuario = ?";

private static final String UPDATE_USER_FULL_NO_PASSWORD_QUERY = "UPDATE Usuarios SET nombre = ?, segundo_nombre = ?, " +
        "primer_apellido = ?, segundo_apellido = ?, turno = ?, username = ?, ci = ? " +
        "WHERE id_usuario = ?";

// Agrega estos nuevos métodos
public boolean updateUserFull(int userId, String nombre, String segundoNombre, 
        String primerApellido, String segundoApellido, String turno, 
        String username, String password, String ci) {
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_FULL_QUERY)) {
        
        stmt.setString(1, nombre);
        stmt.setString(2, segundoNombre);
        stmt.setString(3, primerApellido);
        stmt.setString(4, segundoApellido);
        stmt.setString(5, turno);
        stmt.setString(6, username);
        stmt.setString(7, password);
        stmt.setString(8, ci);
        stmt.setInt(9, userId);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            DatabaseConnection.notifyDatabaseChanged("Usuarios");
            return true;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

public boolean updateUserFullWithoutPassword(int userId, String nombre, String segundoNombre, 
        String primerApellido, String segundoApellido, String turno, 
        String username, String ci) {
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_FULL_NO_PASSWORD_QUERY)) {
        
        stmt.setString(1, nombre);
        stmt.setString(2, segundoNombre);
        stmt.setString(3, primerApellido);
        stmt.setString(4, segundoApellido);
        stmt.setString(5, turno);
        stmt.setString(6, username);
        stmt.setString(7, ci);
        stmt.setInt(8, userId);
        
        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            DatabaseConnection.notifyDatabaseChanged("Usuarios");
            return true;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
    // Cambios en User login se eliminó "String password" para que solo acepte un parámetro
    public User login(String username) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(LOGIN_QUERY)) {
            
            stmt.setString(1, username);
            //stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
//************************************************************************************    
    
    public boolean register(String nombre, String segundoNombre, String primerApellido,
                      String segundoApellido, String turno, String username, 
                      String password, String ci, String role, String sexo, String correo) {
    
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

        // 1. Primero insertar el usuario
        String userQuery = "INSERT INTO Usuarios (nombre, segundo_nombre, primer_apellido, " +
                         "segundo_apellido, turno, username, password, ci, rol, sexo, estado, correo) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
            stmt.setString(1, nombre);
            stmt.setString(2, segundoNombre);
            stmt.setString(3, primerApellido);
            stmt.setString(4, segundoApellido);
            stmt.setString(5, turno);
            stmt.setString(6, username);
            stmt.setString(7, password);
            stmt.setString(8, ci);
            stmt.setString(9, role);
            stmt.setString(10, sexo);
            // Corre 25/05/25
            stmt.setString(11, correo);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                conn.commit();
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
                return true;
            } else {
                conn.rollback();
                return false;
            }
        }
    } catch (SQLException e) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        e.printStackTrace();
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    return false;
}
    
    public boolean updateUser(int userId, String nombre, String username, String password, String ci) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_QUERY)) {
            
            stmt.setString(1, nombre);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, ci);
            stmt.setInt(5, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateUserWithoutPassword(int userId, String nombre, String username, String ci) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_NO_PASSWORD_QUERY)) {
            
            stmt.setString(1, nombre);
            stmt.setString(2, username);
            stmt.setString(3, ci);
            stmt.setInt(4, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public User getUserById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_USER_BY_ID_QUERY)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_USER_QUERY)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Usuarios");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getAdminByPassword(String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_ADMIN_BY_PASSWORD_QUERY)) {

            stmt.setString(1, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("username"),
                    rs.getString("ci"),
                    rs.getString("rol"),
                    rs.getString("sexo"),
                    // Cambio 25/05/25
                    rs.getString("correo"),    
                    //***********************
                    // Cambio 27/05/25
                    rs.getString("password")    
                    //*********************** 
                );
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id_usuario"),
            rs.getString("nombre"),
            rs.getString("username"),
            rs.getString("ci"),
            rs.getString("rol"),
            rs.getString("sexo"),
            //Cambio 25/05/25
            rs.getString("correo"),    
            // **********************    
            //Cambio 27/05/25
            rs.getString("password")    
            // **********************    
        );
        
        user.setSegundoNombre(rs.getString("segundo_nombre"));
        user.setPrimerApellido(rs.getString("primer_apellido"));
        user.setSegundoApellido(rs.getString("segundo_apellido"));
        user.setTurno(rs.getString("turno"));
        user.setEstado(rs.getBoolean("estado"));
        
        return user;
    }
    
    // Método para verificar si un username ya existe
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM Usuarios WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Método para verificar si una cédula ya existe
    public boolean ciExists(String ci) {
        String query = "SELECT COUNT(*) FROM Usuarios WHERE ci = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, ci);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean ciExistsInTelefono(String ci) {
    String query = "SELECT COUNT(*) FROM Telefono WHERE ci = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, ci);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    //Cambio 16/05/25 se cambio 'ci' por 'id_uduario' en 'query'
private boolean insertarTelefono(String id_usuario, String numero) {
    String query = "INSERT INTO Telefono (id_usuario, numero_telefono) VALUES (?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, id_usuario);
        stmt.setString(2, numero);
        
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
}
