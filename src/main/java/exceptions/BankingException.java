package exceptions;

public abstract class BankingException extends Exception {
    private final String errorCode;
    private final String userFriendlyMessage;

    public BankingException(String errorCode, String message, 
            String userFriendlyMessage) {
        
        super(message);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public BankingException(String errorCode, String message, 
            String userFriendlyMessage, Throwable cause) {
        
        super(message, cause);
        this.errorCode = errorCode;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }
}
