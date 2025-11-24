/**
 *
 * @author Andrei
 */
package controllers;

import models.DatabaseConnection;
import java.sql.*;

public class MantenimientoController {
    public boolean crearMantenimientoEquipo(int idEquipo, String nombre, String tipo, String descripcion, 
                                      int idUsuario, String estado) {
    String insertMantenimiento = "INSERT INTO Mantenimiento (nombre, tipo, tipo_elemento, fecha_inicio, " +
                               "descripcion, Id_Usuario_Responsable, estado) " +
                               "VALUES (?, ?, 'Equipo', NOW(), ?, ?, ?)";
    
    String insertRelacion = "INSERT INTO Mantenimiento_Equipo (Id_Mantenimiento, Id_Equipo) VALUES (?, ?)";
    String updateEquipo = "UPDATE Equipos SET estado = 'Mantenimiento' WHERE Id_Equipo = ?";
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        
        try (PreparedStatement stmtMantenimiento = conn.prepareStatement(insertMantenimiento, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtRelacion = conn.prepareStatement(insertRelacion);
             PreparedStatement stmtUpdateEquipo = conn.prepareStatement(updateEquipo)) {
            
            // Insertar mantenimiento
            stmtMantenimiento.setString(1, nombre);
            stmtMantenimiento.setString(2, tipo);
            stmtMantenimiento.setString(3, descripcion);
            stmtMantenimiento.setInt(4, idUsuario);
            stmtMantenimiento.setString(5, estado);
            
            int affectedRows = stmtMantenimiento.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Obtener ID del mantenimiento creado
            try (ResultSet generatedKeys = stmtMantenimiento.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idMantenimiento = generatedKeys.getInt(1);
                    
                    // Crear relación con equipo
                    stmtRelacion.setInt(1, idMantenimiento);
                    stmtRelacion.setInt(2, idEquipo);
                    stmtRelacion.executeUpdate();
                    
                    // Actualizar estado del equipo
                    stmtUpdateEquipo.setInt(1, idEquipo);
                    stmtUpdateEquipo.executeUpdate();
                    
                    conn.commit();
                    DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                    return true;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

public boolean crearMantenimientoMaterial(int idMaterial, String nombre, String tipo, String descripcion, 
                                        int idUsuario, String estado) {
    String insertMantenimiento = "INSERT INTO Mantenimiento (nombre, tipo, tipo_elemento, fecha_inicio, " +
                               "descripcion, Id_Usuario_Responsable, estado) " +
                               "VALUES (?, ?, 'Material', NOW(), ?, ?, ?)";
    
    String insertRelacion = "INSERT INTO Mantenimiento_Material (Id_Mantenimiento, N_Objeto) VALUES (?, ?)";
    String updateMaterial = "UPDATE Material_Adicional SET daño = TRUE WHERE N_Objeto = ?";
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        
        try (PreparedStatement stmtMantenimiento = conn.prepareStatement(insertMantenimiento, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtRelacion = conn.prepareStatement(insertRelacion);
             PreparedStatement stmtUpdateMaterial = conn.prepareStatement(updateMaterial)) {
            
            // Insertar mantenimiento
            stmtMantenimiento.setString(1, nombre);
            stmtMantenimiento.setString(2, tipo);
            stmtMantenimiento.setString(3, descripcion);
            stmtMantenimiento.setInt(4, idUsuario);
            stmtMantenimiento.setString(5, estado);
            
            int affectedRows = stmtMantenimiento.executeUpdate();
            
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Obtener ID del mantenimiento creado
            try (ResultSet generatedKeys = stmtMantenimiento.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idMantenimiento = generatedKeys.getInt(1);
                    
                    // Crear relación con material
                    stmtRelacion.setInt(1, idMantenimiento);
                    stmtRelacion.setInt(2, idMaterial);
                    stmtRelacion.executeUpdate();
                    
                    // Actualizar estado del material
                    stmtUpdateMaterial.setInt(1, idMaterial);
                    stmtUpdateMaterial.executeUpdate();
                    
                    conn.commit();
                    DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                    DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                    return true;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

public boolean cambiarEstadoMantenimiento(int idMantenimiento, String nuevoEstado) {
    String query = "UPDATE Mantenimiento SET estado = ?, " +
                  (nuevoEstado.equals("Completado") || nuevoEstado.equals("Cancelado") ? 
                   "fecha_fin = NOW() " : "fecha_fin = NULL ") +
                  "WHERE Id_Mantenimiento = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, nuevoEstado);
        stmt.setInt(2, idMantenimiento);
        
        int affectedRows = stmt.executeUpdate();
        
        if (affectedRows > 0) {
            DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
            return true;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

public int obtenerIdElementoMantenimiento(int idMantenimiento, String tipoElemento) {
    String query = tipoElemento.equals("Equipo") ? 
        "SELECT Id_Equipo FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?" :
        "SELECT N_Objeto FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setInt(1, idMantenimiento);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}

    public boolean finalizarMantenimientoEquipo(int idEquipo, String acciones, int idUsuario) {
        String query = "UPDATE Mantenimiento m " +
                     "JOIN Mantenimiento_Equipo me ON m.Id_Mantenimiento = me.Id_Mantenimiento " +
                     "SET m.estado = 'Completado', m.fecha_fin = NOW() " +
                     "WHERE me.Id_Equipo = ? AND m.estado = 'En Proceso'";
        
        String queryEquipo = "UPDATE Equipos SET estado = 'Operativo' WHERE Id_Equipo = ?";
        String queryObservacion = "INSERT INTO Observaciones (nombre, fecha, detalle, realizado, id_equipo) " +
                                "VALUES ('Mantenimiento', NOW(), ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 PreparedStatement stmtEquipo = conn.prepareStatement(queryEquipo);
                 PreparedStatement stmtObservacion = conn.prepareStatement(queryObservacion)) {
                
                stmt.setInt(1, idEquipo);
                stmt.executeUpdate();
                
                stmtEquipo.setInt(1, idEquipo);
                stmtEquipo.executeUpdate();
                
                stmtObservacion.setString(1, acciones);
                stmtObservacion.setString(2, "Técnico");
                stmtObservacion.setInt(3, idEquipo);
                stmtObservacion.executeUpdate();
                
                conn.commit();
                DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean eliminarMantenimiento(int idMantenimiento) {
    // Obtener información del mantenimiento primero
    String getInfoQuery = "SELECT tipo_elemento, " +
                        "COALESCE((SELECT Id_Equipo FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?), 0) AS id_equipo, " +
                        "COALESCE((SELECT N_Objeto FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?), 0) AS id_material " +
                        "FROM Mantenimiento WHERE Id_Mantenimiento = ?";
    
    // Primero eliminar las relaciones en las tablas hijas
    String deleteMaterialQuery = "DELETE FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?";
    String deleteEquipoQuery = "DELETE FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?";
    String deleteMantenimientoQuery = "DELETE FROM Mantenimiento WHERE Id_Mantenimiento = ?";
    
    // Queries para restaurar estados
    String restoreEquipoQuery = "UPDATE Equipos SET estado = 'Operativo' WHERE Id_Equipo = ?";
    String restoreMaterialQuery = "UPDATE Material_Adicional SET daño = FALSE WHERE N_Objeto = ?";
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        
        // Primero obtener información sobre qué está vinculado a este mantenimiento
        int idEquipo = 0;
        int idMaterial = 0;
        String tipoElemento = null;
        
        try (PreparedStatement stmtInfo = conn.prepareStatement(getInfoQuery)) {
            stmtInfo.setInt(1, idMantenimiento);
            stmtInfo.setInt(2, idMantenimiento);
            stmtInfo.setInt(3, idMantenimiento);
            
            ResultSet rs = stmtInfo.executeQuery();
            if (rs.next()) {
                tipoElemento = rs.getString("tipo_elemento");
                idEquipo = rs.getInt("id_equipo");
                idMaterial = rs.getInt("id_material");
            }
        }
        
        try {
            // Eliminar relaciones
            if (tipoElemento != null) {
                if (tipoElemento.equals("Equipo") && idEquipo > 0) {
                    try (PreparedStatement stmt = conn.prepareStatement(deleteEquipoQuery)) {
                        stmt.setInt(1, idMantenimiento);
                        stmt.executeUpdate();
                    }
                    
                    // Restaurar estado del equipo
                    try (PreparedStatement stmt = conn.prepareStatement(restoreEquipoQuery)) {
                        stmt.setInt(1, idEquipo);
                        stmt.executeUpdate();
                    }
                } else if (tipoElemento.equals("Material") && idMaterial > 0) {
                    try (PreparedStatement stmt = conn.prepareStatement(deleteMaterialQuery)) {
                        stmt.setInt(1, idMantenimiento);
                        stmt.executeUpdate();
                    }
                    
                    // Restaurar estado del material
                    try (PreparedStatement stmt = conn.prepareStatement(restoreMaterialQuery)) {
                        stmt.setInt(1, idMaterial);
                        stmt.executeUpdate();
                    }
                }
            }
            
            // Eliminar el mantenimiento
            try (PreparedStatement stmt = conn.prepareStatement(deleteMantenimientoQuery)) {
                stmt.setInt(1, idMantenimiento);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                    if (idEquipo > 0) DatabaseConnection.notifyDatabaseChanged("Equipos");
                    if (idMaterial > 0) DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                    return true;
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
}
