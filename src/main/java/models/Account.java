package models;

public class Account {
    private String numeroCuenta;
    private String userId;
    private double saldo;
    private TipoCuenta tipo;
    
    public enum TipoCuenta {
        CORRIENTE, AHORRO, NOMINA
    }
    
    public Account(String numeroCuenta, String userId, double saldo, 
            TipoCuenta tipo) {
        this.numeroCuenta = numeroCuenta;
        this.userId = userId;
        this.saldo = saldo;
        this.tipo = tipo;
    }
    
    // Getters y setters
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { 
        this.numeroCuenta = numeroCuenta; 
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public TipoCuenta getTipo() { return tipo; }
    public void setTipo(TipoCuenta tipo) { this.tipo = tipo; }
}
