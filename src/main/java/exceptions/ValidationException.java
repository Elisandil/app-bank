package exceptions;

public class ValidationException extends BankingException {

    private final String field;

    public ValidationException(String field, String message) {
        super("VALIDATION_001",
                String.format("Error de validación en campo '%s': %s", 
                        field, 
                        message),
                message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
