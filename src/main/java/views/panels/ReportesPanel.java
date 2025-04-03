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

public class ReportesPanel extends JPanel {
    public ReportesPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        JLabel lblMensaje = new JLabel("Reportes - En construcción", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblMensaje, BorderLayout.CENTER);
    }
}