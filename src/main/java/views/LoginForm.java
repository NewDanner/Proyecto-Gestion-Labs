/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template

/**
 *
 * @author Andrei
 */
package views;

import controllers.AuthController;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
        
        Font largerFont = new Font("Arial", Font.PLAIN, 14);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Usuario:"), gbc);
        
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setFont(largerFont);
        panel.add(txtUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Contraseña:"), gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(largerFont);
        panel.add(txtPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(largerFont);
        btnLogin.addActionListener(this::loginAction);
        panel.add(btnLogin, gbc);
        
        gbc.gridy = 4;
        btnRegister = new JButton("Registrarse");
        btnRegister.setFont(largerFont);
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
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                new MainFrame(user).setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registerAction(ActionEvent evt) {
        new RegisterForm(this).setVisible(true);
    }
}
