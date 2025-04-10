/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Andrei
 */
package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;
import models.User;

public class MantenimientoPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable equiposTable;
    private JButton btnAddMantenimiento, btnFinalizarMantenimiento, btnRefresh;
    private User currentUser;
    
    public MantenimientoPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadEquiposData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel equiposPanel = new JPanel(new BorderLayout());
        equiposTable = new JTable();
        equiposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane equiposScroll = new JScrollPane(equiposTable);
        equiposPanel.add(equiposScroll, BorderLayout.CENTER);
        
        JPanel equiposButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnAddMantenimiento = new JButton("Agregar a Mantenimiento");
        btnAddMantenimiento.addActionListener(this::agregarMantenimiento);
        equiposButtonPanel.add(btnAddMantenimiento);
        
        btnFinalizarMantenimiento = new JButton("Finalizar Mantenimiento");
        btnFinalizarMantenimiento.addActionListener(this::finalizarMantenimiento);
        equiposButtonPanel.add(btnFinalizarMantenimiento);
        
        btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadEquiposData());
        equiposButtonPanel.add(btnRefresh);
        
        equiposPanel.add(equiposButtonPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("Equipos", equiposPanel);
        
        JPanel observacionesPanel = new JPanel(new BorderLayout());
        observacionesPanel.add(new JLabel("Lista de observaciones técnicas", SwingConstants.CENTER), BorderLayout.CENTER);
        tabbedPane.addTab("Observaciones", observacionesPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void loadEquiposData() {
        String query = "SELECT e.Id_Equipo, e.marca, e.modelo, e.numero_de_serie, " +
                      "e.estado, l.nombre AS laboratorio " +
                      "FROM Equipos e " +
                      "LEFT JOIN Laboratorios l ON e.id_laboratorio = l.Id_Laboratorio " +
                      "ORDER BY e.estado, l.nombre";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            model.addColumn("ID");
            model.addColumn("Marca");
            model.addColumn("Modelo");
            model.addColumn("N° Serie");
            model.addColumn("Estado");
            model.addColumn("Laboratorio");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Equipo"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getString("numero_de_serie"),
                    formatEstado(rs.getString("estado")),
                    rs.getString("laboratorio") != null ? rs.getString("laboratorio") : "Sin asignar"
                });
            }
            
            equiposTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar equipos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void agregarMantenimiento(ActionEvent evt) {
        int selectedRow = equiposTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) equiposTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) equiposTable.getValueAt(selectedRow, 4);
        
        if (estadoActual.equalsIgnoreCase("En mantenimiento")) {
            JOptionPane.showMessageDialog(this, "El equipo ya está en mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String observacion = JOptionPane.showInputDialog(this, 
            "Ingrese la observación técnica para el mantenimiento:",
            "Agregar a Mantenimiento", JOptionPane.QUESTION_MESSAGE);
        
        if (observacion != null && !observacion.trim().isEmpty()) {
            cambiarEstadoEquipo(id, "en_mantenimiento", observacion);
        }
    }
    
    private void finalizarMantenimiento(ActionEvent evt) {
        int selectedRow = equiposTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) equiposTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) equiposTable.getValueAt(selectedRow, 4);
        
        if (!estadoActual.equalsIgnoreCase("En mantenimiento")) {
            JOptionPane.showMessageDialog(this, "El equipo no está en mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String observacion = JOptionPane.showInputDialog(this, 
            "Ingrese las acciones realizadas durante el mantenimiento:",
            "Finalizar Mantenimiento", JOptionPane.QUESTION_MESSAGE);
        
        if (observacion != null && !observacion.trim().isEmpty()) {
            cambiarEstadoEquipo(id, "disponible", observacion);
        }
    }
    
    private void cambiarEstadoEquipo(int id, String nuevoEstado, String observacion) {
        String queryEquipo = "UPDATE Equipos SET estado = ? WHERE Id_Equipo = ?";
        String queryObservacion = "INSERT INTO Observaciones (nombre, fecha, detalle, realizado, id_equipo) " +
                                "VALUES (?, NOW(), ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtEquipo = conn.prepareStatement(queryEquipo);
                 PreparedStatement stmtObservacion = conn.prepareStatement(queryObservacion)) {
                
                stmtEquipo.setString(1, nuevoEstado);
                stmtEquipo.setInt(2, id);
                stmtEquipo.executeUpdate();
                
                stmtObservacion.setString(1, "Mantenimiento");
                stmtObservacion.setString(2, observacion);
                stmtObservacion.setString(3, currentUser.getUsername());
                stmtObservacion.setInt(4, id);
                stmtObservacion.executeUpdate();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Estado del equipo actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                DatabaseConnection.notifyDatabaseChanged("Observaciones");
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarEquipo(int id) {
        String query = "DELETE FROM Equipos WHERE Id_Equipo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    DatabaseConnection.resetAutoIncrement("Equipos");
                    conn.commit();
                    
                    JOptionPane.showMessageDialog(this, "Equipo eliminado exitosamente");
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Equipos")) {
            loadEquiposData();
        }
    }
}
