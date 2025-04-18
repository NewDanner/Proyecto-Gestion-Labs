/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Andrei
 */
package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import models.User;

public class MantenimientoPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable equiposTable;
    private JButton btnAddMantenimiento, btnFinalizarMantenimiento, btnRefresh;
    private User currentUser;
    
    // Colores para el degradado
    private final Color color1 = new Color(249, 45, 168); // Rosa
    private final Color color2 = new Color(255, 209, 12);  // Amarillo
    
    public MantenimientoPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadEquiposData();
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
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Panel de equipos
        JPanel equiposPanel = createEquiposPanel();
        tabbedPane.addTab("Equipos", createStyledTabIcon("🔧"), equiposPanel);
        
        // Panel de observaciones
        JPanel observacionesPanel = createObservacionesPanel();
        tabbedPane.addTab("Observaciones", createStyledTabIcon("📝"), observacionesPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createEquiposPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Tabla con estilo mejorado
        equiposTable = new JTable();
        equiposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equiposTable.setFont(new Font("Arial", Font.PLAIN, 14));
        equiposTable.setRowHeight(25);
        
        // Personalizar el header de la tabla
        JTableHeader header = equiposTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(equiposTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones con estilo
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        btnAddMantenimiento = createStyledButton("Agregar a Mantenimiento", new Color(2, 119, 189));
        btnAddMantenimiento.addActionListener(this::agregarMantenimiento);
        buttonPanel.add(btnAddMantenimiento);
        
        btnFinalizarMantenimiento = createStyledButton("Finalizar Mantenimiento", new Color(46, 125, 50));
        btnFinalizarMantenimiento.addActionListener(this::finalizarMantenimiento);
        buttonPanel.add(btnFinalizarMantenimiento);
        
        btnRefresh = createStyledButton("Actualizar", new Color(109, 76, 65));
        btnRefresh.addActionListener(e -> loadEquiposData());
        buttonPanel.add(btnRefresh);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createObservacionesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Lista de observaciones técnicas", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(new Color(70, 70, 70));
        
        panel.add(label, BorderLayout.CENTER);
        return panel;
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
        button.setPreferredSize(new Dimension(180, 40));
        
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
            public int getIconWidth() {
                return 20;
            }
            
            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }
    
    private void loadEquiposData() {
    String query = "SELECT e.Id_Equipo, e.marca, e.modelo, e.numero_de_serie, " +
                  "e.estado, l.nombre AS laboratorio " +
                  "FROM Equipos e " +
                  "LEFT JOIN Laboratorios l ON e.id_laboratorio = l.Id_Laboratorio " +
                  "ORDER BY e.estado, l.nombre";
    
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
        model.addColumn("Marca");
        model.addColumn("Modelo");
        model.addColumn("N° Serie");
        model.addColumn("Estado");
        model.addColumn("Laboratorio");
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("Id_Equipo"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getString("numero_de_serie"),
                formatEstado(rs.getString("estado")),
                rs.getString("laboratorio") != null ? rs.getString("laboratorio") : "Sin asignar"
            });
        }
        
        equiposTable.setModel(model);
        
        // Personalizar el encabezado de la tabla
        JTableHeader header = equiposTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14)); // Fuente en negrita
        header.setForeground(Color.BLACK); // Texto negro
        header.setBackground(new Color(240, 240, 240)); // Fondo gris claro (opcional)
        
        // Opcional: Personalizar el renderer de las celdas
        equiposTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.BLACK); // Texto negro en celdas
                if (isSelected) {
                    c.setBackground(new Color(200, 200, 255)); // Color de selección
                } else {
                    c.setBackground(Color.WHITE); // Fondo blanco
                }
                return c;
            }
        });
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar equipos: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void agregarMantenimiento(ActionEvent evt) {
        int selectedRow = equiposTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) equiposTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) equiposTable.getValueAt(selectedRow, 4);
        
        if (estadoActual.equalsIgnoreCase("En mantenimiento")) {
            JOptionPane.showMessageDialog(this, "El equipo ya está en mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String observacion = JOptionPane.showInputDialog(this, 
            "Ingrese la observación técnica para el mantenimiento:",
            "Agregar a Mantenimiento", JOptionPane.QUESTION_MESSAGE);
        
        if (observacion != null && !observacion.trim().isEmpty()) {
            cambiarEstadoEquipo(id, "en_mantenimiento", observacion);
        }
    }
    
    private void finalizarMantenimiento(ActionEvent evt) {
        int selectedRow = equiposTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) equiposTable.getValueAt(selectedRow, 0);
        String estadoActual = (String) equiposTable.getValueAt(selectedRow, 4);
        
        if (!estadoActual.equalsIgnoreCase("En mantenimiento")) {
            JOptionPane.showMessageDialog(this, "El equipo no está en mantenimiento",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String observacion = JOptionPane.showInputDialog(this, 
            "Ingrese las acciones realizadas durante el mantenimiento:",
            "Finalizar Mantenimiento", JOptionPane.QUESTION_MESSAGE);
        
        if (observacion != null && !observacion.trim().isEmpty()) {
            cambiarEstadoEquipo(id, "disponible", observacion);
        }
    }
    
    private void cambiarEstadoEquipo(int id, String nuevoEstado, String observacion) {
        String queryEquipo = "UPDATE Equipos SET estado = ? WHERE Id_Equipo = ?";
        String queryObservacion = "INSERT INTO Observaciones (nombre, fecha, detalle, realizado, id_equipo) " +
                                "VALUES (?, NOW(), ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtEquipo = conn.prepareStatement(queryEquipo);
                 PreparedStatement stmtObservacion = conn.prepareStatement(queryObservacion)) {
                
                stmtEquipo.setString(1, nuevoEstado);
                stmtEquipo.setInt(2, id);
                stmtEquipo.executeUpdate();
                
                stmtObservacion.setString(1, "Mantenimiento");
                stmtObservacion.setString(2, observacion);
                stmtObservacion.setString(3, currentUser.getUsername());
                stmtObservacion.setInt(4, id);
                stmtObservacion.executeUpdate();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Estado del equipo actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Equipos");
                DatabaseConnection.notifyDatabaseChanged("Observaciones");
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarEquipo(int id) {
        String query = "DELETE FROM Equipos WHERE Id_Equipo = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    DatabaseConnection.resetAutoIncrement("Equipos");
                    conn.commit();
                    
                    JOptionPane.showMessageDialog(this, "Equipo eliminado exitosamente");
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
     @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Equipos") || tableChanged.equals("Observaciones")) {
            loadEquiposData();
        }
    }
}
