package views.panels;

/**
 *
 * @author Andrei
 */
import models.User;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;

public class DashboardPanel extends JPanel {
    private User currentUser;
    private Dimension screenSize;

    public DashboardPanel(User user) {
        this.currentUser = user;
        initComponents();
        loadStats();
       
    }

    private void initComponents() {
        // Configuración del diseño
        setLayout(new BorderLayout());
        setOpaque(false); // Permite que el fondo degradado sea visible

        // Título de bienvenida con fuente y color estilizados
        JLabel lblWelcome = new JLabel("Bienvenido, " + currentUser.getUsername(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Roboto", Font.BOLD, 28)); // Fuente moderna y más grande
        lblWelcome.setForeground(new Color(135,206,250)); // Texto azul
        add(lblWelcome, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Crear un degradado de gris oscuro a gris claro
        Color color1 = new Color(60, 63, 65); // Gris oscuro
        Color color2 = new Color(180, 180, 180); // Gris claro
        int width = getWidth();
        int height = getHeight();

        GradientPaint gradientPaint = new GradientPaint(0, 0, color1, 0, height, color2);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, width, height);

        g2d.dispose(); // Libera recursos gráficos
    }

    private void loadStats() {
        // Panel de estadísticas con diseño visual mejorado
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15)); // Mayor separación entre elementos
        statsPanel.setOpaque(false); // Permite que se vea el fondo degradado

        try {
            // Laboratorios Activos
            int laboratoriosActivos = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'disponible'");
            statsPanel.add(createStatPanel("Laboratorios Activos", String.valueOf(laboratoriosActivos)));

            // Préstamos Hoy
            int prestamosHoy = getCountFromDB("SELECT COUNT(*) FROM Reservas WHERE fecha_reserva = CURDATE()");
            statsPanel.add(createStatPanel("Préstamos Hoy", String.valueOf(prestamosHoy)));

            // En Mantenimiento
            int enMantenimiento = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'en_mantenimiento'");
            statsPanel.add(createStatPanel("En Mantenimiento", String.valueOf(enMantenimiento)));

            // Disponibles
            int disponibles = getCountFromDB("SELECT COUNT(*) FROM Laboratorios WHERE estado = 'disponible'");
            statsPanel.add(createStatPanel("Disponibles", String.valueOf(disponibles)));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estadísticas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        add(statsPanel, BorderLayout.CENTER);
    }
    private int getCountFromDB(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private JPanel createStatPanel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(43, 44, 46)); // Fondo oscuro para paneles individuales
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(135, 206, 250)), // Borde azul claro
                title, 0, 0, new Font("Roboto", Font.BOLD, 20), Color.WHITE // Título estilizado
        ));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Roboto", Font.BOLD, 70)); // Valor estilizado más grande
        lblValue.setForeground(Color.GREEN); // Texto en verde para destacar estadísticas positivas
        panel.add(lblValue, BorderLayout.CENTER);

        return panel;
    }

    private void setResizable(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void setDefaultCloseOperation(int EXIT_ON_CLOSE) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void setExtendedState(int MAXIMIZED_BOTH) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
