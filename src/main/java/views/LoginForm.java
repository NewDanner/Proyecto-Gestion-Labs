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
    private JButton btnSalir;
    private AuthController authController; // Nuevo
    
    public LoginForm() {
        authController = new AuthController();
        initComponents();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        setSize(screenSize);
        setLocation(0, 0);
        setResizable(false);
        setVisible(true);
    }
    
    private void initComponents() {
        setTitle("Sistema de Gestión de Laboratorios - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        Font largerFont = new Font("Arial", Font.PLAIN, 14);
        
         Panel panel = new Panel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

       ImageIcon topIcon = new ImageIcon("resources/USUARIO.png");
        Image img = topIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); 
        ImageIcon resizedIcon = new ImageIcon(img);
        JLabel lblIcon = new JLabel(resizedIcon);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblIcon, gbc);
        
        JLabel lblTitle = new JLabel("INICIO DE SESION", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // ---------------- CAMPO USUARIO ----------------
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
        userPanel.setBackground(new Color(249,45,168)); 

        ImageIcon userIcon = new ImageIcon("resources/minicono.png");
        Image userImg = userIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledUserIcon = new ImageIcon(userImg);
        JLabel lblUserIcon = new JLabel(scaledUserIcon);
        lblUserIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtUsername = new JTextField("Usuario", 20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtUsername.setBackground(Color.WHITE);
        txtUsername.setForeground(Color.GRAY);

        // Placeholder: borrar texto al enfocar
        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUsername.getText().equals("Usuario")) {
                    txtUsername.setText("");
                    txtUsername.setForeground(Color.DARK_GRAY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtUsername.getText().isEmpty()) {
                    txtUsername.setText("Usuario");
                    txtUsername.setForeground(Color.GRAY);
                }
            }
        });

        userPanel.add(lblUserIcon);
        userPanel.add(txtUsername);
        panel.add(userPanel, gbc);


        // ---------------- CAMPO CONTRASEÑA ----------------
        gbc.gridy = 3;

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
        passwordPanel.setBackground(new Color(249,45,168)); 

        ImageIcon lockIcon = new ImageIcon("resources/candado7.png");
        Image lockImg = lockIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledLockIcon = new ImageIcon(lockImg);
        JLabel lblLockIcon = new JLabel(scaledLockIcon);
        lblLockIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtPassword = new JPasswordField("Contraseña", 20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setForeground(Color.GRAY);
        txtPassword.setEchoChar((char) 0); // Mostrar texto como normal inicialmente

        // Placeholder: borrar texto al enfocar
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).equals("Contraseña")) {
                    txtPassword.setText("");
                    txtPassword.setForeground(Color.DARK_GRAY);
                    txtPassword.setEchoChar('\u2022'); // Activar ocultamiento
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).isEmpty()) {
                    txtPassword.setText("Contraseña");
                    txtPassword.setForeground(Color.GRAY);
                    txtPassword.setEchoChar((char) 0); // Mostrar texto como normal
                }
            }
        });

        passwordPanel.add(lblLockIcon);
        passwordPanel.add(txtPassword);
        panel.add(passwordPanel, gbc);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.CENTER;


        btnLogin = new JButton("INICIAR SESIÓN");
        btnLogin.setFont(largerFont.deriveFont(Font.BOLD, 18f)); // Fuente del botón
        btnLogin.setForeground(Color.WHITE); // Color de las letras
        btnLogin.setFocusPainted(false); // Quitar el enfoque visible

        btnLogin.setOpaque(false); 
        btnLogin.setContentAreaFilled(false); 
        btnLogin.setBorderPainted(false); 

        btnLogin.addActionListener(this::loginAction);

        panel.add(btnLogin, gbc);
      
       gbc.gridy = 5;
        btnRegister = new JButton("REGISTRAR NUEVO USUARIO");
        btnRegister.setFont(largerFont.deriveFont(Font.BOLD, 18f));
        btnRegister.setBackground(new Color(255, 255, 255, 70));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setOpaque(false);

        // Añadir ActionListener para llamar a registerAction
        btnRegister.addActionListener(this::registerAction);

        panel.add(btnRegister, gbc);
        add(panel);
        
        gbc.gridy = 30;
        btnSalir = new JButton("SALIR");
        btnSalir.setFont(largerFont.deriveFont(Font.BOLD, 24f));
        btnSalir.setBackground(new Color(255, 255, 255, 70));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setBorderPainted(false);
        btnSalir.setOpaque(false);

        btnSalir.addActionListener(e -> {
            
            ((LoginForm) SwingUtilities.getWindowAncestor(panel)).dispose();
        });

        panel.add(btnSalir, gbc);
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
    // Mostrar cuadro de diálogo para ingresar la contraseña del administrador
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    JLabel label = new JLabel("Para poder crear un nuevo usuario, ingrese la contraseña de un administrador:");
    JPasswordField passwordField = new JPasswordField(15);
    panel.add(label, BorderLayout.NORTH);
    panel.add(passwordField, BorderLayout.CENTER);

    int option = JOptionPane.showConfirmDialog(this, panel, "Autenticación de Administrador",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (option == JOptionPane.OK_OPTION) {
        String inputPassword = new String(passwordField.getPassword());

        // Intentar autenticar con cualquier usuario con rol de Administrador y esa contraseña
        User admin = authController.getAdminByPassword(inputPassword);

        if (admin != null|| inputPassword.equals("214365")) { //    Código para el usuario Administrador
            // Autenticado correctamente
            JOptionPane.showMessageDialog(this, "Administrador verificado correctamente.");
            new RegisterForm(this).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Contraseña incorrecta o no pertenece a un administrador.",
                    "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }
}
}
