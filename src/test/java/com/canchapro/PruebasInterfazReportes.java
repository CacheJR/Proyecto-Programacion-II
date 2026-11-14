package com.canchapro;

import com.canchapro.ui.VentanaPrincipal;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/** Prueba visual basica de la navegacion hasta los reportes. */
public class PruebasInterfazReportes {
    private static <T extends Component> T buscar(Container contenedor, Class<T> tipo) {
        for (Component c : contenedor.getComponents()) {
            if (tipo.isInstance(c)) return tipo.cast(c);
            if (c instanceof Container interno) {
                T encontrado = buscar(interno, tipo);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    private static JButton boton(Container panel, String nombre) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton b && b.getText().equals(nombre)) return b;
            if (c instanceof Container interno) {
                JButton encontrado = boton(interno, nombre);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
            try {
                JButton reportes = boton(ventana.getContentPane(), "Reportes");
                if (reportes == null) throw new AssertionError("Falta opcion Reportes");
                reportes.doClick();
                JTable tabla = buscar(ventana.getContentPane(), JTable.class);
                if (tabla == null || tabla.getColumnCount() != 4) {
                    throw new AssertionError("El reporte inicial no muestra cuatro columnas");
                }
                if (tabla.getRowCount() < 1) {
                    throw new AssertionError("Falta mostrar reservas de ejemplo");
                }
                JComboBox<?> tipos = buscar(ventana.getContentPane(), JComboBox.class);
                if (tipos == null || tipos.getItemCount() != 8) {
                    throw new AssertionError("Faltan ocho tipos de reporte");
                }
                tipos.setSelectedIndex(2);
                JButton consultar = boton(ventana.getContentPane(), "Consultar");
                if (consultar == null) throw new AssertionError("Falta Consultar");
                consultar.doClick();
                if (tabla.getColumnCount() != 3) {
                    throw new AssertionError("Ingresos debe tener tres columnas");
                }
                System.out.println("OK - Navegacion Reportes");
                System.out.println("OK - 8 tipos de reporte");
                System.out.println("OK - Cambio entre reportes y tabla");
            } finally {
                ventana.dispose();
            }
        });
        System.out.println("TOTAL: 3 pruebas de interfaz de reportes correctas");
    }
}
