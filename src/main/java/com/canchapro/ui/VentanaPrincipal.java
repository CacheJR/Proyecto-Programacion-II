package com.canchapro.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Pantalla inicial de CanchaPro. Avance 01: solo navegacion.
 * Todavia no guarda ni administra reservas, clientes o pagos.
 */
public class VentanaPrincipal extends JFrame {
    private static final Color AZUL_FONDO = new Color(15, 30, 48);
    private static final Color AZUL_MENU = new Color(21, 43, 67);
    private static final Color AZUL_CLARO = new Color(36, 75, 111);
    private static final Color BLANCO = new Color(245, 248, 252);
    private static final Color GRIS_TEXTO = new Color(184, 200, 217);

    private static final String[][] MODULOS = {
        {"Inicio", "INICIO"}, {"Clientes", "CLIENTES"},
        {"Canchas", "CANCHAS"}, {"Disponibilidad", "DISPONIBILIDAD"},
        {"Reservas", "RESERVAS"}, {"Pagos", "PAGOS"},
        {"Reportes", "REPORTES"}, {"Configuracion", "CONFIGURACION"}
    };

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel panelPantallas = new JPanel(tarjetas);
    private final JLabel tituloVista = new JLabel("Inicio");

    public VentanaPrincipal() {
        setTitle("CanchaPro | Complejo Deportivo La 10");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(AZUL_FONDO);
        principal.add(crearEncabezado(), BorderLayout.NORTH);
        principal.add(crearMenu(), BorderLayout.WEST);
        principal.add(crearContenido(), BorderLayout.CENTER);
        setContentPane(principal);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(AZUL_CLARO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        JLabel nombre = new JLabel("CANCHAPRO");
        nombre.setForeground(BLANCO);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel descripcion = new JLabel("Sistema de gestion y reservas | La 10");
        descripcion.setForeground(GRIS_TEXTO);
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        encabezado.add(nombre, BorderLayout.WEST);
        encabezado.add(descripcion, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(AZUL_MENU);
        menu.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        menu.setPreferredSize(new Dimension(230, 0));
        for (String[] modulo : MODULOS) {
            menu.add(crearBotonMenu(modulo[0], modulo[1]));
            menu.add(Box.createVerticalStrut(10));
        }
        return menu;
    }

    private JButton crearBotonMenu(String texto, String identificador) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 15));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBackground(AZUL_CLARO);
        boton.setForeground(BLANCO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        boton.setMaximumSize(new Dimension(210, 45));
        boton.addActionListener(evento -> mostrarPantalla(identificador, texto));
        return boton;
    }

    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(0, 18));
        contenido.setBackground(AZUL_FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        tituloVista.setForeground(BLANCO);
        tituloVista.setFont(new Font("SansSerif", Font.BOLD, 25));
        contenido.add(tituloVista, BorderLayout.NORTH);
        panelPantallas.setBackground(AZUL_FONDO);
        panelPantallas.add(crearPanelInicio(), "INICIO");
        for (String[] modulo : MODULOS) {
            if (!"INICIO".equals(modulo[1])) {
                panelPantallas.add(crearPanelPendiente(modulo[0]), modulo[1]);
            }
        }
        contenido.add(panelPantallas, BorderLayout.CENTER);
        return contenido;
    }

    private JPanel crearPanelInicio() {
        JPanel inicio = new JPanel(new BorderLayout(0, 20));
        inicio.setBackground(AZUL_FONDO);
        JLabel bienvenida = new JLabel("Bienvenido al Complejo Deportivo La 10");
        bienvenida.setForeground(GRIS_TEXTO);
        bienvenida.setFont(new Font("SansSerif", Font.PLAIN, 18));
        inicio.add(bienvenida, BorderLayout.NORTH);
        JPanel resumen = new JPanel(new GridLayout(2, 2, 16, 16));
        resumen.setBackground(AZUL_FONDO);
        resumen.add(crearTarjeta("Clientes", "Pendiente de implementar"));
        resumen.add(crearTarjeta("Canchas", "Pendiente de implementar"));
        resumen.add(crearTarjeta("Reservas", "Pendiente de implementar"));
        resumen.add(crearTarjeta("Pagos", "Pendiente de implementar"));
        inicio.add(resumen, BorderLayout.CENTER);
        JLabel etapa = new JLabel("Avance 01 - Estructura inicial y navegacion (sin datos reales)");
        etapa.setForeground(GRIS_TEXTO);
        etapa.setFont(new Font("SansSerif", Font.ITALIC, 13));
        inicio.add(etapa, BorderLayout.SOUTH);
        return inicio;
    }

    private JPanel crearTarjeta(String titulo, String subtitulo) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(AZUL_MENU);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel nombre = new JLabel(titulo);
        nombre.setForeground(BLANCO);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 21));
        JLabel detalle = new JLabel(subtitulo);
        detalle.setForeground(GRIS_TEXTO);
        detalle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tarjeta.add(nombre, BorderLayout.NORTH);
        tarjeta.add(detalle, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel crearPanelPendiente(String modulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AZUL_MENU);
        panel.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));
        JLabel aviso = new JLabel("Modulo " + modulo + " - Se implementara en siguientes avances");
        aviso.setFont(new Font("SansSerif", Font.PLAIN, 18));
        aviso.setForeground(BLANCO);
        aviso.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(aviso, BorderLayout.CENTER);
        return panel;
    }

    private void mostrarPantalla(String identificador, String nombre) {
        tarjetas.show(panelPantallas, identificador);
        tituloVista.setText(nombre);
    }
}
