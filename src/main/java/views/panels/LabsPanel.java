/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Andrei
 */

package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;

public class LabsPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable labsTable;
    private JButton btnAddLab, btnEditLab, btnDeleteLab, btnRefresh, btnManageEquipos;
    
    public LabsPanel() {
        DatabaseConnection.addListener(this);
        initComponents();
        loadLabsData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        labsTable = new JTable();
        labsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(labsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        btnAddLab = new JButton("Agregar Laboratorio");
        btnAddLab.addActionListener(this::showAddLabDialog);
        buttonPanel.add(btnAddLab);
        
        btnEditLab = new JButton("Editar Laboratorio");
        btnEditLab.addActionListener(this::showEditLabDialog);
        buttonPanel.add(btnEditLab);
        
        btnDeleteLab = new JButton("Eliminar Laboratorio");
        btnDeleteLab.addActionListener(this::deleteSelectedLab);
        buttonPanel.add(btnDeleteLab);
        
        btnManageEquipos = new JButton("Gestionar Equipos");
        btnManageEquipos.addActionListener(this::manageEquipos);
        buttonPanel.add(btnManageEquipos);
        
        btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> loadLabsData());
        buttonPanel.add(btnRefresh);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void manageEquipos(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un laboratorio",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int labId = (int) labsTable.getValueAt(selectedRow, 0);
        String labName = (String) labsTable.getValueAt(selectedRow, 1);
        
        EquiposDialog dialog = new EquiposDialog(null, labId, labName);
        dialog.setVisible(true);
    }
    
    private void loadLabsData() {
        String query = "SELECT Id_Laboratorio, nombre, capacidad, estado FROM Laboratorios";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            DefaultTableModel model = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            model.addColumn("ID");
            model.addColumn("Nombre");
            model.addColumn("Capacidad");
            model.addColumn("Estado");
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Laboratorio"),
                    rs.getString("nombre"),
                    rs.getInt("capacidad"),
                    formatEstado(rs.getString("estado"))
                });
            }
            
            labsTable.setModel(model);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar laboratorios: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }
    
    private void showAddLabDialog(ActionEvent evt) {
        LabDialog dialog = new LabDialog(null, "Agregar Laboratorio");
        if (dialog.showDialog()) {
            addNewLab(dialog.getNombre(), dialog.getCapacidad(), 
                     dialog.getEstado(), dialog.getDescripcion());
        }
    }
    
    private void showEditLabDialog(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un laboratorio para editar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) labsTable.getValueAt(selectedRow, 0);
        String nombre = (String) labsTable.getValueAt(selectedRow, 1);
        int capacidad = (int) labsTable.getValueAt(selectedRow, 2);
        String estado = (String) labsTable.getValueAt(selectedRow, 3);
        
        String descripcion = getLabDescription(id);
        
        LabDialog dialog = new LabDialog(null, "Editar Laboratorio");
        dialog.setData(nombre, capacidad, estado, descripcion);
        
        if (dialog.showDialog()) {
            updateLab(id, dialog.getNombre(), dialog.getCapacidad(), 
                     dialog.getEstado(), dialog.getDescripcion());
        }
    }
    
    private String getLabDescription(int id) {
        String query = "SELECT descripcion FROM Laboratorios WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("descripcion");
            }
            return "";
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private void deleteSelectedLab(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor seleccione un laboratorio para eliminar",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) labsTable.getValueAt(selectedRow, 0);
        String nombre = (String) labsTable.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar el laboratorio:\n" + nombre + "?",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteLab(id);
        }
    }
    
    private void deleteLab(int id) {
        // Primero verificamos si hay equipos asociados a este laboratorio
        String checkEquiposQuery = "SELECT COUNT(*) FROM Equipos WHERE id_laboratorio = ?";
        String checkPrestamosQuery = "SELECT COUNT(*) FROM Prestamo WHERE Nro_Laboratorio = ?";
        String deleteQuery = "DELETE FROM Laboratorios WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Iniciamos transacción
            
            try {
                // Verificar equipos asociados
                try (PreparedStatement stmt = conn.prepareStatement(checkEquiposQuery)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                            "Este laboratorio tiene equipos asociados. ¿Desea eliminarlos también?",
                            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            // Eliminar equipos asociados
                            String deleteEquipos = "DELETE FROM Equipos WHERE id_laboratorio = ?";
                            try (PreparedStatement stmtEquipos = conn.prepareStatement(deleteEquipos)) {
                                stmtEquipos.setInt(1, id);
                                stmtEquipos.executeUpdate();
                            }
                        } else {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this, 
                                "No se puede eliminar el laboratorio porque tiene equipos asociados",
                                "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                
                // Verificar préstamos asociados
                try (PreparedStatement stmt = conn.prepareStatement(checkPrestamosQuery)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                            "No se puede eliminar el laboratorio porque tiene préstamos asociados",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        conn.rollback();
                        return;
                    }
                }
                
                // Si llegamos aquí, podemos eliminar el laboratorio
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Reiniciar auto_increment
                        try (Statement stmtAI = conn.createStatement()) {
                            stmtAI.executeUpdate("ALTER TABLE Laboratorios AUTO_INCREMENT = 1");
                        }
                        
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Laboratorio eliminado exitosamente");
                        DatabaseConnection.notifyDatabaseChanged("Laboratorios");
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "No se encontró el laboratorio a eliminar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar laboratorio: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error de conexión a la base de datos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addNewLab(String nombre, String capacidad, String estado, String descripcion) {
        String query = "INSERT INTO Laboratorios (nombre, capacidad, estado, descripcion) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, nombre);
            stmt.setInt(2, Integer.parseInt(capacidad));
            stmt.setString(3, estado.toLowerCase().replace(" ", "_"));
            stmt.setString(4, descripcion);
            
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Laboratorio agregado exitosamente");
            DatabaseConnection.notifyDatabaseChanged("Laboratorios");
            
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error al agregar laboratorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateLab(int id, String nombre, String capacidad, String estado, String descripcion) {
        String query = "UPDATE Laboratorios SET nombre = ?, capacidad = ?, estado = ?, descripcion = ? WHERE Id_Laboratorio = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Mapeo de estado
            String estadoDB = estado.toLowerCase().replace(" ", "_");
            
            stmt.setString(1, nombre);
            stmt.setInt(2, Integer.parseInt(capacidad));
            stmt.setString(3, estadoDB);
            stmt.setString(4, descripcion);
            stmt.setInt(5, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Laboratorio actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Laboratorios");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el laboratorio a actualizar",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al actualizar laboratorio: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "La capacidad debe ser un número válido",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Laboratorios")) {
            loadLabsData();
        }
    }
    
    private class LabDialog extends JDialog {
        private JComboBox<String> cbNombre, cbEstado;
        private JTextField txtCapacidad;
        private JTextArea txtDescripcion;
        private boolean confirmed = false;
        
        public LabDialog(JFrame parent, String title) {
            super(parent, title, true);
            setSize(400, 350);
            setLocationRelativeTo(parent);
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            
            cbNombre = new JComboBox<>(new String[]{
                "Laboratorio 1", "Laboratorio 2", "Laboratorio 3", 
                "Laboratorio 4", "Laboratorio 5", "Laboratorio 6", 
                "Laboratorio 7"
            });
            panel.add(new JLabel("Nombre:"));
            panel.add(cbNombre);
            
            txtCapacidad = new JTextField("30");
            panel.add(new JLabel("Capacidad:"));
            panel.add(txtCapacidad);
            
            cbEstado = new JComboBox<>(new String[]{
                "Disponible", "En mantenimiento", "Inactivo"
            });
            panel.add(new JLabel("Estado:"));
            panel.add(cbEstado);
            
            txtDescripcion = new JTextArea(3, 20);
            JScrollPane descScroll = new JScrollPane(txtDescripcion);
            panel.add(new JLabel("Descripción:"));
            panel.add(descScroll);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnOk = new JButton("Aceptar");
            btnOk.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> dispose());
            
            buttonPanel.add(btnOk);
            buttonPanel.add(btnCancel);
            
            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        public boolean showDialog() {
            setVisible(true);
            return confirmed;
        }
        
        public void setData(String nombre, int capacidad, String estado, String descripcion) {
            cbNombre.setSelectedItem(nombre);
            txtCapacidad.setText(String.valueOf(capacidad));
            cbEstado.setSelectedItem(estado);
            txtDescripcion.setText(descripcion);
        }
        
        public String getNombre() {
            return (String) cbNombre.getSelectedItem();
        }
        
        public String getCapacidad() {
            return txtCapacidad.getText();
        }
        
        public String getEstado() {
            return (String) cbEstado.getSelectedItem();
        }
        
        public String getDescripcion() {
            return txtDescripcion.getText();
        }
    }
    
    private class EquiposDialog extends JDialog {
        private JTable equiposTable;
        private JButton btnAddEquipo, btnEditEquipo, btnDeleteEquipo;
        private int labId;
        private String labName;
        
        public EquiposDialog(JFrame parent, int labId, String labName) {
            super(parent, "Equipos del Laboratorio " + labName, true);
            this.labId = labId;
            this.labName = labName;
            initComponents();
            loadEquiposData();
        }
        
        private void initComponents() {
            setSize(800, 600);
            setLocationRelativeTo(getParent());
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            equiposTable = new JTable();
            equiposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(equiposTable);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            
            btnAddEquipo = new JButton("Agregar Equipo");
            btnAddEquipo.addActionListener(this::addEquipo);
            buttonPanel.add(btnAddEquipo);
            
            btnEditEquipo = new JButton("Editar Equipo");
            btnEditEquipo.addActionListener(this::editEquipo);
            buttonPanel.add(btnEditEquipo);
            
            btnDeleteEquipo = new JButton("Eliminar Equipo");
            btnDeleteEquipo.addActionListener(this::deleteEquipo);
            buttonPanel.add(btnDeleteEquipo);
            
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(mainPanel);
        }
        
        private void loadEquiposData() {
            String query = "SELECT Id_Equipo, marca, modelo, numero_de_serie, estado FROM Equipos WHERE id_laboratorio = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, labId);
                ResultSet rs = stmt.executeQuery();
                
                DefaultTableModel model = new DefaultTableModel() {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                model.addColumn("ID");
                model.addColumn("Marca");
                model.addColumn("Modelo");
                model.addColumn("N° Serie");
                model.addColumn("Estado");
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("Id_Equipo"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("numero_de_serie"),
                        formatEstado(rs.getString("estado"))
                    });
                }
                
                equiposTable.setModel(model);
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al cargar equipos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private String formatEstado(String estado) {
            return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
        }
        
        private void addEquipo(ActionEvent evt) {
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            
            JTextField txtMarca = new JTextField();
            JTextField txtModelo = new JTextField();
            JTextField txtSerie = new JTextField();
            JTextField txtProcesador = new JTextField();
            JTextField txtRam = new JTextField();
            JTextField txtAlmacenamiento = new JTextField();
            JTextField txtSO = new JTextField();
            JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Disponible", "En mantenimiento", "Inactivo"});
            
            panel.add(new JLabel("Marca:"));
            panel.add(txtMarca);
            panel.add(new JLabel("Modelo:"));
            panel.add(txtModelo);
            panel.add(new JLabel("N° Serie:"));
            panel.add(txtSerie);
            panel.add(new JLabel("Procesador:"));
            panel.add(txtProcesador);
            panel.add(new JLabel("RAM:"));
            panel.add(txtRam);
            panel.add(new JLabel("Almacenamiento:"));
            panel.add(txtAlmacenamiento);
            panel.add(new JLabel("Sistema Operativo:"));
            panel.add(txtSO);
            panel.add(new JLabel("Estado:"));
            panel.add(cbEstado);
            
            int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Equipo", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                try {
                    agregarEquipo(
                        txtMarca.getText(),
                        txtModelo.getText(),
                        txtSerie.getText(),
                        txtProcesador.getText(),
                        txtRam.getText(),
                        txtAlmacenamiento.getText(),
                        txtSO.getText(),
                        cbEstado.getSelectedItem().toString().toLowerCase().replace(" ", "_")
                    );
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al agregar equipo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void agregarEquipo(String marca, String modelo, String serie, String procesador, 
                                 String ram, String almacenamiento, String so, String estado) throws SQLException {
            String query = "INSERT INTO Equipos (marca, modelo, numero_de_serie, procesador, ram, " +
                         "almacenamiento, sistema_operativo, fecha_de_instalacion, estado, id_laboratorio) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, marca);
                stmt.setString(2, modelo);
                stmt.setString(3, serie);
                stmt.setString(4, procesador);
                stmt.setString(5, ram);
                stmt.setString(6, almacenamiento);
                stmt.setString(7, so);
                stmt.setString(8, estado);
                stmt.setInt(9, labId);
                
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Equipo agregado exitosamente");
                loadEquiposData();
                DatabaseConnection.notifyDatabaseChanged("Equipos");
            }
        }
        
        private void editEquipo(ActionEvent evt) {
            int selectedRow = equiposTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int id = (int) equiposTable.getValueAt(selectedRow, 0);
            String marca = (String) equiposTable.getValueAt(selectedRow, 1);
            String modelo = (String) equiposTable.getValueAt(selectedRow, 2);
            String serie = (String) equiposTable.getValueAt(selectedRow, 3);
            String estado = (String) equiposTable.getValueAt(selectedRow, 4);
            
            // Obtener más detalles del equipo
            String[] detalles = getEquipoDetails(id);
            if (detalles == null) return;
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            
            JTextField txtMarca = new JTextField(marca);
            JTextField txtModelo = new JTextField(modelo);
            JTextField txtSerie = new JTextField(serie);
            txtSerie.setEditable(false);
            JTextField txtProcesador = new JTextField(detalles[0]);
            JTextField txtRam = new JTextField(detalles[1]);
            JTextField txtAlmacenamiento = new JTextField(detalles[2]);
            JTextField txtSO = new JTextField(detalles[3]);
            JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Disponible", "En mantenimiento", "Inactivo"});
            cbEstado.setSelectedItem(estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase());
            
            panel.add(new JLabel("Marca:"));
            panel.add(txtMarca);
            panel.add(new JLabel("Modelo:"));
            panel.add(txtModelo);
            panel.add(new JLabel("N° Serie:"));
            panel.add(txtSerie);
            panel.add(new JLabel("Procesador:"));
            panel.add(txtProcesador);
            panel.add(new JLabel("RAM:"));
            panel.add(txtRam);
            panel.add(new JLabel("Almacenamiento:"));
            panel.add(txtAlmacenamiento);
            panel.add(new JLabel("Sistema Operativo:"));
            panel.add(txtSO);
            panel.add(new JLabel("Estado:"));
            panel.add(cbEstado);
            
            int result = JOptionPane.showConfirmDialog(this, panel, "Editar Equipo", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                try {
                    actualizarEquipo(
                        id,
                        txtMarca.getText(),
                        txtModelo.getText(),
                        txtProcesador.getText(),
                        txtRam.getText(),
                        txtAlmacenamiento.getText(),
                        txtSO.getText(),
                        cbEstado.getSelectedItem().toString().toLowerCase().replace(" ", "_")
                    );
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al actualizar equipo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private String[] getEquipoDetails(int id) {
            String query = "SELECT procesador, ram, almacenamiento, sistema_operativo FROM Equipos WHERE Id_Equipo = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return new String[]{
                        rs.getString("procesador"),
                        rs.getString("ram"),
                        rs.getString("almacenamiento"),
                        rs.getString("sistema_operativo")
                    };
                }
                return null;
                
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        private void actualizarEquipo(int id, String marca, String modelo, String procesador, 
                                    String ram, String almacenamiento, String so, String estado) throws SQLException {
            String query = "UPDATE Equipos SET marca = ?, modelo = ?, procesador = ?, ram = ?, " +
                          "almacenamiento = ?, sistema_operativo = ?, estado = ? WHERE Id_Equipo = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, marca);
                stmt.setString(2, modelo);
                stmt.setString(3, procesador);
                stmt.setString(4, ram);
                stmt.setString(5, almacenamiento);
                stmt.setString(6, so);
                stmt.setString(7, estado);
                stmt.setInt(8, id);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Equipo actualizado exitosamente");
                    loadEquiposData();
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                }
            }
        }
        
        private void deleteEquipo(ActionEvent evt) {
            int selectedRow = equiposTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un equipo",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int id = (int) equiposTable.getValueAt(selectedRow, 0);
            String marca = (String) equiposTable.getValueAt(selectedRow, 1);
            String modelo = (String) equiposTable.getValueAt(selectedRow, 2);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar el equipo:\n" + marca + " " + modelo + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    eliminarEquipo(id);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al eliminar equipo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void eliminarEquipo(int id) throws SQLException {
            String query = "DELETE FROM Equipos WHERE Id_Equipo = ?";
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, id);
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Reiniciar auto_increment
                        try (Statement stmtAI = conn.createStatement()) {
                            stmtAI.executeUpdate("ALTER TABLE Equipos AUTO_INCREMENT = 1");
                        }
                        
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Equipo eliminado exitosamente");
                        loadEquiposData();
                        DatabaseConnection.notifyDatabaseChanged("Equipos");
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }
    }
}
