package com.canchapro;

import com.canchapro.ui.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/** Punto de entrada; la interfaz se inicia en el hilo de eventos de Swing. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (IllegalStateException error) {
                JOptionPane.showMessageDialog(null, error.getMessage(),
                        "Error al iniciar CanchaPro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
