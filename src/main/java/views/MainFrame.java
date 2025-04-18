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
    
    // Obtiene el tamaño de la pantalla
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(screenSize.width, screenSize.height);
    
    setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximiza la ventana
    setLocationRelativeTo(null);
    


        // Panel principal con fondo degradado
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

        // Crear barra de herramientas estilizada
        JToolBar toolBar = createStyledToolBar();
        backgroundPanel.add(toolBar, BorderLayout.NORTH);

        // Panel de cards con transparencia
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        mainPanel.setOpaque(false);

        // Crear barra de menú
        JMenuBar menuBar = new JMenuBar();

        JMenu menuOpciones = new JMenu("Opciones");
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuOpciones.add(itemSalir);
        menuBar.add(menuOpciones);

        JMenu menuUsuario = new JMenu("Usuario");
        JMenuItem itemPerfil = new JMenuItem("Mi Perfil");
        itemPerfil.addActionListener(e -> showPanel("usuario"));
        menuUsuario.add(itemPerfil);
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemCerrarSesion.addActionListener(this::cerrarSesion);
        menuUsuario.add(itemCerrarSesion);
        menuBar.add(menuUsuario);

        // Crear paneles base
        dashboardPanel = new DashboardPanel(currentUser);
        usuarioPanel = new UsuarioPanel(currentUser, this);
        mainPanel.add("dashboard", dashboardPanel);
        mainPanel.add("usuario", usuarioPanel);

        // Paneles según el rol
        if ("Administrador".equals(currentUser.getRole())) {
            labsPanel = new LabsPanel();
            mantenimientoPanel = new MantenimientoPanel(currentUser);
            reportesPanel = new ReportesPanel();
            calendarPanel = new CalendarPanel(currentUser);
            reservacionPanel = new ReservacionPanel(currentUser);

            mainPanel.add("labs", labsPanel);
            mainPanel.add("maintenance", mantenimientoPanel);
            mainPanel.add("reportes", reportesPanel);
            mainPanel.add("calendar", calendarPanel);
            mainPanel.add("reservations", reservacionPanel);

            JMenu menuReportes = new JMenu("Reportes");
            JMenuItem itemReportes = new JMenuItem("Ver Reportes");
            itemReportes.addActionListener(e -> showPanel("reportes"));
            menuReportes.add(itemReportes);
            menuBar.add(menuReportes);
        }

        if ("Técnico".equals(currentUser.getRole())) {
            mantenimientoPanel = new MantenimientoPanel(currentUser);
            mainPanel.add("maintenance", mantenimientoPanel);
        }

        if ("Docente".equals(currentUser.getRole())) {
            calendarPanel = new CalendarPanel(currentUser);
            reservacionPanel = new ReservacionPanel(currentUser);
            mainPanel.add("calendar", calendarPanel);
            mainPanel.add("reservations", reservacionPanel);
        }

        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        setJMenuBar(menuBar);
        showPanel("dashboard");
    }

    private JToolBar createStyledToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        toolBar.add(createToolBarButton("Inicio", "dashboard"));

        if ("Administrador".equals(currentUser.getRole())) {
            toolBar.add(createToolBarButton("Laboratorios", "labs"));
            toolBar.add(createToolBarButton("Mantenimiento", "maintenance"));
            toolBar.add(createToolBarButton("Calendario", "calendar"));
            toolBar.add(createToolBarButton("Reservas", "reservations"));
        }

        if ("Técnico".equals(currentUser.getRole())) {
            toolBar.add(createToolBarButton("Mantenimiento", "maintenance"));
        }

        if ("Docente".equals(currentUser.getRole())) {
            toolBar.add(createToolBarButton("Calendario", "calendar"));
            toolBar.add(createToolBarButton("Reservas", "reservations"));
        }

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
            SwingUtilities.invokeLater(() -> new views.LoginForm().setVisible(true));
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
