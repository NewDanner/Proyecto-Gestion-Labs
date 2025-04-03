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

public class ReservacionPanel extends JPanel {
    public ReservacionPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        add(new JLabel("Panel de Reservas - En construcción", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}