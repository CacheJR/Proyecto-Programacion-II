package com.canchapro.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Reserva {
    private final String codigo;
    private final Cliente cliente;
    private final Cancha cancha;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final int duracionMinutos;
    private final LocalTime horaFin;
    private EstadoReserva estado;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;
    private String operadorCancelacion;

    public Reserva(String codigo, Cliente cliente, Cancha cancha, LocalDate fecha,
                   LocalTime horaInicio, int duracionMinutos) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo de reserva obligatorio");
        }
        this.cliente = Objects.requireNonNull(cliente);
        this.cancha = Objects.requireNonNull(cancha);
        this.fecha = Objects.requireNonNull(fecha);
        this.horaInicio = Objects.requireNonNull(horaInicio);
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duracion debe ser positiva");
        }
        LocalDateTime fin = LocalDateTime.of(fecha, horaInicio).plusMinutes(duracionMinutos);
        if (!fin.toLocalDate().equals(fecha)) {
            throw new IllegalArgumentException("La reserva no puede pasar de medianoche");
        }
        this.horaFin = fin.toLocalTime();
        if (cancha.getEstado() != EstadoCancha.DISPONIBLE) {
            throw new IllegalArgumentException("La cancha esta fuera de servicio");
        }
        if (horaInicio.isBefore(cancha.getHoraApertura()) || horaFin.isAfter(cancha.getHoraCierre())) {
            throw new IllegalArgumentException("Reserva fuera del horario de la cancha");
        }
        this.codigo = codigo.trim();
        this.duracionMinutos = duracionMinutos;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public void confirmar() {
        cambiarEstado(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
    }

    public void iniciar() {
        cambiarEstado(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO);
    }

    public void finalizar() {
        cambiarEstado(EstadoReserva.EN_CURSO, EstadoReserva.FINALIZADA);
    }

    private void cambiarEstado(EstadoReserva esperado, EstadoReserva siguiente) {
        if (estado != esperado) {
            throw new IllegalStateException("No se puede pasar de " + estado + " a " + siguiente);
        }
        estado = siguiente;
    }

    public void cancelar(String motivo, String operador) {
        if (estado != EstadoReserva.PENDIENTE && estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Esta reserva no puede cancelarse");
        }
        if (motivo == null || motivo.isBlank() || operador == null || operador.isBlank()) {
            throw new IllegalArgumentException("Indique motivo y operador");
        }
        this.motivoCancelacion = motivo.trim();
        this.operadorCancelacion = operador.trim();
        this.fechaCancelacion = LocalDateTime.now();
        this.estado = EstadoReserva.CANCELADA;
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Cancha getCancha() { return cancha; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public EstadoReserva getEstado() { return estado; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public String getOperadorCancelacion() { return operadorCancelacion; }
}
