package views;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import models.DatabaseConnection;
import views.panels.*;

import models.User;

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
    private GestionIncidentesPanel gestionIncidentesPanel; 

    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Inicializar componentes principales primero
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);

        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(249, 45, 168),
                    0, getHeight(), new Color(255, 209, 12)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(backgroundPanel);

        // Barra de herramientas
        JToolBar toolBar = createStyledToolBar();
        backgroundPanel.add(toolBar, BorderLayout.NORTH);
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);

        // Menú
        JMenuBar menuBar = new JMenuBar();
        
        // Menú Opciones
        JMenu menuOpciones = new JMenu("Opciones");
        menuOpciones.add(new JMenuItem("Salir", KeyEvent.VK_S) {{
            addActionListener(e -> System.exit(0));
        }});
        menuBar.add(menuOpciones);

        // Menú Usuario
        JMenu menuUsuario = new JMenu("Usuario");
        menuUsuario.add(new JMenuItem("Mi Perfil", KeyEvent.VK_P) {{
            addActionListener(e -> showPanel("usuario"));
        }});
        menuUsuario.add(new JMenuItem("Cerrar Sesión", KeyEvent.VK_C) {{
            addActionListener(e -> cerrarSesion(e));
        }});
        menuBar.add(menuUsuario);

        // Paneles comunes a todos los roles
        dashboardPanel = new DashboardPanel(currentUser);
        usuarioPanel = new UsuarioPanel(currentUser, this);
        mainPanel.add("dashboard", dashboardPanel);
        mainPanel.add("usuario", usuarioPanel);

        // Paneles específicos por rol
        String userRole = currentUser.getRole();

        if ("Administrador(a)".equals(userRole)) {
            labsPanel = new LabsPanel(currentUser);
            mantenimientoPanel = new MantenimientoPanel(currentUser);
            reportesPanel = new ReportesPanel(currentUser);
            calendarPanel = new CalendarPanel(currentUser);
            reservacionPanel = new ReservacionPanel(currentUser);
            gestionIncidentesPanel = new GestionIncidentesPanel(currentUser); // Nuevo panel

            mainPanel.add("labs", labsPanel);
            mainPanel.add("maintenance", mantenimientoPanel);
            mainPanel.add("reportes", reportesPanel);
            mainPanel.add("calendar", calendarPanel);
            mainPanel.add("reservations", reservacionPanel);
            mainPanel.add("gestionIncidentes", gestionIncidentesPanel); // Agregar nuevo panel
            mainPanel.add("historial", new HistorialPanel(currentUser));
        } else if ("Técnico(a) de Mantenimiento".equals(userRole)) {
            mantenimientoPanel = new MantenimientoPanel(currentUser);
            mainPanel.add("maintenance", mantenimientoPanel);
        } else if ("Docente".equals(userRole)) {
            calendarPanel = new CalendarPanel(currentUser);
            reservacionPanel = new ReservacionPanel(currentUser);
            mainPanel.add("calendar", calendarPanel);
            mainPanel.add("reservations", reservacionPanel);
        }

        setJMenuBar(menuBar);
        showPanel("dashboard");
    }

    private JToolBar createStyledToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String userRole = currentUser.getRole();

        JButton btnDashboard = createToolBarButton("Dashboard", "dashboard");
        toolBar.add(btnDashboard);

        if ("Administrador(a)".equals(userRole)) {
        toolBar.add(createToolBarButton("Laboratorios", "labs"));
        toolBar.add(createToolBarButton("Calendario", "calendar"));
        toolBar.add(createToolBarButton("Reservas", "reservations"));
        toolBar.add(createToolBarButton("Mantenimiento", "maintenance"));
        toolBar.add(createToolBarButton("Reportes", "reportes"));
        toolBar.add(createToolBarButton("Gestión Incidentes", "gestionIncidentes")); // Nuevo botón
        toolBar.add(createToolBarButton("Historial", "historial"));
        } else if ("Técnico(a) de Mantenimiento".equals(userRole)) {
            toolBar.add(createToolBarButton("Mantenimiento", "maintenance"));
        } else if ("Docente".equals(userRole)) {
            toolBar.add(createToolBarButton("Calendario", "calendar"));
            toolBar.add(createToolBarButton("Reservas", "reservations"));
        }

        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(createToolBarButton("Mi Perfil", "usuario"));

        return toolBar;
    }
    
    private JButton createToolBarButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);

        button.addActionListener(e -> showPanel(panelName));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(255, 255, 255, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    private void showPanel(String panelName) {
        switch (panelName) {
            case "dashboard":
                dashboardPanel = new DashboardPanel(currentUser);
                break;
            case "labs":
                labsPanel = new LabsPanel(currentUser);
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
            case "reportes":
                reportesPanel = new ReportesPanel(currentUser);
                break;
            case "usuario":
                usuarioPanel = new UsuarioPanel(currentUser, this);
                break;
            case "historial":
            // No necesitamos recrear el panel cada vez
            break;
        }
        cardLayout.show(mainPanel, panelName);
    }

    @Override
    public void dispose() {
        if (dashboardPanel instanceof DatabaseConnection.DatabaseChangeListener) {
            DatabaseConnection.removeListener((DatabaseConnection.DatabaseChangeListener) dashboardPanel);
        }
        if (labsPanel != null) DatabaseConnection.removeListener(labsPanel);
        if (calendarPanel != null) DatabaseConnection.removeListener(calendarPanel);
        if (reservacionPanel != null) DatabaseConnection.removeListener(reservacionPanel);
        if (mantenimientoPanel instanceof DatabaseConnection.DatabaseChangeListener) {
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
            new LoginForm().setVisible(true);
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usuarioPanel.setUser(user);
        setTitle("Sistema de Gestión de Laboratorios - " + currentUser.getUsername());

        getContentPane().remove(mainPanel);
        initComponents();
        revalidate();
        repaint();
    }
}