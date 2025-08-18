package services;

import exceptions.*;

import models.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Servicio de autenticación mejorado con:
 * - Hashing seguro de contraseñas
 * - Validación robusta
 * - Operaciones asíncronas
 * - Logging detallado
 * - Manejo de excepciones tipadas
 */
public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static AuthService instance;
    
    private User usuarioActual;
    private final ThreadPoolManager threadManager;
    private final SecureRandom secureRandom;
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutos
    
    // Simulación de base de datos de usuarios con contraseñas hasheadas
    private static final String VALID_EMAIL = "usuario@cajamar.es";
    private static final String VALID_PASSWORD_HASH = hashPassword("123456", "defaultSalt");
    
    private AuthService() {
        this.threadManager = ThreadPoolManager.getInstance();
        this.secureRandom = new SecureRandom();
        LOGGER.info("ImprovedAuthService inicializado");
    }
    
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Autentica un usuario de forma asíncrona
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @param callback Callback para manejar el resultado
     */
    public void loginAsync(String email, String password, LoginCallback callback) {
        LOGGER.info("Iniciando proceso de login para email: " + email);
        
        // Validaciones síncronas inmediatas
        try {
            validateLoginInput(email, password);
        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validación de entrada falló", e);
            callback.onError(e);
            return;
        }
        
        // Operación asíncrona de autenticación
        threadManager.executeAsync(() -> {
            try {
                // Simular latencia de red/base de datos
                Thread.sleep(1500);
                
                // Verificar credenciales
                if (authenticateUser(email, password)) {
                    User usuario = createUserFromEmail(email);
                    usuarioActual = usuario;
                    
                    LOGGER.info("Login exitoso para usuario: " + usuario.getNombre());
                    callback.onSuccess(usuario);
                } else {
                    AuthenticationException authEx = new AuthenticationException(
                        "Credenciales inválidas para email: " + email
                    );
                    LOGGER.log(Level.WARNING, "Fallo de autenticación", authEx);
                    callback.onError(authEx);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                NetworkException netEx = new NetworkException("Operación interrumpida", e);
                LOGGER.log(Level.SEVERE, "Login interrumpido", netEx);
                callback.onError(netEx);
            } catch (Exception e) {
                ServerException serverEx = new ServerException("Error interno durante login", e);
                LOGGER.log(Level.SEVERE, "Error inesperado en login", serverEx);
                callback.onError(serverEx);
            }
        });
    }
    
    /**
     * Versión con CompletableFuture para mayor flexibilidad
     */
    public CompletableFuture<User> loginAsync(String email, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        loginAsync(email, password, new LoginCallback() {
            @Override
            public void onSuccess(User usuario) {
                future.complete(usuario);
            }
            
            @Override
            public void onError(BankingException error) {
                future.completeExceptionally(error);
            }
        });
        
        return future;
    }
    
    /**
     * Valida los datos de entrada para login
     */
    private void validateLoginInput(String email, String password) throws ValidationException {
        
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "El email es requerido");
        }
        
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("email", "Formato de email inválido");
        }
        
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "La contraseña es requerida");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("password", 
                "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
    }
    
    /**
     * Autentica las credenciales del usuario
     */
    private boolean authenticateUser(String email, String password) {
        // En producción, esto consultaría una base de datos
        // y compararía hashes de contraseñas
        if (!VALID_EMAIL.equals(email.trim().toLowerCase())) {
            return false;
        }
        
        // Comparar hash de contraseña (simulado)
        String passwordHash = hashPassword(password, "defaultSalt");
        return VALID_PASSWORD_HASH.equals(passwordHash);
    }
    
    /**
     * Crea un objeto Usuario basado en el email (simulado)
     */
    private User createUserFromEmail(String email) {
        // Datos Prueba
        return new User("1", "Juan Pérez", email, "666123456", "12345678A");
    }
    
    /**
     * Hash seguro de contraseñas usando SHA-256
     */
    private static String hashPassword(String password, String salt) {
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
    
    /**
     * Genera un salt aleatorio para hashing de contraseñas
     */
    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Cierra sesión del usuario actual
     */
    public void logout() {
        
        if (usuarioActual != null) {
            LOGGER.info("Cerrando sesión para usuario: " + 
                    usuarioActual.getNombre());
            usuarioActual = null;
        }
    }
    
    /**
     * Obtiene el usuario actualmente autenticado
     */
    public User getUsuarioActual() {
        return usuarioActual;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return usuarioActual != null;
    }
    
    /**
     * Callback para operaciones de login
     */
    public interface LoginCallback {
        void onSuccess(User usuario);
        void onError(BankingException error);
    }
}