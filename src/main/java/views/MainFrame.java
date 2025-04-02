package views;

import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import views.panels.DashboardPanel;
import views.panels.LabsPanel;
import views.panels.ReservacionPanel;
import views.panels.MantenimientoPanel;

public class MainFrame extends JFrame {
    private User currentUser;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Paneles
    private DashboardPanel dashboardPanel;
    private LabsPanel labsPanel;
    private ReservacionPanel reservacionPanel;
    private MantenimientoPanel mantenimientoPanel;
    
    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Crear el menú
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuOpciones = new JMenu("Opciones");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuOpciones.add(itemSalir);
        menuBar.add(menuOpciones);
        
        setJMenuBar(menuBar);
        
        // Crear la barra de herramientas
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnDashboard = new JButton("Inicio");
        btnDashboard.addActionListener(e -> showPanel("dashboard"));
        toolBar.add(btnDashboard);
        
        JButton btnLabs = new JButton("Laboratorios");
        btnLabs.addActionListener(e -> showPanel("labs"));
        toolBar.add(btnLabs);
        
        JButton btnReservations = new JButton("Reservas");
        btnReservations.addActionListener(e -> showPanel("reservaciones"));
        toolBar.add(btnReservations);
        
        JButton btnMaintenance = new JButton("Mantenimiento");
        btnMaintenance.addActionListener(e -> showPanel("mantenimiento"));
        toolBar.add(btnMaintenance);
        
        add(toolBar, BorderLayout.NORTH);
        
        // Crear el panel principal con CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Inicializar paneles
        dashboardPanel = new DashboardPanel(currentUser);
        labsPanel = new LabsPanel();
        reservacionPanel = new ReservacionPanel();
        mantenimientoPanel = new MantenimientoPanel();
        
        // Agregar paneles al CardLayout
        mainPanel.add("dashboard", dashboardPanel);
        mainPanel.add("labs", labsPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Mostrar el panel de inicio por defecto
        showPanel("dashboard");
    }
    
    private void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        
        // Actualizar datos al mostrar el panel
        switch (panelName) {
            case "dashboard":
                dashboardPanel = new DashboardPanel(currentUser);
                break;
            case "labs":
                labsPanel = new LabsPanel();
                break;
            case "reservaciones":
                reservacionPanel = new ReservacionPanel();
                break;
            case "mantenimiento":
                mantenimientoPanel = new MantenimientoPanel();
                break;
        }
    }
    
    private void updateTitle() {
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateTitle();
    }
}