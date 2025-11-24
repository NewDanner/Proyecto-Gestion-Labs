/**
 *
 * @author Andrei
 */
package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import models.MaterialAdicional;
import models.User;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MaterialFormDialog extends JDialog {
    private MaterialAdicional material;
    private boolean confirmed = false;
    
    private JTextField txtNombre, txtCategoria, txtCantidad, txtMinimo, txtObservaciones, txtProveedor, txtCosto;
    private JCheckBox chkExtravio, chkDano;
    private JFormattedTextField txtFechaCompra;
    private JButton btnSave, btnCancel;
    private int labId;
    private String labName;
    private User currentUser;

    public MaterialFormDialog(JDialog parent, String title, MaterialAdicional material, int labId, String labName, User currentUser) {
        super(parent, title, true);
        this.material = material;
        this.labId = labId;
        this.labName = labName;
        this.currentUser = currentUser;
        initUI();
        pack(); // Added to ensure proper sizing
    }

    private void initUI() {
        setSize(500, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Laboratorio (solo lectura)
        formPanel.add(new JLabel("Laboratorio:"));
        JTextField txtLab = new JTextField(labName);
        txtLab.setEditable(false);
        formPanel.add(txtLab);
        
        // Nombre del objeto
        formPanel.add(new JLabel("Nombre del Objeto:"));
        txtNombre = new JTextField();
        formPanel.add(txtNombre);
        
        // Categoría
        formPanel.add(new JLabel("Categoría:"));
        txtCategoria = new JTextField();
        formPanel.add(txtCategoria);
        
        // Cantidad
        formPanel.add(new JLabel("Cantidad:"));
        txtCantidad = new JTextField();
        formPanel.add(txtCantidad);
        
        // Cantidad mínima
        formPanel.add(new JLabel("Cantidad Mínima:"));
        txtMinimo = new JTextField("1");
        formPanel.add(txtMinimo);
        
        // Extravío
        formPanel.add(new JLabel("Extravío:"));
        chkExtravio = new JCheckBox();
        formPanel.add(chkExtravio);
        
        // Daño
        formPanel.add(new JLabel("Daño:"));
        chkDano = new JCheckBox();
        formPanel.add(chkDano);
        
        // Proveedor
        formPanel.add(new JLabel("Proveedor:"));
        txtProveedor = new JTextField();
        formPanel.add(txtProveedor);
        
        // Fecha de compra
        formPanel.add(new JLabel("Fecha de Compra (yyyy-MM-dd):"));
        txtFechaCompra = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        formPanel.add(txtFechaCompra);
        
        // Costo
        formPanel.add(new JLabel("Costo:"));
        txtCosto = new JTextField();
        formPanel.add(txtCosto);
        
        // Observaciones
        formPanel.add(new JLabel("Observaciones:"));
        txtObservaciones = new JTextField();
        formPanel.add(txtObservaciones);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnSave = new JButton("Guardar");
        btnSave.addActionListener(e -> {
            if (validarDatos()) {
                confirmed = true;
                dispose();
            }
        });
        styleButton(btnSave, Color.BLACK); // Changed to black
        
        btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        styleButton(btnCancel, Color.BLACK); // Changed to black
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        // Rellenar datos si estamos editando
        if (material != null) {
            txtNombre.setText(material.getNombreObjeto());
            txtCategoria.setText(material.getCategoria());
            txtCantidad.setText(String.valueOf(material.getCantidad()));
            txtMinimo.setText(String.valueOf(material.getCantidadMinima()));
            chkExtravio.setSelected(material.isExtravio());
            chkDano.setSelected(material.isDano());
            txtProveedor.setText(material.getProveedor() != null ? material.getProveedor() : "");
            txtFechaCompra.setText(material.getFechaCompra() != null ? material.getFechaCompra().toString() : "");
            txtCosto.setText(material.getCosto() != null ? String.valueOf(material.getCosto()) : "");
            txtObservaciones.setText(material.getObservaciones() != null ? material.getObservaciones() : "");
        }
        
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(Color.BLACK); // Always black now
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setOpaque(true);
        button.setBorderPainted(false); // Added for better black appearance
    }

    private boolean validarDatos() {
        // Validación de nombre
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del objeto es obligatorio", "Error", JOptionPane.ERROR_MESSAGE);
            txtNombre.requestFocus();
            return false;
        }
        
        // Validación de categoría
        if (txtCategoria.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La categoría es obligatoria", "Error", JOptionPane.ERROR_MESSAGE);
            txtCategoria.requestFocus();
            return false;
        }
        
        // Validación de cantidades
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            int minimo = Integer.parseInt(txtMinimo.getText());
            
            if (cantidad < 0 || minimo < 0) {
                JOptionPane.showMessageDialog(this, "Las cantidades deben ser números positivos", "Error", JOptionPane.ERROR_MESSAGE);
                txtCantidad.requestFocus();
                return false;
            }
            
            if (minimo == 0) {
                JOptionPane.showMessageDialog(this, "La cantidad mínima no puede ser cero", "Error", JOptionPane.ERROR_MESSAGE);
                txtMinimo.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Las cantidades deben ser números válidos", "Error", JOptionPane.ERROR_MESSAGE);
            txtCantidad.requestFocus();
            return false;
        }
        
        // Validación de fecha si se ingresó
        if (!txtFechaCompra.getText().isEmpty()) {
            try {
                new SimpleDateFormat("yyyy-MM-dd").parse(txtFechaCompra.getText());
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
                txtFechaCompra.requestFocus();
                return false;
            }
        }
        
        // Validación de costo si se ingresó
        if (!txtCosto.getText().isEmpty()) {
            try {
                double costo = Double.parseDouble(txtCosto.getText());
                if (costo < 0) {
                    JOptionPane.showMessageDialog(this, "El costo debe ser un número positivo", "Error", JOptionPane.ERROR_MESSAGE);
                    txtCosto.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "El costo debe ser un número válido", "Error", JOptionPane.ERROR_MESSAGE);
                txtCosto.requestFocus();
                return false;
            }
        }
        
        return true;
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public MaterialAdicional getMaterial() {
        MaterialAdicional mat = new MaterialAdicional();
        
        if (material != null) {
            mat.setNObjeto(material.getNObjeto());
        }
        
        mat.setNombreObjeto(txtNombre.getText());
        mat.setCategoria(txtCategoria.getText());
        mat.setCantidad(Integer.parseInt(txtCantidad.getText()));
        mat.setCantidadMinima(Integer.parseInt(txtMinimo.getText()));
        mat.setExtravio(chkExtravio.isSelected());
        mat.setDano(chkDano.isSelected());
        mat.setObservaciones(txtObservaciones.getText());
        mat.setIdLaboratorio(labId);
        mat.setReportadoPor(currentUser.getId());
        
        if (!txtProveedor.getText().isEmpty()) {
            mat.setProveedor(txtProveedor.getText());
        }
        
        if (!txtFechaCompra.getText().isEmpty()) {
            try {
                mat.setFechaCompra(new Date(new SimpleDateFormat("yyyy-MM-dd").parse(txtFechaCompra.getText()).getTime()));
            } catch (ParseException e) {
                // Si hay error en el formato, se deja null
            }
        }
        
        if (!txtCosto.getText().isEmpty()) {
            try {
                mat.setCosto(Double.parseDouble(txtCosto.getText()));
            } catch (NumberFormatException e) {
                // Si hay error en el formato, se deja null
            }
        }
        
        return mat;
    }
    
}