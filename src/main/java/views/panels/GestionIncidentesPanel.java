package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;
import models.DatabaseConnection;
import models.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DateFormat;
import java.util.Date;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Andrei
 */
public class GestionIncidentesPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTabbedPane tabbedPane;
    private JTable incidentesTable, reposicionTable, bajasTable;
    private User currentUser;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    
    public GestionIncidentesPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadIncidentesData();
        loadReposicionData();
        loadBajasData();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        
        // Pestaña de Incidentes
        JPanel incidentesPanel = createIncidentesPanel();
        incidentesPanel.setOpaque(false);
        
        // Pestaña de Reposición
        JPanel reposicionPanel = createReposicionPanel();
        reposicionPanel.setOpaque(false);
        
        // Pestaña de Bajas
        JPanel bajasPanel = createBajasPanel();
        bajasPanel.setOpaque(false);
        
        tabbedPane.addTab("Gestión de Incidentes", incidentesPanel);
        tabbedPane.addTab("Reposición de Dispositivos", reposicionPanel);
        tabbedPane.addTab("Dispositivos de Baja", bajasPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private JPanel createIncidentesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        incidentesTable = new JTable();
        configureTable(incidentesTable);
        incidentesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(incidentesTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnNuevo = createStyledButton("Nuevo Incidente", new Color(46, 125, 50), this::nuevoIncidente);
        JButton btnResolver = createStyledButton("Marcar como Resuelto", new Color(2, 119, 189), this::resolverIncidente);
        
        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnResolver);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReposicionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        reposicionTable = new JTable();
        configureTable(reposicionTable);
        reposicionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(reposicionTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnNuevaReposicion = createStyledButton("Nueva Solicitud", new Color(46, 125, 50), this::nuevaReposicion);
        JButton btnAprobar = createStyledButton("Aprobar", new Color(2, 119, 189), this::aprobarReposicion);
        
        buttonPanel.add(btnNuevaReposicion);
        buttonPanel.add(btnAprobar);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBajasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        bajasTable = new JTable();
        configureTable(bajasTable);
        bajasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(bajasTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnNuevaBaja = createStyledButton("Registrar Baja", new Color(46, 125, 50), this::nuevaBaja);
        
        buttonPanel.add(btnNuevaBaja);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void configureTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setGridColor(new Color(200, 200, 200));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
    }
    
    private JButton createStyledButton(String text, Color bgColor, ActionListener listener) {
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
                g2d.drawString(getText(), (getWidth() - (int) r.getWidth()) / 2, 
                    (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent());
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(180, 40));
        button.addActionListener(listener);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(MouseEvent e) { button.setCursor(Cursor.getDefaultCursor()); }
        });

        return button;
    }
    
    private void loadIncidentesData() {
        String query = "SELECT * FROM Gestion_Incidentes ORDER BY fecha_reporte DESC";
        
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
            model.addColumn("Título");
            model.addColumn("Prioridad");
            model.addColumn("Fecha Reporte");
            model.addColumn("Estado");
            model.addColumn("Equipo");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Incidente"),
                    rs.getString("titulo"),
                    rs.getString("prioridad"),
                    rs.getTimestamp("fecha_reporte"),
                    rs.getString("estado"),
                    getNombreEquipo(rs.getInt("id_equipo"))
                });
            }
            
            incidentesTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar incidentes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadReposicionData() {
        String query = "SELECT * FROM Reposicion_Dispositivos ORDER BY fecha_solicitud DESC";
        
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
            model.addColumn("Fecha Solicitud");
            model.addColumn("Estado");
            model.addColumn("Costo Estimado");
            model.addColumn("Aprobado Por");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Reposicion"),
                    rs.getTimestamp("fecha_solicitud"),
                    rs.getString("estado"),
                    rs.getDouble("costo_estimado"),
                    getNombreUsuario(rs.getInt("aprobado_por"))
                });
            }
            
            reposicionTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar solicitudes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadBajasData() {
        String query = "SELECT * FROM Reporte_Dispositivos_Baja ORDER BY fecha_baja DESC";
        
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
            model.addColumn("Descripción");
            model.addColumn("Tipo");
            model.addColumn("Motivo");
            model.addColumn("Fecha Baja");
            model.addColumn("Autorizado Por");
            model.addColumn("Rol");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Baja"),
                    rs.getString("Descripcion"),
                    rs.getString("Tipo_Elemento"),
                    rs.getString("motivo"),
                    rs.getTimestamp("fecha_baja"),
                    rs.getString("Autorizado_Por"),
                    rs.getString("Rol_Autorizador")
                });
            }
            
            bajasTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar bajas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getNombreEquipo(int idEquipo) {
        if (idEquipo == 0) return "N/A";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT marca, modelo FROM Equipos WHERE Id_Equipo = ?")) {
            
            stmt.setInt(1, idEquipo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("marca") + " " + rs.getString("modelo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }
    
    private String getNombreUsuario(int idUsuario) {
        if (idUsuario == 0) return "N/A";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT nombre FROM Usuarios WHERE id_usuario = ?")) {
            
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }
    
    private void nuevoIncidente(ActionEvent evt) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Nuevo Incidente", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JTextField txtTitulo = new JTextField();
        JTextArea txtDescripcion = new JTextArea(5, 20);
        JComboBox<String> cbPrioridad = new JComboBox<>(new String[]{"Baja", "Media", "Alta", "Crítica"});
        JComboBox<String> cbEquipos = new JComboBox<>(getListaEquipos());
        JTextArea txtObservaciones = new JTextArea(5, 20);
        
        formPanel.add(new JLabel("Título:"));
        formPanel.add(txtTitulo);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(new JScrollPane(txtDescripcion));
        formPanel.add(new JLabel("Prioridad:"));
        formPanel.add(cbPrioridad);
        formPanel.add(new JLabel("Equipo relacionado:"));
        formPanel.add(cbEquipos);
        formPanel.add(new JLabel("Observaciones:"));
        formPanel.add(new JScrollPane(txtObservaciones));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnGuardar = createStyledButton("Guardar", new Color(46, 125, 50), e -> {
            if (txtTitulo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El título es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            registrarIncidente(
                txtTitulo.getText(),
                txtDescripcion.getText(),
                (String) cbPrioridad.getSelectedItem(),
                txtObservaciones.getText(),
                getIdEquipoSeleccionado((String) cbEquipos.getSelectedItem())
            );
            dialog.dispose();
        });
        
        JButton btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40), e -> dialog.dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void registrarIncidente(String titulo, String descripcion, String prioridad, 
                              String observaciones, int idEquipo) {
        String query = "INSERT INTO Gestion_Incidentes (titulo, descripcion, verificacion, " +
                      "prioridad, observaciones, id_usuario, id_equipo) " +
                      "VALUES (?, ?, 'Pendiente', ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, titulo);
            stmt.setString(2, descripcion);
            stmt.setString(3, prioridad);
            stmt.setString(4, observaciones);
            stmt.setInt(5, currentUser.getId());
            stmt.setInt(6, idEquipo);
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Incidente registrado exitosamente");
            DatabaseConnection.notifyDatabaseChanged("Gestion_Incidentes");
            loadIncidentesData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar incidente: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resolverIncidente(ActionEvent evt) {
        int selectedRow = incidentesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un incidente", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) incidentesTable.getValueAt(selectedRow, 0);
        String titulo = (String) incidentesTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Marcar como resuelto el incidente: " + titulo + "?", 
            "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            actualizarEstadoIncidente(id, "Resuelto");
        }
    }
    
    private void nuevaReposicion(ActionEvent evt) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Nueva Solicitud de Reposición", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JTextField txtMotivo = new JTextField();
        JTextField txtCostoEstimado = new JTextField();
        JTextField txtFechaEstimada = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        JTextArea txtObservaciones = new JTextArea(5, 20);
        
        formPanel.add(new JLabel("Motivo:"));
        formPanel.add(txtMotivo);
        formPanel.add(new JLabel("Costo Estimado:"));
        formPanel.add(txtCostoEstimado);
        formPanel.add(new JLabel("Fecha Estimada Entrega:"));
        formPanel.add(txtFechaEstimada);
        formPanel.add(new JLabel("Observaciones:"));
        formPanel.add(new JScrollPane(txtObservaciones));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnGuardar = createStyledButton("Guardar", new Color(46, 125, 50), e -> {
            try {
                double costo = Double.parseDouble(txtCostoEstimado.getText());
                Date fecha = txtFechaEstimada.getText().isEmpty() ? null : 
                    new Date(new SimpleDateFormat("yyyy-MM-dd").parse(txtFechaEstimada.getText()).getTime());
                
                registrarReposicion(
                    txtMotivo.getText(),
                    costo,
                    fecha,
                    txtObservaciones.getText()
                );
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Costo debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido (yyyy-MM-dd)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40), e -> dialog.dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void aprobarReposicion(ActionEvent evt) {
        int selectedRow = reposicionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) reposicionTable.getValueAt(selectedRow, 0);
        String estado = (String) reposicionTable.getValueAt(selectedRow, 2);
        
        if (!"Pendiente".equalsIgnoreCase(estado)) {
            JOptionPane.showMessageDialog(this, "Solo se pueden aprobar solicitudes pendientes", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Aprobar esta solicitud de reposición?", 
            "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            aprobarSolicitudReposicion(id);
        }
    }
    
    private void nuevaBaja(ActionEvent evt) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Registrar Baja de Equipo", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JComboBox<String> cbEquipos = new JComboBox<>(getListaEquiposOperativos());
        JComboBox<String> cbMotivo = new JComboBox<>(new String[]{"Obsoleto", "Daño Irreparable", "Pérdida", "Actualización", "Otro"});
        JTextArea txtDescripcion = new JTextArea(5, 20);
        
        formPanel.add(new JLabel("Equipo:"));
        formPanel.add(cbEquipos);
        formPanel.add(new JLabel("Motivo:"));
        formPanel.add(cbMotivo);
        formPanel.add(new JLabel("Descripción:"));
        formPanel.add(new JScrollPane(txtDescripcion));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnGuardar = createStyledButton("Guardar", new Color(46, 125, 50), e -> {
            if (cbEquipos.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(dialog, "Seleccione un equipo", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            registrarBaja(
                getIdEquipoSeleccionado((String) cbEquipos.getSelectedItem()),
                (String) cbMotivo.getSelectedItem(),
                txtDescripcion.getText()
            );
            dialog.dispose();
        });
        
        JButton btnCancelar = createStyledButton("Cancelar", new Color(198, 40, 40), e -> dialog.dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private String[] getListaEquipos() {
        List<String> equipos = new ArrayList<>();
        equipos.add("Ninguno");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id_Equipo, marca, modelo FROM Equipos")) {
            
            while (rs.next()) {
                equipos.add(rs.getString("marca") + " " + rs.getString("modelo") + " (ID: " + rs.getInt("Id_Equipo") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return equipos.toArray(new String[0]);
    }
    
    private String[] getListaEquiposOperativos() {
        List<String> equipos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT Id_Equipo, marca, modelo FROM Equipos WHERE estado = 'Operativo'")) {
            
            while (rs.next()) {
                equipos.add(rs.getString("marca") + " " + rs.getString("modelo") + " (ID: " + rs.getInt("Id_Equipo") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return equipos.toArray(new String[0]);
    }
    
    private int getIdEquipoSeleccionado(String seleccion) {
        if (seleccion.equals("Ninguno")) return 0;
        
        try {
            int start = seleccion.lastIndexOf("(ID: ") + 5;
            int end = seleccion.lastIndexOf(")");
            return Integer.parseInt(seleccion.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void registrarReposicion(String motivo, double costoEstimado, Date fechaEstimada, 
                               String observaciones) {
        String query = "INSERT INTO Reposicion_Dispositivos (fecha_solicitud, estado, " +
                      "motivo_rechazo, fecha_estimada_entrega, costo_estimado, observaciones) " +
                      "VALUES (NOW(), 'Pendiente', NULL, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            if (fechaEstimada != null) {
                stmt.setDate(1, new java.sql.Date(fechaEstimada.getTime()));
            } else {
                stmt.setNull(1, Types.DATE);
            }
            stmt.setDouble(2, costoEstimado);
            stmt.setString(3, observaciones);
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Solicitud de reposición registrada");
            DatabaseConnection.notifyDatabaseChanged("Reposicion_Dispositivos");
            loadReposicionData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar reposición: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarEstadoIncidente(int id, String estado) {
        String query = "UPDATE Gestion_Incidentes SET estado = ?, fecha_solucion = NOW() " +
                      "WHERE Id_Incidente = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, estado);
            stmt.setInt(2, id);
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Estado del incidente actualizado");
            DatabaseConnection.notifyDatabaseChanged("Gestion_Incidentes");
            loadIncidentesData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar incidente: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void aprobarSolicitudReposicion(int id) {
        String query = "UPDATE Reposicion_Dispositivos SET estado = 'Aprobado', " +
                      "aprobado_por = ?, fecha_aprobacion = NOW() " +
                      "WHERE Id_Reposicion = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUser.getId());
            stmt.setInt(2, id);
            
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Solicitud aprobada exitosamente");
            DatabaseConnection.notifyDatabaseChanged("Reposicion_Dispositivos");
            loadReposicionData();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al aprobar solicitud: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registrarBaja(int idEquipo, String motivo, String descripcion) {
        String query = "INSERT INTO Dispositivos_Baja (Id_Equipo, fecha_baja, motivo, " +
                      "descripcion_motivo, autorizado_por) " +
                      "VALUES (?, NOW(), ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idEquipo);
                stmt.setString(2, motivo);
                stmt.setString(3, descripcion);
                stmt.setInt(4, currentUser.getId());
                
                stmt.executeUpdate();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Baja registrada exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Dispositivos_Baja");
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                loadBajasData();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar baja: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Gestion_Incidentes")) {
            loadIncidentesData();
        } else if (tableChanged.equals("Reposicion_Dispositivos")) {
            loadReposicionData();
        } else if (tableChanged.equals("Dispositivos_Baja") || tableChanged.equals("Equipos")) {
            loadBajasData();
        }
    }
}