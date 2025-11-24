package views.panels;

import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import controllers.AuthController;
import javax.swing.border.*;
import views.LoginForm;
import java.text.SimpleDateFormat;
import java.util.Date;
// Cambios 15/05/25
import javax.swing.text.*;

public class UsuarioPanel extends JPanel {
    private User currentUser;
    private JFrame parentFrame;
    private AuthController authController;
    private String sexoUsuario;

    private JLabel lblUsername, lblCI, lblRole, lblNombreCompleto, lblTurno, lblFechaRegistro;
    private JButton btnEditar, btnCerrarSesion;
    private JPanel editPanel;

    public UsuarioPanel(User user, JFrame parent) {
        this.currentUser = user;
        this.parentFrame = parent;
        this.authController = new AuthController();
        this.sexoUsuario = currentUser.getSexo();
        setOpaque(false);
        initComponents();
    }

    public void setUser(User user) {
        this.currentUser = user;
        this.sexoUsuario = currentUser.getSexo();
        loadUserData();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Color colorSuperior = new Color(63, 81, 181);
        Color colorInferior = new Color(171, 71, 188);
        GradientPaint gradient = new GradientPaint(0, 0, colorSuperior, 0, getHeight(), colorInferior);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        JPanel infoPanel = createUserInfoPanel();
        JPanel buttonPanel = createButtonPanel();
        editPanel = createEditPanel();
        editPanel.setVisible(false);

        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(editPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        loadUserData();
    }

    private JPanel createUserInfoPanel() {
    JPanel infoPanel = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g);
        }
    };
    infoPanel.setOpaque(false);
    infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    // 1. Cargar icono según el sexo del usuario
    String imagePath;
    if (currentUser.getSexo() != null && currentUser.getSexo().equalsIgnoreCase("Mujer")) {
        imagePath = "resources/user_female.png"; // Imagen para mujer
    } else {
        imagePath = "resources/user_male.png";   // Imagen para hombre (por defecto)
    }

    // 2. Cargar la imagen con manejo de errores robusto
    ImageIcon icon;
    try {
        // Verificar si el archivo existe
        java.io.File file = new java.io.File(imagePath);
        if (!file.exists()) {
            throw new Exception("Archivo de imagen no encontrado: " + imagePath);
        }
        
        icon = new ImageIcon(imagePath);
        
        // Verificar si la imagen se cargó correctamente
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            throw new Exception("Error al cargar la imagen: " + imagePath);
        }
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        // Usar imagen por defecto si hay algún problema
        imagePath = "resources/default_user.png";
        icon = new ImageIcon(imagePath);
        
        // Si la imagen por defecto tampoco existe, usar un ícono vacío
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            icon = new ImageIcon(); // Ícono vacío
            System.err.println("No se pudo cargar la imagen por defecto");
        }
    }

    // 3. Escalar la imagen manteniendo la relación de aspecto
    int width = 150;
    int height = 150;
    Image img = icon.getImage();
    if (img != null) {
        // Calcular dimensiones manteniendo aspect ratio
        double aspectRatio = (double) icon.getIconWidth() / icon.getIconHeight();
        if (icon.getIconWidth() > icon.getIconHeight()) {
            height = (int) (width / aspectRatio);
        } else {
            width = (int) (height * aspectRatio);
        }
        img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    JLabel lblIcon = new JLabel(new ImageIcon(img));
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
    
    // 4. Posicionar la imagen en el panel
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 6;
    gbc.fill = GridBagConstraints.VERTICAL;
    infoPanel.add(lblIcon, gbc);

    // 5. Configurar título
    JLabel lblTitulo = new JLabel("INFORMACIÓN DEL PERFIL", SwingConstants.LEFT);
    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
    lblTitulo.setForeground(new Color(44, 62, 80));
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.gridheight = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    infoPanel.add(lblTitulo, gbc);

    // 6. Configurar estilos para las etiquetas
    Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
    Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);
    Color labelColor = new Color(44, 62, 80);
    Color valueColor = new Color(52, 73, 94);

    // 7. Campos de información del usuario
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    JLabel lblNombreLabel = new JLabel("Nombre completo:");
    styleLabel(lblNombreLabel, labelFont, labelColor);
    infoPanel.add(lblNombreLabel, gbc);

    lblNombreCompleto = new JLabel();
    styleLabel(lblNombreCompleto, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblNombreCompleto, gbc);

    gbc.gridx = 1;
    gbc.gridy = 2;
    JLabel lblUserLabel = new JLabel("Usuario:");
    styleLabel(lblUserLabel, labelFont, labelColor);
    infoPanel.add(lblUserLabel, gbc);

    lblUsername = new JLabel();
    styleLabel(lblUsername, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblUsername, gbc);

    gbc.gridx = 1;
    gbc.gridy = 3;
    JLabel lblCiLabel = new JLabel("Cédula:");
    styleLabel(lblCiLabel, labelFont, labelColor);
    infoPanel.add(lblCiLabel, gbc);

    lblCI = new JLabel();
    styleLabel(lblCI, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblCI, gbc);

    gbc.gridx = 1;
    gbc.gridy = 4;
    JLabel lblRoleLabel = new JLabel("Rol:");
    styleLabel(lblRoleLabel, labelFont, labelColor);
    infoPanel.add(lblRoleLabel, gbc);

    lblRole = new JLabel();
    styleLabel(lblRole, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblRole, gbc);

    gbc.gridx = 1;
    gbc.gridy = 5;
    JLabel lblTurnoLabel = new JLabel("Turno:");
    styleLabel(lblTurnoLabel, labelFont, labelColor);
    infoPanel.add(lblTurnoLabel, gbc);

    lblTurno = new JLabel(currentUser.getTurno() != null ? currentUser.getTurno() : "No asignado");
    styleLabel(lblTurno, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblTurno, gbc);

    gbc.gridx = 1;
    gbc.gridy = 6;
    JLabel lblFechaLabel = new JLabel("Registrado desde:");
    styleLabel(lblFechaLabel, labelFont, labelColor);
    infoPanel.add(lblFechaLabel, gbc);

    String fechaRegistro = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
    lblFechaRegistro = new JLabel(fechaRegistro);
    styleLabel(lblFechaRegistro, valueFont, valueColor);
    gbc.gridx = 2;
    infoPanel.add(lblFechaRegistro, gbc);

    return infoPanel;
}

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        btnEditar = new JButton("Editar Perfil");
        styleButton(btnEditar, new Color(52, 152, 219));
        btnEditar.addActionListener(this::mostrarPanelEdicion);

        btnCerrarSesion = new JButton("Cerrar Sesión");
        styleButton(btnCerrarSesion, new Color(231, 76, 60));
        btnCerrarSesion.addActionListener(this::cerrarSesion);

        buttonPanel.add(btnEditar);
        buttonPanel.add(btnCerrarSesion);

        return buttonPanel;
    }
    
    private JPanel createEditPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                "Editar Información del Perfil",
                TitledBorder.CENTER,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(41, 128, 185))
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtNombre = new JTextField(25);
        // Cambios 15/05/25
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
        JTextField txtSegundoNombre = new JTextField(25);
        // Cambios 15/05/25
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
        
        JTextField txtPrimerApellido = new JTextField(25);
        // Cambios 15/05/25
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
        
        JTextField txtSegundoApellido = new JTextField(25);
        //Cambios 15/05/25
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
        
        JTextField txtUsername = new JTextField(25);
        // Cambios 15/05/25
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
        
        // ************** Cambios 15/05/25
        JTextField txtCI = new JTextField(25);
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
        
        JPasswordField txtPassword = new JPasswordField(25);
        
        // Cambio: JComboBox para el turno en lugar de JTextField
        JComboBox<String> cmbTurno = new JComboBox<>();
        cmbTurno.addItem(""); // Opción vacía
        cmbTurno.addItem("Mañana");
        cmbTurno.addItem("Tarde");
        cmbTurno.addItem("Noche");
        if (currentUser.getTurno() != null) {
            cmbTurno.setSelectedItem(currentUser.getTurno());
        }
            
        txtNombre.setText(currentUser.getNombre());
        txtSegundoNombre.setText(currentUser.getSegundoNombre() != null ? currentUser.getSegundoNombre() : "");
        txtPrimerApellido.setText(currentUser.getPrimerApellido() != null ? currentUser.getPrimerApellido() : "");
        txtSegundoApellido.setText(currentUser.getSegundoApellido() != null ? currentUser.getSegundoApellido() : "");
        txtCI.setText(currentUser.getCi() != null ? currentUser.getCi() : "");
        txtUsername.setText(currentUser.getUsername());

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Color labelColor = new Color(44, 62, 80);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblNombre = new JLabel("Nombre:");
        styleLabel(lblNombre, labelFont, labelColor);
        panel.add(lblNombre, gbc);

        gbc.gridx = 1;
        panel.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblSegundoNombre = new JLabel("Segundo nombre:");
        styleLabel(lblSegundoNombre, labelFont, labelColor);
        panel.add(lblSegundoNombre, gbc);

        gbc.gridx = 1;
        panel.add(txtSegundoNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblPrimerApellido = new JLabel("Primer apellido:");
        styleLabel(lblPrimerApellido, labelFont, labelColor);
        panel.add(lblPrimerApellido, gbc);

        gbc.gridx = 1;
        panel.add(txtPrimerApellido, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel lblSegundoApellido = new JLabel("Segundo apellido:");
        styleLabel(lblSegundoApellido, labelFont, labelColor);
        panel.add(lblSegundoApellido, gbc);

        gbc.gridx = 1;
        panel.add(txtSegundoApellido, gbc);

        // Cambios 15/05/25
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblUsername = new JLabel("Usuario:");
        styleLabel(lblUsername, labelFont, labelColor);
        panel.add(lblUsername, gbc);

        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        // **************************
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel lblCI = new JLabel("CI:");
        styleLabel(lblCI, labelFont, labelColor);
        panel.add(lblCI, gbc);

        gbc.gridx = 1;
        panel.add(txtCI, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel lblPassword = new JLabel("Contraseña (dejar en blanco para no cambiar):");
        styleLabel(lblPassword, labelFont, labelColor);
        panel.add(lblPassword, gbc);

        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        JLabel lblTurno = new JLabel("Turno:");
        styleLabel(lblTurno, labelFont, labelColor);
        panel.add(lblTurno, gbc);

        gbc.gridx = 1;
        panel.add(cmbTurno, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnPanel.setOpaque(false);

        JButton btnGuardar = new JButton("Guardar Cambios");
        styleButton(btnGuardar, new Color(46, 204, 113));
        btnGuardar.addActionListener(e -> {
            actualizarDatosUsuario(
                    txtNombre.getText(),
                    txtSegundoNombre.getText(),
                    txtPrimerApellido.getText(),
                    txtSegundoApellido.getText(),
                    txtUsername.getText(),
                    new String(txtPassword.getPassword()),
                    cmbTurno.getSelectedItem().toString(), // Usamos el JComboBox
                    txtCI.getText()
            );
            editPanel.setVisible(false);
        });

        JButton btnCancelar = new JButton("Cancelar");
        styleButton(btnCancelar, new Color(231, 76, 60));
        btnCancelar.addActionListener(e -> editPanel.setVisible(false));

        btnPanel.add(btnGuardar);
        btnPanel.add(btnCancelar);
        panel.add(btnPanel, gbc);

        return panel;
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            icon = new ImageIcon("resources/default_user.png");
        }
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void styleLabel(JLabel label, Font font, Color color) {
        label.setFont(font);
        label.setForeground(color);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK); // Texto en negro
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue())),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(
                        Math.max(bgColor.getRed() - 20, 0),
                        Math.max(bgColor.getGreen() - 20, 0),
                        Math.max(bgColor.getBlue() - 20, 0)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private void mostrarPanelEdicion(ActionEvent e) {
        editPanel.setVisible(true);
    }

    private void loadUserData() {
        lblUsername.setText(currentUser.getUsername());
        lblCI.setText(currentUser.getCi());
        lblRole.setText(currentUser.getRole());
        lblNombreCompleto.setText(currentUser.getNombreCompleto());
        sexoUsuario = currentUser.getSexo();
        
        if (currentUser.getTurno() != null) {
            lblTurno.setText(currentUser.getTurno());
        }
    }

    private void actualizarDatosUsuario(String nombre, String segundoNombre, String primerApellido, 
                                      String segundoApellido, String username, String password, 
                                      String turno, String ci) {
        if (nombre.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y usuario son campos obligatorios",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar turno
        if (!turno.isEmpty() && !turno.equals("Mañana") && !turno.equals("Tarde") && !turno.equals("Noche")) {
            JOptionPane.showMessageDialog(this, "Turno no válido. Las opciones son: Mañana, Tarde o Noche",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success;
        if (!password.isEmpty()) {
            success = authController.updateUserFull(
                    currentUser.getId(), 
                    nombre,
                    segundoNombre.isEmpty() ? null : segundoNombre,
                    primerApellido.isEmpty() ? null : primerApellido,
                    segundoApellido.isEmpty() ? null : segundoApellido,
                    turno.isEmpty() ? null : turno,
                    username, 
                    password,
                    //Cambio 16/05/25
                    ci //currentUser.getCi()
            );
        } else {
            success = authController.updateUserFullWithoutPassword(
                    currentUser.getId(), 
                    nombre,
                    segundoNombre.isEmpty() ? null : segundoNombre,
                    primerApellido.isEmpty() ? null : primerApellido,
                    segundoApellido.isEmpty() ? null : segundoApellido,
                    turno.isEmpty() ? null : turno,
                    username,
                    //Cambio 160/5/25
                    ci //currentUser.getCi()
            );
        }

        if (success) {
            currentUser = new User(
                currentUser.getId(),
                nombre,
                username,
                currentUser.getCi(),
                currentUser.getRole(),
                currentUser.getSexo(),
                // Cambio 25/05/25
                currentUser.getCorreo(),
                // ********************
                // Cambio 27/05/25
                currentUser.getPassword()
                // ********************
            );
            
            currentUser.setSegundoNombre(segundoNombre.isEmpty() ? null : segundoNombre);
            currentUser.setPrimerApellido(primerApellido.isEmpty() ? null : primerApellido);
            currentUser.setSegundoApellido(segundoApellido.isEmpty() ? null : segundoApellido);
            currentUser.setTurno(turno.isEmpty() ? null : turno);
            
            loadUserData();
            JOptionPane.showMessageDialog(this, "Datos actualizados exitosamente", 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Error al actualizar los datos",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Cierre de Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            parentFrame.dispose();
            new LoginForm().setVisible(true);
        }
    }
}