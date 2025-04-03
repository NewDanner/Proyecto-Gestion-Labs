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
import java.awt.event.*;
import controllers.AuthController;
import views.LoginForm;

public class UsuarioPanel extends JPanel {
    private User currentUser;
    private JFrame parentFrame;
    private AuthController authController;
    
    private JLabel lblUsername, lblCI, lblRole;
    private JButton btnEditar, btnCerrarSesion;
    private JPanel editPanel;
    
    public UsuarioPanel(User user, JFrame parent) {
        this.currentUser = user;
        this.parentFrame = parent;
        this.authController = new AuthController();
        initComponents();
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel de información
        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitulo = new JLabel("Mi Perfil", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        infoPanel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        infoPanel.add(new JLabel("Usuario:"), gbc);
        
        lblUsername = new JLabel();
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        infoPanel.add(lblUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        infoPanel.add(new JLabel("Cédula:"), gbc);
        
        lblCI = new JLabel();
        lblCI.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        infoPanel.add(lblCI, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        infoPanel.add(new JLabel("Rol:"), gbc);
        
        lblRole = new JLabel();
        lblRole.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        infoPanel.add(lblRole, gbc);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        btnEditar = new JButton("Editar Perfil");
        btnEditar.setFont(new Font("Arial", Font.BOLD, 14));
        btnEditar.addActionListener(this::mostrarPanelEdicion);
        buttonPanel.add(btnEditar);
        
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrarSesion.addActionListener(this::cerrarSesion);
        buttonPanel.add(btnCerrarSesion);
        
        // Panel de edición (inicialmente oculto)
        editPanel = crearPanelEdicion();
        editPanel.setVisible(false);
        
        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(editPanel, BorderLayout.NORTH);
        
        loadUserData();
    }
    
    private JPanel crearPanelEdicion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Editar Perfil"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JTextField txtCI = new JTextField(20);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nuevo Usuario:"), gbc);
        
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Nueva Contraseña:"), gbc);
        
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Nueva Cédula:"), gbc);
        
        gbc.gridx = 1;
        panel.add(txtCI, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.add(btnGuardar);
        btnPanel.add(btnCancelar);
        panel.add(btnPanel, gbc);
        
        // Llenar con datos actuales
        txtUsername.setText(currentUser.getUsername());
        txtCI.setText(currentUser.getCi());
        
        // Acciones de los botones
        btnGuardar.addActionListener(e -> {
            actualizarDatosUsuario(txtUsername.getText(), 
                                 new String(txtPassword.getPassword()), 
                                 txtCI.getText());
            editPanel.setVisible(false);
        });
        
        btnCancelar.addActionListener(e -> {
            editPanel.setVisible(false);
        });
        
        return panel;
    }
    
    private void mostrarPanelEdicion(ActionEvent e) {
        editPanel.setVisible(true);
    }
    
    private void loadUserData() {
        lblUsername.setText(currentUser.getUsername());
        lblCI.setText(currentUser.getCi());
        lblRole.setText(currentUser.getRole());
    }
    
    private void actualizarDatosUsuario(String username, String password, String ci) {
        if (username.isEmpty() || ci.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y cédula son campos obligatorios", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success;
        if (!password.isEmpty()) {
            success = authController.updateUser(
                currentUser.getId(), username, password, ci);
        } else {
            success = authController.updateUserWithoutPassword(
                currentUser.getId(), username, ci);
        }
        
        if (success) {
            currentUser = new User(
                currentUser.getId(), username, ci, currentUser.getRole());
            loadUserData();
            JOptionPane.showMessageDialog(this, "Datos actualizados exitosamente");
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar los datos", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cerrarSesion(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Cierre de Sesión", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            parentFrame.dispose();
            new LoginForm().setVisible(true);
        }
    }
}