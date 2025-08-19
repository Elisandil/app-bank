package presenters;

import java.util.List;
import models.Account;
import models.User;
import services.AuthService;
import services.BankingService;
import interfaces.TransferenceContract;

public class TransferencePresenter implements TransferenceContract.Presenter {
    private TransferenceContract.View view;
    private final AuthService authService;
    private final BankingService bankingService;
    
    public TransferencePresenter(TransferenceContract.View view) {
        this.view = view;
        this.authService = AuthService.getInstance();
        this.bankingService = BankingService.getInstance();
    }
    
    @Override
    public void cargarCuentas() {
        User usuario = authService.getUsuarioActual();
        
        if (usuario != null && view != null) {
            bankingService.obtenerCuentas(usuario.getId(), 
                    new BankingService.CuentasCallback() {
                @Override
                public void onSuccess(List<Account> cuentas) {
                   
                    if (view != null) {
                        view.mostrarCuentasOrigen(cuentas);
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (view != null) {
                        view.mostrarError(error);
                    }
                }
            });
        }
    }
    
    @Override
    public void realizarTransferencia(String cuentaOrigen, String cuentaDestino, 
            double cantidad, String concepto) {

        if (cuentaOrigen == null || cuentaOrigen.trim().isEmpty()) {
            view.mostrarError("Seleccione una cuenta de origen");
            return;
        }
        if (cuentaDestino == null || cuentaDestino.trim().isEmpty()) {
            view.mostrarError("Ingrese la cuenta de destino");
            return;
        }
        if (cantidad <= 0) {
            view.mostrarError("La cantidad debe ser mayor a 0");
            return;
        }
        if (concepto == null || concepto.trim().isEmpty()) {
            view.mostrarError("Ingrese un concepto");
            return;
        }
        view.mostrarCargando();
        
        bankingService.realizarTransferencia(cuentaOrigen, cuentaDestino, 
                cantidad, concepto, new BankingService.TransferenciaCallback() {
                
                    @Override
                    public void onSuccess(String mensaje) {
                    
                        if (view != null) {
                        view.ocultarCargando();
                        view.mostrarExito(mensaje);
                        view.limpiarFormulario();
                    }
                }
                
                @Override
                public void onError(String error) {
                    
                    if (view != null) {
                        view.ocultarCargando();
                        view.mostrarError(error);
                    }
                }
            });
    }
    
    @Override
    public void onDestroy() {
        view = null;
    }
}
