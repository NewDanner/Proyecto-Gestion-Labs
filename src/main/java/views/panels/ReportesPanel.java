package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import models.DatabaseConnection;
import models.DatabaseConnection.DatabaseChangeListener;
import models.User;

public class ReportesPanel extends JPanel implements DatabaseChangeListener {
    private JTable reportesTable;
    private JComboBox<String> cbTipoReporte;
    private JComboBox<Integer> cbAnio;
    private JList<String> listMeses;
    private JButton btnGenerarPDF;
    private JButton btnGenerarGraficos;
    private JButton btnFiltrar;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    
    private static final String[] LABORATORIOS_GRAFICOS = {
        "Uso por mes", 
        "Horarios ocupados", 
        "Estados laboratorios"
    };
    
    private static final String[] MANTENIMIENTO_GRAFICOS = {
        "Tipos de mantenimiento",
        "Duración mantenimiento",
        "Mantenimientos por responsable",
        "Estados de mantenimiento"
    };
    
    private static final String[] BAJAS_GRAFICOS = {
        "Motivos de baja",
        "Bajas por mes"
    };

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
        lblTipoReporte.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        controlPanel.add(lblTipoReporte, gbc);
        
        gbc.gridx = 1;
        cbTipoReporte = new JComboBox<>(new String[]{
            "Reporte_Laboratorios", 
            "Reporte_Mantenimiento", 
            "Reporte_Dispositivos_Baja"
        });
        cbTipoReporte.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        controlPanel.add(cbTipoReporte, gbc);
        
        // Año
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        controlPanel.add(lblAnio, gbc);
        
        gbc.gridx = 1;
        cbAnio = new JComboBox<>();
        cbAnio.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        int anioActual = LocalDate.now().getYear();
        for (int i = anioActual - 5; i <= anioActual + 1; i++) {
            cbAnio.addItem(i);
        }
        cbAnio.setSelectedItem(anioActual);
        controlPanel.add(cbAnio, gbc);
        
        // Meses
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblMeses = new JLabel("Meses:");
        lblMeses.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        controlPanel.add(lblMeses, gbc);
        
        gbc.gridx = 1;
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                         "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        listMeses = new JList<>(meses);
        listMeses.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
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
        btnGenerarGraficos = createStyledButton("Generar Gráficos", new Color(128, 0, 128), this::mostrarDialogoGraficos);
        
        buttonPanel.add(btnFiltrar);
        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnGenerarGraficos);
        
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
        
        // Listeners
        cbTipoReporte.addActionListener(e -> aplicarFiltros());
        cbAnio.addActionListener(e -> aplicarFiltros());
    }

    private void configureTable(JTable table) {
        table.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setGridColor(new Color(200, 200, 200));

        JTableHeader header = table.getTableHeader();
        header.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        
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

        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(150, 40));
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

    private void mostrarDialogoGraficos(ActionEvent e) {
        String reporte = (String) cbTipoReporte.getSelectedItem();
        String[] opciones = obtenerOpcionesGraficos(reporte);
        
        if (opciones.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay gráficos disponibles para este reporte", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> listaGraficos = new JList<>(opciones);
        listaGraficos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaGraficos);
        
        panel.add(new JLabel("Seleccione los gráficos a visualizar:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Seleccionar Gráficos", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            generarGraficosSeleccionados(reporte, listaGraficos.getSelectedValuesList());
        }
    }

    private String[] obtenerOpcionesGraficos(String reporte) {
        switch (reporte) {
            case "Reporte_Laboratorios":
                return LABORATORIOS_GRAFICOS;
            case "Reporte_Mantenimiento":
                return MANTENIMIENTO_GRAFICOS;
            case "Reporte_Dispositivos_Baja":
                return BAJAS_GRAFICOS;
            default:
                return new String[0];
        }
    }

    private void generarGraficosSeleccionados(String reporte, List<String> seleccionados) {
        int anio = (int) cbAnio.getSelectedItem();
        
        for (String grafico : seleccionados) {
            switch (reporte) {
                case "Reporte_Laboratorios":
                    generarGraficoLaboratorio(grafico, anio);
                    break;
                case "Reporte_Mantenimiento":
                    generarGraficoMantenimiento(grafico, anio);
                    break;
                case "Reporte_Dispositivos_Baja":
                    generarGraficoBaja(grafico, anio);
                    break;
            }
        }
    }

    private void generarGraficoLaboratorio(String grafico, int anio) {
        switch (grafico) {
            case "Uso por mes":
                ChartGenerator.generarGraficoUsoLaboratoriosPorMes(anio);
                break;
            case "Horarios ocupados":
                ChartGenerator.generarGraficoHorariosOcupados();
                break;
            case "Estados laboratorios":
                ChartGenerator.generarGraficoEstadosLaboratorios();
                break;
        }
    }

    private void generarGraficoMantenimiento(String grafico, int anio) {
        switch (grafico) {
            case "Tipos de mantenimiento":
                ChartGenerator.generarGraficoTiposMantenimiento();
                break;
            case "Duración mantenimiento":
                ChartGenerator.generarGraficoDuracionMantenimiento();
                break;
            case "Mantenimientos por responsable":
                ChartGenerator.generarGraficoMantenimientosPorResponsable();
                break;
            case "Estados de mantenimiento":
                ChartGenerator.generarGraficoEstadosMantenimiento();
                break;
        }
    }

    private void generarGraficoBaja(String grafico, int anio) {
        switch (grafico) {
            case "Motivos de baja":
                ChartGenerator.generarGraficoMotivosBaja();
                break;
            case "Bajas por mes":
                ChartGenerator.generarGraficoBajasPorMes(anio);
                break;
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

        // Configuración avanzada con checkboxes
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("CONFIGURACIÓN AVANZADA"));

        // Obtener opciones de gráficos según el reporte
        String[] opcionesGraficos = obtenerOpcionesGraficos(selectedReport);
        
        // Crear checkboxes para cada gráfico disponible
        JCheckBox[] checkboxes = new JCheckBox[opcionesGraficos.length];
        for (int i = 0; i < opcionesGraficos.length; i++) {
            checkboxes[i] = new JCheckBox(opcionesGraficos[i]);
            checkboxes[i].setSelected(true); // Seleccionados por defecto
            configPanel.add(checkboxes[i]);
            configPanel.add(Box.createVerticalStrut(5));
        }

        // Checkbox para incluir tabla de datos
        JCheckBox cbIncluirTabla = new JCheckBox("Incluir tabla de datos");
        cbIncluirTabla.setSelected(true);
        configPanel.add(Box.createVerticalStrut(10));
        configPanel.add(cbIncluirTabla);

        int result = JOptionPane.showConfirmDialog(
            this, 
            configPanel, 
            "Seleccione qué incluir en el PDF", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return; // El usuario canceló
        }

        // Obtener qué gráficos incluir
        List<String> graficosSeleccionados = new ArrayList<>();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].isSelected()) {
                graficosSeleccionados.add(opcionesGraficos[i]);
            }
        }

        boolean incluirTabla = cbIncluirTabla.isSelected();

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

                // Título del reporte
                Paragraph title = new Paragraph(tituloReporte, titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                // Subtítulo con filtros aplicados
                Paragraph subTitle = new Paragraph(subtitulo.toString(), subTitleFont);
                subTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(subTitle);

                document.add(new Paragraph("\n"));

                // Incluir tabla de datos si está seleccionado
                if (incluirTabla) {
                    com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                    com.itextpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

                    PdfPTable pdfTable = new PdfPTable(reportesTable.getColumnCount());
                    pdfTable.setWidthPercentage(100);

                    // Encabezados de la tabla
                    for (int i = 0; i < reportesTable.getColumnCount(); i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(reportesTable.getColumnName(i), headerFont));
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        pdfTable.addCell(cell);
                    }

                    // Datos de la tabla
                    for (int row = 0; row < reportesTable.getRowCount(); row++) {
                        for (int col = 0; col < reportesTable.getColumnCount(); col++) {
                            Object value = reportesTable.getValueAt(row, col);
                            pdfTable.addCell(new Phrase(value != null ? value.toString() : "", cellFont));
                        }
                    }

                    document.add(pdfTable);
                }

                // Incluir gráficos seleccionados
                if (!graficosSeleccionados.isEmpty()) {
                    for (String grafico : graficosSeleccionados) {
                        document.newPage();
                        com.itextpdf.text.Image imgGrafico = null;
                        
                        switch (selectedReport) {
                            case "Reporte_Laboratorios":
                                switch (grafico) {
                                    case "Uso por mes":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Uso por mes", anio);
                                        break;
                                    case "Horarios ocupados":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Horarios ocupados", anio);
                                        break;
                                    case "Estados laboratorios":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Estado laboratorios", anio);
                                        break;
                                }
                                break;
                                
                            case "Reporte_Mantenimiento":
                                switch (grafico) {
                                    case "Tipos de mantenimiento":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Tipos de mantenimiento", anio);
                                        break;
                                    case "Duración mantenimiento":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Duración mantenimiento", anio);
                                        break;
                                    case "Mantenimientos por responsable":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Mantenimientos por responsable", anio);
                                        break;
                                    case "Estados de mantenimiento":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Estados de mantenimiento", anio);
                                        break;
                                }
                                break;
                                
                            case "Reporte_Dispositivos_Baja":
                                switch (grafico) {
                                    case "Motivos de baja":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Motivos de baja", anio);
                                        break;
                                    case "Bajas por mes":
                                        imgGrafico = ChartGenerator.generarImagenParaPDF("Bajas por mes", anio);
                                        break;
                                }
                                break;
                        }
                        
                        if (imgGrafico != null) {
                            document.add(imgGrafico);
                        }
                    }
                }
                
                document.close();
                JOptionPane.showMessageDialog(this, "Reporte generado exitosamente en:\n" + file.getAbsolutePath(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onDatabaseChanged(String tableChanged) {
        aplicarFiltros();
    }
}