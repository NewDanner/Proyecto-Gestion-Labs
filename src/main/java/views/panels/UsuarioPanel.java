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
import javax.swing.border.TitledBorder;
import views.LoginForm;

public class UsuarioPanel extends JPanel {
    private User currentUser;
    private JFrame parentFrame;
    private AuthController authController;

    private JLabel lblUsername, lblCI, lblRole;
    private JButton btnEditar, btnCerrarSesion;
    private JPanel editPanel;
    private String sexoUsuario;

    public UsuarioPanel(User user, JFrame parent) {
        this.currentUser = user;
        this.parentFrame = parent;
        this.authController = new AuthController();
        setOpaque(false);
        initComponents();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Color colorSuperior = new Color(249, 45, 168);
        Color colorInferior = new Color(255, 209, 12);

        GradientPaint gradient = new GradientPaint(0, 0, colorSuperior, 0, getHeight(), colorInferior);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        infoPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Cargar icono según el sexo del usuario
        if ("Hombre".equals(sexoUsuario)) {
            ImageIcon topIconM = new ImageIcon("resources/icon3.png");
            Image img = topIconM.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel lblIconM = new JLabel(new ImageIcon(img));
            lblIconM.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            infoPanel.add(lblIconM, gbc);
            
        } else {
            ImageIcon topIcon = new ImageIcon("resources/user2.png");
            Image img = topIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            JLabel lblIcon = new JLabel(new ImageIcon(img));
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            infoPanel.add(lblIcon, gbc);
        }

        JLabel lblTitulo = new JLabel("MI PERFIL", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 25));
        lblTitulo.setForeground(Color.WHITE);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        infoPanel.add(lblTitulo, gbc);

        
        int fontSize = 25;
        // Definimos un color plomo negreado (gris oscuro)
        Color plomoNegreado = new Color(125, 121, 120); // RGB para un gris oscuro
        // Alternativa: Color plomoNegreado = Color.DARK_GRAY;

        Font labelFont = new Font("Arial", Font.PLAIN, fontSize);
        Font staticLabelFont = new Font("Arial", Font.BOLD, fontSize);

        gbc.gridwidth = 1;

        // Usuario
        gbc.gridy = 2;
        gbc.gridx = 2;
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(staticLabelFont);
        userLabel.setForeground(plomoNegreado); // Color plomo negreado
        infoPanel.add(userLabel, gbc);

        lblUsername = new JLabel();
        lblUsername.setFont(labelFont);
        lblUsername.setForeground(plomoNegreado); // Color plomo negreado
        gbc.gridx = 3;
        infoPanel.add(lblUsername, gbc);

        // Cédula
        gbc.gridx = 2;
        gbc.gridy = 3;
        JLabel ciLabel = new JLabel("Cédula:");
        ciLabel.setFont(staticLabelFont);
        ciLabel.setForeground(plomoNegreado); // Color plomo negreado
        infoPanel.add(ciLabel, gbc);

        lblCI = new JLabel();
        lblCI.setFont(labelFont);
        lblCI.setForeground(plomoNegreado); // Color plomo negreado
        gbc.gridx = 3;
        infoPanel.add(lblCI, gbc);

        // Rol
        gbc.gridx = 2;
        gbc.gridy = 4;
        JLabel roleLabel = new JLabel("Rol:");
        roleLabel.setFont(staticLabelFont);
        roleLabel.setForeground(plomoNegreado); // Color plomo negreado
        infoPanel.add(roleLabel, gbc);

        lblRole = new JLabel();
        lblRole.setFont(labelFont);
        lblRole.setForeground(plomoNegreado); // Color plomo negreado
        gbc.gridx = 3;
        infoPanel.add(lblRole, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        btnEditar = new JButton("Editar Perfil");
        styleButton(btnEditar);
        btnEditar.addActionListener(this::mostrarPanelEdicion);

        btnCerrarSesion = new JButton("Cerrar Sesión");
        styleButton(btnCerrarSesion);
        btnCerrarSesion.addActionListener(this::cerrarSesion);

        buttonPanel.add(btnEditar);
        buttonPanel.add(btnCerrarSesion);

        editPanel = crearPanelEdicion();
        editPanel.setVisible(false);
        editPanel.setOpaque(false);

        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(editPanel, BorderLayout.NORTH);

        loadUserData();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(249, 45, 168)),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private JPanel crearPanelEdicion() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 200));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(25, 25, 112)),
                "Editar Perfil",
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 14),
                new Color(25, 25, 112)
        ));

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
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.add(btnGuardar);
        btnPanel.add(btnCancelar);
        panel.add(btnPanel, gbc);

        txtUsername.setText(currentUser.getUsername());
        txtCI.setText(currentUser.getCi());

        btnGuardar.addActionListener(e -> {
            actualizarDatosUsuario(txtUsername.getText(),
                    new String(txtPassword.getPassword()),
                    txtCI.getText());
            editPanel.setVisible(false);
        });

        btnCancelar.addActionListener(e -> editPanel.setVisible(false));

        return panel;
    }

    private void mostrarPanelEdicion(ActionEvent e) {
        editPanel.setVisible(true);
    }

    private void loadUserData() {
        lblUsername.setText(currentUser.getUsername());
        lblCI.setText(currentUser.getCi());
        lblRole.setText(currentUser.getRole());
        sexoUsuario = currentUser.getSexo();
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
                    currentUser.getId(), username, ci, currentUser.getRole(), currentUser.getSexo());
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
