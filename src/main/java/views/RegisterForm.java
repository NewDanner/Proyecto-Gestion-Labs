/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views;

/**
 *
 * @author Andrei
 */
import controllers.AuthController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtCI;
    private JButton btnRegister;
    private AuthController authController;
    private JFrame parent;
    
    public RegisterForm(JFrame parent) {
        this.parent = parent;
        authController = new AuthController();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Registro de Coordinador");
        setSize(400, 350);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Cédula de Identidad:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtCI = new JTextField(20);
        panel.add(txtCI, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        btnRegister = new JButton("Registrarse");
        btnRegister.addActionListener(this::registerAction);
        panel.add(btnRegister, gbc);
        
        add(panel);
    }
    
    private void registerAction(ActionEvent evt) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String ci = txtCI.getText();
        
        if (username.isEmpty() || password.isEmpty() || ci.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success = authController.register(username, password, ci, "coordinador");
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Registro exitoso. Ahora puede iniciar sesión.");
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                parent.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Error en el registro. El usuario ya existe.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
