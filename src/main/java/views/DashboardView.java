package views;

import interfaces.DashboardContract;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import models.Account;
import models.Transaction;
import models.User;
import presenters.DashboardPresenter;

public class DashboardView extends JFrame implements DashboardContract.View {
    private final DashboardContract.Presenter presenter;
    private JLabel welcomeLabel;
    private JPanel cuentasPanel;
    private JPanel transaccionesPanel;
    private JButton transferenciasButton;
    private JButton historialButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public DashboardView() {
        presenter = new DashboardPresenter(this);
        initializeUI();
        presenter.cargarDatos();
    }
    
    private void initializeUI() {
        setTitle("Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);
        
        // Panel superior con bienvenida
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0, 120, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        welcomeLabel = new JLabel("Bienvenido", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Botones de navegación
        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.setOpaque(false);
        
        transferenciasButton = new JButton("Transferencias");
        transferenciasButton.setBackground(Color.WHITE);
        transferenciasButton.setForeground(new Color(0, 120, 180));
        transferenciasButton.addActionListener(e -> presenter.onTransferenciasClick());
        
        historialButton = new JButton("Historial");
        historialButton.setBackground(Color.WHITE);
        historialButton.setForeground(new Color(0, 120, 180));
        historialButton.addActionListener(e -> presenter.onHistorialClick());
        
        navPanel.add(transferenciasButton);
        navPanel.add(historialButton);
        topPanel.add(navPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central con contenido
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de cuentas
        JPanel cuentasContainer = new JPanel(new BorderLayout());
        cuentasContainer.setBorder(BorderFactory.createTitledBorder("Mis Cuentas"));
        
        cuentasPanel = new JPanel();
        cuentasPanel.setLayout(new BoxLayout(cuentasPanel, BoxLayout.Y_AXIS));
        JScrollPane cuentasScroll = new JScrollPane(cuentasPanel);
        cuentasScroll.setPreferredSize(new Dimension(350, 200));
        cuentasContainer.add(cuentasScroll, BorderLayout.CENTER);
        
        // Panel de transacciones
        JPanel transaccionesContainer = new JPanel(new BorderLayout());
        transaccionesContainer.setBorder(BorderFactory.createTitledBorder("Últimas Transacciones"));
        
        transaccionesPanel = new JPanel();
        transaccionesPanel.setLayout(new BoxLayout(transaccionesPanel, BoxLayout.Y_AXIS));
        JScrollPane transaccionesScroll = new JScrollPane(transaccionesPanel);
        transaccionesScroll.setPreferredSize(new Dimension(350, 200));
        transaccionesContainer.add(transaccionesScroll, BorderLayout.CENTER);
        
        centerPanel.add(cuentasContainer);
        centerPanel.add(transaccionesContainer);
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior con estado
        JPanel bottomPanel = new JPanel(new FlowLayout());
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        
        statusLabel = new JLabel("");
        bottomPanel.add(progressBar);
        bottomPanel.add(statusLabel);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    @Override
    public void mostrarUsuario(User usuario) {
        SwingUtilities.invokeLater(() -> {
            welcomeLabel.setText("Bienvenido, " + usuario.getNombre());
        });
    }
    
    @Override
    public void mostrarCuentas(java.util.List<Account> cuentas) {
        SwingUtilities.invokeLater(() -> {
            cuentasPanel.removeAll();
            
            for (Account cuenta : cuentas) {
                JPanel cuentaPanel = createCuentaPanel(cuenta);
                cuentasPanel.add(cuentaPanel);
                cuentasPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            cuentasPanel.revalidate();
            cuentasPanel.repaint();
        });
    }
    
    private JPanel createCuentaPanel(Account cuenta) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        panel.setBackground(Color.WHITE);
        
        // Información de la cuenta
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel numeroLabel = new JLabel(formatearNumeroCuenta(cuenta.getNumeroCuenta()));
        numeroLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel tipoLabel = new JLabel("Cuenta " + cuenta.getTipo().toString().toLowerCase());
        tipoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        tipoLabel.setForeground(Color.GRAY);
        
        infoPanel.add(numeroLabel);
        infoPanel.add(tipoLabel);
        
        // Saldo
        JLabel saldoLabel = new JLabel(String.format("%.2f €", cuenta.getSaldo()));
        saldoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        saldoLabel.setForeground(new Color(0, 120, 180));
        saldoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(saldoLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    @Override
    public void mostrarUltimasTransacciones(java.util.List<Transaction> transacciones) {
        SwingUtilities.invokeLater(() -> {
            transaccionesPanel.removeAll();
            
            if (transacciones.isEmpty()) {
                JLabel noTransaccionesLabel = new JLabel("No hay transacciones recientes");
                noTransaccionesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noTransaccionesLabel.setForeground(Color.GRAY);
                transaccionesPanel.add(noTransaccionesLabel);
            } else {
                for (Transaction transaccion : transacciones) {
                    JPanel transaccionPanel = createTransaccionPanel(transaccion);
                    transaccionesPanel.add(transaccionPanel);
                    transaccionesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
            
            transaccionesPanel.revalidate();
            transaccionesPanel.repaint();
        });
    }
    
    private JPanel createTransaccionPanel(Transaction transaccion) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.setBackground(Color.WHITE);
        
        // Información de la transacción
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel conceptoLabel = new JLabel(transaccion.getConcepto());
        conceptoLabel.setFont(new Font("Arial", Font.BOLD, 11));
        
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        JLabel fechaLabel = new JLabel(transaccion.getFecha().format(formatter));
        fechaLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        fechaLabel.setForeground(Color.GRAY);
        
        infoPanel.add(conceptoLabel);
        infoPanel.add(fechaLabel);
        
        // Cantidad
        String signo = transaccion.getTipo() == Transaction.TipoTransaccion.INGRESO ? "+" : "-";
        Color color = transaccion.getTipo() == Transaction.TipoTransaccion.INGRESO ? 
                     new Color(0, 150, 0) : new Color(200, 0, 0);
        
        JLabel cantidadLabel = new JLabel(String.format("%s%.2f €", signo, transaccion.getCantidad()));
        cantidadLabel.setFont(new Font("Arial", Font.BOLD, 12));
        cantidadLabel.setForeground(color);
        cantidadLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(cantidadLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private String formatearNumeroCuenta(String numeroCuenta) {
        // Mostrar solo los últimos 4 dígitos
        if (numeroCuenta.length() > 4) {
            String ultimosDigitos = numeroCuenta.substring(numeroCuenta.length() - 4);
            return "**** **** **** " + ultimosDigitos;
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
    public void mostrarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            statusLabel.setText("Cargando...");
            statusLabel.setForeground(Color.BLUE);
        });
    }
    
    @Override
    public void ocultarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            statusLabel.setText("");
        });
    }
    
    @Override
    public void navegarATransferencias() {
        SwingUtilities.invokeLater(() -> {
            new TransferenceView().setVisible(true);
        });
    }
    
    @Override
    public void navegarAHistorial() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Funcionalidad de historial en desarrollo", 
                "Información", 
                JOptionPane.INFORMATION_MESSAGE);
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
