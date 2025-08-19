package presenters;

import interfaces.DashboardContract;
import java.util.List;
import models.Account;
import models.Transaction;
import models.User;
import services.AuthService;
import services.BankingService;

public class DashboardPresenter implements DashboardContract.Presenter {
    private DashboardContract.View view;
    private final AuthService authService;
    private final BankingService bankingService;
    
    public DashboardPresenter(DashboardContract.View view) {
        this.view = view;
        this.authService = AuthService.getInstance();
        this.bankingService = BankingService.getInstance();
    }
    
    @Override
    public void cargarDatos() {
        User usuario = authService.getUsuarioActual();
        
        if (usuario != null && view != null) {
            view.mostrarUsuario(usuario);
            view.mostrarCargando();

            bankingService.obtenerCuentas(usuario.getId(), 
                    new BankingService.CuentasCallback() {
                @Override
                public void onSuccess(List<Account> cuentas) {
                    
                    if (view != null) {
                        view.mostrarCuentas(cuentas);
                    }
                }
                
                @Override
                public void onError(String error) {
                    
                    if (view != null) {
                        view.mostrarError(error);
                    }
                }
            });
            bankingService.obtenerTransacciones(usuario.getId(), 
                    new BankingService.TransaccionesCallback() {
                @Override
                public void onSuccess(List<Transaction> transacciones) {
                    
                    if (view != null) {
                        view.ocultarCargando();
                        view.mostrarUltimasTransacciones(transacciones);
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
    }
    
    @Override
    public void onTransferenciasClick() {
        
        if (view != null) {
            view.navegarATransferencias();
        }
    }
    
    @Override
    public void onHistorialClick() {
        
        if (view != null) {
            view.navegarAHistorial();
        }
    }
    
    @Override
    public void onDestroy() {
        view = null;
    }
}
