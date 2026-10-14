package com.canchapro;

import com.canchapro.ui.VentanaPrincipal;
import javax.swing.SwingUtilities;

/** Punto de entrada; la interfaz se inicia en el hilo de eventos de Swing. */
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
