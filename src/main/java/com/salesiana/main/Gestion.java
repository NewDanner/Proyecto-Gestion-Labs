package com.salesiana.main;

/**
 *
 * @author Andrei
 */
import javax.swing.*;

public class Gestion {
    public static void main(String[] args) {
        try {
            // Establecer el look and feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Configuración adicional para mejorar la apariencia
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JFrame.setDefaultLookAndFeelDecorated(true);
            
        } catch (Exception e) {
            System.err.println("Error al configurar la apariencia:");
            e.printStackTrace();
        }
        
        // Iniciar la aplicación en el EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            try {
                // Mostrar la ventana de login
                views.LoginForm loginForm = new views.LoginForm();
                loginForm.setVisible(true);
                
                // Centrar la ventana en la pantalla
                loginForm.setLocationRelativeTo(null);
            } catch (Exception e) {
                System.err.println("Error al iniciar la aplicación:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error crítico al iniciar la aplicación:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}