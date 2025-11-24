package views.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import models.DatabaseConnection;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import models.User;

public class LabsPanel extends JPanel implements DatabaseConnection.DatabaseChangeListener {
    private JTable labsTable;
    private JButton btnAddLab, btnEditLab, btnDeleteLab, btnRefresh, btnManageEquipos, btnManageMateriales;
    private final Color color1 = new Color(249, 45, 168);
    private final Color color2 = new Color(255, 209, 12);
    private User currentUser;

    public LabsPanel(User user) {
        this.currentUser = user;
        DatabaseConnection.addListener(this);
        initUI();
        loadLabsData();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel mainPanel = createMainPanel();
        add(mainPanel);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 180));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createTitleLabel(), BorderLayout.NORTH);
        mainPanel.add(createTableScrollPane(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Gestión de Laboratorios", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        return titleLabel;
    }

    private JScrollPane createTableScrollPane() {
        labsTable = new JTable();
        labsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        labsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        labsTable.setRowHeight(25);

        JTableHeader header = labsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(labsTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        btnAddLab = createStyledButton("Agregar Laboratorio", new Color(46, 125, 50), this::showAddLabDialog);
        btnEditLab = createStyledButton("Editar Laboratorio", new Color(2, 119, 189), this::showEditLabDialog);
        btnDeleteLab = createStyledButton("Eliminar Laboratorio", new Color(198, 40, 40), this::deleteSelectedLab);
        btnManageEquipos = createStyledButton("Gestionar Equipos", new Color(142, 36, 170), this::manageEquipos);
        btnManageMateriales = createStyledButton("Gestionar Materiales", new Color(100, 150, 200), this::manageMateriales);
        btnRefresh = createStyledButton("Actualizar", new Color(109, 76, 65), e -> loadLabsData());

        buttonPanel.add(btnAddLab);
        buttonPanel.add(btnEditLab);
        buttonPanel.add(btnDeleteLab);
        buttonPanel.add(btnManageEquipos);
        buttonPanel.add(btnManageMateriales);
        buttonPanel.add(btnRefresh);

        return buttonPanel;
    }
    
    private void manageMateriales(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Por favor seleccione un laboratorio");
            return;
        }
        new MaterialesDialog(
            (JFrame)SwingUtilities.getWindowAncestor(this), 
            (int) labsTable.getValueAt(selectedRow, 0), 
            (String) labsTable.getValueAt(selectedRow, 1),
            currentUser
        ).setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor, ActionListener listener) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                g2d.drawString(getText(), (getWidth() - (int) r.getWidth()) / 2, 
                    (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent());
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setPreferredSize(new Dimension(180, 40));
        button.addActionListener(listener);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override public void mouseExited(MouseEvent e) { button.setCursor(Cursor.getDefaultCursor()); }
        });

        return button;
    }

    private void manageEquipos(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Por favor seleccione un laboratorio");
            return;
        }
        new EquiposDialog(null, (int) labsTable.getValueAt(selectedRow, 0), 
                         (String) labsTable.getValueAt(selectedRow, 1)).setVisible(true);
    }

    private void loadLabsData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id_Laboratorio, nombre, capacidad, estado FROM Laboratorios")) {
            
            DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Nombre", "Capacidad", "Estado"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("Id_Laboratorio"),
                    rs.getString("nombre"),
                    rs.getInt("capacidad"),
                    formatEstado(rs.getString("estado"))
                });
            }
            
            labsTable.setModel(model);
            
            JTableHeader header = labsTable.getTableHeader();
            header.setFont(new Font("Arial", Font.BOLD, 14));
            header.setBackground(new Color(70, 130, 180));
            header.setForeground(Color.BLACK);
            
            labsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setForeground(Color.BLACK);
                    if (isSelected) {
                        c.setBackground(new Color(200, 200, 255));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    return c;
                }
            });
            
            labsTable.setBackground(Color.WHITE);
            labsTable.setGridColor(new Color(220, 220, 220));
            
        } catch (SQLException e) {
            showError("Error al cargar laboratorios: " + e.getMessage());
        }
    }

    private String formatEstado(String estado) {
        return estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
    }

    private void showAddLabDialog(ActionEvent evt) {
        LabDialog dialog = new LabDialog(null, "Agregar Laboratorio");
        if (dialog.showDialog()) {
            addNewLab(dialog.getNombre(), dialog.getCapacidad(), dialog.getEstado(), dialog.getDescripcion());
        }
    }

    private void showEditLabDialog(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Por favor seleccione un laboratorio para editar");
            return;
        }

        int id = (int) labsTable.getValueAt(selectedRow, 0);
        LabDialog dialog = new LabDialog(null, "Editar Laboratorio");
        dialog.setData(
            (String) labsTable.getValueAt(selectedRow, 1),
            (int) labsTable.getValueAt(selectedRow, 2),
            (String) labsTable.getValueAt(selectedRow, 3),
            getLabDescription(id)
        );

        if (dialog.showDialog()) {
            updateLab(id, dialog.getNombre(), dialog.getCapacidad(), dialog.getEstado(), dialog.getDescripcion());
        }
    }

    private String getLabDescription(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT descripcion FROM Laboratorios WHERE Id_Laboratorio = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("descripcion") : "";
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void deleteSelectedLab(ActionEvent evt) {
        int selectedRow = labsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Por favor seleccione un laboratorio para eliminar");
            return;
        }

        int id = (int) labsTable.getValueAt(selectedRow, 0);
        String nombre = (String) labsTable.getValueAt(selectedRow, 1);

        if (confirmAction("¿Está seguro que desea eliminar el laboratorio:\n" + nombre + "?")) {
            deleteLab(id);
        }
    }

    private void deleteLab(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Desasociar equipos
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Equipos SET Id_Laboratorio = NULL WHERE Id_Laboratorio = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // 2. Desasociar materiales
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Material_Adicional SET Id_Laboratorio = NULL WHERE Id_Laboratorio = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // 3. Eliminar reservas
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Reservas WHERE Nro_Laboratorio = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                // Ignorar si la tabla no existe
                if (!e.getMessage().contains("Table 'gestor.reservas' doesn't exist")) {
                    throw e;
                }
            }
            
            // 4. Eliminar laboratorio
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Laboratorios WHERE Id_Laboratorio = ?")) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    showMessage("Laboratorio eliminado exitosamente");
                    DatabaseConnection.notifyDatabaseChanged("Laboratorios");
                } else {
                    conn.rollback();
                    showError("No se encontró el laboratorio a eliminar");
                }
            }
        } catch (SQLException e) {
            showError("Error al eliminar laboratorio: " + e.getMessage());
        }
    }

    private void addNewLab(String nombre, String capacidad, String estado, String descripcion) {
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(
             "INSERT INTO Laboratorios (nombre, capacidad, estado, descripcion) VALUES (?, ?, ?, ?)")) {
        
        stmt.setString(1, nombre);
        stmt.setInt(2, Integer.parseInt(capacidad));
        
        // Modificar esta línea para enviar el estado exacto como está en el ENUM
        String estadoFormateado = estado.substring(0, 1).toUpperCase() + estado.substring(1).toLowerCase();
        stmt.setString(3, estadoFormateado);
        
        stmt.setString(4, descripcion);
        
        stmt.executeUpdate();
        showMessage("Laboratorio agregado exitosamente");
        DatabaseConnection.notifyDatabaseChanged("Laboratorios");
    } catch (SQLException | NumberFormatException e) {
        showError("Error al agregar laboratorio: " + e.getMessage());
    }
}

    private void updateLab(int id, String nombre, String capacidad, String estado, String descripcion) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE Laboratorios SET nombre = ?, capacidad = ?, estado = ?, descripcion = ? WHERE Id_Laboratorio = ?")) {
            
            stmt.setString(1, nombre);
            stmt.setInt(2, Integer.parseInt(capacidad));
            stmt.setString(3, estado);
            stmt.setString(4, descripcion);
            stmt.setInt(5, id);
            
            if (stmt.executeUpdate() > 0) {
                showMessage("Laboratorio actualizado exitosamente");
                DatabaseConnection.notifyDatabaseChanged("Laboratorios");
            } else {
                showError("No se encontró el laboratorio a actualizar");
            }
        } catch (SQLException e) {
            showError("Error al actualizar laboratorio: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("La capacidad debe ser un número válido");
        }
    }

    @Override
    public void onDatabaseChanged(String tableChanged) {
        if (tableChanged.equals("Laboratorios")) loadLabsData();
    }

    // Helper methods for dialogs
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmAction(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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
            panel.add(new JLabel("Nombre:"));
            cbNombre = new JComboBox<>(new String[]{"Laboratorio 1", "Laboratorio 2", "Laboratorio 3", 
                "Laboratorio 4", "Laboratorio 5", "Laboratorio 6", "Laboratorio 7"});
            panel.add(cbNombre);

            panel.add(new JLabel("Capacidad:"));
            txtCapacidad = new JTextField("30");
            panel.add(txtCapacidad);

            panel.add(new JLabel("Estado:"));
            cbEstado = new JComboBox<>(new String[]{"Disponible", "En Mantenimiento", "Fuera de Servicio"});
            panel.add(cbEstado);

            panel.add(new JLabel("Descripción:"));
            txtDescripcion = new JTextArea(3, 20);
            panel.add(new JScrollPane(txtDescripcion));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnOk = new JButton("Aceptar");
            btnOk.addActionListener(e -> { confirmed = true; dispose(); });
            buttonPanel.add(btnOk);

            JButton btnCancel = new JButton("Cancelar");
            btnCancel.addActionListener(e -> dispose());
            buttonPanel.add(btnCancel);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public boolean showDialog() { setVisible(true); return confirmed; }

        public void setData(String nombre, int capacidad, String estado, String descripcion) {
            cbNombre.setSelectedItem(nombre);
            txtCapacidad.setText(String.valueOf(capacidad));
            cbEstado.setSelectedItem(estado);
            txtDescripcion.setText(descripcion);
        }

        public String getNombre() { return (String) cbNombre.getSelectedItem(); }
        public String getCapacidad() { return txtCapacidad.getText(); }
        public String getEstado() { return (String) cbEstado.getSelectedItem(); }
        public String getDescripcion() { return txtDescripcion.getText(); }
    }

    private class EquiposDialog extends JDialog {
        private JTable equiposTable;
        private int labId;

        public EquiposDialog(JFrame parent, int labId, String labName) {
            super(parent, "Equipos del Laboratorio " + labName, true);
            this.labId = labId;
            initUI();
            loadEquiposData();
        }

        private void initUI() {
            setSize(800, 600);
            setLocationRelativeTo(getParent());
            
            equiposTable = new JTable();
            equiposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            buttonPanel.add(createButton("Agregar Equipo", this::addEquipo));
            buttonPanel.add(createButton("Editar Equipo", this::editEquipo));
            buttonPanel.add(createButton("Eliminar Equipo", this::deleteEquipo));

            add(new JScrollPane(equiposTable), BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private JButton createButton(String text, ActionListener listener) {
            JButton button = new JButton(text);
            button.addActionListener(listener);
            return button;
        }

        private void loadEquiposData() {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT Id_Equipo, marca, modelo, numero_de_serie, estado FROM Equipos WHERE id_laboratorio = ?")) {
                
                stmt.setInt(1, labId);
                ResultSet rs = stmt.executeQuery();
                
                DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Marca", "Modelo", "N° Serie", "Estado"}, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("Id_Equipo"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("numero_de_serie"),
                        convertirEstadoParaVisual(rs.getString("estado"))
                    });
                }
                equiposTable.setModel(model);
            } catch (SQLException e) {
                showError("Error al cargar equipos: " + e.getMessage());
            }
        }

        private String convertirEstadoParaVisual(String estadoBD) {
            if (estadoBD == null) return "Desconocido";
            switch(estadoBD) {
                case "Operativo": return "Operativo";
                case "Mantenimiento": return "En Mantenimiento";
                case "Dañado": return "Dañado";
                default: return estadoBD;
            }
        }

        private String convertirEstadoParaBD(String estadoVisual) {
            if (estadoVisual == null) return "Operativo";
            switch(estadoVisual.toLowerCase()) {
                case "operativo":
                case "en operativo": return "Operativo";
                case "mantenimiento":
                case "en mantenimiento": return "Mantenimiento";
                case "dañado":
                case "daño": return "Dañado";
                default: return "Operativo";
            }
        }

        private void addEquipo(ActionEvent evt) {
    EquipoForm form = new EquipoForm("Agregar Equipo", null);
    if (form.showDialog()) {
        try {
            // Consulta corregida (eliminado el campo Tipo)
            executeUpdate("INSERT INTO Equipos (marca, modelo, numero_de_serie, procesador, ram, " +
                        "almacenamiento, SO, estado, id_laboratorio) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", form.getData());
            showMessage("Equipo agregado exitosamente");
            loadEquiposData();
            DatabaseConnection.notifyDatabaseChanged("Equipos");
        } catch (SQLException e) {
            showError("Error al agregar equipo: " + e.getMessage());
        }
    }
}

        private void editEquipo(ActionEvent evt) {
            int selectedRow = equiposTable.getSelectedRow();
            if (selectedRow == -1) {
                showWarning("Por favor seleccione un equipo");
                return;
            }

            int id = (int) equiposTable.getValueAt(selectedRow, 0);
            String[] detalles = getEquipoDetails(id);
            if (detalles == null) return;

            EquipoForm form = new EquipoForm("Editar Equipo", new String[]{
                (String) equiposTable.getValueAt(selectedRow, 1),
                (String) equiposTable.getValueAt(selectedRow, 2),
                (String) equiposTable.getValueAt(selectedRow, 3),
                detalles[0], detalles[1], detalles[2], detalles[3],
                convertirEstadoParaBD((String) equiposTable.getValueAt(selectedRow, 4))
            });

            if (form.showDialog()) {
                try {
                    executeUpdate("UPDATE Equipos SET marca = ?, modelo = ?, procesador = ?, ram = ?, " +
                                "almacenamiento = ?, SO = ?, estado = ? WHERE Id_Equipo = ?", 
                                form.getDataWithId(id));
                    showMessage("Equipo actualizado exitosamente");
                    loadEquiposData();
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                } catch (SQLException e) {
                    showError("Error al actualizar equipo: " + e.getMessage());
                }
            }
        }

        private String[] getEquipoDetails(int id) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT procesador, ram, almacenamiento, SO FROM Equipos WHERE Id_Equipo = ?")) {
                
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? new String[]{
                    rs.getString("procesador"),
                    rs.getString("ram"),
                    rs.getString("almacenamiento"),
                    rs.getString("SO")
                } : null;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        private void deleteEquipo(ActionEvent evt) {
            int selectedRow = equiposTable.getSelectedRow();
            if (selectedRow == -1) {
                showWarning("Por favor seleccione un equipo");
                return;
            }

            int id = (int) equiposTable.getValueAt(selectedRow, 0);
            String marca = (String) equiposTable.getValueAt(selectedRow, 1);
            String modelo = (String) equiposTable.getValueAt(selectedRow, 2);

            if (confirmAction("¿Está seguro que desea eliminar el equipo:\n" + marca + " " + modelo + "?")) {
                try {
                    executeUpdate("DELETE FROM Equipos WHERE Id_Equipo = ?", new Object[]{id});
                    showMessage("Equipo eliminado exitosamente");
                    loadEquiposData();
                    DatabaseConnection.notifyDatabaseChanged("Equipos");
                } catch (SQLException e) {
                    showError("Error al eliminar equipo: " + e.getMessage());
                }
            }
        }

        private void executeUpdate(String sql, Object[] params) throws SQLException {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            }
        }

        private class EquipoForm extends JDialog {
            private JTextField[] fields = new JTextField[7];
            private JComboBox<String> cbEstado;
            private boolean confirmed = false;

            public EquipoForm(String title, String[] initialValues) {
                super(EquiposDialog.this, title, true);
                setSize(500, 400);
                setLocationRelativeTo(EquiposDialog.this);
                
                String[] labels = {"Marca:", "Modelo:", "N° Serie:", "Procesador:", "RAM:", "Almacenamiento:", "Sistema Operativo:"};
                JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

                for (int i = 0; i < fields.length; i++) {
                    panel.add(new JLabel(labels[i]));
                    fields[i] = new JTextField();
                    if (initialValues != null && i < initialValues.length) {
                        fields[i].setText(initialValues[i]);
                        if (i == 2) fields[i].setEditable(false); // N° Serie no editable
                    }
                    panel.add(fields[i]);
                }

                panel.add(new JLabel("Estado:"));
                cbEstado = new JComboBox<>(new String[]{"Operativo", "Mantenimiento", "Dañado"});
                if (initialValues != null && initialValues.length > 7) {
                    cbEstado.setSelectedItem(initialValues[7]);
                }
                panel.add(cbEstado);

                JButton btnOk = new JButton("Aceptar");
                btnOk.addActionListener(e -> { confirmed = true; dispose(); });

                JButton btnCancel = new JButton("Cancelar");
                btnCancel.addActionListener(e -> dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.add(btnOk);
                buttonPanel.add(btnCancel);

                add(panel, BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);
            }

            public boolean showDialog() { setVisible(true); return confirmed; }

            public Object[] getData() {
                return new Object[]{
                    fields[0].getText(), fields[1].getText(), fields[2].getText(),
                    fields[3].getText(), fields[4].getText(), fields[5].getText(),
                    fields[6].getText(), cbEstado.getSelectedItem().toString(),
                    labId
                };
            }

            public Object[] getDataWithId(int id) {
                return new Object[]{
                    fields[0].getText(), fields[1].getText(), fields[3].getText(),
                    fields[4].getText(), fields[5].getText(), fields[6].getText(),
                    cbEstado.getSelectedItem().toString(), id
                };
            }
        }
    }
}