package views;

import interfaces.LoginContract;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import presenters.LoginPresenter;

public class LoginView extends JFrame implements LoginContract.View {
    private final LoginContract.Presenter presenter;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    public LoginView() {
        presenter = new LoginPresenter(this);
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Banco - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Logo/Título
        JLabel titleLabel = new JLabel("Bank", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 120, 180));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // Email
        gbc.gridwidth = 1; gbc.gridy++;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField("usuario@banco.es", 15);
        mainPanel.add(emailField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy++;
        mainPanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField("123456", 15);
        mainPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setBackground(new Color(0, 120, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presenter.login(emailField.getText(), 
                               new String(passwordField.getPassword()));
            }
        });
        mainPanel.add(loginButton, gbc);
        
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
        pack();
        setLocationRelativeTo(null);
    }
    
    @Override
    public void mostrarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            loginButton.setEnabled(false);
            statusLabel.setText("Iniciando sesión...");
            statusLabel.setForeground(Color.BLUE);
        });
    }
    
    @Override
    public void ocultarCargando() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            loginButton.setEnabled(true);
        });
    }
    
    @Override
    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(mensaje);
            statusLabel.setForeground(Color.RED);
        });
    }
    
    @Override
    public void navegarADashboard() {
        SwingUtilities.invokeLater(() -> {
            this.dispose();
            new DashboardView().setVisible(true);
        });
    }
    
    @Override
    public void limpiarCampos() {
        SwingUtilities.invokeLater(() -> {
            passwordField.setText("");
            statusLabel.setText("");
        });
    }
}
