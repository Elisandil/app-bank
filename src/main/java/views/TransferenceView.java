package views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import models.Account;
import presenters.TransferencePresenter;
import interfaces.TransferenceContract;

public class TransferenceView extends JFrame implements TransferenceContract.View {
    private final TransferenceContract.Presenter presenter;
    private JComboBox<String> cuentaOrigenCombo;
    private JTextField cuentaDestinoField;
    private JTextField cantidadField;
    private JTextField conceptoField;
    private JButton transferirButton;
    private JButton cancelarButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private java.util.List<Account> cuentasDisponibles;
    
    public TransferenceView() {
        presenter = new TransferencePresenter(this);
        initializeUI();
        presenter.cargarCuentas();
    }
    
    private void initializeUI() {
        setTitle("Banco - Nueva Transferencia");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(500, 400);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        JLabel titleLabel = new JLabel("Nueva Transferencia");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 120, 180));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Cuenta origen
        gbc.gridy++;
        mainPanel.add(new JLabel("Cuenta origen:"), gbc);
        gbc.gridx = 1;
        cuentaOrigenCombo = new JComboBox<>();
        cuentaOrigenCombo.setPreferredSize(new Dimension(250, 25));
        mainPanel.add(cuentaOrigenCombo, gbc);
        
        // Cuenta destino
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Cuenta destino:"), gbc);
        gbc.gridx = 1;
        cuentaDestinoField = new JTextField(20);
        mainPanel.add(cuentaDestinoField, gbc);
        
        // Cantidad
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Cantidad (€):"), gbc);
        gbc.gridx = 1;
        cantidadField = new JTextField(20);
        mainPanel.add(cantidadField, gbc);
        
        // Concepto
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Concepto:"), gbc);
        gbc.gridx = 1;
        conceptoField = new JTextField(20);
        mainPanel.add(conceptoField, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        transferirButton = new JButton("Realizar Transferencia");
        transferirButton.setBackground(new Color(0, 120, 180));
        transferirButton.setForeground(Color.WHITE);
        transferirButton.addActionListener(e -> realizarTransferencia());
        
        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> dispose());
        
        buttonPanel.add(transferirButton);
        buttonPanel.add(cancelarButton);
        mainPanel.add(buttonPanel, gbc);
        
        // Progress bar
        gbc.gridy++;
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, gbc);
        
        // Status label
        gbc.gridy++;
        statusLabel = new JLabel("", SwingConstants.CENTER);
        mainPanel.add(statusLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    
    private void realizarTransferencia() {
        
        try {
            String cuentaOrigen = getSelectedCuentaNumero();
            String cuentaDestino = cuentaDestinoField.getText().trim();
            double cantidad = Double.parseDouble(cantidadField.getText().trim());
            String concepto = conceptoField.getText().trim();
            
            presenter.realizarTransferencia(cuentaOrigen, cuentaDestino, 
                    cantidad, concepto);
        } catch (NumberFormatException e) {
            mostrarError("Ingrese una cantidad válida");
        }
    }
    
    private String getSelectedCuentaNumero() {
        int selectedIndex = cuentaOrigenCombo.getSelectedIndex();
        
        if (selectedIndex >= 0 && cuentasDisponibles != null && 
                selectedIndex < cuentasDisponibles.size()) {
            return cuentasDisponibles.get(selectedIndex).getNumeroCuenta();
        }
        return null;
    }
    
    @Override
    public void mostrarCuentasOrigen(java.util.List<Account> cuentas) {
        
        SwingUtilities.invokeLater(() -> {
            this.cuentasDisponibles = cuentas;
            cuentaOrigenCombo.removeAllItems();
            
            for (Account cuenta : cuentas) {
                String item = String.format("%s - %.2f €", 
                    formatearNumeroCuenta(cuenta.getNumeroCuenta()), 
                    cuenta.getSaldo());
                cuentaOrigenCombo.addItem(item);
            }
        });
    }
    
    private String formatearNumeroCuenta(String numeroCuenta) {
        
        if (numeroCuenta.length() > 4) {
            String ultimosDigitos = numeroCuenta
                    .substring(numeroCuenta.length() - 4);
            return "**** " + ultimosDigitos;
        }
        return numeroCuenta;
    }
    
    @Override
    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, mensaje, "Error", 
                    JOptionPane.ERROR_MESSAGE);
        });
    }
    
    @Override
    public void mostrarExito(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setForeground(new Color(0, 150, 0));
            JOptionPane.showMessageDialog(this, mensaje, "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    @Override
    public void mostrarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            transferirButton.setEnabled(false);
            statusLabel.setText("Procesando transferencia...");
            statusLabel.setForeground(Color.BLUE);
        });
    }
    
    @Override
    public void ocultarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            transferirButton.setEnabled(true);
        });
    }
    
    @Override
    public void limpiarFormulario() {
        SwingUtilities.invokeLater(() -> {
            cuentaDestinoField.setText("");
            cantidadField.setText("");
            conceptoField.setText("");
            
            if (cuentaOrigenCombo.getItemCount() > 0) {
                cuentaOrigenCombo.setSelectedIndex(0);
            }
        });
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (presenter != null) {
            presenter.onDestroy();
        }
        super.finalize();
    }
}
