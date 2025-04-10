package views;

import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import models.DatabaseConnection;
import views.panels.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private DashboardPanel dashboardPanel;
    private LabsPanel labsPanel;
    private CalendarPanel calendarPanel;
    private ReservacionPanel reservacionPanel;
    private MantenimientoPanel mantenimientoPanel;
    private ReportesPanel reportesPanel;
    private UsuarioPanel usuarioPanel;
    
    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        Font largerFont = new Font("Arial", Font.PLAIN, 14);
        UIManager.put("Button.font", largerFont);
        UIManager.put("Label.font", largerFont);
        UIManager.put("TextField.font", largerFont);
        UIManager.put("ComboBox.font", largerFont);
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuOpciones = new JMenu("Opciones");
        menuOpciones.setFont(new Font("Arial", Font.BOLD, 14));
        
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuOpciones.add(itemSalir);
        
        JMenu menuReportes = new JMenu("Reportes");
        menuReportes.setFont(new Font("Arial", Font.BOLD, 14));
        JMenuItem itemReportes = new JMenuItem("Ver Reportes");
        itemReportes.addActionListener(e -> showPanel("reportes"));
        menuReportes.add(itemReportes);
        
        JMenu menuUsuario = new JMenu("Usuario");
        menuUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        JMenuItem itemPerfil = new JMenuItem("Mi Perfil");
        itemPerfil.addActionListener(e -> showPanel("usuario"));
        menuUsuario.add(itemPerfil);
        
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemCerrarSesion.addActionListener(this::cerrarSesion);
        menuUsuario.add(itemCerrarSesion);
        
        menuBar.add(menuOpciones);
        menuBar.add(menuReportes);
        menuBar.add(menuUsuario);
        setJMenuBar(menuBar);
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnDashboard = new JButton("Inicio");
        btnDashboard.addActionListener(e -> showPanel("dashboard"));
        toolBar.add(btnDashboard);
        
        JButton btnLabs = new JButton("Laboratorios");
        btnLabs.addActionListener(e -> showPanel("labs"));
        toolBar.add(btnLabs);
        
        JButton btnCalendar = new JButton("Calendario");
        btnCalendar.addActionListener(e -> showPanel("calendar"));
        toolBar.add(btnCalendar);
        
        JButton btnReservations = new JButton("Reservas");
        btnReservations.addActionListener(e -> showPanel("reservations"));
        toolBar.add(btnReservations);
        
        JButton btnMaintenance = new JButton("Mantenimiento");
        btnMaintenance.addActionListener(e -> showPanel("maintenance"));
        toolBar.add(btnMaintenance);
        
        add(toolBar, BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Inicializar todos los paneles con el usuario actual
        dashboardPanel = new DashboardPanel(currentUser);
        labsPanel = new LabsPanel();
        calendarPanel = new CalendarPanel(currentUser);
        reservacionPanel = new ReservacionPanel(currentUser);
        mantenimientoPanel = new MantenimientoPanel(currentUser);
        reportesPanel = new ReportesPanel();
        usuarioPanel = new UsuarioPanel(currentUser, this);
        
        mainPanel.add("dashboard", dashboardPanel);
        mainPanel.add("labs", labsPanel);
        mainPanel.add("calendar", calendarPanel);
        mainPanel.add("reservations", reservacionPanel);
        mainPanel.add("maintenance", mantenimientoPanel);
        mainPanel.add("reportes", reportesPanel);
        mainPanel.add("usuario", usuarioPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        showPanel("dashboard");
    }
    
    private void showPanel(String panelName) {
        switch (panelName) {
            case "dashboard":
                dashboardPanel = new DashboardPanel(currentUser);
                break;
            case "labs":
                labsPanel = new LabsPanel();
                break;
            case "calendar":
                calendarPanel = new CalendarPanel(currentUser);
                break;
            case "reservations":
                reservacionPanel = new ReservacionPanel(currentUser);
                break;
            case "maintenance":
                mantenimientoPanel = new MantenimientoPanel(currentUser);
                break;
            case "usuario":
                usuarioPanel = new UsuarioPanel(currentUser, this);
                break;
        }
        
        cardLayout.show(mainPanel, panelName);
    }
    
    @Override
    public void dispose() {
        // Limpiar listeners correctamente
        if (dashboardPanel instanceof DatabaseConnection.DatabaseChangeListener) {
            DatabaseConnection.removeListener((DatabaseConnection.DatabaseChangeListener) dashboardPanel);
        }
        if (labsPanel != null) {
            DatabaseConnection.removeListener(labsPanel);
        }
        if (calendarPanel != null) {
            DatabaseConnection.removeListener(calendarPanel);
        }
        if (reservacionPanel != null) {
            DatabaseConnection.removeListener(reservacionPanel);
        }
        if (mantenimientoPanel != null) {
            DatabaseConnection.removeListener((DatabaseConnection.DatabaseChangeListener) mantenimientoPanel);
        }
        if (reportesPanel instanceof DatabaseConnection.DatabaseChangeListener) {
            DatabaseConnection.removeListener((DatabaseConnection.DatabaseChangeListener) reportesPanel);
        }
        if (usuarioPanel instanceof DatabaseConnection.DatabaseChangeListener) {
            DatabaseConnection.removeListener((DatabaseConnection.DatabaseChangeListener) usuarioPanel);
        }
        
        super.dispose();
    }
    
    private void cerrarSesion(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Cierre de Sesión", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                new views.LoginForm().setVisible(true);
            });
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        usuarioPanel.setUser(user);
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());
    }
}
