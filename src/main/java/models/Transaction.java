package models;

import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private String cuentaOrigen;
    private String cuentaDestino;
    private double cantidad;
    private TipoTransaccion tipo;
    private LocalDateTime fecha;
    private String concepto;
    
    public enum TipoTransaccion {
        TRANSFERENCIA, INGRESO, RETIRADA, PAGO
    }
    
    public Transaction(String id, String cuentaOrigen, String cuentaDestino, 
                      double cantidad, TipoTransaccion tipo, String concepto) {
        this.id = id;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
        this.concepto = concepto;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(String cuentaOrigen) { 
        this.cuentaOrigen = cuentaOrigen; 
    }
    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String cuentaDestino) { 
        this.cuentaDestino = cuentaDestino; 
    }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
}

