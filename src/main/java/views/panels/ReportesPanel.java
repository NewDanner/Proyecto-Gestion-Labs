package views.panels;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

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
    private JButton btnConfigHeader;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);

    private ReportHeaderConfig headerConfig;

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

    private static final String[] TRIGGERS_GRAFICOS = {
        "Operaciones por tabla",
        "Operaciones por tipo",
        "Transacciones por mes"
    };

    public ReportesPanel(User user) {
        headerConfig = new ReportHeaderConfig(user.getId());
        headerConfig.setUserName(user.getNombre() + " " + user.getPrimerApellido());
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
            "Reporte_Dispositivos_Baja",
            "Reporte_Triggers"
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
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
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
        btnConfigHeader = createStyledButton("Config. Encabezado", new Color(169, 169, 169), e -> configurarEncabezado());

        buttonPanel.add(btnFiltrar);
        buttonPanel.add(btnGenerarPDF);
        buttonPanel.add(btnGenerarGraficos);
        buttonPanel.add(btnConfigHeader);

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

    private void configurarEncabezado() {
        JDialog configDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Configurar Encabezado", true);
        configDialog.setLayout(new BorderLayout());
        configDialog.setSize(500, 400);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Campos del formulario
        JTextField txtCompanyName = new JTextField(headerConfig.getCompanyName());
        JTextField txtCompanyAddress = new JTextField(headerConfig.getCompanyAddress());
        JTextField txtUserName = new JTextField(headerConfig.getUserName());
        txtUserName.setEditable(false); // Hacer el campo no editable

        JLabel lblLogoPreview = new JLabel("(Sin logo)");
        lblLogoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogoPreview.setPreferredSize(new Dimension(150, 100));

        if (headerConfig.hasLogo()) {
            lblLogoPreview.setIcon(new ImageIcon(headerConfig.getLogo()));
            lblLogoPreview.setText("");
        }

        JButton btnSelectLogo = new JButton("Seleccionar Logo");
        btnSelectLogo.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
            if (fileChooser.showOpenDialog(configDialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.Image originalImage = ImageIO.read(fileChooser.getSelectedFile());
                    java.awt.Image scaledImage = originalImage.getScaledInstance(150, -1, java.awt.Image.SCALE_SMOOTH);
                    headerConfig.setLogo(scaledImage);
                    lblLogoPreview.setIcon(new ImageIcon(scaledImage));
                    lblLogoPreview.setText("");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(configDialog,
                            "Error al cargar la imagen: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Agregar componentes al formulario
        formPanel.add(new JLabel("Nombre de Empresa:"));
        formPanel.add(txtCompanyName);
        formPanel.add(new JLabel("Dirección de Empresa:"));
        formPanel.add(txtCompanyAddress);
        formPanel.add(new JLabel("Nombre de Usuario:"));
        formPanel.add(txtUserName);
        formPanel.add(new JLabel("Logo:"));
        formPanel.add(lblLogoPreview);
        formPanel.add(new JLabel());
        formPanel.add(btnSelectLogo);

        // Botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");

        btnSave.addActionListener(e -> {
            headerConfig.setCompanyName(txtCompanyName.getText());
            headerConfig.setCompanyAddress(txtCompanyAddress.getText());
            headerConfig.setUserName(txtUserName.getText());
            headerConfig.saveToDatabase(); // Guardar en la base de datos
            configDialog.dispose();
        });

        btnCancel.addActionListener(e -> configDialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        configDialog.add(formPanel, BorderLayout.CENTER);
        configDialog.add(buttonPanel, BorderLayout.SOUTH);
        configDialog.setLocationRelativeTo(this);
        configDialog.setVisible(true);
    }

    private void configureTable(JTable table) {
        table.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setGridColor(new Color(200, 200, 200));

        JTableHeader header = table.getTableHeader();
        header.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
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
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();

                // Dibujar borde negro (offset en las 8 direcciones)
                g2d.setColor(Color.BLACK);
                g2d.drawString(getText(), x - 1, y - 1);
                g2d.drawString(getText(), x - 1, y + 1);
                g2d.drawString(getText(), x + 1, y - 1);
                g2d.drawString(getText(), x + 1, y + 1);
                g2d.drawString(getText(), x - 1, y);
                g2d.drawString(getText(), x + 1, y);
                g2d.drawString(getText(), x, y - 1);
                g2d.drawString(getText(), x, y + 1);

                // Dibujar texto blanco encima
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
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
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(Cursor.getDefaultCursor());
            }
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
            } else if (vista.equals("Reporte_Triggers")) {
                query.append("YEAR(fecha) = ? AND MONTH(fecha) IN (");
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
                    row[i - 1] = rs.getObject(i);
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
            case "Reporte_Triggers":
                return TRIGGERS_GRAFICOS;
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
                case "Reporte_Triggers":
                    generarGraficoTriggers(grafico, anio);
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

    private void generarGraficoTriggers(String grafico, int anio) {
        switch (grafico) {
            case "Operaciones por tabla":
                ChartGenerator.generarGraficoOperacionesPorTabla(anio);
                break;
            case "Operaciones por tipo":
                ChartGenerator.generarGraficoOperacionesPorTipo(anio);
                break;
            case "Transacciones por mes":
                ChartGenerator.generarGraficoTransaccionesPorMes(anio);
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

        String[] opcionesGraficos = obtenerOpcionesGraficos(selectedReport);

        JCheckBox[] checkboxes = new JCheckBox[opcionesGraficos.length];
        for (int i = 0; i < opcionesGraficos.length; i++) {
            checkboxes[i] = new JCheckBox(opcionesGraficos[i]);
            checkboxes[i].setSelected(true);
            configPanel.add(checkboxes[i]);
            configPanel.add(Box.createVerticalStrut(5));
        }

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
            return;
        }

        List<String> graficosSeleccionados = new ArrayList<>();
        for (int i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].isSelected()) {
                graficosSeleccionados.add(opcionesGraficos[i]);
            }
        }

        boolean incluirTabla = cbIncluirTabla.isSelected();

        // Crear archivo temporal
        File tempFile = null;
        try {
            tempFile = File.createTempFile("reporte_preview_", ".pdf");
            tempFile.deleteOnExit();

            // Generar PDF
            generarPDFCompleto(tempFile, tituloReporte, subtitulo.toString(), graficosSeleccionados, incluirTabla);

            // Mostrar vista previa
            mostrarVistaPrevia(tempFile);

        } catch (IOException | DocumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al generar PDF: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void generarPDFCompleto(File file, String tituloReporte, String subtitulo,
                                   List<String> graficosSeleccionados, boolean incluirTabla)
            throws DocumentException, IOException {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Agregar encabezado personalizado
        addCustomHeader(document);

        com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        com.itextpdf.text.Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        // Título del reporte
        Paragraph title = new Paragraph(tituloReporte, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Subtítulo con filtros aplicados
        Paragraph subTitle = new Paragraph(subtitulo, subTitleFont);
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
            String selectedReport = (String) cbTipoReporte.getSelectedItem();
            int anio = (int) cbAnio.getSelectedItem();

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
                                imgGrafico = ChartGenerator.generarImagenParaPDF("Estados laboratorios", anio);
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

                    case "Reporte_Triggers":
                        switch (grafico) {
                            case "Operaciones por tabla":
                                imgGrafico = ChartGenerator.generarImagenParaPDF("Operaciones por tabla", anio);
                                break;
                            case "Operaciones por tipo":
                                imgGrafico = ChartGenerator.generarImagenParaPDF("Operaciones por tipo", anio);
                                break;
                            case "Transacciones por mes":
                                imgGrafico = ChartGenerator.generarImagenParaPDF("Transacciones por mes", anio);
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
    }

    private void mostrarVistaPrevia(File pdfFile) {
        JDialog previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Vista Previa del Reporte", true);
        previewDialog.setLayout(new BorderLayout());
        previewDialog.setSize(900, 700);
        previewDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Panel de control con zoom
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnZoomIn = new JButton("+");
        JButton btnZoomOut = new JButton("-");
        JLabel lblZoom = new JLabel("50%");
        
        controlPanel.add(btnZoomOut);
        controlPanel.add(lblZoom);
        controlPanel.add(btnZoomIn);
        
        // Panel para el PDF con JScrollPane
        JPanel pdfPanel = new JPanel();
        pdfPanel.setLayout(new BoxLayout(pdfPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(pdfPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Contenedor principal
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Usar una variable final para el documento
        final PDDocument[] documentHolder = new PDDocument[1];
        
        try {
            documentHolder[0] = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(documentHolder[0]);
            
            // Escala inicial
            final float[] scaleHolder = {0.5f};
            
            // Función para actualizar las imágenes con nueva escala
            Runnable updatePages = () -> {
                pdfPanel.removeAll();
                
                int pageCount = Math.min(documentHolder[0].getNumberOfPages(), 10);
                for (int i = 0; i < pageCount; i++) {
                    try {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300 * scaleHolder[0]);
                        ImageIcon icon = new ImageIcon(bim);
                        JLabel pageLabel = new JLabel(icon);
                        pageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        pdfPanel.add(pageLabel);
                        
                        // Añadir separador entre páginas
                        if (i < pageCount - 1) {
                            pdfPanel.add(new JSeparator(JSeparator.HORIZONTAL));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
                lblZoom.setText(Math.round(scaleHolder[0] * 100) + "%");
                pdfPanel.revalidate();
                pdfPanel.repaint();
            };
            
            // Configurar botones de zoom
            btnZoomIn.addActionListener(e -> {
                if (scaleHolder[0] < 2.0f) {
                    scaleHolder[0] += 0.1f;
                    updatePages.run();
                }
            });
            
            btnZoomOut.addActionListener(e -> {
                if (scaleHolder[0] > 0.1f) {
                    scaleHolder[0] -= 0.1f;
                    updatePages.run();
                }
            });

            // Agregar listener para Ctrl + Rueda del ratón
            scrollPane.addMouseWheelListener(e -> {
                if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    if (e.getWheelRotation() < 0) {
                        if (scaleHolder[0] < 2.0f) {
                            scaleHolder[0] += 0.1f;
                            updatePages.run();
                        }
                    } else {
                        if (scaleHolder[0] > 0.1f) {
                            scaleHolder[0] -= 0.1f;
                            updatePages.run();
                        }
                    }
                    e.consume();
                }
            });
            
            // Mostrar páginas iniciales
            updatePages.run();

        } catch (IOException e) {
            mainPanel.add(new JLabel("<html><center>No se pudo cargar la vista previa PDF.<br>"
                    + "El reporte se generó correctamente pero no se puede previsualizar.</center></html>"),
                    BorderLayout.CENTER);
            e.printStackTrace();
        }
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar Reporte");
        JButton btnCerrar = new JButton("Cerrar");

        btnGuardar.addActionListener(e -> {
            guardarReportePermanente(pdfFile);
            previewDialog.dispose();
        });

        btnCerrar.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                previewDialog,
                "¿Desea salir de la vista previa?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                if (documentHolder[0] != null) {
                    try {
                        documentHolder[0].close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                previewDialog.dispose();
            }
        });

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCerrar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        previewDialog.add(mainPanel);
        
        previewDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (documentHolder[0] != null) {
                    try {
                        documentHolder[0].close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        previewDialog.setVisible(true);
    }

    private void guardarReportePermanente(File tempFile) {
        String selectedReport = (String) cbTipoReporte.getSelectedItem();
        String tituloReporte = selectedReport.replace("_", " ");
        int anio = (int) cbAnio.getSelectedItem();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte PDF");
        fileChooser.setSelectedFile(new File(tituloReporte + "_" + anio + ".pdf"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destino = fileChooser.getSelectedFile();
            if (!destino.getName().toLowerCase().endsWith(".pdf")) {
                destino = new File(destino.getAbsolutePath() + ".pdf");
            }

            try {
                java.nio.file.Files.copy(
                        tempFile.toPath(),
                        destino.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Reporte guardado exitosamente en:\n" + destino.getAbsolutePath(),
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al guardar el archivo: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void addCustomHeader(Document document) throws DocumentException, IOException {
        // Fuentes para el encabezado
        com.itextpdf.text.Font companyNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        com.itextpdf.text.Font companyAddressFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        com.itextpdf.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Crear tabla para el encabezado (3 columnas)
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2, 3, 2});

        // Celda del logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        if (headerConfig.hasLogo()) {
            try {
                BufferedImage bufferedLogo = headerConfig.getLogoAsBufferedImage();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedLogo, "png", baos);
                com.itextpdf.text.Image pdfLogo = com.itextpdf.text.Image.getInstance(baos.toByteArray());
                pdfLogo.scaleToFit(100, 100);
                logoCell.addElement(pdfLogo);
            } catch (Exception e) {
                System.err.println("Error al procesar logo: " + e.getMessage());
            }
        }
        headerTable.addCell(logoCell);

        // Celda información empresa (centro)
        PdfPCell companyInfoCell = new PdfPCell();
        companyInfoCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        companyInfoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph companyInfo = new Paragraph();
        companyInfo.add(new Chunk(headerConfig.getCompanyName() + "\n", companyNameFont));
        companyInfo.add(new Chunk(headerConfig.getCompanyAddress() + "\n", companyAddressFont));
        companyInfoCell.addElement(companyInfo);
        headerTable.addCell(companyInfoCell);

        // Celda fecha/usuario (derecha)
        PdfPCell dateUserCell = new PdfPCell();
        dateUserCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        dateUserCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph dateUserInfo = new Paragraph();
        dateUserInfo.add(new Chunk("Fecha: " + LocalDate.now().toString() + "\n", infoFont));
        dateUserInfo.add(new Chunk("Generado por: " + headerConfig.getUserName(), infoFont));
        dateUserCell.addElement(dateUserInfo);
        headerTable.addCell(dateUserCell);

        document.add(headerTable);

        // Línea separadora
        document.add(new Chunk(new LineSeparator()));
        document.add(Paragraph.getInstance("\n"));
    }

    @Override
    public void onDatabaseChanged(String tableChanged) {
        aplicarFiltros();
    }

    public BufferedImage getLogoAsBufferedImage() {
        if (!headerConfig.hasLogo()) return null;

        java.awt.Image logo = headerConfig.getLogo();

        if (logo instanceof BufferedImage) {
            return (BufferedImage) logo;
        }

        // Convertir Image a BufferedImage
        BufferedImage bufferedImage = new BufferedImage(
                logo.getWidth(null),
                logo.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(logo, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }
}