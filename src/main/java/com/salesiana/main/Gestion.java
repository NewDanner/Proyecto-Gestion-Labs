/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.salesiana.main;

/**
 *
 * @author Andrei
 * @author Danner
 * @author Frank
 */
import javax.swing.*;

public class Gestion {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new views.LoginForm().setVisible(true);
        });
    }
}