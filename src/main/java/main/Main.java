package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import views.LoginView;

public class Main {
    
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            
        }

        SwingUtilities.invokeLater(() -> {
            new LoginView().setVisible(true);
        });
    }
}
