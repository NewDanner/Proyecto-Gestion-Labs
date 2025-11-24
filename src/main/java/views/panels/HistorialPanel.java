/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author ANDREI/DANNER
 */

import javax.swing.*;
import views.DegradablePanel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import models.DatabaseConnection;
import models.User;

public class HistorialPanel extends DegradablePanel {
    private JTable tablaHistorial;
    private JButton btnTodos, btnReservas, btnMantenimiento, btnBajas, btnEquipos, btnMateriales, btnUsuarios;
    private JScrollPane scrollPane;
    private User currentUser;

    public HistorialPanel(User user) {
        this.currentUser = user;
        initComponents();
        cargarTodosLosHistoriales();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel principal con fondo semitransparente
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

        // Panel de botones de filtro
        JPanel panelFiltros = new JPanel(new GridLayout(1, 7, 5, 5));
        panelFiltros.setOpaque(false);
        
        btnTodos = createStyledButton("Todos");
        btnUsuarios = createStyledButton("Usuarios");
        btnEquipos = createStyledButton("Equipos");
        btnMateriales = createStyledButton("Materiales");
        btnReservas = createStyledButton("Reservas");
        btnMantenimiento = createStyledButton("Mantenimiento");
        btnBajas = createStyledButton("Bajas");
        
        panelFiltros.add(btnTodos);
        panelFiltros.add(btnUsuarios);
        panelFiltros.add(btnEquipos);
        panelFiltros.add(btnMateriales);
        panelFiltros.add(btnReservas);
        panelFiltros.add(btnMantenimiento);
        panelFiltros.add(btnBajas);
        
        mainPanel.add(panelFiltros, BorderLayout.NORTH);

        // Tabla de historial
        tablaHistorial = new JTable();
        tablaHistorial.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaHistorial.setRowHeight(25);
        
        JTableHeader header = tablaHistorial.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.BLACK);

        scrollPane = new JScrollPane(tablaHistorial);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Configurar listeners para los botones
        btnTodos.addActionListener(e -> cargarTodosLosHistoriales());
        btnUsuarios.addActionListener(e -> cargarHistorialPorTipo("USUARIO"));
        btnEquipos.addActionListener(e -> cargarHistorialPorTipo("EQUIPO"));
        btnMateriales.addActionListener(e -> cargarHistorialPorTipo("MATERIAL"));
        btnReservas.addActionListener(e -> cargarHistorialPorTipo("RESERVA"));
        btnMantenimiento.addActionListener(e -> cargarHistorialPorTipo("MANTENIMIENTO"));
        btnBajas.addActionListener(e -> cargarHistorialPorTipo("BAJA"));
    }

    private void cargarTodosLosHistoriales() {
        cargarDatos("SELECT h.*, u.username FROM Historial_Transacciones h JOIN Usuarios u ON h.id_usuario = u.id_usuario ORDER BY h.fecha_transaccion DESC");
    }

    private void cargarHistorialPorTipo(String tipo) {
        cargarDatos("SELECT h.*, u.username FROM Historial_Transacciones h JOIN Usuarios u ON h.id_usuario = u.id_usuario " +
                   "WHERE h.tipo_historial = '" + tipo + "' ORDER BY h.fecha_transaccion DESC");
    }

    private void cargarDatos(String query) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Fecha");
        model.addColumn("Usuario");
        model.addColumn("Tabla");
        model.addColumn("Operación");
        model.addColumn("Tipo");
        model.addColumn("Descripción");
        model.addColumn("Detalles");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            
            while (rs.next()) {
                String detalles = "";
                if (rs.getString("tipo_operacion").equals("UPDATE")) {
                    detalles = "Antes: " + rs.getString("datos_anteriores") + "\nDespués: " + rs.getString("datos_nuevos");
                } else {
                    detalles = rs.getString("tipo_operacion").equals("INSERT") 
                            ? rs.getString("datos_nuevos") 
                            : rs.getString("datos_anteriores");
                }
                
                model.addRow(new Object[]{
                    rs.getInt("id_transaccion"),
                    sdf.format(rs.getTimestamp("fecha_transaccion")),
                    rs.getString("username"),
                    rs.getString("tabla_afectada"),
                    rs.getString("tipo_operacion"),
                    rs.getString("tipo_historial"),
                    rs.getString("descripcion"),
                    detalles
                });
            }
            
            tablaHistorial.setModel(model);
            
            // Configurar renderizador para las celdas
            tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar el historial: " + ex.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}