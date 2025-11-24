package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.geom.Rectangle2D;
import models.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import controllers.MaterialController;
//****************************************
import java.time.LocalDateTime;
import java.time.LocalTime;
//****************************************
import models.MaterialAdicional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReservacionPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable reservasTable;
    private JButton btnNuevaReserva, btnEditar, btnCancelar, btnCompletar, btnEliminar, btnRefresh;
    private User currentUser;
    
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    
    // Cambio 16/05/25 Notificaciones para las reservaciones
    private JPanel notificationPanel;
    private JLabel lblNotification;
    // ******************************************************
    
    public ReservacionPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadReservasData();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    // Cambio 16/05/25 ***********************************************************************
    private void initNotificationPanel() {
        notificationPanel = new JPanel();
        notificationPanel.setBackground(new Color(255, 223, 186)); // Color suave
        notificationPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    
        lblNotification = new JLabel("Sin notificaciones pendientes");
        lblNotification.setFont(new Font("Arial", Font.BOLD, 14));
    
        notificationPanel.add(lblNotification);
    
        add(notificationPanel, BorderLayout.NORTH); // Agregarlo en la parte superior
    }
    
    private void mostrarNotificacion(String laboratorio, Time horaInicio) {
       /*SwingUtilities.invokeLater(() -> {
            lblNotification.setText("⚠️ Reserva próxima en " + laboratorio + " a las " + horaInicio);
            notificationPanel.setBackground(new Color(255, 99, 71)); // Color rojo suave para destacar
        });*/
        SwingUtilities.invokeLater(() -> {
        lblNotification.setText("⚠️ Reserva próxima en " + laboratorio + " a las " + horaInicio);
        notificationPanel.setBackground(new Color(255, 99, 71)); // Color rojo suave para destacar

        // Enviar correo de notificación
        String destinatario = "newotherman3030@gmail.com"; // Dirección de quien recibirá el correo
        String asunto = "🔔 Reserva próxima en " + laboratorio;
        String mensaje = "¡Atención! Tienes una reserva en " + laboratorio + " a las " + horaInicio + ".";
        EmailSender.enviarCorreo(destinatario, asunto, mensaje);
    });
    }
    
    private void verificarReservasProximas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime enDiezMinutos = ahora.plusMinutes(10);

        String query = "SELECT l.nombre AS laboratorio, r.hora_inicio " +
                    "FROM Reservas r " +
                    "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                    "WHERE r.fecha_reserva = CURDATE() " +
                    "AND r.hora_inicio BETWEEN ? AND ? " +
                    "AND r.estado = 'pendiente'";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
        
            stmt.setTime(1, Time.valueOf(LocalTime.from(ahora)));
            stmt.setTime(2, Time.valueOf(LocalTime.from(enDiezMinutos)));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String laboratorio = rs.getString("laboratorio");
                Time horaInicio = rs.getTime("hora_inicio");

                mostrarNotificacion(laboratorio, horaInicio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //***************************************************************************************
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Cambio 16/05/25*******
        initNotificationPanel();
        verificarReservasProximas();
        //***********************
        
        reservasTable = new JTable();
        reservasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservasTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reservasTable.setRowHeight(25);
        
        JTableHeader header = reservasTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK); 

        JScrollPane scrollPane = new JScrollPane(reservasTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        btnNuevaReserva = createStyledButton("Nueva Reserva", new Color(46, 125, 50));
        btnNuevaReserva.addActionListener(e -> abrirCalendarioParaReserva());
        buttonPanel.add(btnNuevaReserva);
        
        btnEditar = createStyledButton("Editar", new Color(2, 119, 189));
        btnEditar.addActionListener(this::editarReserva);
        buttonPanel.add(btnEditar);
        
        btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40));
        btnCancelar.addActionListener(this::cancelarReserva);
        buttonPanel.add(btnCancelar);
        
        btnCompletar = createStyledButton("Completar", new Color(142, 36, 170));
        btnCompletar.addActionListener(this::completarReserva);
        buttonPanel.add(btnCompletar);
        
        btnEliminar = createStyledButton("Eliminar", new Color(109, 76, 65));
        btnEliminar.addActionListener(this::eliminarReserva);
        buttonPanel.add(btnEliminar);
        
        btnRefresh = createStyledButton("Actualizar", new Color(30, 30, 30));
        btnRefresh.addActionListener(e -> loadReservasData());
        buttonPanel.add(btnRefresh);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(120, 40));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
    
    private void abrirCalendarioParaReserva() {
        JFrame calendarFrame = new JFrame("Calendario de Reservas");
        calendarFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        CalendarPanel calendarPanel = new CalendarPanel(currentUser);
        calendarPanel.setReservationListener((labName, fecha, horaInicio, horaFin) -> {
            calendarFrame.dispose();
            SwingUtilities.invokeLater(() -> {
                crearNuevaReserva(labName, fecha, horaInicio, horaFin);
            });
        });
        
        calendarFrame.add(calendarPanel);
        calendarFrame.pack();
        calendarFrame.setSize(1200, 800);
        calendarFrame.setLocationRelativeTo(null);
        
        calendarFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                calendarFrame.dispose();
            }
        });
        
        calendarFrame.setVisible(true);
    }
    
    private void crearNuevaReserva(String labName, LocalDate fecha, Time horaInicio, Time horaFin) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String fechaFormateada = fecha.format(formatter);
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "<html><div style='text-align: center;'><b>¿Confirmar fecha de reserva?</b><br><br>" +
            "Laboratorio: " + labName + "<br>" +
            "Fecha: " + fechaFormateada + "<br>" +
            "Hora: " + horaInicio + " - " + horaFin + "</div></html>",
            "Confirmar Fecha",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion != JOptionPane.YES_OPTION) {
            abrirCalendarioParaReserva();
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel basicDataPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        basicDataPanel.setOpaque(false);
        basicDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JComboBox<String> cbMateria = new JComboBox<>(new String[]{
            "Electrónica", "Hardware", "Redes y Telecomunicaciones"
        });
        cbMateria.setFont(new Font("Arial", Font.PLAIN, 14));
        basicDataPanel.add(new JLabel("Materia:"));
        basicDataPanel.add(cbMateria);
        
        JPanel materialesPanel = new JPanel(new BorderLayout());
        materialesPanel.setBorder(BorderFactory.createTitledBorder("Materiales Adicionales"));
        
        DefaultTableModel materialesModel = new DefaultTableModel(
            new Object[]{"Material", "Disponible", "Usar", "Cantidad"}, 0) {
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : super.getColumnClass(columnIndex);
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 1;
            }
            
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 3) {
                    int disponible = (Integer) getValueAt(row, 1);
                    int nuevaCantidad = (Integer) aValue;
                    
                    if (nuevaCantidad > disponible) {
                        JOptionPane.showMessageDialog(null, 
                            "No puede seleccionar más de " + disponible + " unidades",
                            "Error en cantidad", JOptionPane.ERROR_MESSAGE);
                        super.setValueAt(disponible, row, column);
                        return;
                    } else if (nuevaCantidad < 1) {
                        super.setValueAt(1, row, column);
                        return;
                    }
                }
                super.setValueAt(aValue, row, column);
            }
        };

        int labId = getLabIdByName(labName);
        MaterialController materialController = new MaterialController();
        List<MaterialAdicional> materiales = materialController.obtenerMaterialesDisponibles(labId);
        
        for (MaterialAdicional material : materiales) {
            materialesModel.addRow(new Object[]{
                material.getNombreObjeto(),
                material.getCantidad(),
                false,
                1
            });
        }
        
        JTable materialesTable = new JTable(materialesModel);
        materialesTable.getColumn("Cantidad").setCellEditor(new SpinnerCellEditor());
        
        materialesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        materialesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        materialesTable.setRowHeight(25);
        materialesTable.setGridColor(new Color(220, 220, 220));
        
        JScrollPane materialesScroll = new JScrollPane(materialesTable);
        materialesPanel.add(materialesScroll, BorderLayout.CENTER);
        
        panel.add(basicDataPanel, BorderLayout.NORTH);
        panel.add(materialesPanel, BorderLayout.CENTER);
        
        Object[] options = {"Confirmar Reserva", "Cancelar"};
        int result = JOptionPane.showOptionDialog(
            this, 
            panel, 
            "Detalles de la Reserva", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.PLAIN_MESSAGE, 
            null, 
            options, 
            options[0]
        );
        
        if (result == 0) {
            String materia = (String) cbMateria.getSelectedItem();
            
            List<MaterialReserva> materialesSeleccionados = new ArrayList<>();
            for (int i = 0; i < materialesModel.getRowCount(); i++) {
                if ((Boolean) materialesModel.getValueAt(i, 2)) {
                    int cantidad = (Integer) materialesModel.getValueAt(i, 3);
                    materialesSeleccionados.add(new MaterialReserva(
                        materiales.get(i).getNObjeto(),
                        cantidad
                    ));
                }
            }
            
            insertarReservaEnDB(labName, fecha, horaInicio, horaFin, materia, materialesSeleccionados);
        }
    }

    private int getLabIdByName(String labName) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Id_Laboratorio FROM Laboratorios WHERE nombre = ?")) {
            stmt.setString(1, labName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt("Id_Laboratorio") : -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    private void insertarReservaEnDB(String labName, LocalDate fecha, Time horaInicio, Time horaFin, 
                                   String materia, List<MaterialReserva> materiales) {
        String query = "INSERT INTO Reservas (Nro_Laboratorio, tipo_de_prestamo, materia, " +
                     "fecha_reserva, hora_inicio, hora_fin, estado, id_usuario, usa_material_adicional) " +
                     "SELECT l.Id_Laboratorio, 'clase', ?, ?, ?, ?, 'pendiente', ?, ? " +
                     "FROM Laboratorios l WHERE l.nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, materia);
                stmt.setDate(2, Date.valueOf(fecha));
                stmt.setTime(3, horaInicio);
                stmt.setTime(4, horaFin);
                stmt.setInt(5, currentUser.getId());
                stmt.setBoolean(6, !materiales.isEmpty());
                stmt.setString(7, labName);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int idReserva = rs.getInt(1);
                        
                        if (!materiales.isEmpty()) {
                            String insertMaterial = "INSERT INTO Reserva_Material_Adicional (Id_Reserva, N_Objeto, cantidad) VALUES (?, ?, ?)";
                            try (PreparedStatement stmtMaterial = conn.prepareStatement(insertMaterial)) {
                                for (MaterialReserva material : materiales) {
                                    stmtMaterial.setInt(1, idReserva);
                                    stmtMaterial.setInt(2, material.getNObjeto());
                                    stmtMaterial.setInt(3, material.getCantidad());
                                    stmtMaterial.addBatch();
                                }
                                stmtMaterial.executeBatch();
                            }
                        }
                        
                        conn.commit();
                        JOptionPane.showMessageDialog(this, 
                            "<html><b>Reserva creada exitosamente</b><br><br>" +
                            "<b>Laboratorio:</b> " + labName + "<br>" +
                            "<b>Fecha:</b> " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
                            "<b>Hora:</b> " + horaInicio + " - " + horaFin + "</html>",
                            "Reserva Exitosa", JOptionPane.INFORMATION_MESSAGE);
                        
                        DatabaseConnection.notifyDatabaseChanged("Reservas");
                        DatabaseConnection.notifyDatabaseChanged("Material_Adicional");
                        loadReservasData();
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al crear reserva: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error de conexión a la base de datos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadReservasData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT r.Id_Reserva AS id, l.nombre AS laboratorio, " +
                             "r.tipo_de_prestamo, r.fecha_reserva, " +
                             "r.hora_inicio, r.hora_fin, r.estado, " +
                             "u.username AS usuario, r.usa_material_adicional " +
                             "FROM Reservas r " +
                             "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                             "JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                             "WHERE r.id_usuario = ? " + // Filtra por el usuario actual Cambio 25/05/25
                             "ORDER BY r.fecha_reserva DESC, r.hora_inicio DESC";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) { // Cambio 25/05/25

                    // Cambio 25/05/25 **************************************************************
                    stmt.setInt(1, currentUser.getId()); // Filtra por el usuario que inició sesión
                    ResultSet rs = stmt.executeQuery();
                    //*******************************************************************************
                    
                    DefaultTableModel model = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

                    model.addColumn("ID");
                    model.addColumn("Laboratorio");
                    model.addColumn("Tipo");
                    model.addColumn("Fecha");
                    model.addColumn("Hora Inicio");
                    model.addColumn("Hora Fin");
                    model.addColumn("Estado");
                    model.addColumn("Usuario");
                    model.addColumn("Materiales");

                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("laboratorio"),
                            formatTipoReserva(rs.getString("tipo_de_prestamo")),
                            rs.getDate("fecha_reserva"),
                            rs.getTime("hora_inicio"),
                            rs.getTime("hora_fin"),
                            formatEstado(rs.getString("estado")),
                            rs.getString("usuario"),
                            rs.getBoolean("usa_material_adicional") ? "Sí" : "No"
                        });
                    }

                    SwingUtilities.invokeLater(() -> {
                        reservasTable.setModel(model);

                        // Esta parte también debe ser modificada
                        JTableHeader header = reservasTable.getTableHeader();
                        header.setFont(new Font("Arial", Font.BOLD, 14));
                        header.setBackground(new Color(70, 130, 180));
                        header.setForeground(Color.BLACK); 
                        
                        reservasTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                                c.setForeground(Color.BLACK);
                                if (isSelected) {
                                    c.setBackground(new Color(200, 200, 255));
                                } else {
                                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                                }
                                return c;
                            }
                        });
                        
                        reservasTable.setGridColor(new Color(220, 220, 220));
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, 
                            "Error al cargar reservas: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        }.execute();
    }
    
    private String formatTipoReserva(String tipo) {
        return tipo.substring(0, 1).toUpperCase() + tipo.substring(1).toLowerCase();
    }
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void editarReserva(ActionEvent evt) {
        int selectedRow = reservasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una reserva para editar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String estado = reservasTable.getModel().getValueAt(selectedRow, 6).toString().toLowerCase();
        
        if (!"pendiente".equals(estado)) {
            JOptionPane.showMessageDialog(this, "Solo se pueden editar reservas pendientes. Estado actual: " + estado,
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) reservasTable.getValueAt(selectedRow, 0);
        String laboratorio = (String) reservasTable.getValueAt(selectedRow, 1);
        Date fecha = (Date) reservasTable.getValueAt(selectedRow, 3);
        Time horaInicio = (Time) reservasTable.getValueAt(selectedRow, 4);
        Time horaFin = (Time) reservasTable.getValueAt(selectedRow, 5);
        
        JDialog editDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Editar Reserva", true);
        editDialog.setLayout(new BorderLayout());
        editDialog.setSize(600, 500);
        editDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel basicDataPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        JLabel lblLaboratorio = new JLabel("Laboratorio:");
        JComboBox<String> cbLaboratorio = new JComboBox<>(new String[]{
            "Laboratorio 1", "Laboratorio 2", "Laboratorio 3", 
            "Laboratorio 4", "Laboratorio 5", "Laboratorio 6", 
            "Laboratorio 7"
        });
        cbLaboratorio.setSelectedItem(laboratorio);
        
        JLabel lblFecha = new JLabel("Fecha (YYYY-MM-DD):");
        JTextField txtFecha = new JTextField(fecha.toString());
        
        JLabel lblHoraInicio = new JLabel("Hora Inicio (HH:MM:SS):");
        JTextField txtHoraInicio = new JTextField(horaInicio.toString());
        
        JLabel lblHoraFin = new JLabel("Hora Fin (HH:MM:SS):");
        JTextField txtHoraFin = new JTextField(horaFin.toString());
        
        JLabel lblMateria = new JLabel("Materia:");
        String[] materias = {"Electrónica", "Hardware", "Redes y Telecomunicaciones"};
        JComboBox<String> cbMateria = new JComboBox<>(materias);
        
        String materiaActual = getMateriaReserva(id);
        if(materiaActual != null) {
            cbMateria.setSelectedItem(materiaActual);
        }

        basicDataPanel.add(lblLaboratorio);
        basicDataPanel.add(cbLaboratorio);
        basicDataPanel.add(lblFecha);
        basicDataPanel.add(txtFecha);
        basicDataPanel.add(lblHoraInicio);
        basicDataPanel.add(txtHoraInicio);
        basicDataPanel.add(lblHoraFin);
        basicDataPanel.add(txtHoraFin);
        basicDataPanel.add(lblMateria);
        basicDataPanel.add(cbMateria);
        
        panel.add(basicDataPanel, BorderLayout.NORTH);
        
        JPanel materialesPanel = new JPanel(new BorderLayout());
        materialesPanel.setBorder(BorderFactory.createTitledBorder("Materiales Adicionales"));
        
        DefaultTableModel materialesModel = new DefaultTableModel(
            new Object[]{"Material", "Disponible", "Usar", "Cantidad"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? Boolean.class : super.getColumnClass(columnIndex);
            }
        };
        
        JTable materialesTable = new JTable(materialesModel);
        materialesTable.getColumn("Cantidad").setCellEditor(new SpinnerCellEditor());
        
        JScrollPane materialesScroll = new JScrollPane(materialesTable);
        materialesPanel.add(materialesScroll, BorderLayout.CENTER);
        
        panel.add(materialesPanel, BorderLayout.CENTER);
        
        Runnable cargarMateriales = () -> {
            String labSeleccionado = (String) cbLaboratorio.getSelectedItem();
            MaterialController materialController = new MaterialController();
            List<MaterialAdicional> materiales = materialController.obtenerMaterialesDisponibles(getLabIdByName(labSeleccionado));
            
            List<MaterialReserva> materialesActuales = getMaterialesReserva(id);
            
            materialesModel.setRowCount(0);
            
            for (MaterialAdicional material : materiales) {
                boolean enUso = materialesActuales.stream()
                    .anyMatch(m -> m.getNObjeto() == material.getNObjeto());
                int cantidad = enUso ? materialesActuales.stream()
                    .filter(m -> m.getNObjeto() == material.getNObjeto())
                    .findFirst().get().getCantidad() : 1;
                    
                materialesModel.addRow(new Object[]{
                    material.getNombreObjeto(),
                    material.getCantidad(),
                    enUso,
                    cantidad
                });
            }
        };
        
        cargarMateriales.run();
        
        cbLaboratorio.addActionListener(e -> {
            cargarMateriales.run();
        });
        
        JPanel buttonPanel = new JPanel();
        JButton btnGuardar = createStyledButton("Guardar", new Color(46, 125, 50));
        JButton btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40));
        
        btnGuardar.addActionListener(e -> {
            try {
                LocalDate nuevaFecha = LocalDate.parse(txtFecha.getText());
                Time nuevaHoraInicio = Time.valueOf(txtHoraInicio.getText());
                Time nuevaHoraFin = Time.valueOf(txtHoraFin.getText());
                
                if (nuevaHoraInicio.after(nuevaHoraFin)) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "La hora de inicio debe ser anterior a la hora de fin", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String nuevoLab = (String) cbLaboratorio.getSelectedItem();
                if(!verificarDisponibilidad(id, nuevoLab, nuevaFecha, nuevaHoraInicio, nuevaHoraFin)) {
                    JOptionPane.showMessageDialog(editDialog, 
                        "El laboratorio no está disponible en ese horario", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String materia = (String) cbMateria.getSelectedItem();
                
                MaterialController materialController = new MaterialController();
                List<MaterialAdicional> materiales = materialController.obtenerMaterialesDisponibles(getLabIdByName(nuevoLab));
                
                List<MaterialReserva> materialesSeleccionados = new ArrayList<>();
                for (int i = 0; i < materialesModel.getRowCount(); i++) {
                    if ((Boolean) materialesModel.getValueAt(i, 2)) {
                        int cantidad = (Integer) materialesModel.getValueAt(i, 3);
                        if (cantidad > (Integer) materialesModel.getValueAt(i, 1)) {
                            JOptionPane.showMessageDialog(editDialog, 
                                "No hay suficiente stock de " + materialesModel.getValueAt(i, 0),
                                "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        materialesSeleccionados.add(new MaterialReserva(
                            materiales.get(i).getNObjeto(),
                            cantidad
                        ));
                    }
                }
                
                actualizarReservaYMateriales(id, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, materia, materialesSeleccionados);
                editDialog.dispose();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editDialog, 
                    "Formato de fecha u hora inválido", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> editDialog.dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        
        editDialog.add(panel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        editDialog.setVisible(true);
    }
    
    private List<MaterialReserva> getMaterialesReserva(int idReserva) {
        List<MaterialReserva> materiales = new ArrayList<>();
        String query = "SELECT N_Objeto, cantidad FROM Reserva_Material_Adicional WHERE Id_Reserva = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idReserva);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                materiales.add(new MaterialReserva(
                    rs.getInt("N_Objeto"),
                    rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materiales;
    }
    
    private void actualizarReservaYMateriales(int id, LocalDate fecha, Time horaInicio, Time horaFin, 
                                           String materia, List<MaterialReserva> materiales) {
        String queryReserva = "UPDATE Reservas SET fecha_reserva = ?, hora_inicio = ?, hora_fin = ?, materia = ?, " +
                             "usa_material_adicional = ? WHERE Id_Reserva = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                try (PreparedStatement stmt = conn.prepareStatement(queryReserva)) {
                    stmt.setDate(1, Date.valueOf(fecha));
                    stmt.setTime(2, horaInicio);
                    stmt.setTime(3, horaFin);
                    stmt.setString(4, materia);
                    stmt.setBoolean(5, !materiales.isEmpty());
                    stmt.setInt(6, id);
                    stmt.executeUpdate();
                }
                
                String deleteMateriales = "DELETE FROM Reserva_Material_Adicional WHERE Id_Reserva = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteMateriales)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                
                if (!materiales.isEmpty()) {
                    String insertMaterial = "INSERT INTO Reserva_Material_Adicional (Id_Reserva, N_Objeto, cantidad) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(insertMaterial)) {
                        for (MaterialReserva material : materiales) {
                            stmt.setInt(1, id);
                            stmt.setInt(2, material.getNObjeto());
                            stmt.setInt(3, material.getCantidad());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
                
                conn.commit();
                JOptionPane.showMessageDialog(this, "Reserva y materiales actualizados exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Reservas");
                DatabaseConnection.notifyDatabaseChanged("Reserva_Material_Adicional");
                loadReservasData();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al actualizar reserva: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private String getMateriaReserva(int idReserva) {
        String query = "SELECT materia FROM Reservas WHERE Id_Reserva = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, idReserva);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("materia");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean verificarDisponibilidad(int idReserva, String labName, LocalDate fecha, Time horaInicio, Time horaFin) {
        String query = "SELECT COUNT(*) FROM Reservas r " +
              "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
              "WHERE l.nombre = ? AND r.fecha_reserva = ? " +
              "AND r.Id_Reserva != ? " +
              "AND r.estado != 'cancelado' " +
              "AND ((r.hora_inicio < ? AND r.hora_fin > ?) OR " +
              "(r.hora_inicio BETWEEN ? AND ?) OR " +
              "(r.hora_fin BETWEEN ? AND ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, labName);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setInt(3, idReserva);
            stmt.setTime(4, horaInicio);
            stmt.setTime(5, horaFin);
            stmt.setTime(6, horaInicio);
            stmt.setTime(7, horaFin);
            stmt.setTime(8, horaInicio);
            stmt.setTime(9, horaFin);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void cancelarReserva(ActionEvent evt) {
        cambiarEstadoReserva("cancelado");
    }
    
    private void completarReserva(ActionEvent evt) {
        cambiarEstadoReserva("completado");
    }
    
    private void cambiarEstadoReserva(String nuevoEstado) {
        int selectedRow = reservasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una reserva",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) reservasTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) reservasTable.getValueAt(selectedRow, 6);
        
        if (estadoActual.equalsIgnoreCase(nuevoEstado)) {
            JOptionPane.showMessageDialog(this, "La reserva ya está en estado " + nuevoEstado,
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String mensaje = String.format("¿Está seguro que desea cambiar el estado de la reserva #%d a %s?", 
                                      id, nuevoEstado);
        
        int confirm = JOptionPane.showConfirmDialog(this, mensaje,
            "Confirmar Cambio de Estado", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            actualizarEstadoReserva(id, nuevoEstado);
        }
    }
    
    private void actualizarEstadoReserva(int id, String nuevoEstado) {
        String query = "UPDATE Reservas SET estado = ? WHERE Id_Reserva = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Estado de la reserva actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Reservas");
                loadReservasData();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar reserva: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarReserva(ActionEvent evt) {
        int selectedRow = reservasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una reserva para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) reservasTable.getValueAt(selectedRow, 0);
        String laboratorio = (String) reservasTable.getValueAt(selectedRow, 1);
        String fecha = reservasTable.getValueAt(selectedRow, 3).toString();
        String hora = reservasTable.getValueAt(selectedRow, 4).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>¿Eliminar esta reserva?</b><br><br>" +
            "<b>Laboratorio:</b> " + laboratorio + "<br>" +
            "<b>Fecha:</b> " + fecha + "<br>" +
            "<b>Hora:</b> " + hora + "</html>",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            eliminarReservaDeDB(id);
        }
    }
    
    private void eliminarReservaDeDB(int id) {
        String deleteQuery = "DELETE FROM Reservas WHERE Id_Reserva = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String deleteMateriales = "DELETE FROM Reserva_Material_Adicional WHERE Id_Reserva = ?";
                try (PreparedStatement stmtMateriales = conn.prepareStatement(deleteMateriales)) {
                    stmtMateriales.setInt(1, id);
                    stmtMateriales.executeUpdate();
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Reserva eliminada exitosamente");
                        DatabaseConnection.notifyDatabaseChanged("Reservas");
                        DatabaseConnection.notifyDatabaseChanged("Reserva_Material_Adicional");
                        DatabaseConnection.resetAutoIncrement("Reservas");
                        loadReservasData();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "No se encontró la reserva a eliminar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar reserva: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error de conexión a la base de datos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Reservas") || tableChanged.equals("Material_Adicional") || 
            tableChanged.equals("Reserva_Material_Adicional")) {
            loadReservasData();
        }
    }
    
    // --- CLASES AUXILIARES ---
    
    private static class MaterialReserva {
        private final int nObjeto;
        private final int cantidad;
        
        public MaterialReserva(int nObjeto, int cantidad) {
            this.nObjeto = nObjeto;
            this.cantidad = cantidad;
        }
        
        public int getNObjeto() { return nObjeto; }
        public int getCantidad() { return cantidad; }
    }

    private static class LimitedSpinnerModel extends SpinnerNumberModel {
        private final int maxValue;
        
        public LimitedSpinnerModel(int value, int min, int max, int step) {
            super(value, min, max, step);
            this.maxValue = max;
        }
        
        @Override
        public void setValue(Object value) {
            int val = (Integer) value;
            if (val > maxValue) {
                super.setValue(maxValue);
            } else {
                super.setValue(value);
            }
        }
    }

    private static class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JSpinner spinner;
        
        public SpinnerCellEditor() {
            this.spinner = new JSpinner();
        }
        
        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            int disponible = (Integer) table.getModel().getValueAt(row, 1);
            spinner.setModel(new LimitedSpinnerModel(
                (Integer) value, 
                1, 
                disponible, 
                1
            ));
            spinner.setValue(value);
            return spinner;
        }
    }
}