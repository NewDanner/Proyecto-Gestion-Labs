/**
 *
 * @author Andrei
 */
package controllers;

import models.MaterialAdicional;
import models.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialController {
    private static final String INSERT_MATERIAL = "INSERT INTO Material_Adicional (nombre_objeto, categoria, cantidad, " +
            "cantidad_minima, extravio, daño, observaciones, Id_Laboratorio, reportado_por) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_MATERIAL = "UPDATE Material_Adicional SET nombre_objeto = ?, categoria = ?, " +
            "cantidad = ?, cantidad_minima = ?, extravio = ?, daño = ?, observaciones = ? WHERE N_Objeto = ?";
    
    private static final String DELETE_MATERIAL = "DELETE FROM Material_Adicional WHERE N_Objeto = ?";
    
    private static final String GET_MATERIALES_BY_LAB = "SELECT * FROM Material_Adicional WHERE Id_Laboratorio = ?";
    
    private static final String GET_MATERIAL_DISPONIBLE = "SELECT * FROM Material_Adicional WHERE Id_Laboratorio = ? " +
            "AND extravio = FALSE AND daño = FALSE AND cantidad > 0";
    
    private static final String UPDATE_CANTIDAD = "UPDATE Material_Adicional SET cantidad = cantidad - ? WHERE N_Objeto = ?";

    public boolean agregarMaterial(MaterialAdicional material) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_MATERIAL)) {
            
            stmt.setString(1, material.getNombreObjeto());
            stmt.setString(2, material.getCategoria());
            stmt.setInt(3, material.getCantidad());
            stmt.setInt(4, material.getCantidadMinima());
            stmt.setBoolean(5, material.isExtravio());
            stmt.setBoolean(6, material.isDano());
            stmt.setString(7, material.getObservaciones());
            stmt.setInt(8, material.getIdLaboratorio());
            stmt.setInt(9, material.getReportadoPor());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean actualizarMaterial(MaterialAdicional material) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_MATERIAL)) {
            
            stmt.setString(1, material.getNombreObjeto());
            stmt.setString(2, material.getCategoria());
            stmt.setInt(3, material.getCantidad());
            stmt.setInt(4, material.getCantidadMinima());
            stmt.setBoolean(5, material.isExtravio());
            stmt.setBoolean(6, material.isDano());
            stmt.setString(7, material.getObservaciones());
            stmt.setInt(8, material.getNObjeto());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminarMaterial(int nObjeto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_MATERIAL)) {
            
            stmt.setInt(1, nObjeto);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<MaterialAdicional> obtenerMaterialesPorLaboratorio(int idLaboratorio) {
        List<MaterialAdicional> materiales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_MATERIALES_BY_LAB)) {
            
            stmt.setInt(1, idLaboratorio);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MaterialAdicional material = new MaterialAdicional(
                    rs.getInt("N_Objeto"),
                    rs.getString("nombre_objeto"),
                    rs.getString("categoria"),
                    rs.getInt("cantidad"),
                    rs.getInt("cantidad_minima"),
                    rs.getBoolean("extravio"),
                    rs.getBoolean("daño"),
                    rs.getString("observaciones"),
                    rs.getInt("Id_Laboratorio"),
                    rs.getTimestamp("fecha_reporte"),
                    rs.getInt("reportado_por")
                );
                material.setProveedor(rs.getString("proveedor"));
                material.setFechaCompra(rs.getDate("fecha_compra"));
                material.setCosto(rs.getDouble("costo"));
                materiales.add(material);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiales;
    }

    public List<MaterialAdicional> obtenerMaterialesDisponibles(int idLaboratorio) {
        List<MaterialAdicional> materiales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_MATERIAL_DISPONIBLE)) {
            
            stmt.setInt(1, idLaboratorio);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                MaterialAdicional material = new MaterialAdicional(
                    rs.getInt("N_Objeto"),
                    rs.getString("nombre_objeto"),
                    rs.getString("categoria"),
                    rs.getInt("cantidad"),
                    rs.getInt("cantidad_minima"),
                    rs.getBoolean("extravio"),
                    rs.getBoolean("daño"),
                    rs.getString("observaciones"),
                    rs.getInt("Id_Laboratorio"),
                    rs.getTimestamp("fecha_reporte"),
                    rs.getInt("reportado_por")
                );
                materiales.add(material);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiales;
    }

    public boolean actualizarCantidadMaterial(int nObjeto, int cantidadUsada) {
        String query = "UPDATE Material_Adicional SET cantidad = cantidad - ? WHERE N_Objeto = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, cantidadUsada);
            stmt.setInt(2, nObjeto);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean verificarDisponibilidadMaterial(int nObjeto, int cantidadRequerida) {
        String query = "SELECT cantidad FROM Material_Adicional WHERE N_Objeto = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, nObjeto);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("cantidad") >= cantidadRequerida;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String obtenerNombreMaterial(int nObjeto) {
        String query = "SELECT nombre_objeto FROM Material_Adicional WHERE N_Objeto = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, nObjeto);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nombre_objeto");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Material desconocido";
    }
}
