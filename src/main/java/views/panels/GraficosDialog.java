/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views.panels;

/**
 *
 * @author Danner
 */

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GraficosDialog extends JDialog {
    private List<String> seleccionados;
    private boolean aceptado;
    
    public GraficosDialog(JFrame parent, String title, String[] opciones) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(parent);
        
        seleccionados = new ArrayList<>();
        JPanel panel = new JPanel(new GridLayout(opciones.length + 1, 1));
        
        for (String opcion : opciones) {
            JCheckBox checkBox = new JCheckBox(opcion);
            panel.add(checkBox);
        }
        
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.addActionListener(e -> {
            aceptado = true;
            for (Component comp : panel.getComponents()) {
                if (comp instanceof JCheckBox && ((JCheckBox)comp).isSelected()) {
                    seleccionados.add(((JCheckBox)comp).getText());
                }
            }
            dispose();
        });
        
        add(panel, BorderLayout.CENTER);
        add(btnAceptar, BorderLayout.SOUTH);
    }
    
    public List<String> getSeleccionados() {
        return seleccionados;
    }
    
    public boolean isAceptado() {
        return aceptado;
    }
}