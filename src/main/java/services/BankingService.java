package services;

import java.util.ArrayList;
import java.util.List;
import models.Account;
import models.Transaction;

public class BankingService {
    private static BankingService instance;
    private List<Account> cuentas;
    private List<Transaction> transacciones;
    
    private BankingService() {
        inicializarDatos();
    }
    
    public static BankingService getInstance() {
        if (instance == null) {
            instance = new BankingService();
        }
        return instance;
    }
    
    private void inicializarDatos() {
        cuentas = new ArrayList<>();
        cuentas.add(new Account("ES21 3058 0001 2720 0123 4567", "1", 2500.75, 
                Account.TipoCuenta.CORRIENTE));
        cuentas.add(new Account("ES21 3058 0001 2720 0987 6543", "1", 1200.30, 
                Account.TipoCuenta.AHORRO));
        
        transacciones = new ArrayList<>();
        transacciones.add(new Transaction("T001", "ES21 3058 0001 2720 0123 4567", 
                "ES21 1234 5678 9012 3456 7890", 150.0, 
                Transaction.TipoTransaccion.TRANSFERENCIA, "Pago alquiler"));
        transacciones.add(new Transaction("T002", null, "ES21 3058 0001 2720 0123 4567", 
                1000.0, Transaction.TipoTransaccion.INGRESO, "Nómina"));
    }
    
    public void obtenerCuentas(String userId, CuentasCallback callback) {
        new Thread(() -> {
            
            try {
                Thread.sleep(800);
                List<Account> cuentasUsuario = cuentas.stream()
                    .filter(c -> c.getUserId().equals(userId))
                    .collect(java.util.stream.Collectors.toList());
                callback.onSuccess(cuentasUsuario);
            } catch (InterruptedException e) {
                callback.onError("Error al cargar cuentas");
            }
        }).start();
    }
    
    public void obtenerTransacciones(String userId, TransaccionesCallback callback) {
        new Thread(() -> {
            
            try {
                Thread.sleep(600);
                // Obtener últimas 5 transacciones
                List<Transaction> ultimasTransacciones = transacciones.stream()
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList());
                callback.onSuccess(ultimasTransacciones);
            } catch (InterruptedException e) {
                callback.onError("Error al cargar transacciones");
            }
        }).start();
    }
    
    public void realizarTransferencia(String cuentaOrigen, String cuentaDestino, 
            double cantidad, String concepto, TransferenciaCallback callback) {
        new Thread(() -> {
            
            try {
                Thread.sleep(1200);
                
                // Validar fondos suficientes
                Account cuenta = cuentas.stream()
                    .filter(c -> c.getNumeroCuenta().equals(cuentaOrigen))
                    .findFirst().orElse(null);
                
                if (cuenta != null && cuenta.getSaldo() >= cantidad) {
                    // Actualizar saldo
                    cuenta.setSaldo(cuenta.getSaldo() - cantidad);
                    
                    // Crear transacción
                    String id = "T" + System.currentTimeMillis();
                    Transaction nuevaTransaccion = new Transaction(id, 
                            cuentaOrigen, cuentaDestino, cantidad, 
                            Transaction.TipoTransaccion.TRANSFERENCIA, concepto);
                    transacciones.add(0, nuevaTransaccion);
                    
                    callback.onSuccess("Transferencia realizada con éxito");
                } else {
                    callback.onError("Fondos insuficientes");
                }
            } catch (InterruptedException e) {
                callback.onError("Error al realizar la transferencia");
            }
        }).start();
    }
    
    public interface CuentasCallback {
        void onSuccess(java.util.List<Account> cuentas);
        void onError(String error);
    }
    
    public interface TransaccionesCallback {
        void onSuccess(java.util.List<Transaction> transacciones);
        void onError(String error);
    }
    
    public interface TransferenciaCallback {
        void onSuccess(String mensaje);
        void onError(String error);
    }
}
