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
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private AuthController authController;
    
    public LoginForm() {
        authController = new AuthController();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Gestión de Laboratorios - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);
        
        // Buttons
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.addActionListener(this::loginAction);
        panel.add(btnLogin, gbc);
        
        gbc.gridx = 2;
        btnRegister = new JButton("Registrarse");
        btnRegister.addActionListener(this::registerAction);
        panel.add(btnRegister, gbc);
        
        add(panel);
    }
    
    private void loginAction(ActionEvent evt) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        User user = authController.login(username, password);
        
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Bienvenido " + user.getUsername());
            new MainFrame(user).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registerAction(ActionEvent evt) {
        new RegisterForm(this).setVisible(true);
    }
}
