package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.servicio.GestionRegistros;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

/** Pruebas del avance 06: crear, editar, buscar, reservar y recuperar CSV. */
public class PruebasFormularios {
    private static int correctas;
    private static void verificar(boolean valor, String prueba) {
        if (!valor) throw new AssertionError("Fallo: " + prueba);
        correctas++;
        System.out.println("OK " + correctas + ": " + prueba);
    }
    private static void rechazar(Runnable accion, String prueba) {
        try { accion.run(); }
        catch (IllegalArgumentException | IllegalStateException esperado) {
            verificar(true, prueba);
            return;
        }
        throw new AssertionError("No rechazo: " + prueba);
    }
    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("canchapro06-pruebas-");
        ArchivosCSV archivos = new ArchivosCSV(carpeta.resolve("datos"));
        DatosSistema datos = archivos.cargarOIniciar();
        GestionRegistros g = new GestionRegistros(datos, archivos);
        int inicialClientes = datos.getClientes().size();
        Cliente ana = g.registrarCliente("CL99", "Ana Lopez", "44445555", "ana@correo.gt");
        verificar(datos.getClientes().size() == inicialClientes + 1, "Agregar cliente");
        verificar(g.buscarClientes("ana").contains(ana), "Buscar por nombre");
        verificar(g.buscarClientes("44445555").contains(ana), "Buscar por telefono");
        verificar(g.buscarClientes("cl99").contains(ana), "Buscar por codigo");
        rechazar(() -> g.registrarCliente("CL99", "Otro", "44446666", ""), "Codigo cliente duplicado");
        rechazar(() -> g.registrarCliente("CL98", "Otro", "44445555", ""), "Telefono duplicado");
        g.actualizarCliente(ana, "Ana Maria Lopez", "44447777", "");
        verificar(ana.getNombre().equals("Ana Maria Lopez") && ana.getTelefono().equals("44447777"), "Editar cliente");
        rechazar(() -> g.actualizarCliente(ana, "", "44447777", ""), "Nombre vacio invalido");
        verificar(ana.getNombre().equals("Ana Maria Lopez"), "Fallo no cambia nombre");
        Cancha nueva = g.registrarCancha("C99", "Cancha de prueba", TipoCancha.FUTBOL_5,
            10, new BigDecimal("155.00"), EstadoCancha.DISPONIBLE,
            LocalTime.of(8,0), LocalTime.of(23,0));
        verificar(datos.getCanchas().contains(nueva), "Agregar cancha");
        rechazar(() -> g.registrarCancha("C99", "Repetida", TipoCancha.FUTBOL_5,
            10, new BigDecimal("155"), EstadoCancha.DISPONIBLE,
            LocalTime.of(8,0), LocalTime.of(23,0)), "Codigo cancha duplicado");
        g.cambiarEstadoCancha(nueva, EstadoCancha.MANTENIMIENTO);
        verificar(nueva.getEstado() == EstadoCancha.MANTENIMIENTO, "Cambiar a mantenimiento");
        LocalDate fecha = LocalDate.now().plusDays(5);
        rechazar(() -> g.registrarReserva("R99", ana, nueva, fecha, LocalTime.of(18,0), 60),
                "Mantenimiento impide reservar");
        g.cambiarEstadoCancha(nueva, EstadoCancha.DISPONIBLE);
        Reserva r = g.registrarReserva("R99", ana, nueva, fecha, LocalTime.of(18,0), 90);
        verificar(r.getHoraFin().equals(LocalTime.of(19,30)), "Hora final calculada");
        rechazar(() -> g.registrarReserva("R100", ana, nueva, fecha, LocalTime.of(19,0), 60),
                "Evitar conflicto de horario");
        rechazar(() -> g.registrarReserva("R99", ana, nueva, fecha.plusDays(1), LocalTime.of(18,0), 60),
                "Codigo de reserva duplicado");
        Reserva siguiente = g.registrarReserva("R100", ana, nueva, fecha, LocalTime.of(19,30), 60);
        verificar(siguiente != null, "Reserva justo despues permitida");
        g.cancelarReserva(r, "Cliente aviso", "Recepcion");
        verificar(r.getEstado() == EstadoReserva.CANCELADA, "Cancelar reserva");
        verificar(r.getMotivoCancelacion().equals("Cliente aviso"), "Guardar motivo de cancelacion");
        rechazar(() -> g.cancelarReserva(r, "Otra", "Recepcion"), "No cancelar dos veces");
        Reserva espacioLibre = g.registrarReserva("R101", ana, nueva, fecha, LocalTime.of(18,0), 60);
        verificar(espacioLibre != null, "Cancelar libera la franja");
        DatosSistema recuperados = archivos.cargar();
        verificar(recuperados.getClientes().stream().anyMatch(c -> c.getCodigo().equals("CL99") && c.getNombre().equals("Ana Maria Lopez")), "Cliente editado persiste en CSV");
        verificar(recuperados.getCanchas().stream().anyMatch(c -> c.getCodigo().equals("C99")), "Cancha persiste en CSV");
        verificar(recuperados.getReservas().stream().anyMatch(v -> v.getCodigo().equals("R99") && v.getEstado() == EstadoReserva.CANCELADA), "Cancelacion persiste en CSV");
        verificar(recuperados.getReservas().stream().anyMatch(v -> v.getCodigo().equals("R101")), "Nueva reserva persiste en CSV");
        System.out.println("TOTAL: " + correctas + " pruebas correctas.");
    }
}
