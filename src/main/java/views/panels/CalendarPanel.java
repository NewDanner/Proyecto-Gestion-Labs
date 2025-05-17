package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import models.User;
import java.awt.geom.Rectangle2D;

public class CalendarPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    public interface ReservationListener {
        void onReservationSelected(String labName, LocalDate fecha, Time horaInicio, Time horaFin);
    }
    
    private JComboBox<String> labSelector;
    private JPanel calendarPanel;
    private JLabel weekLabel;
    private LocalDate currentWeek;
    private User currentUser;
    private ReservationListener reservationListener;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    
    public CalendarPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initComponents();
        loadLabs();
        updateWeekDisplay();
    }
    
    public void setReservationListener(ReservationListener listener) {
        this.reservationListener = listener;
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
        
        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        controlPanel.setOpaque(false);
        
        JPanel labPanel = new JPanel();
        labPanel.setOpaque(false);
        JLabel labLabel = new JLabel("Laboratorio:");
        labLabel.setFont(new Font("Arial", Font.BOLD, 14));
        labPanel.add(labLabel);
        labSelector = new JComboBox<>();
        labSelector.setFont(new Font("Arial", Font.PLAIN, 14));
        labSelector.addActionListener(e -> updateWeekDisplay());
        labPanel.add(labSelector);
        
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        currentWeek = LocalDate.now();
        
        JButton btnPrev = createStyledButton("< Semana Anterior", new Color(70, 130, 180), e -> {
            currentWeek = currentWeek.minusWeeks(1);
            updateWeekDisplay();
        });
        
        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JButton btnNext = createStyledButton("Semana Siguiente >", new Color(70, 130, 180), e -> {
            currentWeek = currentWeek.plusWeeks(1);
            updateWeekDisplay();
        });
        
        navPanel.add(btnPrev);
        navPanel.add(weekLabel);
        navPanel.add(btnNext);
        
        controlPanel.add(labPanel);
        controlPanel.add(navPanel);
        
        calendarPanel = new JPanel(new GridLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
            }
        };
        calendarPanel.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
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
    
    private void loadLabs() {
        String query = "SELECT nombre FROM Laboratorios WHERE estado = 'Disponible' ORDER BY nombre";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                model.addElement(rs.getString("nombre"));
            }
            
            labSelector.setModel(model);
            if (model.getSize() > 0) {
                labSelector.setSelectedIndex(0);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar laboratorios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateWeekDisplay() {
    calendarPanel.removeAll();
    
    if (labSelector.getSelectedItem() == null) {
        JLabel noLabLabel = new JLabel("Seleccione un laboratorio", SwingConstants.CENTER);
        noLabLabel.setFont(new Font("Arial", Font.BOLD, 16));
        calendarPanel.add(noLabLabel);
        calendarPanel.revalidate();
        calendarPanel.repaint();
        return;
    }
    
    LocalDate monday = currentWeek.with(DayOfWeek.MONDAY);
    LocalDate sunday = monday.plusDays(6);
    
    weekLabel.setText(monday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " +
                     sunday.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    
    calendarPanel.add(createHeaderLabel("Horario"));
    
    // Mapeo de días de la semana a español
    String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    
    for (int i = 0; i < 7; i++) {
        LocalDate day = monday.plusDays(i);
        String nombreDia = diasSemana[i]; // Usamos el array en español
        calendarPanel.add(createHeaderLabel(nombreDia + " " + day.getDayOfMonth()));
    }
    
    String[][] timeSlots = {
        {"07:30 - 09:00", "09:15 - 10:45", "11:00 - 12:30"},
        {"13:30 - 15:00", "15:15 - 16:45", "17:00 - 18:15"},
        {"18:30 - 20:00", "20:15 - 21:45", "21:45 - 22:30"}
    };
    
    for (String[] timeBlock : timeSlots) {
        for (String timeSlot : timeBlock) {
            calendarPanel.add(createTimeLabel(timeSlot));
            
            for (int day = 0; day < 7; day++) {
                LocalDate date = monday.plusDays(day);
                String status = getStatusForTimeSlot(date, timeSlot);
                calendarPanel.add(createStatusLabel(status, date, timeSlot));
            }
        }
    }
    
    calendarPanel.revalidate();
    calendarPanel.repaint();
}
    
    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(70, 130, 180));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }
    
    private JLabel createTimeLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBackground(new Color(100, 150, 200));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }
    
    private String getStatusForTimeSlot(LocalDate date, String timeSlot) {
        String[] times = timeSlot.split(" - ");
        Time startTime = Time.valueOf(times[0] + ":00");
        Time endTime = Time.valueOf(times[1] + ":00");
        
        String labName = (String) labSelector.getSelectedItem();
        if (labName == null) return "N/A";
        
        String query = "SELECT r.estado FROM Reservas r " +
                      "JOIN Laboratorios l ON r.Nro_Laboratorio = l.Id_Laboratorio " +
                      "WHERE l.nombre = ? AND r.fecha_reserva = ? " +
                      "AND ((r.hora_inicio BETWEEN ? AND ?) OR " +
                      "(r.hora_fin BETWEEN ? AND ?) OR " +
                      "(r.hora_inicio <= ? AND r.hora_fin >= ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, labName);
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
            } else {
                return "Libre";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }
    }
    
    private JLabel createStatusLabel(String status, LocalDate date, String timeSlot) {
        JLabel label = new JLabel(status, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        
        switch (status) {
            case "Libre":
                label.setBackground(new Color(46, 125, 50)); // Verde
                label.setForeground(Color.WHITE);
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (reservationListener != null) {
                            String[] times = timeSlot.split(" - ");
                            Time horaInicio = Time.valueOf(times[0] + ":00");
                            Time horaFin = Time.valueOf(times[1] + ":00");
                            
                            reservationListener.onReservationSelected(
                                (String) labSelector.getSelectedItem(),
                                date,
                                horaInicio,
                                horaFin
                            );
                        }
                    }
                });
                break;
            case "Ocupado":
                label.setBackground(new Color(198, 40, 40)); // Rojo
                label.setForeground(Color.WHITE);
                break;
            default:
                label.setBackground(new Color(255, 235, 59)); // Amarillo
                label.setForeground(Color.BLACK);
        }
        
        return label;
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Reservas") || tableChanged.equals("Laboratorios")) {
            updateWeekDisplay();
        }
    }
}