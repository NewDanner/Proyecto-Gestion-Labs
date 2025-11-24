
/**
 *
 * @author Andrei
 */
package views.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import models.MaterialAdicional;
import controllers.MaterialController;
import models.DatabaseConnection;
import models.User;
import java.sql.*;
import java.util.List;

public class MaterialesDialog extends JDialog {
    private JTable materialesTable;
    private int labId;
    private String labName;
    private MaterialController materialController;
    private User currentUser;

    public MaterialesDialog(JFrame parent, int labId, String labName, User currentUser) {
        super(parent, "Materiales Adicionales - " + labName, true);
        this.labId = labId;
        this.labName = labName;
        this.currentUser = currentUser;
        this.materialController = new MaterialController();
        initUI();
        loadMaterialesData();
    }

    private void initUI() {
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tabla de materiales
        materialesTable = new JTable();
        materialesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        materialesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        materialesTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(materialesTable);
        scrollPane.setPreferredSize(new Dimension(850, 450));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnAdd = new JButton("Agregar Material");
        btnAdd.addActionListener(this::addMaterial);
        styleButton(btnAdd, new Color(46, 125, 50)); // Verde
        
        JButton btnEdit = new JButton("Editar Material");
        btnEdit.addActionListener(this::editMaterial);
        styleButton(btnEdit, new Color(2, 119, 189)); // Azul
        
        JButton btnDelete = new JButton("Eliminar Material");
        btnDelete.addActionListener(this::deleteMaterial);
        styleButton(btnDelete, new Color(198, 40, 40)); // Rojo
        
        JButton btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());
        styleButton(btnClose, new Color(100, 100, 100)); // Gris
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClose);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void loadMaterialesData() {
        List<MaterialAdicional> materiales = materialController.obtenerMaterialesPorLaboratorio(labId);
        
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Categoría", "Cantidad", "Mínimo", "Extravío", "Daño", "Observaciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (MaterialAdicional material : materiales) {
            model.addRow(new Object[]{
                material.getNObjeto(),
                material.getNombreObjeto(),
                material.getCategoria(),
                material.getCantidad(),
                material.getCantidadMinima(),
                material.isExtravio() ? "Sí" : "No",
                material.isDano() ? "Sí" : "No",
                material.getObservaciones()
            });
        }
        
        materialesTable.setModel(model);
        
        // Personalizar el header de la tabla
        JTableHeader header = materialesTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
    }

    private void addMaterial(ActionEvent evt) {
        MaterialFormDialog dialog = new MaterialFormDialog(this, "Agregar Material", null, labId, labName, currentUser);
        if (dialog.showDialog()) {
            MaterialAdicional nuevoMaterial = dialog.getMaterial();
            if (materialController.agregarMaterial(nuevoMaterial)) {
                JOptionPane.showMessageDialog(this, "Material agregado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadMaterialesData();
            } else {
                JOptionPane.showMessageDialog(this, "Error al agregar material", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMaterial(ActionEvent evt) {
        int selectedRow = materialesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un material", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int nObjeto = (int) materialesTable.getValueAt(selectedRow, 0);
        MaterialAdicional material = materialController.obtenerMaterialesPorLaboratorio(labId).stream()
                .filter(m -> m.getNObjeto() == nObjeto)
                .findFirst()
                .orElse(null);

        if (material != null) {
            MaterialFormDialog dialog = new MaterialFormDialog(this, "Editar Material", material, labId, labName, currentUser);
            if (dialog.showDialog()) {
                MaterialAdicional materialEditado = dialog.getMaterial();
                if (materialController.actualizarMaterial(materialEditado)) {
                    JOptionPane.showMessageDialog(this, "Material actualizado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    loadMaterialesData();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar material", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteMaterial(ActionEvent evt) {
        int selectedRow = materialesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un material", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int nObjeto = (int) materialesTable.getValueAt(selectedRow, 0);
        String nombreMaterial = (String) materialesTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar el material: " + nombreMaterial + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (materialController.eliminarMaterial(nObjeto)) {
                JOptionPane.showMessageDialog(this, "Material eliminado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                loadMaterialesData();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar material", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}