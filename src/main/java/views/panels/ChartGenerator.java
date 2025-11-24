
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nb://SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Danner
 */

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.sql.*;
import java.time.Month;
import javax.imageio.*;
import com.itextpdf.text.Image;
import models.DatabaseConnection;

public class ChartGenerator {

    // ==================== MÉTODOS PARA MOSTRAR GRÁFICOS ====================
    
    private static void showChart(JFreeChart chart, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(750, 550));
        frame.add(chartPanel);
        frame.setVisible(true);
    }

    // ==================== GRÁFICOS PARA LABORATORIOS ====================
    
    public static void generarGraficoUsoLaboratoriosPorMes(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT l.nombre, MONTH(r.fecha_reserva) AS mes, COUNT(*) AS reservas " +
                      "FROM Laboratorios l LEFT JOIN Reservas r ON l.Id_Laboratorio = r.Nro_Laboratorio " +
                      "WHERE YEAR(r.fecha_reserva) = ? GROUP BY l.nombre, MONTH(r.fecha_reserva)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String laboratorio = rs.getString("nombre");
                int mes = rs.getInt("mes");
                int reservas = rs.getInt("reservas");
                dataset.addValue(reservas, laboratorio, Month.of(mes).toString());
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Uso de Laboratorios por Mes (" + anio + ")",
                "Mes",
                "Reservas",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );
            showChart(chart, "Uso de Laboratorios");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoHorariosOcupados() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT HOUR(hora_inicio) AS hora, COUNT(*) AS reservas FROM Reservas GROUP BY HOUR(hora_inicio)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int hora = rs.getInt("hora");
                int reservas = rs.getInt("reservas");
                dataset.addValue(reservas, "Reservas", hora + ":00");
            }

            JFreeChart chart = ChartFactory.createLineChart(
                "Horarios más Ocupados",
                "Hora",
                "Reservas",
                dataset
            );
            showChart(chart, "Horarios Ocupados");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoEstadosLaboratorios() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT estado, COUNT(*) AS cantidad FROM Laboratorios GROUP BY estado";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String estado = rs.getString("estado");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(estado, cantidad);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Estado de Laboratorios",
                dataset,
                true, true, false
            );
            showChart(chart, "Estados Laboratorios");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== GRÁFICOS PARA MANTENIMIENTO ====================
    
    public static void generarGraficoTiposMantenimiento() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tipo, COUNT(*) AS cantidad FROM Mantenimiento GROUP BY tipo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Mantenimientos", tipo);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Tipos de Mantenimiento",
                "Tipo",
                "Cantidad",
                dataset
            );
            showChart(chart, "Tipos Mantenimiento");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoDuracionMantenimiento() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tipo, AVG(TIMESTAMPDIFF(HOUR, fecha_inicio, fecha_fin)) AS horas " +
                      "FROM Mantenimiento WHERE fecha_fin IS NOT NULL GROUP BY tipo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double horas = rs.getDouble("horas");
                dataset.addValue(horas, "Horas", tipo);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Duración Promedio de Mantenimientos",
                "Tipo",
                "Horas",
                dataset
            );
            showChart(chart, "Duración Mantenimientos");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoMantenimientosPorResponsable() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT u.nombre, COUNT(*) AS cantidad " +
                      "FROM Mantenimiento m JOIN Usuarios u ON m.Id_Usuario_Responsable = u.id_usuario " +
                      "GROUP BY u.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String responsable = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Mantenimientos", responsable);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Mantenimientos por Responsable",
                "Responsable",
                "Cantidad",
                dataset,
                PlotOrientation.HORIZONTAL,
                true, true, false
            );
            showChart(chart, "Mantenimientos por Responsable");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoEstadosMantenimiento() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT estado, COUNT(*) AS cantidad FROM Mantenimiento GROUP BY estado";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String estado = rs.getString("estado");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(estado, cantidad);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Estados de Mantenimiento",
                dataset,
                true, true, false
            );
            showChart(chart, "Estados Mantenimiento");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== GRÁFICOS PARA DISPOSITIVOS BAJA ====================
    
    public static void generarGraficoMotivosBaja() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT motivo, COUNT(*) AS cantidad FROM Dispositivos_Baja GROUP BY motivo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String motivo = rs.getString("motivo");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(motivo, cantidad);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Motivos de Baja de Dispositivos",
                dataset,
                true, true, false
            );
            showChart(chart, "Motivos Baja");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoBajasPorMes(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT MONTH(fecha_baja) AS mes, COUNT(*) AS bajas " +
                      "FROM Dispositivos_Baja WHERE YEAR(fecha_baja) = ? GROUP BY MONTH(fecha_baja)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int mes = rs.getInt("mes");
                int bajas = rs.getInt("bajas");
                dataset.addValue(bajas, "Bajas", Month.of(mes).toString());
            }

            JFreeChart chart = ChartFactory.createLineChart(
                "Bajas de Dispositivos por Mes (" + anio + ")",
                "Mes",
                "Bajas",
                dataset
            );
            showChart(chart, "Bajas por Mes");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== GRÁFICOS PARA TRIGGERS ====================
    
    public static void generarGraficoOperacionesPorTabla(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tabla_afectada, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY tabla_afectada";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tabla = rs.getString("tabla_afectada");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Operaciones", tabla);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                "Operaciones por Tabla (" + anio + ")",
                "Tabla",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );
            showChart(chart, "Operaciones por Tabla");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoOperacionesPorTipo(int anio) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT tipo_operacion, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY tipo_operacion";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo_operacion");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(tipo, cantidad);
            }

            JFreeChart chart = ChartFactory.createPieChart(
                "Operaciones por Tipo (" + anio + ")",
                dataset,
                true, true, false
            );
            showChart(chart, "Operaciones por Tipo");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generarGraficoTransaccionesPorMes(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT MONTH(fecha_transaccion) AS mes, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY MONTH(fecha_transaccion)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, anio);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int mes = rs.getInt("mes");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Transacciones", Month.of(mes).toString());
            }

            JFreeChart chart = ChartFactory.createLineChart(
                "Transacciones por Mes (" + anio + ")",
                "Mes",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );
            showChart(chart, "Transacciones por Mes");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== MÉTODOS PARA GENERAR IMÁGENES PARA PDF ====================
    
    public static com.itextpdf.text.Image generarImagenParaPDF(String tipoGrafico, int anio) {
        try {
            JFreeChart chart = null;
            
            switch (tipoGrafico) {
                case "Uso por mes":
                    chart = crearGraficoUsoMensual(anio);
                    break;
                case "Estados laboratorios": // Updated to match TRIGGERS_GRAFICOS
                    chart = crearGraficoEstadosLaboratorios();
                    break;
                case "Horarios ocupados":
                    chart = crearGraficoHorariosOcupados();
                    break;
                case "Tipos de mantenimiento":
                    chart = crearGraficoTiposMantenimiento();
                    break;
                case "Duración mantenimiento":
                    chart = crearGraficoDuracionMantenimiento();
                    break;
                case "Mantenimientos por responsable":
                    chart = crearGraficoMantenimientosPorResponsable();
                    break;
                case "Estados de mantenimiento":
                    chart = crearGraficoEstadosMantenimiento();
                    break;
                case "Motivos de baja":
                    chart = crearGraficoMotivosBaja();
                    break;
                case "Bajas por mes":
                    chart = crearGraficoBajasPorMes(anio);
                    break;
                case "Operaciones por tabla":
                    chart = crearGraficoOperacionesPorTabla(anio);
                    break;
                case "Operaciones por tipo":
                    chart = crearGraficoOperacionesPorTipo(anio);
                    break;
                case "Transacciones por mes":
                    chart = crearGraficoTransaccionesPorMes(anio);
                    break;
            }
            
            if (chart != null) {
                BufferedImage bufferedImage = chart.createBufferedImage(500, 300);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", baos);
                return com.itextpdf.text.Image.getInstance(baos.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== MÉTODOS AUXILIARES PARA CREAR GRÁFICOS ====================
    
    private static JFreeChart crearGraficoUsoMensual(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT l.nombre, MONTH(r.fecha_reserva) AS mes, COUNT(*) AS reservas " +
                      "FROM Laboratorios l LEFT JOIN Reservas r ON l.Id_Laboratorio = r.Nro_Laboratorio " +
                      "WHERE YEAR(r.fecha_reserva) = ? GROUP BY l.nombre, MONTH(r.fecha_reserva)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String laboratorio = rs.getString("nombre");
                int mes = rs.getInt("mes");
                int reservas = rs.getInt("reservas");
                dataset.addValue(reservas, laboratorio, Month.of(mes).toString());
            }

            return ChartFactory.createBarChart(
                "Uso de Laboratorios por Mes (" + anio + ")",
                "Mes",
                "Reservas",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoEstadosLaboratorios() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT estado, COUNT(*) AS cantidad FROM Laboratorios GROUP BY estado";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String estado = rs.getString("estado");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(estado, cantidad);
            }

            return ChartFactory.createPieChart(
                "Estado de Laboratorios",
                dataset,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoHorariosOcupados() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT HOUR(hora_inicio) AS hora, COUNT(*) AS reservas FROM Reservas GROUP BY HOUR(hora_inicio)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int hora = rs.getInt("hora");
                int reservas = rs.getInt("reservas");
                dataset.addValue(reservas, "Reservas", hora + ":00");
            }

            return ChartFactory.createLineChart(
                "Horarios más Ocupados",
                "Hora",
                "Reservas",
                dataset
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoTiposMantenimiento() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tipo, COUNT(*) AS cantidad FROM Mantenimiento GROUP BY tipo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Mantenimientos", tipo);
            }

            return ChartFactory.createBarChart(
                "Tipos de Mantenimiento",
                "Tipo",
                "Cantidad",
                dataset
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoDuracionMantenimiento() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tipo, AVG(TIMESTAMPDIFF(HOUR, fecha_inicio, fecha_fin)) AS horas " +
                      "FROM Mantenimiento WHERE fecha_fin IS NOT NULL GROUP BY tipo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double horas = rs.getDouble("horas");
                dataset.addValue(horas, "Horas", tipo);
            }

            return ChartFactory.createBarChart(
                "Duración Promedio de Mantenimientos",
                "Tipo",
                "Horas",
                dataset
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoMantenimientosPorResponsable() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT u.nombre, COUNT(*) AS cantidad " +
                      "FROM Mantenimiento m JOIN Usuarios u ON m.Id_Usuario_Responsable = u.id_usuario " +
                      "GROUP BY u.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String responsable = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Mantenimientos", responsable);
            }

            return ChartFactory.createBarChart(
                "Mantenimientos por Responsable",
                "Responsable",
                "Cantidad",
                dataset,
                PlotOrientation.HORIZONTAL,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoEstadosMantenimiento() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT estado, COUNT(*) AS cantidad FROM Mantenimiento GROUP BY estado";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String estado = rs.getString("estado");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(estado, cantidad);
            }

            return ChartFactory.createPieChart(
                "Estados de Mantenimiento",
                dataset,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoMotivosBaja() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT motivo, COUNT(*) AS cantidad FROM Dispositivos_Baja GROUP BY motivo";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String motivo = rs.getString("motivo");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(motivo, cantidad);
            }

            return ChartFactory.createPieChart(
                "Motivos de Baja de Dispositivos",
                dataset,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoBajasPorMes(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT MONTH(fecha_baja) AS mes, COUNT(*) AS bajas " +
                      "FROM Dispositivos_Baja WHERE YEAR(fecha_baja) = ? GROUP BY MONTH(fecha_baja)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int mes = rs.getInt("mes");
                int bajas = rs.getInt("bajas");
                dataset.addValue(bajas, "Bajas", Month.of(mes).toString());
            }

            return ChartFactory.createLineChart(
                "Bajas de Dispositivos por Mes (" + anio + ")",
                "Mes",
                "Bajas",
                dataset
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoOperacionesPorTabla(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT tabla_afectada, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY tabla_afectada";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tabla = rs.getString("tabla_afectada");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Operaciones", tabla);
            }

            return ChartFactory.createBarChart(
                "Operaciones por Tabla (" + anio + ")",
                "Tabla",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoOperacionesPorTipo(int anio) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String query = "SELECT tipo_operacion, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY tipo_operacion";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo_operacion");
                int cantidad = rs.getInt("cantidad");
                dataset.setValue(tipo, cantidad);
            }

            return ChartFactory.createPieChart(
                "Operaciones por Tipo (" + anio + ")",
                dataset,
                true, true, false
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JFreeChart crearGraficoTransaccionesPorMes(int anio) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String query = "SELECT MONTH(fecha_transaccion) AS mes, COUNT(*) AS cantidad " +
                      "FROM Historial_Transacciones WHERE YEAR(fecha_transaccion) = ? " +
                      "GROUP BY MONTH(fecha_transaccion)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, anio);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int mes = rs.getInt("mes");
                int cantidad = rs.getInt("cantidad");
                dataset.addValue(cantidad, "Transacciones", Month.of(mes).toString());
            }

            return ChartFactory.createLineChart(
                "Transacciones por Mes (" + anio + ")",
                "Mes",
                "Cantidad",
                dataset
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
