package views;

import controllers.AuthController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
// Nueva importación 14/05/25
import java.awt.event.KeyEvent;
import java.sql.SQLException;
// Nuevo import 27/05/25
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterForm extends JFrame {
    private JTextField txtNombre;
    private JTextField txtSegundoNombre;
    private JTextField txtPrimerApellido;
    private JTextField txtSegundoApellido;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtCI;
    private JButton btnRegister;
    private final AuthController authController;
    private JFrame parent;
    private JComboBox<String> cbRole;
    private JComboBox<String> cbSexo;
    private JComboBox<String> cbTurno;
    private JButton btnCancelar;
    // Cambio 25/05/25
    private JTextField txtCorreo;
    
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
    setTitle("Registro de Usuario");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
    // Panel principal con fondo degradado
    JPanel panel = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = new Color(249, 45, 168);
            Color color2 = new Color(255, 209, 12);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10); // Espaciado interno
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    // Título e imagen
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);
    
    // Imagen de registro (ajusta la ruta según tu estructura de archivos)
    ImageIcon registroIcon = new ImageIcon("resources/NEW.png"); // Cambia por tu ruta correcta
    Image img = registroIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
    JLabel lblIcon = new JLabel(new ImageIcon(img));
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
    headerPanel.add(lblIcon, BorderLayout.WEST);
    
    JLabel lblTitle = new JLabel("REGISTRO DE USUARIO", SwingConstants.CENTER);
    lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
    lblTitle.setForeground(Color.WHITE);
    headerPanel.add(lblTitle, BorderLayout.CENTER);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    panel.add(headerPanel, gbc);
    
    // Definir color para las etiquetas
    Color labelColor = new Color(70, 70, 70);
    Font labelFont = new Font("Arial", Font.BOLD, 14);
    
    // Primera fila: Nombre y Segundo Nombre
    gbc.gridy++;
    gbc.gridwidth = 1;
    
    JPanel nombrePanel = new JPanel(new GridLayout(1, 2, 10, 5));
    nombrePanel.setOpaque(false);
    
    // Nombre
    JPanel nombreSubPanel = new JPanel(new BorderLayout(5, 5));
    nombreSubPanel.setOpaque(false);
    JLabel lblNombre = new JLabel("Nombre:");
    lblNombre.setFont(labelFont);
    lblNombre.setForeground(labelColor);
    nombreSubPanel.add(lblNombre, BorderLayout.NORTH);
    txtNombre = new JTextField();
    txtNombre.setFont(new Font("Arial", Font.PLAIN, 14));
    nombreSubPanel.add(txtNombre, BorderLayout.CENTER);
    nombrePanel.add(nombreSubPanel);
    // Cambio 14/05/25 
    txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Validar solo letras y espacios
            if (!Character.isLetter(c) && c != ' ' && c != 'á' && c != 'é' && c != 'í' && c != 'ó' && c != 'ú' 
                && c != 'Á' && c != 'É' && c != 'Í' && c != 'Ó' && c != 'Ú' && c != 'ñ' && c != 'Ñ') {
                evt.consume(); // Bloquear la entrada del carácter no permitido
                JOptionPane.showMessageDialog(null, "Solo se permiten letras y espacios.");
                }
            }
    });


    
    // Segundo Nombre
    JPanel segundoNombreSubPanel = new JPanel(new BorderLayout(5, 5));
    segundoNombreSubPanel.setOpaque(false);
    JLabel lblSegundoNombre = new JLabel("Segundo Nombre:");
    lblSegundoNombre.setFont(labelFont);
    lblSegundoNombre.setForeground(labelColor);
    segundoNombreSubPanel.add(lblSegundoNombre, BorderLayout.NORTH);
    txtSegundoNombre = new JTextField();
    txtSegundoNombre.setFont(new Font("Arial", Font.PLAIN, 14));
    segundoNombreSubPanel.add(txtSegundoNombre, BorderLayout.CENTER);
    nombrePanel.add(segundoNombreSubPanel);
    // Cambio 14/05/25
    txtSegundoNombre.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Validar solo letras y espacios
            if (!Character.isLetter(c) && c != ' ' && c != 'á' && c != 'é' && c != 'í' && c != 'ó' && c != 'ú' 
                && c != 'Á' && c != 'É' && c != 'Í' && c != 'Ó' && c != 'Ú' && c != 'ñ' && c != 'Ñ') {
                evt.consume(); // Bloquear la entrada del carácter no permitido
                JOptionPane.showMessageDialog(null, "Solo se permiten letras y espacios.");
                }
            }
    });
    
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    panel.add(nombrePanel, gbc);
    
    // Resto de campos en dos columnas
    gbc.gridwidth = 1;
    gbc.gridy++;
    
    // Primera columna
    gbc.gridx = 0;
    
    // Primer Apellido
    JLabel lblPrimerApellido = new JLabel("Primer Apellido:");
    lblPrimerApellido.setFont(labelFont);
    lblPrimerApellido.setForeground(labelColor);
    panel.add(lblPrimerApellido, gbc);
    
    gbc.gridy++;
    txtPrimerApellido = new JTextField();
    txtPrimerApellido.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(txtPrimerApellido, gbc);
    // Cambio 14/05/25
    txtPrimerApellido.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Validar solo letras y espacios
            if (!Character.isLetter(c) && c != ' ' && c != 'á' && c != 'é' && c != 'í' && c != 'ó' && c != 'ú' 
                && c != 'Á' && c != 'É' && c != 'Í' && c != 'Ó' && c != 'Ú' && c != 'ñ' && c != 'Ñ') {
                evt.consume(); // Bloquear la entrada del carácter no permitido
                JOptionPane.showMessageDialog(null, "Solo se permiten letras y espacios.");
                }
            }
    });
    
    // Usuario
    gbc.gridy++;
    JLabel lblUsuario = new JLabel("Usuario:");
    lblUsuario.setFont(labelFont);
    lblUsuario.setForeground(labelColor);
    panel.add(lblUsuario, gbc);
    
    gbc.gridy++;
    txtUsername = new JTextField();
    txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(txtUsername, gbc);
    // Cambio 14/05/25
    txtUsername.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Validar solo letras y espacios
            if (!Character.isLetter(c) && c != ' ' && c != 'á' && c != 'é' && c != 'í' && c != 'ó' && c != 'ú' 
                && c != 'Á' && c != 'É' && c != 'Í' && c != 'Ó' && c != 'Ú' && c != 'ñ' && c != 'Ñ') {
                evt.consume(); // Bloquear la entrada del carácter no permitido
                JOptionPane.showMessageDialog(null, "Solo se permiten letras y espacios.");
                }
            }
    });
    
    // Rol
    gbc.gridy++;
    JLabel lblRol = new JLabel("Rol:");
    lblRol.setFont(labelFont);
    lblRol.setForeground(labelColor);
    panel.add(lblRol, gbc);
    
    gbc.gridy++;
    cbRole = new JComboBox<>(new String[]{"Administrador(a)", "Docente", "Técnico(a) de Mantenimiento"});
    cbRole.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(cbRole, gbc);
    
    // Segunda columna
    gbc.gridy = 2;
    gbc.gridx = 1;
    
    // Segundo Apellido
    JLabel lblSegundoApellido = new JLabel("Segundo Apellido:");
    lblSegundoApellido.setFont(labelFont);
    lblSegundoApellido.setForeground(labelColor);
    panel.add(lblSegundoApellido, gbc);
    
    gbc.gridy++;
    txtSegundoApellido = new JTextField();
    txtSegundoApellido.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(txtSegundoApellido, gbc);
    // Cambio 14/05/25
    txtSegundoApellido.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Validar solo letras y espacios
            if (!Character.isLetter(c) && c != ' ' && c != 'á' && c != 'é' && c != 'í' && c != 'ó' && c != 'ú' 
                && c != 'Á' && c != 'É' && c != 'Í' && c != 'Ó' && c != 'Ú' && c != 'ñ' && c != 'Ñ') {
                evt.consume(); // Bloquear la entrada del carácter no permitido
                JOptionPane.showMessageDialog(null, "Solo se permiten letras y espacios.");
                }
            }
    });
    
    // Contraseña
    gbc.gridy++;
    JLabel lblContraseña = new JLabel("Contraseña:");
    lblContraseña.setFont(labelFont);
    lblContraseña.setForeground(labelColor);
    panel.add(lblContraseña, gbc);
    
    gbc.gridy++;
    txtPassword = new JPasswordField();
    txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(txtPassword, gbc);
    
    // Cédula
    gbc.gridy++;
    JLabel lblCI = new JLabel("Cédula de Identidad:");
    lblCI.setFont(labelFont);
    lblCI.setForeground(labelColor);
    panel.add(lblCI, gbc);
    
    gbc.gridy++;
    txtCI = new JTextField();
    txtCI.setFont(new Font("Arial", Font.PLAIN, 14));
    panel.add(txtCI, gbc);
    // Cambio 14/05/25
    txtCI.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyTyped(java.awt.event.KeyEvent evt) {
            char c = evt.getKeyChar();

            // Permitir borrar (Backspace y Delete)
            if (c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
                return;
            }

            // Solo permitir números del 0 al 9
            if (!Character.isDigit(c)) {
                evt.consume(); // Bloquea caracteres no numéricos
                JOptionPane.showMessageDialog(null, "Solo se permiten números.");
            }
        }
    });

    
    // Fila para Turno y Sexo
    gbc.gridy++;
    JPanel turnoSexoPanel = new JPanel(new GridLayout(1, 2, 10, 5));
    turnoSexoPanel.setOpaque(false);
    
    // Turno
    JPanel turnoPanel = new JPanel(new BorderLayout(5, 5));
    turnoPanel.setOpaque(false);
    JLabel lblTurno = new JLabel("Turno:");
    lblTurno.setFont(labelFont);
    lblTurno.setForeground(labelColor);
    turnoPanel.add(lblTurno, BorderLayout.NORTH);
    cbTurno = new JComboBox<>(new String[]{"Mañana", "Tarde", "Noche"});
    cbTurno.setFont(new Font("Arial", Font.PLAIN, 14));
    turnoPanel.add(cbTurno, BorderLayout.CENTER);
    turnoSexoPanel.add(turnoPanel);
    
    // Sexo
    JPanel sexoPanel = new JPanel(new BorderLayout(5, 5));
    sexoPanel.setOpaque(false);
    JLabel lblSexo = new JLabel("Sexo:");
    lblSexo.setFont(labelFont);
    lblSexo.setForeground(labelColor);
    sexoPanel.add(lblSexo, BorderLayout.NORTH);
    cbSexo = new JComboBox<>(new String[]{"Hombre", "Mujer"});
    cbSexo.setFont(new Font("Arial", Font.PLAIN, 14));
    sexoPanel.add(cbSexo, BorderLayout.CENTER);
    turnoSexoPanel.add(sexoPanel);
    
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    panel.add(turnoSexoPanel, gbc);
    
    // Cambio 25/05/25
    // Correo
    JPanel correoPanel = new JPanel(new BorderLayout(5, 5));
    correoPanel.setOpaque(false);
    JLabel lblCorreo = new JLabel("Correo:");
    lblCorreo.setFont(labelFont);
    lblCorreo.setForeground(labelColor);
    correoPanel.add(lblCorreo, BorderLayout.NORTH);

    txtCorreo = new JTextField();
    txtCorreo.setFont(new Font("Arial", Font.PLAIN, 14));
    correoPanel.add(txtCorreo, BorderLayout.CENTER);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    panel.add(correoPanel, gbc);

    
    // Panel de botones
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.CENTER;
    
    JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 10));
    buttonPanel.setOpaque(false);
    
    btnRegister = new JButton("REGISTRAR");
    btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
    btnRegister.setBackground(new Color(76, 175, 80)); // Verde
    btnRegister.setForeground(Color.BLACK);
    btnRegister.addActionListener(this::registerAction);
    buttonPanel.add(btnRegister);
    
    btnCancelar = new JButton("CANCELAR");
    btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
    btnCancelar.setBackground(new Color(244, 67, 54)); // Rojo
    btnCancelar.setForeground(Color.BLACK);
    btnCancelar.addActionListener(e -> {
        this.dispose();
        //Cambio 15/05/25
        new LoginForm().setVisible(true);
    });
    buttonPanel.add(btnCancelar);
    
    panel.add(buttonPanel, gbc);
    
    add(panel);
}
    
    /* Cambio 27/05/25 **************************************************
    private String encryptPassword(String password)
    {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }
    */
    
    private void registerAction(ActionEvent evt) {
    // Obtener todos los valores del formulario
    String nombre = txtNombre.getText().trim();
    String segundoNombre = txtSegundoNombre.getText().trim();
    String primerApellido = txtPrimerApellido.getText().trim();
    String segundoApellido = txtSegundoApellido.getText().trim();
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword()).trim();
    // Nuevo cambio 27/05/25
    //String encryptPassword = encryptPassword(password);
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    //***********************
    String ci = txtCI.getText().trim();
    String role = cbRole.getSelectedItem().toString();
    String sexo = cbSexo.getSelectedItem().toString();
    String turno = cbTurno.getSelectedItem().toString();
    // Cambios 25/05/25
    String correo = txtCorreo.getText().trim();
    // ****************************************

    // Validaciones básicas
    if (nombre.isEmpty() || primerApellido.isEmpty() || username.isEmpty() || 
        password.isEmpty() || ci.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Los campos obligatorios deben ser completados", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validar longitud máxima según la base de datos
    if (nombre.length() > 20 || primerApellido.length() > 20 || 
        (segundoNombre.length() > 20) || (segundoApellido.length() > 20)) {
        JOptionPane.showMessageDialog(this, 
            "Los campos de nombre y apellidos no deben exceder 20 caracteres", 
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        // Verificar si el username ya existe
        if (authController.usernameExists(username)) {
            JOptionPane.showMessageDialog(this, 
                "El nombre de usuario ya está en uso", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si la cédula ya existe
        if (authController.ciExists(ci)) {
            JOptionPane.showMessageDialog(this, 
                "La cédula de identidad ya está registrada", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Registrar el usuario
        boolean success = authController.register(
            nombre,
            segundoNombre.isEmpty() ? null : segundoNombre,
            primerApellido,
            segundoApellido.isEmpty() ? null : segundoApellido,
            turno,
            username,
            hashedPassword,  // Cambio 27/05/25
            ci,
            role,
            sexo,
            correo);

        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Registro exitoso. Ahora puede iniciar sesión.");
            this.dispose();
            //Cambio 16/05/25
            new LoginForm().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error al registrar el usuario", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error en el registro: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

}