package com.canchapro.ui;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.*;
import com.canchapro.servicio.GestionRegistros;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Formularios de Clientes, Canchas y Reservas. Las reglas estan en GestionRegistros. */
public class PanelesGestion {
    private final JFrame ventana;
    private final DatosSistema datos;
    private final GestionRegistros gestion;

    public PanelesGestion(JFrame ventana, DatosSistema datos, GestionRegistros gestion) {
        this.ventana = ventana;
        this.datos = datos;
        this.gestion = gestion;
    }

    private DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JPanel base(JPanel controles, DefaultTableModel modelo, JTable tabla, String nota) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(controles, BorderLayout.NORTH);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(new JLabel(nota), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel filaBotones() { return new JPanel(new FlowLayout(FlowLayout.LEFT)); }

    private JPanel formulario() { return new JPanel(new GridLayout(0, 2, 10, 10)); }

    private void campo(JPanel panel, String titulo, JComponent componente) {
        panel.add(new JLabel(titulo));
        panel.add(componente);
    }

    private boolean aceptar(JPanel formulario, String titulo) {
        return JOptionPane.showConfirmDialog(ventana, formulario, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
            JOptionPane.showMessageDialog(ventana, "Operacion realizada y guardada.");
        } catch (RuntimeException error) {
            JOptionPane.showMessageDialog(ventana, error.getMessage(), "Revisar datos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int seleccion(JTable tabla) {
        if (tabla.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(ventana, "Seleccione una fila de la tabla.");
            return -1;
        }
        return tabla.convertRowIndexToModel(tabla.getSelectedRow());
    }

    private static String[] nombresClientes(List<Cliente> clientes) {
        String[] nombres = new String[clientes.size()];
        for (int i = 0; i < clientes.size(); i++) {
            Cliente c = clientes.get(i);
            nombres[i] = c.getCodigo() + " - " + c.getNombre();
        }
        return nombres;
    }

    private static String[] nombresCanchas(List<Cancha> canchas) {
        String[] nombres = new String[canchas.size()];
        for (int i = 0; i < canchas.size(); i++) {
            Cancha c = canchas.get(i);
            nombres[i] = c.getCodigo() + " - " + c.getNombre() + " (" + c.getEstado() + ")";
        }
        return nombres;
    }

    public JPanel crearClientes() {
        DefaultTableModel modelo = modelo("Codigo", "Nombre", "Telefono", "Correo", "Registro");
        JTable tabla = new JTable(modelo);
        JTextField busqueda = new JTextField(16);
        JPanel botones = new JPanel(new GridLayout(2, 1));
        JPanel filaBusqueda = filaBotones();
        JPanel filaAcciones = filaBotones();
        JButton buscar = new JButton("Buscar");
        JButton todos = new JButton("Mostrar todos");
        JButton nuevo = new JButton("Agregar cliente");
        JButton editar = new JButton("Editar seleccionado");
        filaBusqueda.add(new JLabel("Codigo, nombre o telefono:"));
        filaBusqueda.add(busqueda);
        filaBusqueda.add(buscar); filaBusqueda.add(todos);
        filaAcciones.add(nuevo); filaAcciones.add(editar);
        botones.add(filaBusqueda); botones.add(filaAcciones);
        Runnable actualizar = () -> {
            modelo.setRowCount(0);
            for (Cliente c : gestion.buscarClientes(busqueda.getText())) {
                modelo.addRow(new Object[]{c.getCodigo(), c.getNombre(), c.getTelefono(),
                        c.getCorreo(), c.getFechaRegistro()});
            }
        };
        buscar.addActionListener(e -> actualizar.run());
        todos.addActionListener(e -> { busqueda.setText(""); actualizar.run(); });
        nuevo.addActionListener(e -> {
            JTextField codigo = new JTextField();
            JTextField nombre = new JTextField();
            JTextField telefono = new JTextField();
            JTextField correo = new JTextField();
            JPanel f = formulario();
            campo(f, "Codigo:", codigo); campo(f, "Nombre:", nombre);
            campo(f, "Telefono:", telefono); campo(f, "Correo (opcional):", correo);
            if (aceptar(f, "Agregar cliente")) {
                ejecutar(() -> gestion.registrarCliente(codigo.getText(), nombre.getText(),
                        telefono.getText(), correo.getText()));
                actualizar.run();
            }
        });
        editar.addActionListener(e -> {
            int fila = seleccion(tabla);
            if (fila == -1) return;
            String codigo = modelo.getValueAt(fila, 0).toString();
            Cliente c = datos.getClientes().stream()
                    .filter(x -> x.getCodigo().equals(codigo)).findFirst().orElse(null);
            if (c == null) return;
            JTextField nombre = new JTextField(c.getNombre());
            JTextField telefono = new JTextField(c.getTelefono());
            JTextField correo = new JTextField(c.getCorreo());
            JPanel f = formulario();
            campo(f, "Nombre:", nombre); campo(f, "Telefono:", telefono);
            campo(f, "Correo:", correo);
            if (aceptar(f, "Editar " + codigo)) {
                ejecutar(() -> gestion.actualizarCliente(c, nombre.getText(),
                        telefono.getText(), correo.getText()));
                actualizar.run();
            }
        });
        actualizar.run();
        return base(botones, modelo, tabla, "Los clientes se guardan en datos/clientes.csv");
    }

    public JPanel crearCanchas() {
        DefaultTableModel modelo = modelo("Codigo", "Nombre", "Tipo", "Capacidad", "Base Q", "Estado", "Apertura", "Cierre");
        JTable tabla = new JTable(modelo);
        JPanel botones = filaBotones();
        JButton nuevo = new JButton("Agregar cancha");
        JButton estado = new JButton("Cambiar estado");
        botones.add(nuevo); botones.add(estado);
        Runnable actualizar = () -> {
            modelo.setRowCount(0);
            for (Cancha c : datos.getCanchas()) {
                modelo.addRow(new Object[]{c.getCodigo(), c.getNombre(), c.getTipo(),
                        c.getCapacidad(), c.getPrecioBase(), c.getEstado(),
                        c.getHoraApertura(), c.getHoraCierre()});
            }
        };
        nuevo.addActionListener(e -> {
            JTextField codigo = new JTextField(); JTextField nombre = new JTextField();
            JComboBox<TipoCancha> tipo = new JComboBox<>(TipoCancha.values());
            JTextField capacidad = new JTextField("10"); JTextField precio = new JTextField("150.00");
            JComboBox<EstadoCancha> est = new JComboBox<>(EstadoCancha.values());
            JTextField apertura = new JTextField("08:00"); JTextField cierre = new JTextField("23:00");
            JPanel f = formulario();
            campo(f, "Codigo:", codigo); campo(f, "Nombre:", nombre);
            campo(f, "Tipo:", tipo); campo(f, "Capacidad:", capacidad);
            campo(f, "Precio base Q:", precio); campo(f, "Estado:", est);
            campo(f, "Abre HH:mm:", apertura); campo(f, "Cierra HH:mm:", cierre);
            if (aceptar(f, "Nueva cancha")) {
                ejecutar(() -> gestion.registrarCancha(codigo.getText(), nombre.getText(),
                        (TipoCancha) tipo.getSelectedItem(), Integer.parseInt(capacidad.getText().trim()),
                        new BigDecimal(precio.getText().trim()), (EstadoCancha) est.getSelectedItem(),
                        LocalTime.parse(apertura.getText().trim()), LocalTime.parse(cierre.getText().trim())));
                actualizar.run();
            }
        });
        estado.addActionListener(e -> {
            int fila = seleccion(tabla);
            if (fila == -1) return;
            Cancha c = datos.getCanchas().get(fila);
            EstadoCancha nuevoEstado = (EstadoCancha) JOptionPane.showInputDialog(ventana,
                    "Nuevo estado de " + c.getNombre(), "Estado de cancha", JOptionPane.QUESTION_MESSAGE,
                    null, EstadoCancha.values(), c.getEstado());
            if (nuevoEstado != null) {
                ejecutar(() -> gestion.cambiarEstadoCancha(c, nuevoEstado));
                actualizar.run();
            }
        });
        actualizar.run();
        return base(botones, modelo, tabla, "El estado MANTENIMIENTO bloquea nuevas reservas.");
    }

    public JPanel crearReservas() {
        DefaultTableModel modelo = modelo("Codigo", "Cliente", "Cancha", "Fecha", "Inicio", "Fin", "Estado", "Total Q", "Saldo Q");
        JTable tabla = new JTable(modelo);
        JPanel botones = filaBotones();
        JButton nuevo = new JButton("Nueva reserva");
        JButton cancelar = new JButton("Cancelar seleccionada");
        botones.add(nuevo); botones.add(cancelar);
        Runnable actualizar = () -> {
            modelo.setRowCount(0);
            for (Reserva r : datos.getReservas()) {
                modelo.addRow(new Object[]{r.getCodigo(), r.getCliente().getNombre(),
                        r.getCancha().getNombre(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(),
                        r.getEstado(), r.getTotal(), r.getSaldo()});
            }
        };
        nuevo.addActionListener(e -> {
            if (datos.getClientes().isEmpty() || datos.getCanchas().isEmpty()) {
                JOptionPane.showMessageDialog(ventana, "Primero registre clientes y canchas."); return;
            }
            JTextField codigo = new JTextField();
            JComboBox<String> cliente = new JComboBox<>(nombresClientes(datos.getClientes()));
            JComboBox<String> cancha = new JComboBox<>(nombresCanchas(datos.getCanchas()));
            JTextField fecha = new JTextField(LocalDate.now().plusDays(1).toString());
            JTextField inicio = new JTextField("18:00");
            JTextField minutos = new JTextField("60");
            JPanel f = formulario();
            campo(f, "Codigo:", codigo); campo(f, "Cliente:", cliente);
            campo(f, "Cancha:", cancha); campo(f, "Fecha AAAA-MM-DD:", fecha);
            campo(f, "Hora HH:mm:", inicio); campo(f, "Duracion minutos:", minutos);
            if (aceptar(f, "Nueva reserva")) {
                ejecutar(() -> gestion.registrarReserva(codigo.getText(),
                        datos.getClientes().get(cliente.getSelectedIndex()),
                        datos.getCanchas().get(cancha.getSelectedIndex()),
                        LocalDate.parse(fecha.getText().trim()), LocalTime.parse(inicio.getText().trim()),
                        Integer.parseInt(minutos.getText().trim())));
                actualizar.run();
            }
        });
        cancelar.addActionListener(e -> {
            int fila = seleccion(tabla);
            if (fila == -1) return;
            Reserva r = datos.getReservas().get(fila);
            JTextField motivo = new JTextField(); JTextField operador = new JTextField();
            JPanel f = formulario();
            campo(f, "Motivo:", motivo); campo(f, "Operador responsable:", operador);
            if (aceptar(f, "Cancelar reserva " + r.getCodigo())) {
                ejecutar(() -> gestion.cancelarReserva(r, motivo.getText(), operador.getText()));
                actualizar.run();
            }
        });
        actualizar.run();
        return base(botones, modelo, tabla, "No se pueden crear reservas que choquen con otra en la misma cancha.");
    }
}
