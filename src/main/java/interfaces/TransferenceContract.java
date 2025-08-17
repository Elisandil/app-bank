package interfaces;

import java.util.List;
import models.Account;

public interface TransferenceContract {
    interface View {
        void mostrarCuentasOrigen(List<Account> cuentas);
        void mostrarError(String mensaje);
        void mostrarExito(String mensaje);
        void mostrarCargando();
        void ocultarCargando();
        void limpiarFormulario();
    }
    
    interface Presenter {
        void cargarCuentas();
        void realizarTransferencia(String cuentaOrigen, String cuentaDestino, 
                double cantidad, String concepto);
        void onDestroy();
    }
}
