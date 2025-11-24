package views.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.sql.*;
import models.Equipo;
import controllers.EquipoController;
import controllers.MantenimientoController;
import models.DatabaseConnection;
import models.User;

public class MantenimientoPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable equiposTable, materialesTable, mantenimientosTable;
    private JButton btnNuevoMantenimiento, btnRefresh, btnRefreshEquipos;
    private JButton btnEditarMantenimiento, btnEliminarMantenimiento;
    private User currentUser;
    
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    private final EquipoController equipoController = new EquipoController();
    private final MantenimientoController mantenimientoController = new MantenimientoController();
    
    public MantenimientoPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadEquiposData();
        loadMaterialesData();
        loadMantenimientosData();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(
            0, 0, color1,
            getWidth(), getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
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
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Pestaña de Equipos
        JPanel equiposPanel = createEquiposPanel();
        tabbedPane.addTab("Equipos", createStyledTabIcon("💻"), equiposPanel);
        
        // Pestaña de Dispositivos/Materiales
        JPanel materialesPanel = createMaterialesPanel();
        tabbedPane.addTab("Dispositivos", createStyledTabIcon("🖥️"), materialesPanel);
        
        // Pestaña de Mantenimientos
        JPanel mantenimientosPanel = createMantenimientosPanel();
        tabbedPane.addTab("Mantenimientos", createStyledTabIcon("🔧"), mantenimientosPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createEquiposPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        equiposTable = new JTable();
        equiposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equiposTable.setFont(new Font("Arial", Font.PLAIN, 14));
        equiposTable.setRowHeight(25);
        equiposTable.setForeground(Color.BLACK);
        
        JTableHeader header = equiposTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(equiposTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        btnNuevoMantenimiento = createStyledButton("Nuevo Mant. Equipo", new Color(156, 39, 176));
        btnNuevoMantenimiento.setPreferredSize(new Dimension(180, 40));
        btnNuevoMantenimiento.addActionListener(e -> abrirFormularioMantenimiento());
        buttonPanel.add(btnNuevoMantenimiento);
        
        btnRefreshEquipos = createStyledButton("Actualizar", new Color(109, 76, 65));
        btnRefreshEquipos.setPreferredSize(new Dimension(120, 40));
        btnRefreshEquipos.addActionListener(e -> {
            loadEquiposData();
            loadMaterialesData();
            loadMantenimientosData();
        });
        buttonPanel.add(btnRefreshEquipos);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createMaterialesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        materialesTable = new JTable();
        materialesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        materialesTable.setRowHeight(25);
        materialesTable.setForeground(Color.BLACK);
        
        JTableHeader header = materialesTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(materialesTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        JButton btnNuevoMantenimientoMaterial = createStyledButton("Nuevo Mant. Material", new Color(156, 39, 176));
        btnNuevoMantenimientoMaterial.setPreferredSize(new Dimension(180, 40));
        btnNuevoMantenimientoMaterial.addActionListener(e -> abrirFormularioMantenimientoMaterial());
        buttonPanel.add(btnNuevoMantenimientoMaterial);
        
        btnRefresh = createStyledButton("Actualizar", new Color(109, 76, 65));
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> {
            loadEquiposData();
            loadMaterialesData();
            loadMantenimientosData();
        });
        buttonPanel.add(btnRefresh);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel createMantenimientosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        mantenimientosTable = new JTable();
        mantenimientosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mantenimientosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        mantenimientosTable.setRowHeight(25);
        mantenimientosTable.setForeground(Color.BLACK);
        
        JTableHeader header = mantenimientosTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(mantenimientosTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        btnEditarMantenimiento = createStyledButton("Editar", new Color(2, 119, 189));
        btnEditarMantenimiento.addActionListener(this::editarMantenimiento);
        buttonPanel.add(btnEditarMantenimiento);
        
        btnEliminarMantenimiento = createStyledButton("Eliminar", new Color(198, 40, 40));
        btnEliminarMantenimiento.addActionListener(this::eliminarMantenimiento);
        buttonPanel.add(btnEliminarMantenimiento);
        
        JButton btnEnProceso = createStyledButton("En Proceso", new Color(70, 130, 180));
        btnEnProceso.addActionListener(e -> cambiarEstadoMantenimiento("En Proceso"));
        buttonPanel.add(btnEnProceso);
        
        JButton btnCompletado = createStyledButton("Completado", new Color(46, 125, 50));
        btnCompletado.addActionListener(e -> cambiarEstadoMantenimiento("Completado"));
        buttonPanel.add(btnCompletado);
        
        JButton btnCancelado = createStyledButton("Cancelado", new Color(198, 40, 40));
        btnCancelado.addActionListener(e -> cambiarEstadoMantenimiento("Cancelado"));
        buttonPanel.add(btnCancelado);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    private void abrirFormularioMantenimiento() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MantenimientoFormDialog form = new MantenimientoFormDialog(parent, currentUser);
        form.setTipoElemento("Equipo");
        form.setVisible(true);
    }
    
    private void abrirFormularioMantenimientoMaterial() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MantenimientoFormDialog form = new MantenimientoFormDialog(parent, currentUser);
        form.setTipoElemento("Material");
        form.setVisible(true);
    }
    
    private void editarMantenimiento(ActionEvent evt) {
        int selectedRow = mantenimientosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idMantenimiento = (int) mantenimientosTable.getValueAt(selectedRow, 0);
        String estado = (String) mantenimientosTable.getValueAt(selectedRow, 8);
        
        if (estado.equalsIgnoreCase("Completado") || estado.equalsIgnoreCase("Cancelado")) {
            JOptionPane.showMessageDialog(this, "No se puede editar un mantenimiento finalizado",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        MantenimientoFormDialog form = new MantenimientoFormDialog(parent, currentUser);
        form.setMantenimientoId(idMantenimiento);
        form.setVisible(true);
    }
    
    private void eliminarMantenimiento(ActionEvent evt) {
        int selectedRow = mantenimientosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idMantenimiento = (int) mantenimientosTable.getValueAt(selectedRow, 0);
        String nombreMantenimiento = (String) mantenimientosTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el mantenimiento: " + nombreMantenimiento + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (mantenimientoController.eliminarMantenimiento(idMantenimiento)) {
                JOptionPane.showMessageDialog(this, "Mantenimiento eliminado exitosamente");
                DatabaseConnection.notifyMantenimientoChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar mantenimiento",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cambiarEstadoMantenimiento(String nuevoEstado) {
        int selectedRow = mantenimientosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!currentUser.getRole().equals("Técnico(a) de Mantenimiento") && 
            !currentUser.getRole().equals("Administrador(a)")) {
            JOptionPane.showMessageDialog(this, "Solo técnicos y administradores pueden cambiar estados",
                "Permiso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idMantenimiento = (int) mantenimientosTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) mantenimientosTable.getValueAt(selectedRow, 8);
        
        if (estadoActual.equalsIgnoreCase("Completado") || estadoActual.equalsIgnoreCase("Cancelado")) {
            JOptionPane.showMessageDialog(this, "No se puede cambiar el estado de un mantenimiento ya finalizado",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String query = "UPDATE Mantenimiento SET estado = ?, fecha_fin = ? WHERE Id_Mantenimiento = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, nuevoEstado);
                
                if (nuevoEstado.equalsIgnoreCase("Completado") || nuevoEstado.equalsIgnoreCase("Cancelado")) {
                    stmt.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                } else {
                    stmt.setNull(2, java.sql.Types.TIMESTAMP);
                }
                
                stmt.setInt(3, idMantenimiento);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    if ((nuevoEstado.equalsIgnoreCase("Completado") || nuevoEstado.equalsIgnoreCase("Cancelado")) && 
                        mantenimientosTable.getValueAt(selectedRow, 3).equals("Equipo")) {
                        int idEquipo = obtenerIdElementoMantenimiento(idMantenimiento, "Equipo");
                        if (idEquipo != -1) {
                            actualizarEstadoEquipo(idEquipo, "Operativo");
                        }
                    }
                    
                    JOptionPane.showMessageDialog(this, "Estado del mantenimiento actualizado exitosamente");
                    DatabaseConnection.notifyMantenimientoChanged();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar estado: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int obtenerIdElementoMantenimiento(int idMantenimiento, String tipoElemento) throws SQLException {
        String query;
        if (tipoElemento.equals("Equipo")) {
            query = "SELECT Id_Equipo FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?";
        } else {
            query = "SELECT N_Objeto FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?";
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idMantenimiento);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    private void actualizarEstadoEquipo(int idEquipo, String estado) throws SQLException {
        String query = "UPDATE Equipos SET estado = ? WHERE Id_Equipo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, estado);
            stmt.setInt(2, idEquipo);
            stmt.executeUpdate();
        }
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
    
    private Icon createStyledTabIcon(String emoji) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                label.setBounds(x, y, getIconWidth(), getIconHeight());
                label.paint(g);
            }
            
            @Override
            public int getIconWidth() { return 20; }
            
            @Override
            public int getIconHeight() { return 20; }
        };
    }
    
    private void loadEquiposData() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        model.addColumn("ID");
        model.addColumn("Marca");
        model.addColumn("Modelo");
        model.addColumn("N° Serie");
        model.addColumn("Estado");
        model.addColumn("Laboratorio");
        
        // Cambiado a obtenerTodosEquipos()
        for (Equipo equipo : equipoController.obtenerTodosEquipos()) {
            model.addRow(new Object[]{
                equipo.getIdEquipo(),
                equipo.getMarca(),
                equipo.getModelo(),
                equipo.getNumeroSerie(),
                formatEstado(equipo.getEstado()),
                equipo.getIdLaboratorio() != null ? "Laboratorio " + equipo.getIdLaboratorio() : "Sin asignar"
            });
        }
        
        equiposTable.setModel(model);
        equiposTable.getColumnModel().getColumn(4).setCellRenderer(new EstadoCellRenderer());
    }
    
    private void loadMaterialesData() {
        String query = "SELECT N_Objeto, nombre_objeto, categoria, cantidad, " +
                     "cantidad_minima, extravio, daño, observaciones " +
                     "FROM Material_Adicional " +
                     "WHERE extravio = FALSE AND daño = FALSE " +
                     "ORDER BY nombre_objeto";
        
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
            model.addColumn("Nombre");
            model.addColumn("Categoría");
            model.addColumn("Cantidad");
            model.addColumn("Mínimo");
            model.addColumn("Extravío");
            model.addColumn("Daño");
            model.addColumn("Observaciones");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("N_Objeto"),
                    rs.getString("nombre_objeto"),
                    rs.getString("categoria"),
                    rs.getInt("cantidad"),
                    rs.getInt("cantidad_minima"),
                    rs.getBoolean("extravio") ? "Sí" : "No",
                    rs.getBoolean("daño") ? "Sí" : "No",
                    rs.getString("observaciones")
                });
            }
            
            materialesTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar materiales: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadMantenimientosData() {
    String query = "SELECT m.Id_Mantenimiento, m.nombre, m.tipo, m.tipo_elemento, " +
                 "COALESCE(" +
                 "  CASE WHEN m.tipo_elemento = 'Equipo' THEN " +
                 "    (SELECT CONCAT(e.marca, ' ', e.modelo) FROM Equipos e " +
                 "     JOIN Mantenimiento_Equipo me ON e.Id_Equipo = me.Id_Equipo " +
                 "     WHERE me.Id_Mantenimiento = m.Id_Mantenimiento)" +
                 "  ELSE " +
                 "    (SELECT ma.nombre_objeto FROM Material_Adicional ma " +
                 "     JOIN Mantenimiento_Material mm ON ma.N_Objeto = mm.N_Objeto " +
                 "     WHERE mm.Id_Mantenimiento = m.Id_Mantenimiento)" +
                 "  END, 'No especificado') AS nombre_elemento, " +
                 "DATE_FORMAT(m.fecha_inicio, '%Y-%m-%d %H:%i') AS fecha_inicio, " +
                 "DATE_FORMAT(m.fecha_fin, '%Y-%m-%d %H:%i') AS fecha_fin, " +
                 "m.descripcion, " +
                 "CONCAT(u.nombre, ' ', u.primer_apellido) AS responsable, " +
                 "m.estado " +
                 "FROM Mantenimiento m " +
                 "JOIN Usuarios u ON m.Id_Usuario_Responsable = u.id_usuario " +
                 "ORDER BY m.fecha_inicio DESC";
    
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
        model.addColumn("Nombre");
        model.addColumn("Tipo");
        model.addColumn("Elemento");
        model.addColumn("Detalle Elemento");
        model.addColumn("Fecha Inicio");
        model.addColumn("Fecha Fin");
        model.addColumn("Descripción");
        model.addColumn("Responsable");
        model.addColumn("Estado");
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("Id_Mantenimiento"),
                rs.getString("nombre"),
                rs.getString("tipo"),
                rs.getString("tipo_elemento"),
                rs.getString("nombre_elemento"),
                rs.getString("fecha_inicio"),
                rs.getString("fecha_fin"),
                rs.getString("descripcion"),
                rs.getString("responsable"),
                rs.getString("estado")
            });
        }
        
        mantenimientosTable.setModel(model);
        mantenimientosTable.getColumnModel().getColumn(9).setCellRenderer(new EstadoMantenimientoCellRenderer());
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar mantenimientos: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private String formatEstado(String estado) {
        if (estado == null || estado.isEmpty()) {
            return estado;
        }
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Equipos") || tableChanged.equals("Material_Adicional") || 
            tableChanged.equals("Mantenimiento") || tableChanged.equals("Observaciones")) {
            loadEquiposData();
            loadMaterialesData();
            loadMantenimientosData();
        }
    }
    
    class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(Color.BLACK);
            
            String estado = value.toString().toLowerCase();
            switch (estado) {
                case "operativo":
                    c.setBackground(new Color(200, 255, 200));
                    break;
                case "mantenimiento":
                    c.setBackground(new Color(255, 255, 150));
                    break;
                case "dañado":
                    c.setBackground(new Color(255, 200, 200));
                    break;
                default:
                    c.setBackground(Color.WHITE);
            }
            
            if (isSelected) {
                c.setBackground(new Color(150, 150, 255));
            }
            
            return c;
        }
    }
    
    class EstadoMantenimientoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setForeground(Color.BLACK);
            
            String estado = value.toString().toLowerCase();
            switch (estado) {
                case "pendiente":
                    c.setBackground(new Color(255, 255, 150));
                    break;
                case "en proceso":
                    c.setBackground(new Color(150, 200, 255));
                    break;
                case "completado":
                    c.setBackground(new Color(200, 255, 200));
                    break;
                case "cancelado":
                    c.setBackground(new Color(255, 200, 200));
                    break;
                default:
                    c.setBackground(Color.WHITE);
            }
            
            if (isSelected) {
                c.setBackground(new Color(150, 150, 255));
            }
            
            return c;
        }
    }
}