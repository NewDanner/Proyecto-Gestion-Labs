/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Andrei
 * @author Danner
 */
import javax.swing.*;
import java.awt.*;
import java.time.Year;

public class ReportesPanel extends JPanel 
{
    public ReportesPanel() 
    {
        initComponents();
    }
    
    private void initComponents() 
    {
        setLayout(new BorderLayout());
        JLabel lblMensaje = new JLabel("Reportes - En construcción", SwingConstants.CENTER);
        lblMensaje.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblMensaje, BorderLayout.CENTER);
        
        // Crear JComboBox con años
        JComboBox<String> comboAnios = new JComboBox<>();
        int anioActual = Year.now().getValue();
        for (int i = 0; i < 10; i++) { // Mostrar los próximos 10 años
            comboAnios.addItem(String.valueOf(anioActual + i));
        }
        
        // Panel para el JComboBox
        JPanel panelInferior = new JPanel();
        panelInferior.add(new JLabel("Selecciona una gestión:"));
        panelInferior.add(comboAnios);
        
        add(panelInferior, BorderLayout.WEST);
        
        // Crear botones seleccionables para los meses
        JPanel panelMeses = new JPanel();
        panelMeses.setLayout(new GridLayout(3, 4, 5, 5)); // Organiza en una cuadrícula de 3 filas y 4 columnas
        
        ButtonGroup grupoMeses = new ButtonGroup();
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        
        for (String mes : meses) {
            JRadioButton btnMes = new JRadioButton(mes);
            grupoMeses.add(btnMes); // Asegura selección única
            panelMeses.add(btnMes); // Añade al panel
        }
        
        add(panelMeses, BorderLayout.EAST);
    }
}