package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import models.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservacionPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable prestamosTable;
    private JButton btnNuevaReserva, btnEditar, btnCancelar, btnCompletar, btnEliminar, btnRefresh;
    private User currentUser;
    
    // Colores para el degradado
    private final Color color1 = new Color(249, 45, 168); // Rosa
    private final Color color2 = new Color(255, 209, 12);  // Amarillo
    
    public ReservacionPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadPrestamosData();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Fondo degradado diagonal
        GradientPaint gradient = new GradientPaint(
            0, 0, color1,
            getWidth(), getHeight(), color2);
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel principal con transparencia
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo semitransparente con bordes redondeados
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Tabla con estilo mejorado
        prestamosTable = new JTable();
        prestamosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prestamosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prestamosTable.setRowHeight(25);
        
        // Personalizar el header de la tabla
        JTableHeader header = prestamosTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(prestamosTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones con estilo
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        btnNuevaReserva = createStyledButton("Nueva Reserva", new Color(46, 125, 50)); // Verde
        btnNuevaReserva.addActionListener(e -> abrirCalendarioParaReserva());
        buttonPanel.add(btnNuevaReserva);
        
        btnEditar = createStyledButton("Editar", new Color(2, 119, 189)); // Azul
        btnEditar.addActionListener(this::editarReserva);
        buttonPanel.add(btnEditar);
        
        btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40)); // Rojo
        btnCancelar.addActionListener(this::cancelarPrestamo);
        buttonPanel.add(btnCancelar);
        
        btnCompletar = createStyledButton("Completar", new Color(142, 36, 170)); // Morado
        btnCompletar.addActionListener(this::completarPrestamo);
        buttonPanel.add(btnCompletar);
        
        btnEliminar = createStyledButton("Eliminar", new Color(109, 76, 65)); // Marrón
        btnEliminar.addActionListener(this::eliminarReserva);
        buttonPanel.add(btnEliminar);
        
        btnRefresh = createStyledButton("Actualizar", new Color(30, 30, 30)); // Gris oscuro
        btnRefresh.addActionListener(e -> loadPrestamosData());
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
                
                // Fondo con esquinas redondeadas
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Borde
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                
                // Texto
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
        
        // Efecto hover
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
    
    
    private String getMateriaReserva(int idPrestamo) {
        String query = "SELECT materia FROM Prestamo WHERE Id_Prestamo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, idPrestamo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("materia");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean verificarDisponibilidad(int idPrestamo, String labName, LocalDate fecha, Time horaInicio, Time horaFin) {
        String query = "SELECT COUNT(*) FROM Prestamo p " +
                      "JOIN Laboratorios l ON p.Nro_Laboratorio = l.Id_Laboratorio " +
                      "WHERE l.nombre = ? AND p.fecha_reserva = ? " +
                      "AND p.Id_Prestamo != ? " +
                      "AND ((p.hora_inicio BETWEEN ? AND ?) OR " +
                      "(p.hora_fin BETWEEN ? AND ?) OR " +
                      "(p.hora_inicio <= ? AND p.hora_fin >= ?)) " +
                      "AND p.estado != 'cancelado'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, labName);
            stmt.setDate(2, Date.valueOf(fecha));
            stmt.setInt(3, idPrestamo);
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
    
    private void actualizarReserva(int id, LocalDate fecha, Time horaInicio, Time horaFin, String materia) {
        String query = "UPDATE Prestamo SET fecha_reserva = ?, hora_inicio = ?, hora_fin = ?, materia = ? WHERE Id_Prestamo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(fecha));
            stmt.setTime(2, horaInicio);
            stmt.setTime(3, horaFin);
            stmt.setString(4, materia);
            stmt.setInt(5, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Reserva actualizada exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Prestamo");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la reserva",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar reserva: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void abrirCalendarioParaReserva() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            CalendarPanel calendarPanel = new CalendarPanel(currentUser);
            JFrame frame = new JFrame("Calendario de Reservas");
            frame.setContentPane(calendarPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void loadPrestamosData() {
    new SwingWorker<Void, Void>() {
        @Override
        protected Void doInBackground() throws Exception {
            String query = "SELECT p.Id_Prestamo, l.nombre AS laboratorio, " +
                         "p.tipo_de_prestamo, p.fecha_reserva, " +
                         "p.hora_inicio, p.hora_fin, p.estado, " +
                         "u.username AS usuario " +
                         "FROM Prestamo p " +
                         "JOIN Laboratorios l ON p.Nro_Laboratorio = l.Id_Laboratorio " +
                         "JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                         "ORDER BY p.fecha_reserva DESC, p.hora_inicio DESC";

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

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

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("Id_Prestamo"),
                        rs.getString("laboratorio"),
                        formatTipoPrestamo(rs.getString("tipo_de_prestamo")),
                        rs.getDate("fecha_reserva"),
                        rs.getTime("hora_inicio"),
                        rs.getTime("hora_fin"),
                        formatEstado(rs.getString("estado")),
                        rs.getString("usuario")
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    prestamosTable.setModel(model);
                    
                    // Personalizar el header de la tabla
                    JTableHeader header = prestamosTable.getTableHeader();
                    header.setFont(new Font("Arial", Font.BOLD, 14));
                    header.setBackground(new Color(70, 130, 180));
                    header.setForeground(Color.BLACK);
                    
                    // Renderer para las celdas
                    prestamosTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                            c.setForeground(Color.BLACK);
                            if (isSelected) {
                                c.setBackground(new Color(200, 200, 255));
                            } else {
                                c.setBackground(Color.WHITE);
                            }
                            return c;
                        }
                    });
                    
                    prestamosTable.setBackground(Color.WHITE);
                    prestamosTable.setGridColor(new Color(220, 220, 220));
                });
            } catch (SQLException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Error al cargar préstamos: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
            return null;
        }
    }.execute();
}
    
    private String formatTipoPrestamo(String tipo) {
        return tipo.substring(0, 1).toUpperCase() + tipo.substring(1).toLowerCase();
    }
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void eliminarReserva(ActionEvent evt) {
        int selectedRow = prestamosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione una reserva para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) prestamosTable.getValueAt(selectedRow, 0);
        String laboratorio = (String) prestamosTable.getValueAt(selectedRow, 1);
        String fecha = prestamosTable.getValueAt(selectedRow, 3).toString();
        String hora = prestamosTable.getValueAt(selectedRow, 4).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>¿Eliminar esta reserva?</b><br><br>" +
            "<b>Laboratorio:</b> " + laboratorio + "<br>" +
            "<b>Fecha:</b> " + fecha + "<br>" +
            "<b>Hora:</b> " + hora + "</html>",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            eliminarPrestamo(id);
        }
    }
    
    private void eliminarPrestamo(int id) {
        String checkDependientes = "SELECT COUNT(*) FROM Registro_cliente WHERE Nro_Prestamo = ?";
        String deleteQuery = "DELETE FROM Prestamo WHERE Id_Prestamo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Verificar registros dependientes
                try (PreparedStatement stmtCheck = conn.prepareStatement(checkDependientes)) {
                    stmtCheck.setInt(1, id);
                    ResultSet rs = stmtCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                            "Este préstamo tiene registros de clientes asociados. ¿Desea eliminarlos también?",
                            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            String deleteDependientes = "DELETE FROM Registro_cliente WHERE Nro_Prestamo = ?";
                            try (PreparedStatement stmtDelDep = conn.prepareStatement(deleteDependientes)) {
                                stmtDelDep.setInt(1, id);
                                stmtDelDep.executeUpdate();
                            }
                        } else {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, 
                                "No se puede eliminar el préstamo porque tiene registros asociados",
                                "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                
                // Eliminar el préstamo
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Reserva eliminada exitosamente");
                        DatabaseConnection.notifyDatabaseChanged("Prestamo");
                        DatabaseConnection.resetAutoIncrement("Prestamo");
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
    
    
    private void cancelarPrestamo(ActionEvent evt) {
        cambiarEstadoPrestamo("cancelado");
    }
    
    private void completarPrestamo(ActionEvent evt) {
        cambiarEstadoPrestamo("completado");
    }
    
    private void cambiarEstadoPrestamo(String nuevoEstado) {
        int selectedRow = prestamosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un préstamo",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) prestamosTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) prestamosTable.getValueAt(selectedRow, 6);
        
        if (estadoActual.equalsIgnoreCase(nuevoEstado)) {
            JOptionPane.showMessageDialog(this, "El préstamo ya está en estado " + nuevoEstado,
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String mensaje = String.format("¿Está seguro que desea cambiar el estado del préstamo #%d a %s?", 
                                      id, nuevoEstado);
        
        int confirm = JOptionPane.showConfirmDialog(this, mensaje,
            "Confirmar Cambio de Estado", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            actualizarEstadoPrestamo(id, nuevoEstado);
        }
    }
    private void editarReserva(ActionEvent evt) {
    int selectedRow = prestamosTable.getSelectedRow();
    if (selectedRow == -1) {
        showMessage("Por favor seleccione una reserva para editar", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Obtener el estado directamente del modelo de la tabla
    String estado = prestamosTable.getModel().getValueAt(selectedRow, 6).toString().toLowerCase();
    
    if (!"pendiente".equals(estado)) {
        showMessage("Solo se pueden editar reservas pendientes. Estado actual: " + estado,
            "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int id = (int) prestamosTable.getValueAt(selectedRow, 0);
    String laboratorio = (String) prestamosTable.getValueAt(selectedRow, 1);
    Date fecha = (Date) prestamosTable.getValueAt(selectedRow, 3);
    Time horaInicio = (Time) prestamosTable.getValueAt(selectedRow, 4);
    Time horaFin = (Time) prestamosTable.getValueAt(selectedRow, 5);
    
    // Crear diálogo de edición
    JDialog editDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Editar Reserva", true);
    editDialog.setLayout(new BorderLayout());
    editDialog.setSize(400, 300);
    editDialog.setLocationRelativeTo(this);
    
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
    JLabel lblLaboratorio = new JLabel("Laboratorio:");
    JTextField txtLaboratorio = new JTextField(laboratorio);
    txtLaboratorio.setEditable(false);
    
    JLabel lblFecha = new JLabel("Fecha (YYYY-MM-DD):");
    JTextField txtFecha = new JTextField(fecha.toString());
    
    JLabel lblHoraInicio = new JLabel("Hora Inicio (HH:MM:SS):");
    JTextField txtHoraInicio = new JTextField(horaInicio.toString());
    
    JLabel lblHoraFin = new JLabel("Hora Fin (HH:MM:SS):");
    JTextField txtHoraFin = new JTextField(horaFin.toString());
    
    JLabel lblMateria = new JLabel("Materia:");
    String[] materias = {"Electrónica", "Hardware", "Redes y Telecomunicaciones"};
    JComboBox<String> cbMateria = new JComboBox<>(materias);
    
    // Establecer materia actual si existe
    String materiaActual = getMateriaReserva(id);
    if(materiaActual != null) {
        String materiaFormateada = materiaActual.replace("_", " ").replace("telecomunicaciones", "y telecomunicaciones");
        materiaFormateada = materiaFormateada.substring(0, 1).toUpperCase() + materiaFormateada.substring(1);
        cbMateria.setSelectedItem(materiaFormateada);
    }
    
    panel.add(lblLaboratorio);
    panel.add(txtLaboratorio);
    panel.add(lblFecha);
    panel.add(txtFecha);
    panel.add(lblHoraInicio);
    panel.add(txtHoraInicio);
    panel.add(lblHoraFin);
    panel.add(txtHoraFin);
    panel.add(lblMateria);
    panel.add(cbMateria);
    
    JPanel buttonPanel = new JPanel();
    JButton btnGuardar = createStyledButton("Guardar", new Color(46, 125, 50));
    JButton btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40));
    
    btnGuardar.addActionListener(e -> {
        try {
            // Validar datos
            LocalDate nuevaFecha = LocalDate.parse(txtFecha.getText());
            Time nuevaHoraInicio = Time.valueOf(txtHoraInicio.getText());
            Time nuevaHoraFin = Time.valueOf(txtHoraFin.getText());
            
            if (nuevaHoraInicio.after(nuevaHoraFin)) {
                showMessage("La hora de inicio debe ser anterior a la hora de fin", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verificar disponibilidad
            if(!verificarDisponibilidad(id, laboratorio, nuevaFecha, nuevaHoraInicio, nuevaHoraFin)) {
                showMessage("El laboratorio no está disponible en ese horario", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Mapear materia a formato de base de datos
            String materia = ((String)cbMateria.getSelectedItem()).toLowerCase()
                .replace(" y ", "_").replace("ó", "o");
            
            // Actualizar reserva
            actualizarReserva(id, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, materia);
            editDialog.dispose();
            
        } catch (Exception ex) {
            showMessage("Formato de fecha u hora inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
    
    btnCancelar.addActionListener(e -> editDialog.dispose());
    
    buttonPanel.add(btnGuardar);
    buttonPanel.add(btnCancelar);
    
    editDialog.add(panel, BorderLayout.CENTER);
    editDialog.add(buttonPanel, BorderLayout.SOUTH);
    editDialog.setVisible(true);
}
    
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    private void actualizarEstadoPrestamo(int id, String nuevoEstado) {
        String query = "UPDATE Prestamo SET estado = ? WHERE Id_Prestamo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Estado del préstamo actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Prestamo");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar préstamo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Prestamo")) {
            loadPrestamosData();
        }
    }
    
}
