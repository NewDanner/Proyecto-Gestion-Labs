/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Andrei
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import models.DatabaseConnection;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class CalendarPanel extends JPanel {
    private JComboBox<String> labSelector;
    private JPanel calendarPanel;
    private JLabel weekLabel;
    private LocalDate currentWeek;
    private JButton btnReservar;
    
    // Mapeo de días de la semana en español
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
    
    // Bloques horarios según especificación
    private final String[][] timeSlots = {
        {"07:30 - 09:00", "09:15 - 10:45", "11:00 - 12:30"},
        {"13:30 - 15:00", "15:15 - 16:45", "16:45 - 18:15"},
        {"18:30 - 20:00", "20:15 - 21:45", "21:45 - 22:30"}
    };
    
    // Variables para selección de préstamo
    private LocalDate selectedDate;
    private String selectedTimeSlot;
    private int selectedLabId;
    
    public CalendarPanel() {
        initComponents();
        loadLabs();
        updateWeekDisplay();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panel superior con controles
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // Panel para selección de laboratorio
        JPanel labControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        labSelector = new JComboBox<>();
        labSelector.addActionListener(e -> updateWeekDisplay());
        labControlPanel.add(new JLabel("Seleccionar Laboratorio:"));
        labControlPanel.add(labSelector);
        controlPanel.add(labControlPanel);
        
        // Panel para navegación semanal
        JPanel weekControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        currentWeek = LocalDate.now();
        
        JButton btnPrevWeek = new JButton("< Semana Anterior");
        btnPrevWeek.addActionListener(e -> {
            currentWeek = currentWeek.minusWeeks(1);
            updateWeekDisplay();
        });
        weekControlPanel.add(btnPrevWeek);
        
        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 14));
        weekControlPanel.add(weekLabel);
        
        JButton btnNextWeek = new JButton("Semana Siguiente >");
        btnNextWeek.addActionListener(e -> {
            currentWeek = currentWeek.plusWeeks(1);
            updateWeekDisplay();
        });
        weekControlPanel.add(btnNextWeek);
        
        controlPanel.add(weekControlPanel);
        add(controlPanel, BorderLayout.NORTH);
        
        // Panel del calendario
        calendarPanel = new JPanel(new GridLayout(0, 8)); // 8 columnas (días + horas)
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel inferior con botón de reserva
        JPanel bottomPanel = new JPanel(new BorderLayout());
        btnReservar = new JButton("Reservar Laboratorio");
        btnReservar.setEnabled(false);
        btnReservar.addActionListener(this::reservarAction);
        bottomPanel.add(btnReservar, BorderLayout.EAST);
        
        add(new JScrollPane(calendarPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createTimeSlotLabel(String timeSlot) {
        JLabel label = new JLabel(timeSlot, SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }
    
    private void updateWeekDisplay() {
        calendarPanel.removeAll();
        btnReservar.setEnabled(false);
        
        // Verificar si hay un laboratorio seleccionado
        if (labSelector.getSelectedItem() == null) {
            calendarPanel.add(new JLabel("Seleccione un laboratorio para ver su disponibilidad", SwingConstants.CENTER));
            calendarPanel.revalidate();
            calendarPanel.repaint();
            return;
        }
        
        // Obtener lunes de la semana actual
        LocalDate monday = currentWeek.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        
        // Configurar título de la semana
        String labName = (String) labSelector.getSelectedItem();
        weekLabel.setText(labName + " - Semana del " + 
                         monday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                         " al " + sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Agregar encabezados de columnas (días)
        calendarPanel.add(new JLabel("Horario", SwingConstants.CENTER));
        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            String nombreDia = DIAS_ESPANOL.get(day.getDayOfWeek());
            JLabel dayLabel = new JLabel(
                "<html><center>" + 
                nombreDia + 
                "<br>" + day.format(DateTimeFormatter.ofPattern("dd/MM")) + 
                "</center></html>", 
                SwingConstants.CENTER
            );
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            calendarPanel.add(dayLabel);
        }
        
        // Agregar bloques horarios
        for (String[] timeBlock : timeSlots) {
            for (String timeSlot : timeBlock) {
                calendarPanel.add(createTimeSlotLabel(timeSlot));
                
                for (int day = 0; day < 7; day++) {
                    LocalDate date = monday.plusDays(day);
                    String[] times = timeSlot.split(" - ");
                    Time startTime = Time.valueOf(times[0] + ":00");
                    Time endTime = Time.valueOf(times[1] + ":00");
                    
                    JPanel cell = createCalendarCell(date, timeSlot, startTime, endTime);
                    calendarPanel.add(cell);
                }
            }
            
            // Agregar separador entre bloques
            calendarPanel.add(new JLabel(""));
            for (int i = 0; i < 7; i++) {
                calendarPanel.add(new JLabel(""));
            }
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
    
    private JPanel createCalendarCell(LocalDate date, String timeSlot, Time startTime, Time endTime) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        String status = getLabStatus(date, startTime, endTime);
        JButton statusButton = new JButton(status);
        statusButton.setOpaque(true);
        statusButton.setContentAreaFilled(true);
        statusButton.setBorderPainted(false);
        statusButton.addActionListener(e -> selectTimeSlot(date, timeSlot));
        
        // Configurar colores según estado
        switch (status) {
            case "Libre":
                statusButton.setBackground(new Color(144, 238, 144)); // Verde claro
                statusButton.setToolTipText("Disponible para reserva");
                break;
            case "Ocupado":
                statusButton.setBackground(new Color(255, 99, 71)); // Rojo tomate
                statusButton.setToolTipText("Horario ocupado");
                break;
            case "Mantenimiento":
                statusButton.setBackground(new Color(255, 255, 0)); // Amarillo
                statusButton.setToolTipText("En mantenimiento");
                break;
            default:
                statusButton.setBackground(Color.WHITE);
        }
        
        cell.add(statusButton, BorderLayout.CENTER);
        return cell;
    }
    
    private void selectTimeSlot(LocalDate date, String timeSlot) {
        selectedDate = date;
        selectedTimeSlot = timeSlot;
        selectedLabId = getSelectedLabId();
        
        if (selectedLabId != -1) {
            String status = getLabStatus(date, 
                Time.valueOf(timeSlot.split(" - ")[0] + ":00"),
                Time.valueOf(timeSlot.split(" - ")[1] + ":00"));
            
            if ("Libre".equals(status)) {
                btnReservar.setEnabled(true);
                String labName = (String) labSelector.getSelectedItem();
                JOptionPane.showMessageDialog(this, 
                    "<html><b>Reserva seleccionada:</b><br>" +
                    "Laboratorio: " + labName + "<br>" +
                    "Fecha: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
                    "Horario: " + timeSlot + "</html>",
                    "Confirmar Reserva", JOptionPane.INFORMATION_MESSAGE);
            } else {
                btnReservar.setEnabled(false);
                JOptionPane.showMessageDialog(this, 
                    "Este horario no está disponible para reserva", 
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void reservarAction(ActionEvent e) {
        if (selectedDate == null || selectedTimeSlot == null || selectedLabId == -1) {
            JOptionPane.showMessageDialog(this, "No hay horario seleccionado", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] tiempos = selectedTimeSlot.split(" - ");
        String horaInicio = tiempos[0];
        String horaFin = tiempos[1];
        String labName = (String) labSelector.getSelectedItem();
        
        // Mostrar diálogo de confirmación
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>¿Confirmar reserva?</b><br><br>" +
            "<b>Laboratorio:</b> " + labName + "<br>" +
            "<b>Fecha:</b> " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
            "<b>Horario:</b> " + selectedTimeSlot + "</html>",
            "Confirmar Reserva", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                crearPrestamo(selectedLabId, selectedDate, horaInicio, horaFin);
                JOptionPane.showMessageDialog(this, 
                    "<html><b>Reserva creada exitosamente</b><br><br>" +
                    "Laboratorio: " + labName + "<br>" +
                    "Fecha: " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "<br>" +
                    "Horario: " + selectedTimeSlot + "</html>");
                updateWeekDisplay();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "<html><b>Error al crear reserva:</b><br>" + 
                    ex.getMessage() + "</html>",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void crearPrestamo(int labId, LocalDate fecha, String horaInicio, String horaFin) throws SQLException {
        String query = "INSERT INTO Prestamo (Nro_Laboratorio, tipo_de_prestamo, fecha_reserva, " +
                      "hora_inicio, hora_fin, estado, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            stmt.setString(2, "clase"); // Tipo de préstamo
            stmt.setDate(3, Date.valueOf(fecha));
            stmt.setTime(4, Time.valueOf(horaInicio + ":00"));
            stmt.setTime(5, Time.valueOf(horaFin + ":00"));
            stmt.setString(6, "confirmado"); // Estado
            stmt.setInt(7, 1); // ID de usuario (deberías obtener el real del usuario logueado)
            
            stmt.executeUpdate();
        }
    }
    
    private String getLabStatus(LocalDate date, Time startTime, Time endTime) {
        int labId = getSelectedLabId();
        if (labId == -1) return "N/A";

        // Consulta para verificar disponibilidad
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
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Ocupado";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }

        // Consulta para verificar mantenimiento
        query = "SELECT estado FROM Laboratorios WHERE Id_Laboratorio = ? AND estado = 'en_mantenimiento'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, labId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Mantenimiento";
            }
            
            return "Libre";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }
    }
    
    private void loadLabs() {
        String query = "SELECT nombre FROM Laboratorios ORDER BY nombre";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                model.addElement(rs.getString("nombre"));
            }
            
            labSelector.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "<html><b>Error al cargar laboratorios:</b><br>" + 
                e.getMessage() + "</html>",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private int getSelectedLabId() {
        String selected = (String) labSelector.getSelectedItem();
        if (selected == null) return -1;
        
        String query = "SELECT Id_Laboratorio FROM Laboratorios WHERE nombre = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, selected);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Id_Laboratorio");
            }
            return -1;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}