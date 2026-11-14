package com.canchapro;

import com.canchapro.tienda.ConfiguracionModulos;
import com.canchapro.ui.VentanaPrincipal;
import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;

/** Probar con entorno grafico. Usa datos de prueba del directorio actual. */
public class PruebasInterfazTienda {
    private static JButton boton(Component componente,String texto){
        if(componente instanceof JButton b && b.getText().equals(texto)) return b;
        if(componente instanceof Container p) for(Component c:p.getComponents()){
            JButton encontrado=boton(c,texto);
            if(encontrado!=null)return encontrado;
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        ConfiguracionModulos conf=new ConfiguracionModulos(Path.of("datos"));
        conf.configurarTienda(true);
        SwingUtilities.invokeAndWait(()->{
            VentanaPrincipal v=new VentanaPrincipal();v.setVisible(true);
            JButton tienda=boton(v.getContentPane(),"Tienda");
            if(tienda==null||!tienda.isVisible())throw new AssertionError("Boton de tienda habilitada no visible");
            System.out.println("OK 1 - Tienda habilitada y visible");
            tienda.doClick();
            if(boton(v.getContentPane(),"Nueva venta")==null)
                throw new AssertionError("No se muestra la pantalla de tienda");
            System.out.println("OK 2 - Formulario de ventas disponible");
            v.dispose();
        });
        conf.configurarTienda(false);
        SwingUtilities.invokeAndWait(()->{
            VentanaPrincipal v=new VentanaPrincipal();
            JButton tienda=boton(v.getContentPane(),"Tienda");
            if(tienda==null||tienda.isVisible())throw new AssertionError("Tienda inactiva sigue visible");
            System.out.println("OK 3 - Tienda se oculta cuando esta desactivada");
            v.dispose();
        });
        System.out.println("TOTAL: 3 pruebas de interfaz de tienda correctas.");
    }
}
