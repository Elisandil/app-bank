package presenters;

import config.ConfigurationManager;
import exceptions.BankingException;
import exceptions.ValidationException;
import interfaces.LoginContract;
import models.User;
import services.AuthService;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;


public class LoginPresenter implements LoginContract.Presenter {
    private static final Logger LOGGER = Logger
            .getLogger(LoginPresenter.class.getName());
    
    private LoginContract.View view;
    private final AuthService authService;
    private final ConfigurationManager config;
    private int loginAttempts = 0;
    private long lastFailedAttempt = 0;
    
    public LoginPresenter(LoginContract.View view) {
        this.view = view;
        this.authService = AuthService.getInstance();
        this.config = ConfigurationManager.getInstance();
        
        LOGGER.info("ImprovedLoginPresenter inicializado");
    }
    
    @Override
    public void login(String email, String password) {
        LOGGER.info("Iniciando proceso de login");
        
        if (view == null) {
            LOGGER.warning("Vista es null, cancelando login");
            return;
        }
        if (isLockedOut()) {
            long remainingTime = getRemainingLockoutTime();
            String message = String.format("Cuenta bloqueada. Intente nuevamente "
                    + "en %d segundos.", 
                                          
                    remainingTime / 1000);
            view.mostrarError(message);
            LOGGER.log(Level.WARNING, "Login bloqueado por intentos fallidos. "
                    + "Tiempo restante: {0}ms", 
                    remainingTime);
            return;
        }
        
        try {
            validateLoginInput(email, password);
        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validación de entrada falló", e);
            view.mostrarError(e.getUserFriendlyMessage());
            return;
        }
        view.mostrarCargando();
        CompletableFuture<User> loginFuture = authService
                .loginAsync(email, password);
        
        loginFuture
            .thenAccept(usuario -> {
                LOGGER.log(Level.INFO, "Login exitoso para usuario: {0}", 
                        usuario.getNombre());
                resetLoginAttempts();
                
                if (view != null) {
                    view.ocultarCargando();
                    view.navegarADashboard();
                }
            })
            .exceptionally(throwable -> {
                handleLoginError(throwable);
                return null;
            });
    }
    
    
    private void validateLoginInput(String email, String password) 
            throws ValidationException {
        
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "El email es requerido");
        }        
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("password", "La contraseña es requerida");
        }        
        if (!isValidEmail(email)) {
            throw new ValidationException("email", "Formato de email inválido");
        }
    }

    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    
    private void handleLoginError(Throwable throwable) {
        LOGGER.log(Level.SEVERE, "Error durante login", throwable);
        
        if (view == null) {
            return;
        }
        view.ocultarCargando();
        String errorMessage;
        
        if (throwable.getCause() instanceof BankingException bankingEx) {
            errorMessage = bankingEx.getUserFriendlyMessage();

            if (bankingEx instanceof exceptions.AuthenticationException) {
                incrementLoginAttempts();
            }           
        } else {
            errorMessage = "Error inesperado durante el login. Por favor, "
                    + "intente nuevamente.";
        }
        view.mostrarError(errorMessage);
        view.limpiarCampos();
    }
    

    private boolean isLockedOut() {
        int maxAttempts = config.getInt("security.max.login.attempts", 3);
        long lockoutDuration = config.getLong("security.lockout.duration",
                300000L); // 5 minutos
        
        if (loginAttempts >= maxAttempts) {
            long timeSinceLastAttempt = System.currentTimeMillis() - 
                    lastFailedAttempt;
            return timeSinceLastAttempt < lockoutDuration;
        }
        
        return false;
    }
    

    private long getRemainingLockoutTime() {
        long lockoutDuration = config.getLong("security.lockout.duration", 300000L);
        long timeSinceLastAttempt = System.currentTimeMillis() - lastFailedAttempt;
        return Math.max(0, lockoutDuration - timeSinceLastAttempt);
    }
    

    private void incrementLoginAttempts() {
        loginAttempts++;
        lastFailedAttempt = System.currentTimeMillis();
        
        LOGGER.log(Level.WARNING, "Intento de login fallido. Total de intentos: {0}", 
                loginAttempts);
        
        int maxAttempts = config.getInt("security.max.login.attempts", 3);
        
        if (loginAttempts >= maxAttempts) {
            LOGGER.warning("Número máximo de intentos alcanzado. Cuenta bloqueada "
                    + "temporalmente.");
        }
    }
    

    private void resetLoginAttempts() {
        
        if (loginAttempts > 0) {
            LOGGER.info("Reiniciando contador de intentos de login");
            loginAttempts = 0;
            lastFailedAttempt = 0;
        }
    }
    

    @Override
    public void onDestroy() {
        LOGGER.info("Destruyendo ImprovedLoginPresenter");
        view = null;
    }
    

    public String getDebugInfo() {
        return String.format("LoginAttempts: %d, LastFailedAttempt: %d, "
                + "IsLockedOut: %s", 
                loginAttempts, lastFailedAttempt, isLockedOut());
    }
}