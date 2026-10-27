package com.canchapro.servicio;

import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.Cliente;
import com.canchapro.modelo.EstadoCancha;
import com.canchapro.modelo.EstadoReserva;
import com.canchapro.modelo.Reserva;
import com.canchapro.modelo.TipoCancha;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Reglas para consultar canchas y evitar reservar horarios ocupados. */
public class ServicioDisponibilidad {
    private final List<Cancha> canchas;
    private final List<Reserva> reservas;

    public ServicioDisponibilidad(List<Cancha> canchas, List<Reserva> reservas) {
        this.canchas = Objects.requireNonNull(canchas);
        this.reservas = Objects.requireNonNull(reservas);
    }

    /** Regresa las canchas del tipo solicitado que esten libres. */
    public List<Cancha> buscarDisponibles(TipoCancha tipo, LocalDate fecha,
                                           LocalTime inicio, int minutos) {
        Objects.requireNonNull(tipo, "El tipo de cancha es obligatorio");
        validarSolicitud(fecha, inicio, minutos);
        List<Cancha> libres = new ArrayList<>();
        for (Cancha cancha : canchas) {
            if (cancha.getTipo() == tipo && estaDisponible(cancha, fecha, inicio, minutos)) {
                libres.add(cancha);
            }
        }
        return libres;
    }

    /** Valida estado, horario y reservas existentes para una cancha. */
    public boolean estaDisponible(Cancha cancha, LocalDate fecha,
                                   LocalTime inicio, int minutos) {
        Objects.requireNonNull(cancha, "La cancha es obligatoria");
        LocalTime fin = validarSolicitud(fecha, inicio, minutos);
        if (cancha.getEstado() != EstadoCancha.DISPONIBLE
                || inicio.isBefore(cancha.getHoraApertura())
                || fin.isAfter(cancha.getHoraCierre())) {
            return false;
        }
        for (Reserva reserva : reservas) {
            if (reserva.getCancha().getCodigo().equals(cancha.getCodigo())
                    && reserva.getFecha().equals(fecha)
                    && reserva.getEstado() != EstadoReserva.CANCELADA
                    && seTraslapan(inicio, fin, reserva.getHoraInicio(), reserva.getHoraFin())) {
                return false;
            }
        }
        return true;
    }

    /** Regla: inicioNuevo < finExistente Y finNuevo > inicioExistente. */
    public boolean seTraslapan(LocalTime inicioNuevo, LocalTime finNuevo,
                               LocalTime inicioExistente, LocalTime finExistente) {
        return inicioNuevo.isBefore(finExistente) && finNuevo.isAfter(inicioExistente);
    }

    /** Guarda la reserva SOLO en la lista en memoria, si no hay conflicto. */
    public Reserva registrarReserva(String codigo, Cliente cliente, Cancha cancha,
                                   LocalDate fecha, LocalTime inicio, int minutos) {
        Objects.requireNonNull(cliente, "Seleccione un cliente");
        Objects.requireNonNull(cancha, "Seleccione una cancha");
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Ingrese el codigo de reserva");
        }
        for (Reserva existente : reservas) {
            if (existente.getCodigo().equalsIgnoreCase(codigo.trim())) {
                throw new IllegalArgumentException("El codigo de reserva ya existe");
            }
        }
        boolean existe = false;
        for (Cancha registrada : canchas) {
            if (registrada == cancha) { existe = true; break; }
        }
        if (!existe) {
            throw new IllegalArgumentException("La cancha no esta registrada");
        }
        if (!estaDisponible(cancha, fecha, inicio, minutos)) {
            throw new IllegalArgumentException("La cancha no esta disponible en ese horario");
        }
        Reserva nueva = new Reserva(codigo, cliente, cancha, fecha, inicio, minutos);
        reservas.add(nueva);
        return nueva;
    }

    /** Comprueba la duracion y que el final no pase a otro dia. */
    private LocalTime validarSolicitud(LocalDate fecha, LocalTime inicio, int minutos) {
        Objects.requireNonNull(fecha, "La fecha es obligatoria");
        Objects.requireNonNull(inicio, "La hora es obligatoria");
        if (minutos <= 0) {
            throw new IllegalArgumentException("La duracion debe ser mayor que cero");
        }
        LocalDateTime fin = LocalDateTime.of(fecha, inicio).plusMinutes(minutos);
        if (!fecha.equals(fin.toLocalDate())) {
            throw new IllegalArgumentException("La reserva no puede pasar de medianoche");
        }
        return fin.toLocalTime();
    }
}
