/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Andrei
 */
import models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;

public class DashboardPanel extends JPanel {
    private User currentUser;
    
    public DashboardPanel(User user) {
        this.currentUser = user;
        initComponents();
        loadStats();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JLabel lblWelcome = new JLabel("Bienvenido, " + currentUser.getUsername(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblWelcome, BorderLayout.CENTER);
    }
    
    private void loadStats() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        try {
            // Laboratorios Activos
            int laboratoriosActivos = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'disponible'");
            statsPanel.add(createStatPanel("Laboratorios Activos", String.valueOf(laboratoriosActivos)));
            
            // Préstamos Hoy
            int prestamosHoy = getCountFromDB("SELECT COUNT(*) FROM Prestamo WHERE fecha_reserva = CURDATE()");
            statsPanel.add(createStatPanel("Préstamos Hoy", String.valueOf(prestamosHoy)));
            
            // En Mantenimiento
            int enMantenimiento = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'en_mantenimiento'");
            statsPanel.add(createStatPanel("En Mantenimiento", String.valueOf(enMantenimiento)));
            
            // Disponibles
            int disponibles = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'disponible'");
            statsPanel.add(createStatPanel("Disponibles", String.valueOf(disponibles)));
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estadísticas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    private int getCountFromDB(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    private JPanel createStatPanel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblValue, BorderLayout.CENTER);
        return panel;
    }
}
