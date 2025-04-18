package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import models.DatabaseConnection;
import models.User;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import views.MainFrame;

public class CalendarPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JComboBox<String> labSelector;
    private JPanel calendarPanel;
    private JLabel weekLabel;
    private LocalDate currentWeek;
    private JButton btnReservar, btnAtras;
    private User currentUser;
    private JPanel selectedCell;
    
    private static final Map<DayOfWeek, String> DIAS_ESPANOL = new HashMap<>();
    static {
        DIAS_ESPANOL.put(DayOfWeek.MONDAY, "Lunes");
        DIAS_ESPANOL.put(DayOfWeek.TUESDAY, "Martes");
        DIAS_ESPANOL.put(DayOfWeek.WEDNESDAY, "Miércoles");
        DIAS_ESPANOL.put(DayOfWeek.THURSDAY, "Jueves");
        DIAS_ESPANOL.put(DayOfWeek.FRIDAY, "Viernes");
        DIAS_ESPANOL.put(DayOfWeek.SATURDAY, "Sábado");
        DIAS_ESPANOL.put(DayOfWeek.SUNDAY, "Domingo");
    }
    
    private final String[][] timeSlots = {
        {"07:30 - 09:00", "09:15 - 10:45", "11:00 - 12:30"},
        {"13:30 - 15:00", "15:15 - 16:45", "16:45 - 18:15"},
        {"18:30 - 20:00", "20:15 - 21:45", "21:45 - 22:30"}
    };
    
    private LocalDate selectedDate;
    private String selectedTimeSlot;
    private int selectedLabId;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<LocalDate, Map<String, String>> cacheEstadoCeldas = new HashMap<>();
    
    public CalendarPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadLabs();
        precargarDatosSemana(currentWeek);
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(249, 45, 168),
            0, getHeight(), new Color(255, 209, 12));

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panel superior
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Panel del calendario
        calendarPanel = new JPanel(new GridLayout(0, 8));
        calendarPanel.setOpaque(false);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Panel inferior
        JPanel bottomPanel = createBottomPanel();
        
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        controlPanel.setOpaque(false);
        
        // Panel de selección de laboratorio
        JPanel labControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        labControlPanel.setOpaque(false);
        
        labSelector = new JComboBox<>();
        labSelector.setBackground(new Color(255, 255, 255, 200));
        labSelector.setForeground(Color.BLACK);
        labSelector.addActionListener(e -> updateWeekDisplay());
        
        labControlPanel.add(new JLabel("Seleccionar Laboratorio:") {{
            setForeground(Color.WHITE);
        }});
        labControlPanel.add(labSelector);
        
        // Panel de navegación semanal
        JPanel weekControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        weekControlPanel.setOpaque(false);
        currentWeek = LocalDate.now();
        
        JButton btnPrevWeek = createStyledButton("< Semana Anterior");
        btnPrevWeek.addActionListener(e -> {
            currentWeek = currentWeek.minusWeeks(1);
            precargarDatosSemana(currentWeek);
            updateWeekDisplay();
        });
        
        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 14));
        weekLabel.setForeground(Color.WHITE);
        
        JButton btnNextWeek = createStyledButton("Semana Siguiente >");
        btnNextWeek.addActionListener(e -> {
            currentWeek = currentWeek.plusWeeks(1);
            precargarDatosSemana(currentWeek);
            updateWeekDisplay();
        });
        
        weekControlPanel.add(btnPrevWeek);
        weekControlPanel.add(weekLabel);
        weekControlPanel.add(btnNextWeek);
        
        controlPanel.add(labControlPanel);
        controlPanel.add(weekControlPanel);
        
        return controlPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        
        btnAtras = createStyledButton("Atrás");
        btnAtras.addActionListener(e -> volverAInicio());
        bottomPanel.add(btnAtras, BorderLayout.WEST);
        
        btnReservar = createStyledButton("Reservar Laboratorio");
        btnReservar.setEnabled(false);
        btnReservar.addActionListener(this::reservarAction);
        bottomPanel.add(btnReservar, BorderLayout.EAST);
        
        return bottomPanel;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Prestamo") || tableChanged.equals("Laboratorios")) {
            cacheEstadoCeldas.clear();
            SwingUtilities.invokeLater(() -> {
                precargarDatosSemana(currentWeek);
                updateWeekDisplay();
            });
        }
    }
    
    private void precargarDatosSemana(LocalDate semana) {
        executor.execute(() -> {
            LocalDate monday = semana.with(DayOfWeek.MONDAY);
            for (int i = 0; i < 7; i++) {
                LocalDate date = monday.plusDays(i);
                for (String[] timeBlock : timeSlots) {
                    for (String timeSlot : timeBlock) {
                        String[] times = timeSlot.split(" - ");
                        Time startTime = Time.valueOf(times[0] + ":00");
                        Time endTime = Time.valueOf(times[1] + ":00");
                        getLabStatus(date, startTime, endTime);
                    }
                }
            }
        });
    }
    
    private void updateWeekDisplay() {
        calendarPanel.removeAll();
        btnReservar.setEnabled(false);
        
        JLabel lblCargando = new JLabel("Cargando disponibilidad...", SwingConstants.CENTER);
        lblCargando.setForeground(Color.WHITE);
        calendarPanel.add(lblCargando);
        calendarPanel.revalidate();
        calendarPanel.repaint();

        executor.execute(() -> {
            try {
                if (labSelector.getSelectedItem() == null) {
                    SwingUtilities.invokeLater(() -> {
                        calendarPanel.removeAll();
                        JLabel lbl = new JLabel("Seleccione un laboratorio", SwingConstants.CENTER);
                        lbl.setForeground(Color.WHITE);
                        calendarPanel.add(lbl);
                        calendarPanel.revalidate();
                        calendarPanel.repaint();
                    });
                    return;
                }
                
                LocalDate monday = currentWeek.with(DayOfWeek.MONDAY);
                LocalDate sunday = monday.plusDays(6);
                
                String labName = (String) labSelector.getSelectedItem();
                String weekText = monday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                                  sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                SwingUtilities.invokeLater(() -> weekLabel.setText(weekText));

                JPanel tempPanel = new JPanel(new GridLayout(0, 8));
                tempPanel.setOpaque(false);
                
                // Encabezados de días
                tempPanel.add(new JLabel("Horario", SwingConstants.RIGHT) {{
                    setForeground(Color.WHITE);
                }});
                
                for (int i = 0; i < 7; i++) {
                    LocalDate day = monday.plusDays(i);
                    String nombreDia = DIAS_ESPANOL.get(day.getDayOfWeek());
                    JLabel dayLabel = new JLabel(
                        nombreDia + " " + day.getDayOfMonth(),
                        SwingConstants.CENTER
                    );
                    dayLabel.setForeground(Color.WHITE);
                    tempPanel.add(dayLabel);
                }
                
                // Bloques horarios
                for (String[] timeBlock : timeSlots) {
                    for (String timeSlot : timeBlock) {
                        tempPanel.add(createTimeSlotLabel(timeSlot));
                        
                        for (int day = 0; day < 7; day++) {
                            LocalDate date = monday.plusDays(day);
                            String[] times = timeSlot.split(" - ");
                            Time startTime = Time.valueOf(times[0] + ":00");
                            Time endTime = Time.valueOf(times[1] + ":00");
                            
                            String status = cacheEstadoCeldas
                                .computeIfAbsent(date, k -> new HashMap<>())
                                .computeIfAbsent(timeSlot, k -> getLabStatus(date, startTime, endTime));
                            
                            JPanel cell = createCalendarCell(date, timeSlot, status);
                            tempPanel.add(cell);
                        }
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    calendarPanel.removeAll();
                    for (Component comp : tempPanel.getComponents()) {
                        calendarPanel.add(comp);
                    }
                    calendarPanel.revalidate();
                    calendarPanel.repaint();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(CalendarPanel.this, 
                        "Error al cargar disponibilidad: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    private JLabel createTimeSlotLabel(String timeSlot) {
        JLabel label = new JLabel(timeSlot, SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        return label;
    }
    
    private JPanel createCalendarCell(LocalDate date, String timeSlot, String status) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(false);
        cell.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 150)));
        
        JButton statusButton = new JButton(status);
        statusButton.setOpaque(true);
        statusButton.setContentAreaFilled(true);
        statusButton.setBorderPainted(false);
        statusButton.setFocusPainted(false);
        
        statusButton.addActionListener(e -> handleCellSelection(cell, status, date, timeSlot));
        
        setButtonColor(statusButton, status);
        cell.add(statusButton, BorderLayout.CENTER);
        return cell;
    }
    
    private void handleCellSelection(JPanel cell, String status, LocalDate date, String timeSlot) {
        if (selectedCell != null) {
            JButton prevButton = (JButton) selectedCell.getComponent(0);
            String prevStatus = prevButton.getText();
            setButtonColor(prevButton, prevStatus);
        }
        
        if (selectedCell == cell || !"Libre".equals(status)) {
            selectedCell = null;
            selectedDate = null;
            selectedTimeSlot = null;
            btnReservar.setEnabled(false);
        } else {
            selectedCell = cell;
            selectedDate = date;
            selectedTimeSlot = timeSlot;
            selectedLabId = getSelectedLabId();
            ((JButton)cell.getComponent(0)).setBackground(new Color(173, 216, 230));
            btnReservar.setEnabled(true);
            
            // Mostrar mensaje de selección
            String labName = (String) labSelector.getSelectedItem();
            JOptionPane.showMessageDialog(this, 
                "<html><b>Reserva seleccionada:</b><br>" +
                "Laboratorio: " + labName + "<br>" +
                "Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
                "Horario: " + timeSlot + "</html>",
                "Confirmar Reserva", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void setButtonColor(JButton button, String status) {
        switch (status) {
            case "Libre":
                button.setBackground(new Color(100, 255, 100));
                button.setForeground(Color.BLACK);
                button.setToolTipText("Disponible para reserva");
                break;
            case "Ocupado":
                button.setBackground(new Color(255, 100, 100));
                button.setForeground(Color.WHITE);
                button.setToolTipText("Horario ocupado");
                break;
            case "Mantenimiento":
                button.setBackground(new Color(255, 255, 100));
                button.setForeground(Color.BLACK);
                button.setToolTipText("En mantenimiento");
                break;
            default:
                button.setBackground(Color.WHITE);
                button.setForeground(Color.BLACK);
        }
    }
    
    private void volverAInicio() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(currentUser);
            mainFrame.setVisible(true);
        });
    }
    
    private void loadLabs() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String query = "SELECT nombre FROM Laboratorios WHERE estado = 'disponible' ORDER BY nombre";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    
                    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                    while (rs.next()) {
                        model.addElement(rs.getString("nombre"));
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        labSelector.setModel(model);
                        if (model.getSize() > 0) {
                            labSelector.setSelectedIndex(0);
                        }
                    });
                }
                return null;
            }
        }.execute();
    }
    
    private String getLabStatus(LocalDate date, Time startTime, Time endTime) {
        int labId = getSelectedLabId();
        if (labId == -1) return "N/A";

        // Verificar mantenimiento primero
        if (isLabInMaintenance(labId)) {
            return "Mantenimiento";
        }

        // Verificar disponibilidad
        return isTimeSlotAvailable(labId, date, startTime, endTime) ? "Libre" : "Ocupado";
    }

    private boolean isLabInMaintenance(int labId) {
        String query = "SELECT estado FROM Laboratorios WHERE Id_Laboratorio = ? AND estado = 'en_mantenimiento'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isTimeSlotAvailable(int labId, LocalDate date, Time startTime, Time endTime) {
        String query = "SELECT estado FROM Prestamo WHERE Nro_Laboratorio = ? " +
                      "AND fecha_reserva = ? " +
                      "AND ((hora_inicio BETWEEN ? AND ?) OR " +
                      "(hora_fin BETWEEN ? AND ?) OR " +
                      "(hora_inicio <= ? AND hora_fin >= ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, startTime);
            stmt.setTime(4, endTime);
            stmt.setTime(5, startTime);
            stmt.setTime(6, endTime);
            stmt.setTime(7, startTime);
            stmt.setTime(8, endTime);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private int getSelectedLabId() {
        String selected = (String) labSelector.getSelectedItem();
        if (selected == null) return -1;
        
        String query = "SELECT Id_Laboratorio FROM Laboratorios WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, selected);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("Id_Laboratorio") : -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    private void reservarAction(ActionEvent e) {
        if (selectedDate == null || selectedTimeSlot == null || selectedLabId == -1) {
            JOptionPane.showMessageDialog(this, "No hay horario seleccionado", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Pedir materia para la reserva
        String[] opcionesMateria = {"Electrónica", "Hardware", "Redes y Telecomunicaciones"};
        String materia = (String) JOptionPane.showInputDialog(
            this,
            "Seleccione la materia:",
            "Selección de Materia",
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcionesMateria,
            opcionesMateria[0]);
        
        if (materia == null) return; // Usuario canceló
        
        // Mapear materia a formato de base de datos
        String materiaDB = materia.toLowerCase().replace(" y ", "_").replace("ó", "o");
        
        String[] tiempos = selectedTimeSlot.split(" - ");
        String horaInicio = tiempos[0];
        String horaFin = tiempos[1];
        String labName = (String) labSelector.getSelectedItem();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>¿Confirmar reserva?</b><br><br>" +
            "<b>Laboratorio:</b> " + labName + "<br>" +
            "<b>Materia:</b> " + materia + "<br>" +
            "<b>Fecha:</b> " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
            "<b>Horario:</b> " + selectedTimeSlot + "</html>",
            "Confirmar Reserva", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                crearPrestamo(selectedLabId, materiaDB, selectedDate, horaInicio, horaFin);
                JOptionPane.showMessageDialog(this, 
                    "<html><b>Reserva creada exitosamente</b><br><br>" +
                    "Laboratorio: " + labName + "<br>" +
                    "Materia: " + materia + "<br>" +
                    "Fecha: " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
                    "Horario: " + selectedTimeSlot + "</html>");
                
                volverAInicio();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "<html><b>Error al crear reserva:</b><br>" + 
                    ex.getMessage() + "</html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void crearPrestamo(int labId, String materia, LocalDate fecha, String horaInicio, String horaFin) throws SQLException {
        String query = "INSERT INTO Prestamo (Nro_Laboratorio, tipo_de_prestamo, materia, " +
                      "fecha_reserva, hora_inicio, hora_fin, estado, id_usuario) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            stmt.setString(2, "clase");
            stmt.setString(3, materia);
            stmt.setDate(4, Date.valueOf(fecha));
            stmt.setTime(5, Time.valueOf(horaInicio + ":00"));
            stmt.setTime(6, Time.valueOf(horaFin + ":00"));
            stmt.setString(7, "pendiente");
            stmt.setInt(8, currentUser.getId());
            
            stmt.executeUpdate();
            DatabaseConnection.notifyDatabaseChanged("Prestamo");
        }
    }
}