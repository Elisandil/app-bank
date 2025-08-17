package exceptions;
 
public class InsufficientFundsException extends BankingException {
    private final double availableBalance;
    private final double requestedAmount;

    public InsufficientFundsException(double availableBalance, 
            double requestedAmount) {
        
        super("FUNDS_001",
                String.format("Fondos insuficientes. Disponible: %.2f, "
                        + "Solicitado: %.2f",
                        availableBalance, requestedAmount),
                String.format("Saldo insuficiente. Disponible: %.2f €", 
                        availableBalance));
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}
