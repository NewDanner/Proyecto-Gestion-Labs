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
import javax.swing.table.DefaultTableModel;

public class LabsPanel extends JPanel {
    private JTable labsTable;
    
    public LabsPanel() {
        initComponents();
        loadLabsData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Crear tabla
        labsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(labsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadLabsData());
        
        JButton btnAdd = new JButton("Agregar Laboratorio");
        // Aquí puedes agregar el ActionListener para añadir nuevos laboratorios
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnAdd);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadLabsData() {
        String query = "SELECT Id_Laboratorio, nombre, materia, estado FROM Laboratorios";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Nombre");
            model.addColumn("Materia");
            model.addColumn("Estado");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Laboratorio"),
                    rs.getString("nombre"),
                    rs.getString("materia"),
                    rs.getString("estado")
                });
            }
            
            labsTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los laboratorios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}