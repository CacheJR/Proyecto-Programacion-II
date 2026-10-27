package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.servicio.ServicioDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Ejecutar Run File para probar las reglas, sin usar base de datos. */
public class PruebasDisponibilidad {
    private static int correctas;

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) throw new AssertionError("Fallo: " + descripcion);
        correctas++;
        System.out.println("OK - " + descripcion);
    }

    private static void esperarError(Runnable accion, String descripcion) {
        try {
            accion.run();
            throw new AssertionError("Se esperaba error: " + descripcion);
        } catch (IllegalArgumentException esperado) {
            verificar(true, descripcion);
        }
    }

    public static void main(String[] args) {
        List<Cliente> clientes = DatosEjemplo.crearClientes();
        List<Cancha> canchas = DatosEjemplo.crearCanchas();
        List<Reserva> reservas = DatosEjemplo.crearReservas(clientes, canchas);
        ServicioDisponibilidad motor = new ServicioDisponibilidad(canchas, reservas);
        LocalDate dia = LocalDate.now().plusDays(1);
        Cancha bombonera = canchas.get(0);
        Cancha maracana = canchas.get(1);
        Cancha monumental = canchas.get(2);

        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(18, 30), 60),
                "Traslape parcial rechazado");
        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(18, 15), 30),
                "Reserva contenida rechazada");
        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(17, 30), 150),
                "Reserva que contiene a otra rechazada");
        verificar(motor.estaDisponible(bombonera, dia, LocalTime.of(19, 30), 60),
                "Reserva exactamente despues aceptada");
        verificar(motor.estaDisponible(bombonera, dia, LocalTime.of(17, 0), 60),
                "Reserva exactamente antes aceptada");
        verificar(motor.estaDisponible(bombonera, dia.plusDays(1), LocalTime.of(18, 0), 60),
                "Misma hora en diferente fecha aceptada");
        verificar(motor.estaDisponible(maracana, dia, LocalTime.of(18, 0), 60),
                "Otra cancha puede reservarse");
        verificar(!motor.estaDisponible(monumental, dia, LocalTime.of(18, 0), 60),
                "Cancha en mantenimiento rechazada");
        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(7, 0), 60),
                "Antes de apertura rechazado");
        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(22, 30), 60),
                "Despues del cierre rechazado");
        verificar(motor.buscarDisponibles(TipoCancha.FUTBOL_5, dia,
                LocalTime.of(18, 0), 60).size() == 1,
                "Busqueda devuelve solo una cancha libre");
        verificar(motor.buscarDisponibles(TipoCancha.FUTBOL_7, dia,
                LocalTime.of(18, 0), 60).isEmpty(),
                "Busqueda excluye cancha en mantenimiento");
        esperarError(() -> motor.estaDisponible(bombonera, dia, LocalTime.of(18, 0), 0),
                "Duracion cero no permitida");
        esperarError(() -> motor.estaDisponible(bombonera, dia, LocalTime.of(23, 30), 90),
                "No se permite cambiar de dia");
        esperarError(() -> motor.registrarReserva("R001", clientes.get(0), maracana,
                dia, LocalTime.of(18, 0), 60), "Codigo de reserva duplicado rechazado");
        esperarError(() -> motor.registrarReserva("R003", clientes.get(0), bombonera,
                dia, LocalTime.of(18, 0), 60), "Registro traslapado rechazado");

        Reserva nueva = motor.registrarReserva("R003", clientes.get(0), bombonera,
                dia, LocalTime.of(19, 30), 60);
        verificar(reservas.contains(nueva) && reservas.size() == 3,
                "Registro valido se agrega a la lista en memoria");
        verificar(!motor.estaDisponible(bombonera, dia, LocalTime.of(19, 45), 30),
                "La reserva nueva ya bloquea el horario");
        nueva.cancelar("Cambio de plan", "Operador 1");
        verificar(motor.estaDisponible(bombonera, dia, LocalTime.of(19, 45), 30),
                "Cancelar una reserva libera su horario");

        System.out.println("TOTAL: " + correctas + " pruebas correctas.");
    }
}
