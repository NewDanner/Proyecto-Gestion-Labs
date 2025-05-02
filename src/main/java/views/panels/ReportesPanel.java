package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import models.DatabaseConnection;
import models.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ReportesPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable reportesTable;
    private JComboBox<String> cbTipoReporte;
    private JComboBox<Integer> cbAnio;
    private JList<String> listMeses;
    private JButton btnGenerarPDF;
    private JButton btnFiltrar;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    
    public ReportesPanel(User user) {
        initComponents();
        loadReportesData("Reporte_Laboratorios", null, null);
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
        
        // Panel de controles
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tipo de reporte
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblTipoReporte = new JLabel("Tipo de Reporte:");
        lblTipoReporte.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(lblTipoReporte, gbc);
        
        gbc.gridx = 1;
        cbTipoReporte = new JComboBox<>(new String[]{
            "Reporte_Laboratorios", 
            "Reporte_Mantenimiento", 
            "Reporte_Dispositivos_Baja"
        });
        cbTipoReporte.setFont(new Font("Arial", Font.PLAIN, 14));
        controlPanel.add(cbTipoReporte, gbc);
        
        // Año
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(lblAnio, gbc);
        
        gbc.gridx = 1;
        cbAnio = new JComboBox<>();
        cbAnio.setFont(new Font("Arial", Font.PLAIN, 14));
        int anioActual = LocalDate.now().getYear();
        for (int i = anioActual - 5; i <= anioActual + 1; i++) {
            cbAnio.addItem(i);
        }
        cbAnio.setSelectedItem(anioActual);
        controlPanel.add(cbAnio, gbc);
        
        // Meses (JList con selección múltiple)
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblMeses = new JLabel("Meses:");
        lblMeses.setFont(new Font("Arial", Font.BOLD, 14));
        controlPanel.add(lblMeses, gbc);
        
        gbc.gridx = 1;
        String[] nombresMeses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        listMeses = new JList<>(nombresMeses);
        listMeses.setFont(new Font("Arial", Font.PLAIN, 14));
        listMeses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listMeses.setVisibleRowCount(4);
        JScrollPane scrollMeses = new JScrollPane(listMeses);
        scrollMeses.setOpaque(false);
        scrollMeses.getViewport().setOpaque(false);
        controlPanel.add(scrollMeses, gbc);
        
        // Botones
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);
        
        btnFiltrar = createStyledButton("Filtrar", new Color(70, 130, 180), e -> aplicarFiltros());
        btnGenerarPDF = createStyledButton("Generar PDF", new Color(46, 125, 50), this::generarPDF);
        
        buttonPanel.add(btnFiltrar);
        buttonPanel.add(btnGenerarPDF);
        
        controlPanel.add(buttonPanel, gbc);
        
        // Tabla de reportes
        reportesTable = new JTable();
        configureTable(reportesTable);
        
        JScrollPane scrollPane = new JScrollPane(reportesTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
        
        // Listener para cambiar reporte
        cbTipoReporte.addActionListener(e -> aplicarFiltros());
        cbAnio.addActionListener(e -> aplicarFiltros());
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
        button.setPreferredSize(new Dimension(120, 40));
        button.addActionListener(listener);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(MouseEvent e) { button.setCursor(Cursor.getDefaultCursor()); }
        });

        return button;
    }
    
    private void aplicarFiltros() {
        String selectedReport = (String) cbTipoReporte.getSelectedItem();
        Integer anio = (Integer) cbAnio.getSelectedItem();
        List<Integer> mesesSeleccionados = getMesesSeleccionados();
        
        loadReportesData(selectedReport, anio, mesesSeleccionados);
    }
    
    private List<Integer> getMesesSeleccionados() {
        List<Integer> meses = new ArrayList<>();
        int[] indices = listMeses.getSelectedIndices();
        
        if (indices.length == 0) {
            for (int i = 0; i < listMeses.getModel().getSize(); i++) {
                meses.add(i + 1);
            }
        } else {
            for (int index : indices) {
                meses.add(index + 1);
            }
        }
        
        return meses;
    }
    
    private void loadReportesData(String vista, Integer anio, List<Integer> meses) {
    // Verificar si la vista existe
    if (!viewExists(vista)) {
        JOptionPane.showMessageDialog(this, 
            "La vista '" + vista + "' no existe en la base de datos",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
        StringBuilder query = new StringBuilder("SELECT * FROM " + vista);
        List<Object> params = new ArrayList<>();
        
        if (anio != null && meses != null && !meses.isEmpty()) {
            query.append(" WHERE ");
            
            if (vista.equals("Reporte_Laboratorios")) {
                query.append("YEAR(fecha_reserva) = ? AND MONTH(fecha_reserva) IN (");
            } else if (vista.equals("Reporte_Mantenimiento")) {
                query.append("YEAR(fecha_inicio) = ? AND MONTH(fecha_inicio) IN (");
            } else if (vista.equals("Reporte_Dispositivos_Baja")) {
                query.append("YEAR(fecha_baja) = ? AND MONTH(fecha_baja) IN (");
            }
            
            for (int i = 0; i < meses.size(); i++) {
                if (i > 0) query.append(",");
                query.append("?");
            }
            query.append(")");
            
            params.add(anio);
            params.addAll(meses);
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            DefaultTableModel model = new DefaultTableModel();
            
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i-1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            
            reportesTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar reporte: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean viewExists(String viewName) {
    String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS " +
                  "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setString(1, viewName);
        ResultSet rs = stmt.executeQuery();
        return rs.next();
        
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    
    public static void createViewsIfNotExist() {
    String[] viewCreationQueries = {
        "CREATE OR REPLACE VIEW Reporte_Laboratorios AS " +
        "SELECT l.Id_Laboratorio, l.nombre AS Nombre_Laboratorio, l.capacidad, " +
        "l.estado AS Estado_Laboratorio, r.fecha_reserva, r.hora_inicio, " +
        "r.hora_fin, r.tipo_de_prestamo, u.nombre AS Nombre_Usuario, " +
        "u.username AS Usuario_Reserva FROM Laboratorios l " +
        "LEFT JOIN Reservas r ON l.Id_Laboratorio = r.Nro_Laboratorio " +
        "LEFT JOIN Usuarios u ON r.id_usuario = u.id_usuario",
        
        "CREATE OR REPLACE VIEW Reporte_Mantenimiento AS " +
        "SELECT m.Id_Mantenimiento, m.nombre AS Nombre_Mantenimiento, " +
        "m.tipo AS Tipo_Mantenimiento, m.fecha_inicio, m.fecha_fin, " +
        "m.descripcion, u.nombre AS Responsable, " +
        "u.username AS Usuario_Responsable FROM Mantenimiento m " +
        "LEFT JOIN Usuarios u ON m.Id_Usuario_Responsable = u.id_usuario",
        
        "CREATE OR REPLACE VIEW Reporte_Dispositivos_Baja AS " +
        "SELECT db.Id_Baja, db.Id_Equipo, db.N_Objeto, " +
        "CASE WHEN db.Id_Equipo IS NOT NULL THEN CONCAT(e.Marca, ' ', e.Modelo) " +
        "WHEN db.N_Objeto IS NOT NULL THEN ma.nombre_objeto ELSE 'Desconocido' END AS Descripcion, " +
        "db.fecha_baja, db.motivo, db.descripcion_motivo, u.nombre AS Autorizado_Por, " +
        "u.rol AS Rol_Autorizador, CASE WHEN db.Id_Equipo IS NOT NULL THEN 'Equipo' " +
        "WHEN db.N_Objeto IS NOT NULL THEN 'Material' ELSE 'Desconocido' END AS Tipo_Elemento " +
        "FROM Dispositivos_Baja db LEFT JOIN Equipos e ON db.Id_Equipo = e.Id_Equipo " +
        "LEFT JOIN Material_Adicional ma ON db.N_Objeto = ma.N_Objeto " +
        "LEFT JOIN Usuarios u ON db.autorizado_por = u.id_usuario"
    };
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        for (String query : viewCreationQueries) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(query);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    private void generarPDF(ActionEvent evt) {
        String selectedReport = (String) cbTipoReporte.getSelectedItem();
        String tituloReporte = selectedReport.replace("_", " ");
        Integer anio = (Integer) cbAnio.getSelectedItem();
        List<Integer> meses = getMesesSeleccionados();

        StringBuilder subtitulo = new StringBuilder();
        subtitulo.append("Año: ").append(anio);

        if (meses.size() < 12) {
            subtitulo.append(" - Meses: ");
            for (int i = 0; i < meses.size(); i++) {
                if (i > 0) subtitulo.append(", ");
                subtitulo.append(Month.of(meses.get(i)).toString());
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte PDF");
        fileChooser.setSelectedFile(new File(tituloReporte + "_" + anio + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                com.itextpdf.text.Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

                Paragraph title = new Paragraph(tituloReporte, titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                Paragraph subTitle = new Paragraph(subtitulo.toString(), subTitleFont);
                subTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(subTitle);

                document.add(new Paragraph("\n"));

                PdfPTable pdfTable = new PdfPTable(reportesTable.getColumnCount());
                pdfTable.setWidthPercentage(100);

                for (int i = 0; i < reportesTable.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(reportesTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    pdfTable.addCell(cell);
                }

                for (int row = 0; row < reportesTable.getRowCount(); row++) {
                    for (int col = 0; col < reportesTable.getColumnCount(); col++) {
                        Object value = reportesTable.getValueAt(row, col);
                        pdfTable.addCell(new Phrase(value != null ? value.toString() : "", cellFont));
                    }
                }

                document.add(pdfTable);
                document.close();

                JOptionPane.showMessageDialog(this, "Reporte generado exitosamente en:\n" + file.getAbsolutePath(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al generar PDF: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onDatabaseChanged(String tableChanged) {
        aplicarFiltros();
    }
}