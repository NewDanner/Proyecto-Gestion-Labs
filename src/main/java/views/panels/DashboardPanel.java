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

public class DashboardPanel extends JPanel {
    public DashboardPanel(User user) {
        initComponents(user);
    }
    
    private void initComponents(User user) {
        setLayout(new BorderLayout());
        
        JLabel lblWelcome = new JLabel("Bienvenido, " + user.getUsername(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblWelcome, BorderLayout.CENTER);
        
        // Aquí puedes agregar más componentes como estadísticas, etc.
    }
}
