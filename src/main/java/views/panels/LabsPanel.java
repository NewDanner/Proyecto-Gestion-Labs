/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Andrei
 */
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;

public class LabsPanel extends JPanel {
    private JTable labsTable;
    private JButton btnAddLab, btnEditLab, btnDeleteLab, btnRefresh;
    
    public LabsPanel() {
        initComponents();
        loadLabsData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Configurar tabla
        labsTable = new JTable();
        labsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(labsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        btnAddLab = new JButton("Agregar");
        btnAddLab.addActionListener(this::showAddLabDialog);
        buttonPanel.add(btnAddLab);
        
        btnEditLab = new JButton("Editar");
        btnEditLab.addActionListener(this::showEditLabDialog);
        buttonPanel.add(btnEditLab);
        
        btnDeleteLab = new JButton("Eliminar");
        btnDeleteLab.addActionListener(this::deleteSelectedLab);
        buttonPanel.add(btnDeleteLab);
        
        btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadLabsData());
        buttonPanel.add(btnRefresh);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadLabsData() {
        String query = "SELECT Id_Laboratorio, nombre, materia, capacidad, estado FROM Laboratorios";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Hacer que la tabla no sea editable directamente
                }
            };
            
            model.addColumn("ID");
            model.addColumn("Nombre");
            model.addColumn("Materia");
            model.addColumn("Capacidad");
            model.addColumn("Estado");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Laboratorio"),
                    rs.getString("nombre"),
                    formatMateria(rs.getString("materia")),
                    rs.getInt("capacidad"),
                    formatEstado(rs.getString("estado"))
                });
            }
            
            labsTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar laboratorios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatMateria(String materia) {
        return materia.replace("_", " ").substring(0, 1).toUpperCase() + 
               materia.replace("_", " ").substring(1).toLowerCase();
    }
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void showAddLabDialog(ActionEvent evt) {
        LabDialog dialog = new LabDialog(null, "Agregar Laboratorio");
        if (dialog.showDialog()) {
            addNewLab(dialog.getNombre(), dialog.getMateria(), 
                     dialog.getCapacidad(), dialog.getEstado(), 
                     dialog.getDescripcion());
        }
    }
    
    private void showEditLabDialog(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un laboratorio para editar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) labsTable.getValueAt(selectedRow, 0);
        String nombre = (String) labsTable.getValueAt(selectedRow, 1);
        String materia = (String) labsTable.getValueAt(selectedRow, 2);
        int capacidad = (int) labsTable.getValueAt(selectedRow, 3);
        String estado = (String) labsTable.getValueAt(selectedRow, 4);
        
        // Obtener descripción de la base de datos
        String descripcion = getLabDescription(id);
        
        LabDialog dialog = new LabDialog(null, "Editar Laboratorio");
        dialog.setData(nombre, materia, capacidad, estado, descripcion);
        
        if (dialog.showDialog()) {
            updateLab(id, dialog.getNombre(), dialog.getMateria(), 
                     dialog.getCapacidad(), dialog.getEstado(), 
                     dialog.getDescripcion());
        }
    }
    
    private String getLabDescription(int id) {
        String query = "SELECT descripcion FROM Laboratorios WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("descripcion");
            }
            return "";
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private void deleteSelectedLab(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un laboratorio para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) labsTable.getValueAt(selectedRow, 0);
        String nombre = (String) labsTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el laboratorio:\n" + nombre + "?",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteLab(id);
        }
    }
    
    private void addNewLab(String nombre, String materia, String capacidad, String estado, String descripcion) {
        String query = "INSERT INTO Laboratorios (nombre, materia, capacidad, estado, descripcion) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nombre);
            stmt.setString(2, materia.toLowerCase().replace(" ", "_"));
            stmt.setInt(3, Integer.parseInt(capacidad));
            stmt.setString(4, estado.toLowerCase().replace(" ", "_"));
            stmt.setString(5, descripcion);
            
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Laboratorio agregado exitosamente");
            loadLabsData();
            
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al agregar laboratorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateLab(int id, String nombre, String materia, String capacidad, String estado, String descripcion) {
        String query = "UPDATE Laboratorios SET nombre = ?, materia = ?, capacidad = ?, estado = ?, descripcion = ? WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nombre);
            stmt.setString(2, materia.toLowerCase().replace(" ", "_"));
            stmt.setInt(3, Integer.parseInt(capacidad));
            stmt.setString(4, estado.toLowerCase().replace(" ", "_"));
            stmt.setString(5, descripcion);
            stmt.setInt(6, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Laboratorio actualizado exitosamente");
                loadLabsData();
            }
            
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar laboratorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteLab(int id) {
        String query = "DELETE FROM Laboratorios WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Laboratorio eliminado exitosamente");
                loadLabsData();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar laboratorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Clase interna para el diálogo de laboratorio
    private class LabDialog extends JDialog {
        private JComboBox<String> cbNombre, cbMateria, cbEstado;
        private JTextField txtCapacidad;
        private JTextArea txtDescripcion;
        private boolean confirmed = false;
        
        public LabDialog(JFrame parent, String title) {
            super(parent, title, true);
            setSize(400, 350);
            setLocationRelativeTo(parent);
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            
            // Nombre del laboratorio (combobox con valores 1-7)
            cbNombre = new JComboBox<>(new String[]{
                "Laboratorio 1", "Laboratorio 2", "Laboratorio 3", 
                "Laboratorio 4", "Laboratorio 5", "Laboratorio 6", 
                "Laboratorio 7"
            });
            panel.add(new JLabel("Nombre:"));
            panel.add(cbNombre);
            
            // Materia
            cbMateria = new JComboBox<>(new String[]{
                "Electrónica", "Hardware", "Redes y Telecomunicaciones"
            });
            panel.add(new JLabel("Materia:"));
            panel.add(cbMateria);
            
            // Capacidad
            txtCapacidad = new JTextField("30");
            panel.add(new JLabel("Capacidad:"));
            panel.add(txtCapacidad);
            
            // Estado
            cbEstado = new JComboBox<>(new String[]{
                "Disponible", "En mantenimiento", "Inactivo"
            });
            panel.add(new JLabel("Estado:"));
            panel.add(cbEstado);
            
            // Descripción
            txtDescripcion = new JTextArea(3, 20);
            JScrollPane descScroll = new JScrollPane(txtDescripcion);
            panel.add(new JLabel("Descripción:"));
            panel.add(descScroll);
            
            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnOk = new JButton("Aceptar");
            btnOk.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> dispose());
            
            buttonPanel.add(btnOk);
            buttonPanel.add(btnCancel);
            
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        public boolean showDialog() {
            setVisible(true);
            return confirmed;
        }
        
        public void setData(String nombre, String materia, int capacidad, String estado, String descripcion) {
            cbNombre.setSelectedItem(nombre);
            cbMateria.setSelectedItem(materia);
            txtCapacidad.setText(String.valueOf(capacidad));
            cbEstado.setSelectedItem(estado);
            txtDescripcion.setText(descripcion);
        }
        
        public String getNombre() {
            return (String) cbNombre.getSelectedItem();
        }
        
        public String getMateria() {
            return (String) cbMateria.getSelectedItem();
        }
        
        public String getCapacidad() {
            return txtCapacidad.getText();
        }
        
        public String getEstado() {
            return (String) cbEstado.getSelectedItem();
        }
        
        public String getDescripcion() {
            return txtDescripcion.getText();
        }
    }
}