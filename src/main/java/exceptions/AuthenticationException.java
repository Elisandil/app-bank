package exceptions;

public class AuthenticationException extends BankingException {

    public AuthenticationException(String message) {
        super("AUTH_001", message, "Credenciales incorrectas. Verifique su "
                + "email y contraseña.");
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTH_001", message, "Error de autenticación. Intente nuevamente.", 
                cause);
    }
}