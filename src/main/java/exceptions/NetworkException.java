package exceptions;

public class NetworkException extends BankingException {

    public NetworkException(String message, Throwable cause) {
        super("NETWORK_001", message,
                "Error de conexión. Verifique su conexión a internet e intente "
                        + "nuevamente.", 
                cause);
    }
}
