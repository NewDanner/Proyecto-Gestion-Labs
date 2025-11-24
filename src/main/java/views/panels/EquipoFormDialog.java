package views.panels;

import javax.swing.*;
import java.awt.*;
import models.Equipo;
import models.User;
import java.sql.*;
import javax.swing.border.EmptyBorder;

public class EquipoFormDialog extends JDialog {
    private boolean confirmed = false;
    private User currentUser;
    private boolean isUpdate;
    private int equipoId;

    // Componentes del formulario
    private JTextField txtMarca, txtModelo, txtNumeroSerie, txtProcesador, txtRAM;
    private JTextField txtAlmacenamiento, txtSO;
    private JComboBox<String> cbEstado, cbLaboratorio;

    public EquipoFormDialog(JDialog parent, String title, User currentUser, boolean isUpdate, Integer equipoId) {
        super(parent, title, true);
        this.currentUser = currentUser;
        this.isUpdate = isUpdate;
        this.equipoId = equipoId != null ? equipoId : 0;
        initUI();
    }

    private void initUI() {
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Campos esenciales
        txtMarca = new JTextField();
        txtModelo = new JTextField();
        txtNumeroSerie = new JTextField();
        txtProcesador = new JTextField();
        txtRAM = new JTextField();
        txtAlmacenamiento = new JTextField();
        txtSO = new JTextField();
        
        cbEstado = new JComboBox<>(new String[]{"Operativo", "Mantenimiento", "Dañado"});
        cbLaboratorio = new JComboBox<>();
        cargarLaboratorios();

        // Agregar campos al formulario
        formPanel.add(new JLabel("Marca:"));
        formPanel.add(txtMarca);
        formPanel.add(new JLabel("Modelo:"));
        formPanel.add(txtModelo);
        formPanel.add(new JLabel("Número de Serie:"));
        formPanel.add(txtNumeroSerie);
        formPanel.add(new JLabel("Procesador:"));
        formPanel.add(txtProcesador);
        formPanel.add(new JLabel("RAM:"));
        formPanel.add(txtRAM);
        formPanel.add(new JLabel("Almacenamiento:"));
        formPanel.add(txtAlmacenamiento);
        formPanel.add(new JLabel("Sistema Operativo:"));
        formPanel.add(txtSO);
        formPanel.add(new JLabel("Estado:"));
        formPanel.add(cbEstado);
        formPanel.add(new JLabel("Laboratorio:"));
        formPanel.add(cbLaboratorio);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnSave = new JButton(isUpdate ? "Actualizar" : "Guardar");
        btnSave.addActionListener(e -> {
            if (validarDatos()) {
                confirmed = true;
                dispose();
            }
        });
        styleButton(btnSave, new Color(46, 125, 50)); // Verde
        
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        styleButton(btnCancel, new Color(198, 40, 40)); // Rojo
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        if (isUpdate) {
            cargarDatosEquipo();
        }
    }

    private void cargarLaboratorios() {
        try (Connection conn = models.DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id_Laboratorio, nombre FROM Laboratorios")) {
            
            cbLaboratorio.addItem("Sin asignar");
            while (rs.next()) {
                cbLaboratorio.addItem(rs.getString("nombre") + " (ID: " + rs.getInt("Id_Laboratorio") + ")");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar laboratorios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosEquipo() {
        String query = "SELECT * FROM Equipos WHERE Id_Equipo = ?";
        
        try (Connection conn = models.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, equipoId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                txtMarca.setText(rs.getString("Marca"));
                txtModelo.setText(rs.getString("Modelo"));
                txtNumeroSerie.setText(rs.getString("numero_de_serie"));
                txtProcesador.setText(rs.getString("Procesador"));
                txtRAM.setText(rs.getString("RAM"));
                txtAlmacenamiento.setText(rs.getString("Almacenamiento"));
                txtSO.setText(rs.getString("SO"));
                cbEstado.setSelectedItem(rs.getString("Estado"));
                
                if (rs.getObject("Id_Laboratorio") != null) {
                    int labId = rs.getInt("Id_Laboratorio");
                    for (int i = 0; i < cbLaboratorio.getItemCount(); i++) {
                        if (cbLaboratorio.getItemAt(i).contains("(ID: " + labId + ")")) {
                            cbLaboratorio.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos del equipo: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private boolean validarDatos() {
        if (txtMarca.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La marca es obligatoria", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtModelo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El modelo es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (txtNumeroSerie.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El número de serie es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public Equipo getEquipoFromForm() {
        Equipo equipo = new Equipo();
        equipo.setMarca(txtMarca.getText());
        equipo.setModelo(txtModelo.getText());
        equipo.setNumeroSerie(txtNumeroSerie.getText());
        equipo.setProcesador(txtProcesador.getText());
        equipo.setRam(txtRAM.getText());
        equipo.setAlmacenamiento(txtAlmacenamiento.getText());
        equipo.setSo(txtSO.getText());
        equipo.setEstado((String) cbEstado.getSelectedItem());
        equipo.setIdLaboratorio(getLaboratorioId());
        
        if (isUpdate) {
            equipo.setIdEquipo(equipoId);
        }
        
        return equipo;
    }

    private Integer getLaboratorioId() {
        String selectedLab = (String) cbLaboratorio.getSelectedItem();
        if (selectedLab != null && !selectedLab.equals("Sin asignar")) {
            try {
                int start = selectedLab.lastIndexOf("(ID: ") + 5;
                int end = selectedLab.lastIndexOf(")");
                return Integer.parseInt(selectedLab.substring(start, end));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}