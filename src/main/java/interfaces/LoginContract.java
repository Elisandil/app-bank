package interfaces;

public interface LoginContract {
    interface View {
        void mostrarCargando();
        void ocultarCargando();
        void mostrarError(String mensaje);
        void navegarADashboard();
        void limpiarCampos();
    }
    
    interface Presenter {
        void login(String email, String password);
        void onDestroy();
    }
}
