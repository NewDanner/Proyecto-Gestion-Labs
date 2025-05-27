package views.panels;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import models.DatabaseConnection;
import models.User;
import models.ComboItem;

public class MantenimientoFormDialog extends JDialog {
    private JComboBox<String> cmbTipoElemento;
    private JComboBox<ComboItem> cmbElemento;
    private JComboBox<String> cmbTipoMantenimiento;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JTextArea txtProcedimiento;
    private JTextField txtRepuestos;
    private JTextField txtCosto;
    
    private User currentUser;
    private DefaultComboBoxModel<ComboItem> equiposModel;
    private DefaultComboBoxModel<ComboItem> materialesModel;
    private int mantenimientoId = -1;

    public MantenimientoFormDialog(Frame parent, User user) {
        super(parent, "Registrar Mantenimiento", true);
        this.currentUser = user;
        initUI();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void verificarConexionBD() {
    try {
        // Obtener conexión
        Connection testConn = DatabaseConnection.getConnection();
        
        // Obtener información de la base de datos
        DatabaseMetaData metaData = testConn.getMetaData();
        String dbName = metaData.getDatabaseProductName();
        String dbVersion = metaData.getDatabaseProductVersion();
        
        // Mostrar información en consola
        System.out.println("----------------------------------------");
        System.out.println("Prueba de conexión a base de datos:");
        System.out.println("Producto: " + dbName);
        System.out.println("Versión: " + dbVersion);
        System.out.println("URL: " + metaData.getURL());
        System.out.println("Usuario: " + metaData.getUserName());
        System.out.println("Controlador: " + metaData.getDriverName() + " v" + metaData.getDriverVersion());
        System.out.println("----------------------------------------");
        
        // Verificar si podemos consultar la tabla de equipos
        try (Statement stmt = testConn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Equipos")) {
            if (rs.next()) {
                System.out.println("Número de equipos en la base de datos: " + rs.getInt(1));
            }
        }
        
        // Cerrar conexión
        testConn.close();
        System.out.println("Conexión verificada y cerrada correctamente");
        System.out.println("----------------------------------------");
        
    } catch (SQLException e) {
        System.err.println("ERROR en la conexión a la base de datos:");
        e.printStackTrace();
        System.out.println("----------------------------------------");
        
        // Mostrar mensaje al usuario
        JOptionPane.showMessageDialog(this,
            "Error al conectar con la base de datos:\n" + e.getMessage(),
            "Error de conexión",
            JOptionPane.ERROR_MESSAGE);
    }
}

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 500));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormField(formPanel, gbc, 0, "Tipo de elemento:", cmbTipoElemento = new JComboBox<>(new String[]{"Equipo", "Material"}));
        cmbTipoElemento.addActionListener(e -> updateElementosCombo());
        
        addFormField(formPanel, gbc, 1, "Elemento a mantener:", cmbElemento = new JComboBox<>());
        
        addFormField(formPanel, gbc, 2, "Tipo de mantenimiento:", 
                   cmbTipoMantenimiento = new JComboBox<>(new String[]{"Preventivo", "Correctivo", "Predictivo", "Actualización"}));
        
        addFormField(formPanel, gbc, 3, "Nombre del mantenimiento:", txtNombre = new JTextField(20));
        
        addTextAreaField(formPanel, gbc, 4, "Descripción:", txtDescripcion = new JTextArea(3, 20));
        
        addTextAreaField(formPanel, gbc, 5, "Procedimiento:", txtProcedimiento = new JTextArea(3, 20));
        
        addFormField(formPanel, gbc, 6, "Repuestos utilizados:", txtRepuestos = new JTextField(20));
        
        addFormField(formPanel, gbc, 7, "Costo estimado:", txtCosto = new JTextField(20));

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        buttonPanel.add(btnCancelar);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarMantenimiento());
        buttonPanel.add(btnGuardar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addTextAreaField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextArea textArea) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        addFormField(panel, gbc, row, label, new JScrollPane(textArea));
    }

    private void loadData() {
    equiposModel = new DefaultComboBoxModel<>();
    materialesModel = new DefaultComboBoxModel<>();
    
    // Agregar opción por defecto
    equiposModel.addElement(new ComboItem("Seleccione un equipo", -1));
    materialesModel.addElement(new ComboItem("Seleccione un material", -1));
    
    try (Connection conn = DatabaseConnection.getConnection()) {
        // Cargar equipos
        String queryEquipos = "SELECT Id_Equipo, CONCAT(marca, ' ', modelo, ' - Lab ', "
                           + "COALESCE(Id_Laboratorio, 'Sin asignar'), ' (', estado, ')') AS display "
                           + "FROM Equipos ORDER BY marca, modelo";
        loadComboData(equiposModel, queryEquipos);
        
        // Cargar materiales
        String queryMateriales = "SELECT N_Objeto, CONCAT(nombre_objeto, ' (', categoria, ')') AS display "
                              + "FROM Material_Adicional WHERE extravio = FALSE AND daño = FALSE "
                              + "ORDER BY nombre_objeto";
        loadComboData(materialesModel, queryMateriales);
        
        cmbElemento.setModel(equiposModel);
        
        // Configurar renderizador personalizado
        cmbElemento.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComboItem) {
                    ComboItem item = (ComboItem) value;
                    setText(item.toString());
                    if (item.getValue() == -1) {
                        setForeground(Color.GRAY);
                    } else {
                        setForeground(isSelected ? Color.WHITE : Color.BLACK);
                    }
                }
                return this;
            }
        });
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error al cargar elementos: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void loadComboData(DefaultComboBoxModel<ComboItem> model, String query) throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        
        while (rs.next()) {
            String displayText = rs.getString("display");
            int id = rs.getInt(1); // Obtener el ID (primera columna)
            model.addElement(new ComboItem(displayText, id));
        }
    }
    
    // Configurar renderer para los combos
    cmbElemento.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ComboItem) {
                ComboItem item = (ComboItem) value;
                setText(item.getLabel());
                setForeground(item.getValue() == -1 ? Color.GRAY : Color.BLACK);
            }
            return this;
        }
    });
}

    private void updateElementosCombo() {
        if (cmbTipoElemento.getSelectedItem().equals("Equipo")) {
            cmbElemento.setModel(equiposModel);
        } else {
            cmbElemento.setModel(materialesModel);
        }
        cmbElemento.repaint(); // Forzar redibujado del componente
    }

    public void setTipoElemento(String tipo) {
        cmbTipoElemento.setSelectedItem(tipo);
        updateElementosCombo();
    }

    public void setElementoId(int id) {
        DefaultComboBoxModel<ComboItem> model = (DefaultComboBoxModel<ComboItem>) cmbElemento.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = model.getElementAt(i);
            if (item.getValue() == id) {
                cmbElemento.setSelectedIndex(i);
                break;
            }
        }
    }

    public void setMantenimientoId(int id) {
        this.mantenimientoId = id;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM Mantenimiento WHERE Id_Mantenimiento = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                txtNombre.setText(rs.getString("nombre"));
                cmbTipoMantenimiento.setSelectedItem(rs.getString("tipo"));
                cmbTipoElemento.setSelectedItem(rs.getString("tipo_elemento"));
                txtDescripcion.setText(rs.getString("descripcion"));
                txtProcedimiento.setText(rs.getString("procedimiento"));
                txtRepuestos.setText(rs.getString("repuestos_utilizados"));
                txtCosto.setText(rs.getString("costo") != null ? rs.getString("costo") : "");
                
                String tipoElemento = rs.getString("tipo_elemento");
                int elementoId = 0;
                
                if (tipoElemento.equals("Equipo")) {
                    try (PreparedStatement stmtEquipo = conn.prepareStatement(
                        "SELECT Id_Equipo FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?")) {
                        stmtEquipo.setInt(1, id);
                        ResultSet rsEquipo = stmtEquipo.executeQuery();
                        if (rsEquipo.next()) {
                            elementoId = rsEquipo.getInt("Id_Equipo");
                        }
                    }
                } else {
                    try (PreparedStatement stmtMaterial = conn.prepareStatement(
                        "SELECT N_Objeto FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?")) {
                        stmtMaterial.setInt(1, id);
                        ResultSet rsMaterial = stmtMaterial.executeQuery();
                        if (rsMaterial.next()) {
                            elementoId = rsMaterial.getInt("N_Objeto");
                        }
                    }
                }
                
                setElementoId(elementoId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar mantenimiento: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarMantenimiento() {
    if (validarFormulario()) {
        try {
            String tipoElemento = (String) cmbTipoElemento.getSelectedItem();
            ComboItem elementoSeleccionado = (ComboItem) cmbElemento.getSelectedItem();
            
            // Verificar si el elemento ya está en mantenimiento
            if (elementoYaEnMantenimiento(tipoElemento, elementoSeleccionado.getValue())) {
                JOptionPane.showMessageDialog(this, 
                    "El elemento seleccionado ya tiene un mantenimiento pendiente o en proceso",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String tipoMantenimiento = (String) cmbTipoMantenimiento.getSelectedItem();
            String nombre = txtNombre.getText();
            String descripcion = txtDescripcion.getText();
            String procedimiento = txtProcedimiento.getText();
            String repuestos = txtRepuestos.getText();
            double costo = txtCosto.getText().isEmpty() ? 0 : Double.parseDouble(txtCosto.getText());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaInicio = sdf.format(new Date());
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Iniciar transacción
                
                if (mantenimientoId == -1) {
                    // Crear nuevo mantenimiento
                    String query = "INSERT INTO Mantenimiento (nombre, tipo, tipo_elemento, fecha_inicio, " +
                                 "descripcion, procedimiento, repuestos_utilizados, costo, Id_Usuario_Responsable, estado) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pendiente')";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, nombre);
                        stmt.setString(2, tipoMantenimiento);
                        stmt.setString(3, tipoElemento);
                        stmt.setString(4, fechaInicio);
                        stmt.setString(5, descripcion);
                        stmt.setString(6, procedimiento);
                        stmt.setString(7, repuestos);
                        stmt.setDouble(8, costo);
                        stmt.setInt(9, currentUser.getId());
                        
                        int affectedRows = stmt.executeUpdate();
                        
                        if (affectedRows > 0 && elementoSeleccionado != null) {
                            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int idMantenimiento = generatedKeys.getInt(1);
                                    
                                    if (tipoElemento.equals("Equipo")) {
                                        insertarMantenimientoEquipo(conn, idMantenimiento, elementoSeleccionado.getValue());
                                        actualizarEstadoEquipo(conn, elementoSeleccionado.getValue(), "Mantenimiento");
                                    } else {
                                        insertarMantenimientoMaterial(conn, idMantenimiento, elementoSeleccionado.getValue());
                                        actualizarEstadoMaterial(conn, elementoSeleccionado.getValue(), true);
                                    }
                                    
                                    conn.commit();
                                    DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                                    DatabaseConnection.notifyDatabaseChanged(tipoElemento.equals("Equipo") ? "Equipos" : "Material_Adicional");
                                    
                                    JOptionPane.showMessageDialog(this, "Mantenimiento registrado exitosamente");
                                    dispose();
                                }
                            }
                        }
                    }
                } else {
                    // Actualizar mantenimiento existente
                    String query = "UPDATE Mantenimiento SET nombre = ?, tipo = ?, tipo_elemento = ?, " +
                                 "descripcion = ?, procedimiento = ?, repuestos_utilizados = ?, costo = ? " +
                                 "WHERE Id_Mantenimiento = ?";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, nombre);
                        stmt.setString(2, tipoMantenimiento);
                        stmt.setString(3, tipoElemento);
                        stmt.setString(4, descripcion);
                        stmt.setString(5, procedimiento);
                        stmt.setString(6, repuestos);
                        stmt.setDouble(7, costo);
                        stmt.setInt(8, mantenimientoId);
                        
                        int affectedRows = stmt.executeUpdate();
                        
                        if (affectedRows > 0 && elementoSeleccionado != null) {
                            if (tipoElemento.equals("Equipo")) {
                                actualizarRelacionEquipo(conn, mantenimientoId, elementoSeleccionado.getValue());
                                actualizarEstadoEquipo(conn, elementoSeleccionado.getValue(), "Mantenimiento");
                            } else {
                                actualizarRelacionMaterial(conn, mantenimientoId, elementoSeleccionado.getValue());
                                actualizarEstadoMaterial(conn, elementoSeleccionado.getValue(), true);
                            }
                            
                            conn.commit();
                            DatabaseConnection.notifyDatabaseChanged("Mantenimiento");
                            DatabaseConnection.notifyDatabaseChanged(tipoElemento.equals("Equipo") ? "Equipos" : "Material_Adicional");
                            
                            JOptionPane.showMessageDialog(this, "Mantenimiento actualizado exitosamente");
                            dispose();
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al guardar mantenimiento: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El costo debe ser un valor numérico", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
    private boolean elementoYaEnMantenimiento(String tipoElemento, int idElemento) {
    String query = "SELECT COUNT(*) FROM Mantenimiento m " +
                  (tipoElemento.equals("Equipo") ? 
                   "JOIN Mantenimiento_Equipo me ON m.Id_Mantenimiento = me.Id_Mantenimiento " +
                   "WHERE me.Id_Equipo = ? " :
                   "JOIN Mantenimiento_Material mm ON m.Id_Mantenimiento = mm.Id_Mantenimiento " +
                   "WHERE mm.N_Objeto = ? ") +
                  "AND m.estado IN ('Pendiente', 'En Proceso')";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setInt(1, idElemento);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

private void actualizarEstadoEquipo(Connection conn, int idEquipo, String estado) throws SQLException {
    String query = "UPDATE Equipos SET estado = ? WHERE Id_Equipo = ?";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, estado);
        stmt.setInt(2, idEquipo);
        stmt.executeUpdate();
    }
}

private void actualizarEstadoMaterial(Connection conn, int idMaterial, boolean danado) throws SQLException {
    String query = "UPDATE Material_Adicional SET daño = ? WHERE N_Objeto = ?";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setBoolean(1, danado);
        stmt.setInt(2, idMaterial);
        stmt.executeUpdate();
    }
}
    
    private void insertarMantenimientoEquipo(Connection conn, int idMantenimiento, int idEquipo) throws SQLException {
    String query = "INSERT INTO Mantenimiento_Equipo (Id_Mantenimiento, Id_Equipo) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, idMantenimiento);
        stmt.setInt(2, idEquipo);
        stmt.executeUpdate();
    }
}

private void insertarMantenimientoMaterial(Connection conn, int idMantenimiento, int idMaterial) throws SQLException {
    String query = "INSERT INTO Mantenimiento_Material (Id_Mantenimiento, N_Objeto) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, idMantenimiento);
        stmt.setInt(2, idMaterial);
        stmt.executeUpdate();
    }
}

private void actualizarRelacionEquipo(Connection conn, int idMantenimiento, int idEquipo) throws SQLException {
    String deleteQuery = "DELETE FROM Mantenimiento_Equipo WHERE Id_Mantenimiento = ?";
    try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
        stmt.setInt(1, idMantenimiento);
        stmt.executeUpdate();
    }
    insertarMantenimientoEquipo(conn, idMantenimiento, idEquipo);
}

private void actualizarRelacionMaterial(Connection conn, int idMantenimiento, int idMaterial) throws SQLException {
    String deleteQuery = "DELETE FROM Mantenimiento_Material WHERE Id_Mantenimiento = ?";
    try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
        stmt.setInt(1, idMantenimiento);
        stmt.executeUpdate();
    }
    insertarMantenimientoMaterial(conn, idMantenimiento, idMaterial);
}
    
    private boolean validarFormulario() {
        if (cmbElemento.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un elemento para el mantenimiento",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un nombre para el mantenimiento",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtDescripcion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una descripción del mantenimiento",
                "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
}