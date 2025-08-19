package services;

import exceptions.*;

import models.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class AuthService {
    private static final Logger LOGGER = Logger
            .getLogger(AuthService.class.getName());
    private static AuthService instance;
    
    private User usuarioActual;
    private final ThreadPoolManager threadManager;
    private final SecureRandom secureRandom;
    
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFailedAttempts = new ConcurrentHashMap<>();
    private final Map<String, String> userSalts = new ConcurrentHashMap<>();    
       
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutos
    
    // Simulación de base de datos de usuarios con contraseñas hasheadas
    private static final String VALID_EMAIL = "usuario@banco.es";
    private static final String USER_SALT = "userSpecificSalt123";    
    private static final String VALID_PASSWORD_HASH = hashPassword("123456", 
            USER_SALT);
    
    private AuthService() {
        this.threadManager = ThreadPoolManager.getInstance();
        this.secureRandom = new SecureRandom();
        userSalts.put(VALID_EMAIL, USER_SALT);
        LOGGER.info("ImprovedAuthService inicializado");
    }
    
    public static synchronized AuthService getInstance() {
        
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    

    public void loginAsync(String email, String password, LoginCallback callback) {
        LOGGER.log(Level.INFO, "Iniciando proceso de login para email: {0}", 
                email);
        
        // Verificar si el usuario está bloqueado
        if (isUserLockedOut(email)) {
            long remainingTime = getRemainingLockoutTime(email);
            String errorMessage = String.format(
                "Cuenta bloqueada por exceso de intentos fallidos."
                        + " Intente nuevamente en %d segundos.",
                remainingTime / 1000
            );
            
            LOGGER.log(Level.WARNING, "Usuario bloqueado: {0}. Tiempo restante: {1}ms", 
                    new Object[]{email, remainingTime});
            callback.onError(new AuthenticationException(errorMessage));
            return;
        }

        try {
            validateLoginInput(email, password);
        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validación de entrada falló", e);
            callback.onError(e);
            return;
        }
        threadManager.executeAsync(() -> {
            
            try {
                // Simular latencia de red/base de datos
                Thread.sleep(1500);
                
                // Verificar credenciales
                if (authenticateUser(email, password)) {
                    // Reset intentos en caso de login exitoso
                    resetLoginAttempts(email);
                    
                    User usuario = createUserFromEmail(email);
                    usuarioActual = usuario;
                    
                    LOGGER.log(Level.INFO, "Login exitoso para usuario: {0}", 
                            usuario.getNombre());
                    callback.onSuccess(usuario);
                } else {
                    incrementLoginAttempts(email);
                    String errorMessage = buildAuthenticationErrorMessage(email);
                    AuthenticationException authEx = new AuthenticationException(
                            errorMessage);
                    
                    LOGGER.log(Level.WARNING, "Fallo de autenticaci\u00f3n para: {0}. "
                            + "Intentos: {1}", new Object[]{email, 
                                getLoginAttempts(email)});
                    callback.onError(authEx);
                }                
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                NetworkException netEx = new NetworkException("Operación interrumpida", 
                        ex);
                LOGGER.log(Level.SEVERE, "Login interrumpido", netEx);
                callback.onError(netEx);
            } catch (Exception e) {
                ServerException serverEx = new ServerException("Error interno durante login", 
                        e);
                LOGGER.log(Level.SEVERE, "Error inesperado en login", serverEx);
                callback.onError(serverEx);
            }
        });
    }
    

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
    

    private void validateLoginInput(String email, String password) 
            throws ValidationException {
        
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
                "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + 
                        " caracteres");
        }
    }
    

    private boolean authenticateUser(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        
        if (!VALID_EMAIL.equals(normalizedEmail)) {
            return false;
        }
        String userSalt = getUserSalt(normalizedEmail);

        String passwordHash = hashPassword(password, userSalt);
        return VALID_PASSWORD_HASH.equals(passwordHash);
    }
    
 
    private String getUserSalt(String email) {
        return userSalts.computeIfAbsent(email, k -> {
            String newSalt = generateSalt();
            LOGGER.log(Level.INFO, "Generado nuevo salt para usuario: {0}", 
                    email);
            return newSalt;
        });
    }    
    
    
    private User createUserFromEmail(String email) {
        // Datos de prueba
        return new User("1", "Prueba1", email, "111111111", "12345678A");
    }
    

    private static String hashPassword(String password, String salt) {
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedPassword = md.digest(password
                    .getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
    

    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        String generatedSalt = Base64.getEncoder().encodeToString(salt);
        
        LOGGER.log(Level.FINE, "Generado nuevo salt de longitud: {0}", 
                generatedSalt.length());
        return generatedSalt;
    }
    
   
    private boolean isUserLockedOut(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        int attempts = getLoginAttempts(normalizedEmail);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            Long lastAttempt = lastFailedAttempts.get(normalizedEmail);
            
            if (lastAttempt != null) {
                long timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt;
                return timeSinceLastAttempt < LOCKOUT_DURATION_MS;
            }
        }
        return false;
    }


    private long getRemainingLockoutTime(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Long lastAttempt = lastFailedAttempts.get(normalizedEmail);
        
        if (lastAttempt != null) {
            long timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt;
            return Math.max(0, LOCKOUT_DURATION_MS - timeSinceLastAttempt);
        }
        return 0;
    }


    private void incrementLoginAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        int currentAttempts = getLoginAttempts(normalizedEmail);
        int newAttempts = currentAttempts + 1;
        
        loginAttempts.put(normalizedEmail, newAttempts);
        lastFailedAttempts.put(normalizedEmail, System.currentTimeMillis());
        
        LOGGER.log(Level.WARNING, "Intento fallido para {0}. Total: {1}/{2}", 
                   new Object[]{normalizedEmail, newAttempts, MAX_LOGIN_ATTEMPTS});
        
        if (newAttempts >= MAX_LOGIN_ATTEMPTS) {
            LOGGER.log(Level.WARNING, "Usuario {0} bloqueado por {1} intentos fallidos", 
                       new Object[]{normalizedEmail, MAX_LOGIN_ATTEMPTS});
        }
    }


    private int getLoginAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return loginAttempts.getOrDefault(normalizedEmail, 0);
    }


    private void resetLoginAttempts(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (getLoginAttempts(normalizedEmail) > 0) {
            loginAttempts.remove(normalizedEmail);
            lastFailedAttempts.remove(normalizedEmail);
            LOGGER.log(Level.INFO, "Reiniciados intentos de login para: {0}", 
                    normalizedEmail);
        }
    }


    private String buildAuthenticationErrorMessage(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        int attempts = getLoginAttempts(normalizedEmail);
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts;
        
        if (remainingAttempts <= 0) {
            long lockoutTimeSeconds = LOCKOUT_DURATION_MS / 1000;
            return String.format("Credenciales incorrectas. "
                    + "Cuenta bloqueada por %d segundos.", 
                               lockoutTimeSeconds);
        } else {
            return String.format("Credenciales incorrectas. "
                    + "%d intento(s) restante(s).", 
                               remainingAttempts);
        }
    }    
    

    public void logout() {
        
        if (usuarioActual != null) {
            LOGGER.log(Level.INFO, "Cerrando sesi\u00f3n para usuario: {0}", 
                    usuarioActual.getNombre());
            usuarioActual = null;
        }
    }
    

    public User getUsuarioActual() {
        return usuarioActual;
    }
    

    public boolean isAuthenticated() {
        return usuarioActual != null;
    }
    
    
    public void unlockUser(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        resetLoginAttempts(normalizedEmail);
        LOGGER.log(Level.INFO, "Usuario desbloqueado administrativamente: {0}", 
                normalizedEmail);
    }


    public Map<String, Object> getUserStatus(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Map<String, Object> status = new HashMap<>();
        
        status.put("email", normalizedEmail);
        status.put("loginAttempts", getLoginAttempts(normalizedEmail));
        status.put("maxAttempts", MAX_LOGIN_ATTEMPTS);
        status.put("isLockedOut", isUserLockedOut(normalizedEmail));
        status.put("remainingLockoutTime", getRemainingLockoutTime(normalizedEmail));
        status.put("hasSalt", userSalts.containsKey(normalizedEmail));
        
        return status;
    }    
    

    public interface LoginCallback {
        void onSuccess(User usuario);
        void onError(BankingException error);
    }
}