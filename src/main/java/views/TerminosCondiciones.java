/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views;

/**
 *
 * @author DANNER
 */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class TerminosCondiciones extends JFrame implements ActionListener, ChangeListener {

    private JLabel label1, label2;
    private JCheckBox check1;
    private JButton boton1, boton2;
    private JScrollPane scrollpane1; 
    private JTextArea textarea1;

    public TerminosCondiciones() {
        setTitle("Licencia de uso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);

        // Configurar el layout principal
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Espaciado
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Título
        label1 = new JLabel("TÉRMINOS Y CONDICIONES", SwingConstants.CENTER);
        label1.setFont(new Font("Andale Mono", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(label1, gbc);

        // Área de texto con scroll
        textarea1 = new JTextArea();
        textarea1.setEditable(false);
        textarea1.setFont(new Font("Andale Mono", Font.PLAIN, 9));
        textarea1.setText("""
                            \nT\u00c9RMINOS Y CONDICIONES DE USO DEL SISTEMA DE GESTI\u00d3N DE LABORATORIOS
                            1. INTRODUCCI\u00d3N Y ACEPTACI\u00d3N
                            Al instalar, acceder o utilizar el software, usted "el Usuario" acepta expresamente y se obliga a cumplir 
                            con estos T\u00e9rminos y Condiciones en su totalidad. Si no está de acuerdo con cualquier parte de estos 
                            términos, debe abstenerse inmediatamente de utilizar el Software. Este acuerdo constituye un contrato 
                            legalmente vinculante entre el Usuario y Technolife.
                          
                            2. DEFINICIONES CLAVE
                            \u2022 Software: Aplicaci\u00f3n de gesti\u00f3n de laboratorios incluyendo todos sus m\u00f3dulos, actualizaciones y documentaci\u00f3n asociada.
                            \u2022 Usuario: Persona f\u00edsica o jur\u00eddica autorizada para utilizar el Software.
                            \u2022 Datos Sensibles: Informaci\u00f3n de reservas, equipos, usuarios y cualquier dato procesado por el sistema.
                          
                            3. LICENCIA DE USO
                            3.1 Se concede una licencia no exclusiva, intransferible y revocable para usar el Software \u00fanicamente para:
                            \u2022 Gesti\u00f3n interna de laboratorios.
                            \u2022 Reserva de espacios, equipos y dispositivos extras.
                            \u2022 Generaci\u00f3n de reportes operativos.
                          
                            3.2 Restricciones expl\u00edcitas:
                            \u2022 Prohibida la redistribuci\u00f3n o reventa
                            \u2022 Prohibida la modificaci\u00f3n del c\u00f3digo fuente
                            \u2022 Prohibido el uso para fines distintos a la gesti\u00f3n acad\u00e9mica/investigaci\u00f3n
                          
                            4. RESPONSABILIDADES DEL USUARIO
                            4.1 Uso adecuado:
                            \u2022 Mantener credenciales de acceso seguras
                            \u2022 Verificar la exactitud de los datos ingresados
                            \u2022 Notificar inmediatamente fallos t\u00e9cnicos
                          
                            4.2 Prohibiciones expresas:
                            \u274c Usar el Software para actividades ilegales
                            \u274c Alterar registros de reservas existentes
                            \u274c Sobrecargar intencionalmente el sistema
                          
                            5. EXCLUSI\u00d3N DE GARANT\u00cdAS
                            El software se provee "tal cual" sin garant\u00edas de ning\u00fan tipo, incluyendo, pero no limitado a:
                            \u2714 Garant\u00eda de funcionamiento ininterrumpido
                            \u2714 Precisi\u00f3n absoluta de los datos procesados
                            \u2714 Correcci\u00f3n inmediata de errores
                          
                            ... (contin\u00faa con el resto de los t\u00e9rminos y condiciones)
                          """);
        scrollpane1 = new JScrollPane(textarea1);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollpane1, gbc);

        // Checkbox
        check1 = new JCheckBox("Yo Acepto");
        check1.addChangeListener(this);
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(check1, gbc);

        // Botones en una línea
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        boton1 = new JButton("Continuar");
        boton1.setEnabled(false);
        boton1.addActionListener(this);
        boton2 = new JButton("No Acepto");
        boton2.addActionListener(this);
        buttonPanel.add(boton1);
        buttonPanel.add(boton2);

        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // Imagen
        ImageIcon imagen = new ImageIcon("resources/TECH.png");
        label2 = new JLabel(imagen);
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        panel.add(label2, gbc);

        add(panel);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(check1.isSelected() == true)
        {
            boton1.setEnabled(true);
            boton2.setEnabled(false);
        }
        else
        {
            boton1.setEnabled(false);
            boton2.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == boton1) {
            this.dispose(); // Cerrar la ventana de términos
            new RegisterForm(this).setVisible(true); // Mostrar pantalla de registro
        }
        else if (e.getSource() == boton2) {
            JOptionPane.showMessageDialog(this, "No aceptaste los términos.");
            this.dispose(); // Cierra la ventana de términos
            new LoginForm().setVisible(true); // Regresa al LoginForm
        }
    }
}