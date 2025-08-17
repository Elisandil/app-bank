package interfaces;

import java.util.List;
import models.Account;
import models.Transaction;
import models.User;


public interface DashboardContract {
    interface View {
        void mostrarUsuario(User usuario);
        void mostrarCuentas(List<Account> cuentas);
        void mostrarUltimasTransacciones(List<Transaction> transacciones);
        void mostrarError(String mensaje);
        void mostrarCargando();
        void ocultarCargando();
        void navegarATransferencias();
        void navegarAHistorial();
    }
    
    interface Presenter {
        void cargarDatos();
        void onTransferenciasClick();
        void onHistorialClick();
        void onDestroy();
    }
}
