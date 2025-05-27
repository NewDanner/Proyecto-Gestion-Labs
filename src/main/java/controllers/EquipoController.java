package controllers;

import models.Equipo;
import models.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class EquipoController {
    
    private static final String DELETE_EQUIPO = "DELETE FROM Equipos WHERE Id_Equipo = ?";
    
    private static final String GET_EQUIPOS_BY_LAB = "SELECT * FROM Equipos WHERE Id_Laboratorio = ?";
    
    private static final String GET_EQUIPO_BY_ID = "SELECT * FROM Equipos WHERE Id_Equipo = ?";
    
    private static final String GET_EQUIPOS_OPERATIVOS = "SELECT * FROM Equipos WHERE Estado = 'Operativo'";
    
    private static final String GET_ALL_EQUIPOS = "SELECT * FROM Equipos ORDER BY marca, modelo";
    
    private static final String INSERT_EQUIPO = "INSERT INTO Equipos (" +
        "Marca, Modelo, numero_de_serie, Procesador, RAM, " +
        "Almacenamiento, SO, Estado, Id_Laboratorio) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  

    private static final String UPDATE_EQUIPO = "UPDATE Equipos SET " +
    "Marca = ?, Modelo = ?, numero_de_serie = ?, " +
    "Procesador = ?, RAM = ?, Almacenamiento = ?, " +
    "SO = ?, Estado = ?, Id_Laboratorio = ? " +  // 9 parámetros para SET
    "WHERE Id_Equipo = ?";  // 1 parámetro para WHERE (total 10)

    public boolean agregarEquipo(Equipo equipo) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_EQUIPO, Statement.RETURN_GENERATED_KEYS)) {
            
            // Establecer los 9 parámetros
            stmt.setString(1, equipo.getMarca());
            stmt.setString(2, equipo.getModelo());
            stmt.setString(3, equipo.getNumeroSerie());
            stmt.setString(4, equipo.getProcesador());
            stmt.setString(5, equipo.getRam());
            stmt.setString(6, equipo.getAlmacenamiento());
            stmt.setString(7, equipo.getSo());
            stmt.setString(8, equipo.getEstado());
            
            if (equipo.getIdLaboratorio() != null) {
                stmt.setInt(9, equipo.getIdLaboratorio());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        equipo.setIdEquipo(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al agregar equipo:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error al agregar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean actualizarEquipo(Equipo equipo) {
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(UPDATE_EQUIPO)) {
        
        // Establecer los 9 parámetros de SET
        stmt.setString(1, equipo.getMarca());
        stmt.setString(2, equipo.getModelo());
        stmt.setString(3, equipo.getNumeroSerie());
        stmt.setString(4, equipo.getProcesador());
        stmt.setString(5, equipo.getRam());
        stmt.setString(6, equipo.getAlmacenamiento());
        stmt.setString(7, equipo.getSo());
        stmt.setString(8, equipo.getEstado());
        
        if (equipo.getIdLaboratorio() != null) {
            stmt.setInt(9, equipo.getIdLaboratorio());
        } else {
            stmt.setNull(9, Types.INTEGER);
        }
        stmt.setInt(10, equipo.getIdEquipo());
        
        int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar equipo:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error al actualizar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    public boolean eliminarEquipo(int idEquipo) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_EQUIPO)) {
            
            stmt.setInt(1, idEquipo);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                DatabaseConnection.resetAutoIncrement("Equipos");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Equipo> obtenerEquiposPorLaboratorio(int idLaboratorio) {
        return executeQueryWithParameter(GET_EQUIPOS_BY_LAB, idLaboratorio);
    }
    
    public Equipo obtenerEquipoPorId(int idEquipo) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_EQUIPO_BY_ID)) {
            
            stmt.setInt(1, idEquipo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return crearEquipoDesdeResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Equipo> obtenerEquiposOperativos() {
        return executeQuery(GET_EQUIPOS_OPERATIVOS);
    }
    
    public List<Equipo> obtenerTodosEquipos() {
        return executeQuery(GET_ALL_EQUIPOS);
    }
    
    private List<Equipo> executeQuery(String query) {
        List<Equipo> equipos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                equipos.add(crearEquipoDesdeResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }
    
    private List<Equipo> executeQueryWithParameter(String query, int parameter) {
        List<Equipo> equipos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, parameter);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                equipos.add(crearEquipoDesdeResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipos;
    }
    
    private void setEquipoParameters(PreparedStatement stmt, Equipo equipo) throws SQLException {
        stmt.setString(1, equipo.getMarca());
        stmt.setString(2, equipo.getModelo());
        stmt.setString(3, equipo.getNumeroSerie());
        stmt.setString(4, equipo.getProcesador());  // Corregido a Procesador
        stmt.setString(5, equipo.getRam());
        stmt.setString(6, equipo.getAlmacenamiento());
        stmt.setString(7, equipo.getSo());
        stmt.setString(8, equipo.getEstado());
        
        if (equipo.getIdLaboratorio() != null) {
            stmt.setInt(9, equipo.getIdLaboratorio());
        } else {
            stmt.setNull(9, Types.INTEGER);
        }
    }

    private Equipo crearEquipoDesdeResultSet(ResultSet rs) throws SQLException {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(rs.getInt("Id_Equipo"));
        equipo.setMarca(rs.getString("Marca"));
        equipo.setModelo(rs.getString("Modelo"));
        equipo.setNumeroSerie(rs.getString("numero_de_serie"));
        equipo.setProcesador(rs.getString("Procesador"));  // Nombre correcto
        equipo.setRam(rs.getString("RAM"));
        equipo.setAlmacenamiento(rs.getString("Almacenamiento"));
        equipo.setSo(rs.getString("SO"));
        equipo.setEstado(rs.getString("Estado"));
        
        int labId = rs.getInt("Id_Laboratorio");
        if (!rs.wasNull()) {
            equipo.setIdLaboratorio(labId);
        }
        
        return equipo;
    }
}