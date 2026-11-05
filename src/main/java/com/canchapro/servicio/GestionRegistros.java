package com.canchapro.servicio;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Reglas de los formularios; la interfaz no escribe archivos directamente. */
public class GestionRegistros {
    private final DatosSistema datos;
    private final ArchivosCSV archivos;
    private final ServicioDisponibilidad disponibilidad;

    public GestionRegistros(DatosSistema datos, ArchivosCSV archivos) {
        this.datos = Objects.requireNonNull(datos);
        this.archivos = Objects.requireNonNull(archivos);
        this.disponibilidad = new ServicioDisponibilidad(datos.getCanchas(), datos.getReservas());
    }

    public List<Cliente> buscarClientes(String consulta) {
        String buscar = consulta == null ? "" : consulta.trim().toLowerCase(Locale.ROOT);
        List<Cliente> encontrados = new ArrayList<>();
        for (Cliente c : datos.getClientes()) {
            if (c.getCodigo().toLowerCase(Locale.ROOT).contains(buscar)
                    || c.getNombre().toLowerCase(Locale.ROOT).contains(buscar)
                    || c.getTelefono().contains(buscar)) {
                encontrados.add(c);
            }
        }
        return encontrados;
    }

    public Cliente registrarCliente(String codigo, String nombre, String telefono, String correo) {
        for (Cliente c : datos.getClientes()) {
            if (c.getCodigo().equalsIgnoreCase(codigo == null ? "" : codigo.trim())) {
                throw new IllegalArgumentException("Ya existe ese codigo de cliente");
            }
            if (c.getTelefono().equals(telefono == null ? "" : telefono.trim())) {
                throw new IllegalArgumentException("El telefono ya pertenece a un cliente");
            }
        }
        Cliente nuevo = new Cliente(codigo, nombre, telefono, correo);
        datos.getClientes().add(nuevo);
        try {
            guardar();
        } catch (IllegalStateException e) {
            datos.getClientes().remove(nuevo);
            throw e;
        }
        return nuevo;
    }

    public void actualizarCliente(Cliente cliente, String nombre, String telefono, String correo) {
        if (!datos.getClientes().contains(cliente)) {
            throw new IllegalArgumentException("Seleccione un cliente registrado");
        }
        for (Cliente c : datos.getClientes()) {
            if (c != cliente && c.getTelefono().equals(telefono == null ? "" : telefono.trim())) {
                throw new IllegalArgumentException("El telefono ya pertenece a otro cliente");
            }
        }
        // Validar ambos datos antes de modificar el objeto.
        new Cliente(cliente.getCodigo(), nombre, telefono, correo, cliente.getFechaRegistro());
        String anteriorNombre = cliente.getNombre();
        String anteriorTelefono = cliente.getTelefono();
        String anteriorCorreo = cliente.getCorreo();
        cliente.actualizarNombre(nombre);
        cliente.actualizarContacto(telefono, correo);
        try {
            guardar();
        } catch (IllegalStateException e) {
            cliente.actualizarNombre(anteriorNombre);
            cliente.actualizarContacto(anteriorTelefono, anteriorCorreo);
            throw e;
        }
    }

    public Cancha registrarCancha(String codigo, String nombre, TipoCancha tipo, int capacidad,
            BigDecimal precioBase, EstadoCancha estado, LocalTime apertura, LocalTime cierre) {
        for (Cancha c : datos.getCanchas()) {
            if (c.getCodigo().equalsIgnoreCase(codigo == null ? "" : codigo.trim())) {
                throw new IllegalArgumentException("Ya existe ese codigo de cancha");
            }
        }
        Cancha nueva = new Cancha(codigo, nombre, tipo, capacidad, precioBase, estado, apertura, cierre);
        datos.getCanchas().add(nueva);
        try {
            guardar();
        } catch (IllegalStateException e) {
            datos.getCanchas().remove(nueva);
            throw e;
        }
        return nueva;
    }

    public void cambiarEstadoCancha(Cancha cancha, EstadoCancha estado) {
        if (!datos.getCanchas().contains(cancha)) {
            throw new IllegalArgumentException("Seleccione una cancha registrada");
        }
        EstadoCancha anterior = cancha.getEstado();
        cancha.cambiarEstado(estado);
        try {
            guardar();
        } catch (IllegalStateException e) {
            cancha.cambiarEstado(anterior);
            throw e;
        }
    }

    public Reserva registrarReserva(String codigo, Cliente cliente, Cancha cancha,
            LocalDate fecha, LocalTime inicio, int duracionMinutos) {
        if (!datos.getClientes().contains(cliente)) {
            throw new IllegalArgumentException("El cliente no esta registrado");
        }
        Reserva nueva = disponibilidad.registrarReserva(codigo, cliente, cancha, fecha, inicio, duracionMinutos);
        try {
            guardar();
        } catch (IllegalStateException e) {
            datos.getReservas().remove(nueva);
            throw e;
        }
        return nueva;
    }

    public void cancelarReserva(Reserva reserva, String motivo, String operador) {
        if (!datos.getReservas().contains(reserva)) {
            throw new IllegalArgumentException("Seleccione una reserva registrada");
        }
        // Conservar copia para deshacer el cambio en memoria ante error de guardado.
        Reserva anterior = Reserva.recuperar(reserva.getCodigo(), reserva.getCliente(), reserva.getCancha(),
                reserva.getFecha(), reserva.getHoraInicio(), reserva.getDuracionMinutos(), reserva.getEstado(),
                reserva.getFechaCancelacion(), reserva.getMotivoCancelacion(), reserva.getOperadorCancelacion(),
                reserva.getServicios(), reserva.getPagos());
        int posicion = datos.getReservas().indexOf(reserva);
        reserva.cancelar(motivo, operador);
        try {
            guardar();
        } catch (IllegalStateException e) {
            datos.getReservas().set(posicion, anterior);
            throw e;
        }
    }

    private void guardar() {
        try {
            archivos.guardar(datos);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar los CSV: " + e.getMessage(), e);
        }
    }
}
