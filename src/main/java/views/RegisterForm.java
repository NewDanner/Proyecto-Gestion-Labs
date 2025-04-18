/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views;

import controllers.AuthController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class RegisterForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtCI;
    private JButton btnRegister;
    private AuthController authController;
    private JFrame parent;
    private JComboBox<String> cbRole;
    private JComboBox<String> cbSexo;
    private JButton btnCancelar;
    
    public RegisterForm(JFrame parent) {
        this.parent = parent;
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
        setTitle("Registro de Coordinador");
        setSize(400, 350);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();

            // Colores del gradiente azul
            Color color1 = new Color(249,45,168); // Azul claro
            Color color2 = new Color(255, 209, 12); // Azul oscuro
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);

            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    };
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        ImageIcon topIcon = new ImageIcon("resources/NEW.png");
        if (topIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image img = topIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(img);
            JLabel lblIcon = new JLabel(resizedIcon);
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

            gbc.gridx = 2;
            gbc.gridy = -2;
            panel.add(lblIcon, gbc); // Añadir imagen al panel
        } else {
            System.err.println("No se pudo cargar la imagen.");
        }


                                // Editar la etiqueta "Usuario:"
                JLabel lblUsuario = new JLabel("Usuario:");
                lblUsuario.setFont(new Font("Arial", Font.BOLD, 25)); // Tamaño 16 y texto en negrita
                lblUsuario.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = 3;
                panel.add(lblUsuario, gbc);

                txtUsername = new JTextField(20);
                txtUsername.setFont(new Font("Arial", Font.BOLD, 14)); // Tamaño 14, texto sin grosor (PLAIN)
                txtUsername.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Espacio interno sin borde grueso
                txtUsername.setForeground(Color.BLACK); // Color del texto
                txtUsername.setBackground(Color.WHITE); // Fondo blanco de la caja
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                panel.add(txtUsername, gbc);

                JLabel lblContraseña = new JLabel("Contraseña:");
                lblContraseña.setFont(new Font("Arial", Font.BOLD, 25)); // Tamaño 16 y texto en negrita
                lblContraseña.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = 4;
                panel.add(lblContraseña, gbc);
                
                txtPassword = new JPasswordField(20);
                txtPassword.setFont(new Font("Arial", Font.BOLD, 14)); // Tamaño 14, texto sin grosor (PLAIN)
                txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Espacio interno sin borde grueso
                txtPassword.setForeground(Color.BLACK); // Color del texto
                txtPassword.setBackground(Color.WHITE); // Fondo blanco de la caja
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                panel.add(txtPassword, gbc);

                JLabel lblCI = new JLabel("Cedula de Identidad:");
                lblCI.setFont(new Font("Arial", Font.BOLD, 25)); // Tamaño 16 y texto en negrita
                lblCI.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = 5;
                panel.add(lblCI, gbc);
                
                txtCI = new JTextField(20); // Cambiar JPasswordField por JTextField
                txtCI.setFont(new Font("Arial", Font.BOLD, 14)); // Ajustar el estilo de fuente
                txtCI.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Espaciado interno
                txtCI.setForeground(Color.BLACK); // Color del texto
                txtCI.setBackground(Color.WHITE); // Fondo blanco
                gbc.gridx = 2;
                gbc.gridwidth = 2;
                panel.add(txtCI, gbc);
                
                /// Rol
                JLabel lblRol = new JLabel("Rol:");
                lblRol.setFont(new Font("Arial", Font.BOLD, 25)); // Tamaño 16 y texto en negrita
                lblRol.setForeground(Color.WHITE);
                gbc.gridx = 0;
                gbc.gridy = 7;
                panel.add(lblRol, gbc);

                gbc.gridx = 2;
                cbRole = new JComboBox<>(new String[] { "Administrador", "Docente", "Técnico" });
                cbRole.setFont(new Font("Ärial",Font.PLAIN,18));
                cbRole.setPreferredSize(new Dimension(150, 30)); // Ajusta el tamaño
                panel.add(cbRole, gbc);

// Sexo
JLabel lblSexo = new JLabel("Sexo:");
                lblSexo.setFont(new Font("Arial", Font.BOLD, 25)); // Tamaño 16 y texto en negrita
                lblSexo.setForeground(Color.WHITE);
gbc.gridx = 0;
gbc.gridy = 8;
panel.add(lblSexo, gbc);


gbc.gridx = 2;
cbSexo = new JComboBox<>(new String[] { "Hombre", "Mujer" });
cbSexo.setFont(new Font("Ärial",Font.PLAIN,18));
cbSexo.setPreferredSize(new Dimension(150, 30)); // Ajusta el tamaño
panel.add(cbSexo, gbc);
                

                Font largerFont = new Font("Arial",Font.BOLD,18);
                btnRegister = new JButton("REGISTRAR");
                btnRegister.setFont(largerFont);
                btnRegister.setForeground(Color.BLACK);
                btnRegister.setFocusPainted(false);
                btnRegister.setBorderPainted(false);
                btnRegister.setOpaque(true);

        // Añadir ActionListener para ejecutar registerAction y abrir LoginForm
        btnRegister.addActionListener(e -> {
            registerAction(e); // Ejecutar la lógica de registro
            openLoginForm(); // Abrir la ventana LoginForm
        });

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        panel.add(btnRegister, gbc);
        add(panel);
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(largerFont);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setOpaque(true);
        gbc.gridx = 3;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        btnCancelar.addActionListener(e -> {
    
        openLoginForm();

    ((JFrame) SwingUtilities.getWindowAncestor(panel)).dispose();
});

        panel.add(btnCancelar, gbc);
        add(panel);
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(new Color(34, 139, 34)); // Verde forestal
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
    }
    
    private void registerAction(ActionEvent evt) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String ci = txtCI.getText().trim();
        String role = cbRole.getSelectedItem().toString();
        String sexo = cbSexo.getSelectedItem().toString();

        // Validación de campos
        if (username.isEmpty() || password.isEmpty() || ci.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Todos los campos son obligatorios", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, 
                "La contraseña debe tener al menos 6 caracteres", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = authController.register(username, password, ci, role, sexo);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Registro exitoso. Ahora puede iniciar sesión.");
                this.dispose();
                parent.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error en el registro. El usuario ya existe.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error en el registro: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void openLoginForm() {
        LoginForm loginForm = new LoginForm(); // Crea una nueva instancia de LoginForm
    loginForm.setVisible(true); 
    }
}