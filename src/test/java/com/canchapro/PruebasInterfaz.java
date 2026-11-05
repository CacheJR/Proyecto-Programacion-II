package com.canchapro;

import com.canchapro.ui.VentanaPrincipal;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/** Comprueba navegacion y tablas en una pantalla grafica. */
public class PruebasInterfaz {
    private static JButton buscarBoton(Container c, String texto) {
        for (Component x : c.getComponents()) {
            if (x instanceof JButton b && b.getText().equals(texto)) return b;
            if (x instanceof Container otro) {
                JButton resultado = buscarBoton(otro, texto);
                if (resultado != null) return resultado;
            }
        }
        return null;
    }
    private static JTable buscarTabla(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTable t) return t;
            if (x instanceof Container otro) {
                JTable t = buscarTabla(otro);
                if (t != null) return t;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VentanaPrincipal v = new VentanaPrincipal();
            v.setVisible(true);
            for (String seccion : new String[]{"Clientes", "Canchas", "Reservas", "Pagos", "Disponibilidad"}) {
                JButton boton = buscarBoton(v.getContentPane(), seccion);
                if (boton == null) throw new AssertionError("No hay boton " + seccion);
                boton.doClick();
                JTable tabla = buscarTabla(v.getContentPane());
                if (tabla == null) throw new AssertionError("No hay tabla en " + seccion);
                System.out.println("OK: " + seccion + " con " + tabla.getRowCount() + " filas");
            }
            v.dispose();
        });
        System.out.println("TOTAL: 5 pruebas de interfaz correctas");
    }
}
