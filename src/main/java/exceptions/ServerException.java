package exceptions;

public class ServerException extends BankingException {

    public ServerException(String message, Throwable cause) {
        super("SERVER_001", message,
                "Error interno del servidor. Intente nuevamente en unos momentos.", 
                cause);
    }
}
