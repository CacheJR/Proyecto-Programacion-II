package com.canchapro.ui;

import com.canchapro.DatosEjemplo;
import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.Cliente;
import com.canchapro.modelo.Reserva;
import com.canchapro.modelo.TipoCancha;
import com.canchapro.servicio.ServicioDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
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
 * Avance 03: consulta de disponibilidad y tablas de ejemplo en memoria.
 * Los formularios y el guardado de datos todavia estan pendientes.
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

    private final List<Cliente> clientes = DatosEjemplo.crearClientes();
    private final List<Cancha> canchas = DatosEjemplo.crearCanchas();
    private final List<Reserva> reservas = DatosEjemplo.crearReservas(clientes, canchas);
    private final ServicioDisponibilidad servicio = new ServicioDisponibilidad(canchas, reservas);
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
            switch (modulo[1]) {
                case "CLIENTES" -> panelPantallas.add(crearPanelClientes(), "CLIENTES");
                case "CANCHAS" -> panelPantallas.add(crearPanelCanchas(), "CANCHAS");
                case "RESERVAS" -> panelPantallas.add(crearPanelReservas(), "RESERVAS");
                case "DISPONIBILIDAD" -> panelPantallas.add(crearPanelDisponibilidad(), "DISPONIBILIDAD");
                case "INICIO" -> { }
                default -> panelPantallas.add(crearPanelPendiente(modulo[0]), modulo[1]);
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
        resumen.add(crearTarjeta("Clientes", clientes.size() + " ejemplos"));
        resumen.add(crearTarjeta("Canchas", canchas.size() + " ejemplos"));
        resumen.add(crearTarjeta("Reservas", reservas.size() + " ejemplos"));
        resumen.add(crearTarjeta("Pagos", "Pendiente de implementar"));
        inicio.add(resumen, BorderLayout.CENTER);
        JLabel etapa = new JLabel("Avance 03 - Consulta de disponibilidad (sin guardar archivos)");
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

    private JPanel crearPanelClientes() {
        Object[][] filas = new Object[clientes.size()][5];
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            filas[i] = new Object[] { c.getCodigo(), c.getNombre(), c.getTelefono(),
                c.getCorreo(), c.getFechaRegistro() };
        }
        return crearTabla(new String[] { "Codigo", "Nombre", "Telefono", "Correo", "Registro" }, filas);
    }

    private JPanel crearPanelCanchas() {
        Object[][] filas = new Object[canchas.size()][6];
        for (int i = 0; i < canchas.size(); i++) {
            Cancha c = canchas.get(i);
            filas[i] = new Object[] { c.getCodigo(), c.getNombre(), c.getTipo(),
                c.getCapacidad(), c.getPrecioBase(), c.getEstado() };
        }
        return crearTabla(new String[] { "Codigo", "Cancha", "Tipo", "Capacidad", "Base Q", "Estado" }, filas);
    }

    private JPanel crearPanelReservas() {
        Object[][] filas = new Object[reservas.size()][7];
        for (int i = 0; i < reservas.size(); i++) {
            Reserva r = reservas.get(i);
            filas[i] = new Object[] { r.getCodigo(), r.getCliente().getNombre(),
                r.getCancha().getNombre(), r.getFecha(), r.getHoraInicio(),
                r.getHoraFin(), r.getEstado() };
        }
        return crearTabla(new String[] { "Codigo", "Cliente", "Cancha", "Fecha", "Inicio", "Fin", "Estado" }, filas);
    }

    private JPanel crearTabla(String[] columnas, Object[][] filas) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(AZUL_FONDO);
        JLabel aviso = new JLabel("DATOS DE EJEMPLO - no se pueden editar ni guardar todavia");
        aviso.setForeground(GRIS_TEXTO);
        panel.add(aviso, BorderLayout.NORTH);
        DefaultTableModel modelo = new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.setBackground(AZUL_MENU);
        tabla.setForeground(BLANCO);
        tabla.setGridColor(AZUL_CLARO);
        tabla.setSelectionBackground(AZUL_CLARO);
        tabla.getTableHeader().setBackground(AZUL_CLARO);
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.setFillsViewportHeight(true);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    /** Pantalla que consulta el motor; no decide los horarios manualmente. */
    private JPanel crearPanelDisponibilidad() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(AZUL_FONDO);

        JPanel formulario = new JPanel(new GridLayout(2, 5, 12, 8));
        formulario.setBackground(AZUL_MENU);
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        String[] nombres = {"Fecha (AAAA-MM-DD)", "Hora (HH:mm)", "Minutos", "Tipo", "Consulta"};
        for (String nombre : nombres) {
            JLabel etiqueta = new JLabel(nombre);
            etiqueta.setForeground(BLANCO);
            formulario.add(etiqueta);
        }

        JTextField fecha = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField hora = new JTextField("18:00");
        JComboBox<Integer> duracion = new JComboBox<>(new Integer[] {30, 60, 90, 120});
        duracion.setSelectedItem(60);
        JComboBox<TipoCancha> tipo = new JComboBox<>(TipoCancha.values());
        JButton buscar = new JButton("Consultar");
        formulario.add(fecha);
        formulario.add(hora);
        formulario.add(duracion);
        formulario.add(tipo);
        formulario.add(buscar);
        panel.add(formulario, BorderLayout.NORTH);

        JPanel resultado = new JPanel(new BorderLayout(0, 10));
        resultado.setBackground(AZUL_FONDO);
        JLabel mensaje = new JLabel("Ingrese los datos y presione Consultar.");
        mensaje.setForeground(GRIS_TEXTO);
        resultado.add(mensaje, BorderLayout.NORTH);
        DefaultTableModel modelo = new DefaultTableModel(
                new String[] {"Codigo", "Cancha", "Tipo", "Horario", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(32);
        tabla.setBackground(AZUL_MENU);
        tabla.setForeground(BLANCO);
        tabla.setGridColor(AZUL_CLARO);
        tabla.setSelectionBackground(AZUL_CLARO);
        tabla.setFillsViewportHeight(true);
        resultado.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(resultado, BorderLayout.CENTER);
        buscar.addActionListener(evento -> consultarDisponibilidad(
            fecha, hora, duracion, tipo, modelo, mensaje));
        return panel;
    }

    private void consultarDisponibilidad(JTextField fecha, JTextField hora,
            JComboBox<Integer> duracion, JComboBox<TipoCancha> tipo,
            DefaultTableModel modelo, JLabel mensaje) {
        modelo.setRowCount(0);
        try {
            LocalDate dia = LocalDate.parse(fecha.getText().trim());
            LocalTime inicio = LocalTime.parse(hora.getText().trim());
            int minutos = (Integer) duracion.getSelectedItem();
            TipoCancha seleccionado = (TipoCancha) tipo.getSelectedItem();
            List<Cancha> disponibles = servicio.buscarDisponibles(
                    seleccionado, dia, inicio, minutos);
            for (Cancha cancha : disponibles) {
                modelo.addRow(new Object[] {cancha.getCodigo(), cancha.getNombre(),
                    cancha.getTipo(), inicio + " a " + inicio.plusMinutes(minutos), "LIBRE"});
            }
            mensaje.setForeground(GRIS_TEXTO);
            mensaje.setText("Encontradas " + disponibles.size()
                    + " cancha(s) libre(s). Solo se muestran las disponibles.");
        } catch (DateTimeParseException | IllegalArgumentException error) {
            mensaje.setForeground(new Color(255, 175, 175));
            mensaje.setText("Dato incorrecto: " + error.getMessage());
        }
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
