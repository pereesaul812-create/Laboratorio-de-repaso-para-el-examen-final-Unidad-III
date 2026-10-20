package usuariocrud;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UsuarioCRUD extends JFrame {

    // Configuración para XAMPP (Sin contraseña por defecto)
    private final String bd = "bd_shuttles";
    private final String urlServidor = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String urlBD = "jdbc:mysql://localhost:3306/" + bd + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String user = "root";
    private final String password = ""; // XAMPP no usa clave por defecto
    private Connection conexion;

    // Componentes de la Interfaz Gráfica (Shuttles)
    private JTextField idTextField, pasajeroTextField, destinoTextField, precioTextField;
    private JButton conectarButton, guardarButton, actualizarButton, eliminarButton, limpiarButton;
    private JLabel estadoLabel;
    private JTable tablaShuttles;
    private DefaultTableModel modeloTabla;

    public UsuarioCRUD() {
        super("Atitrans - Sistema de Reservas y Venta de Shuttles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(241, 245, 249));

        construirInterfazVisual();
    }

    private void construirInterfazVisual() {
        // --- CABECERA SUPERIOR ---
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(new Color(15, 23, 42));
        panelHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("🚐 CONTROL DE SHUTTLES Y VIAJES");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelEstado.setOpaque(false);

        estadoLabel = new JLabel("● Desconectado");
        estadoLabel.setForeground(new Color(248, 113, 113));
        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        conectarButton = crearBoton("1. Conectar a XAMPP", new Color(14, 165, 233));
        panelEstado.add(estadoLabel);
        panelEstado.add(conectarButton);

        panelHeader.add(lblTitulo, BorderLayout.WEST);
        panelHeader.add(panelEstado, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // --- FORMULARIO Y BOTONES ---
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 12, 12));
        panelCampos.setBackground(Color.WHITE);
        panelCampos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Información del Ticket / Viaje"),
                new EmptyBorder(12, 15, 12, 15)
        ));

        idTextField = new JTextField();
        idTextField.setEditable(false);
        idTextField.setBackground(new Color(226, 232, 240));
        pasajeroTextField = new JTextField();
        destinoTextField = new JTextField();
        precioTextField = new JTextField();

        panelCampos.add(new JLabel("ID Ticket:"));
        panelCampos.add(idTextField);
        panelCampos.add(new JLabel("Pasajero:"));
        panelCampos.add(pasajeroTextField);
        panelCampos.add(new JLabel("Destino:"));
        panelCampos.add(destinoTextField);
        panelCampos.add(new JLabel("Precio ($):"));
        panelCampos.add(precioTextField);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setOpaque(false);

        guardarButton = crearBoton("Vender / Reservar (INSERT)", new Color(16, 185, 129));
        actualizarButton = crearBoton("Modificar (UPDATE)", new Color(245, 158, 11));
        eliminarButton = crearBoton("Cancelar Viaje (DELETE)", new Color(239, 68, 68));
        limpiarButton = crearBoton("Limpiar", new Color(100, 116, 139));

        panelBotones.add(guardarButton);
        panelBotones.add(actualizarButton);
        panelBotones.add(eliminarButton);
        panelBotones.add(limpiarButton);

        panelCentral.add(panelCampos, BorderLayout.NORTH);
        panelCentral.add(panelBotones, BorderLayout.CENTER);

        // --- TABLA DE REGISTROS (SELECT) ---
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Pasajero", "Destino", "Precio ($)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaShuttles = new JTable(modeloTabla);
        tablaShuttles.setRowHeight(26);
        tablaShuttles.getTableHeader().setBackground(new Color(203, 213, 225));
        tablaShuttles.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tablaShuttles);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Shuttles Reservados (SELECT)"));
        scrollPane.setPreferredSize(new Dimension(760, 220));

        panelCentral.add(scrollPane, BorderLayout.SOUTH);
        add(panelCentral, BorderLayout.CENTER);

        // --- EVENTOS ---
        conectarButton.addActionListener(e -> conectarBaseDeDatos());
        guardarButton.addActionListener(e -> insertarReserva());
        actualizarButton.addActionListener(e -> modificarReserva());
        eliminarButton.addActionListener(e -> borrarReserva());
        limpiarButton.addActionListener(e -> limpiarFormulario());

        tablaShuttles.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaShuttles.getSelectedRow();
            if (fila != -1) {
                idTextField.setText(modeloTabla.getValueAt(fila, 0).toString());
                pasajeroTextField.setText(modeloTabla.getValueAt(fila, 1).toString());
                destinoTextField.setText(modeloTabla.getValueAt(fila, 2).toString());
                precioTextField.setText(modeloTabla.getValueAt(fila, 3).toString());
            }
        });
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- CONEXIÓN AUTOMÁTICA A XAMPP Y CREACIÓN DE TABLA ---
    private void conectarBaseDeDatos() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 1. Crea la base de datos y la tabla automáticamente si no existen
            Connection temp = DriverManager.getConnection(urlServidor, user, password);
            Statement stmt = temp.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + bd);
            stmt.executeUpdate("USE " + bd);
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS shuttles ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "pasajero VARCHAR(100) NOT NULL, "
                    + "destino VARCHAR(100) NOT NULL, "
                    + "precio DOUBLE NOT NULL)");
            temp.close();

            // 2. Conexión formal con transacciones activas (AutoCommit = false para usar rollback)
            conexion = DriverManager.getConnection(urlBD, user, password);
            conexion.setAutoCommit(false);

            estadoLabel.setText("● Conectado a XAMPP");
            estadoLabel.setForeground(new Color(74, 222, 128));
            JOptionPane.showMessageDialog(this, "¡Conexión exitosa con XAMPP! Base de datos 'bd_shuttles' lista.");
            consultarReservas();

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Falta agregar el archivo JAR en Libraries.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            mostrarErrorSQL(ex);
        }
    }

    // --- CREATE (INSERT) ---
    private void insertarReserva() {
        if (!validarConexion() || !validarCampos()) return;

        String sql = "INSERT INTO shuttles (pasajero, destino, precio) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pasajeroTextField.getText().trim());
            ps.setString(2, destinoTextField.getText().trim());
            ps.setDouble(3, Double.parseDouble(precioTextField.getText().trim()));

            ps.executeUpdate();
            conexion.commit(); // Transacción segura

            JOptionPane.showMessageDialog(this, "¡Reserva de Shuttle guardada con éxito!");
            limpiarFormulario();
            consultarReservas();
        } catch (SQLException ex) {
            ejecutarRollback();
            mostrarErrorSQL(ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El campo 'Precio' debe ser un número válido.", "Formato incorrecto", JOptionPane.WARNING_MESSAGE);
        }
    }

    // --- READ (SELECT) ---
    private void consultarReservas() {
        if (!validarConexion()) return;
        modeloTabla.setRowCount(0);

        String sql = "SELECT id, pasajero, destino, precio FROM shuttles";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("pasajero"),
                    rs.getString("destino"),
                    rs.getDouble("precio")
                });
            }
        } catch (SQLException ex) {
            mostrarErrorSQL(ex);
        }
    }

    // --- UPDATE ---
    private void modificarReserva() {
        if (!validarConexion()) return;
        if (idTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro de la tabla para modificar.");
            return;
        }
        if (!validarCampos()) return;

        String sql = "UPDATE shuttles SET pasajero = ?, destino = ?, precio = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, pasajeroTextField.getText().trim());
            ps.setString(2, destinoTextField.getText().trim());
            ps.setDouble(3, Double.parseDouble(precioTextField.getText().trim()));
            ps.setInt(4, Integer.parseInt(idTextField.getText()));

            ps.executeUpdate();
            conexion.commit();

            JOptionPane.showMessageDialog(this, "¡Reserva actualizada correctamente!");
            limpiarFormulario();
            consultarReservas();
        } catch (SQLException ex) {
            ejecutarRollback();
            mostrarErrorSQL(ex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser numérico.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // --- DELETE ---
    private void borrarReserva() {
        if (!validarConexion()) return;
        if (idTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva para eliminar.");
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Deseas cancelar esta reserva de Shuttle?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM shuttles WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idTextField.getText()));

            ps.executeUpdate();
            conexion.commit();

            JOptionPane.showMessageDialog(this, "¡Reserva cancelada con éxito!");
            limpiarFormulario();
            consultarReservas();
        } catch (SQLException ex) {
            ejecutarRollback();
            mostrarErrorSQL(ex);
        }
    }

    private void ejecutarRollback() {
        try {
            if (conexion != null) conexion.rollback();
        } catch (SQLException e) {
            System.err.println("Rollback error: " + e.getMessage());
        }
    }

    private void mostrarErrorSQL(SQLException ex) {
        JOptionPane.showMessageDialog(this,
                "Error SQL: " + ex.getMessage() + "\nCódigo: " + ex.getErrorCode() + "\nEstado: " + ex.getSQLState(),
                "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
    }

    private boolean validarConexion() {
        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Primero haz clic en '1. Conectar a XAMPP'.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validarCampos() {
        if (pasajeroTextField.getText().trim().isEmpty() ||
            destinoTextField.getText().trim().isEmpty() ||
            precioTextField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos (Pasajero, Destino y Precio).", "Vacíos", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        idTextField.setText("");
        pasajeroTextField.setText("");
        destinoTextField.setText("");
        precioTextField.setText("");
        tablaShuttles.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UsuarioCRUD().setVisible(true));
    }
}