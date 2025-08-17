package exceptions;

public class AccountNotFoundException extends BankingException {

    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super("ACCOUNT_001",
                "Cuenta no encontrada: " + accountNumber,
                "La cuenta especificada no existe o no tiene permisos para acceder.");
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
