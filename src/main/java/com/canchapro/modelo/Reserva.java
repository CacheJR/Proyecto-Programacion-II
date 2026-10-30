package com.canchapro.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.canchapro.servicio.CalculadoraTarifas;

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
    private final List<ServicioAdicional> servicios = new ArrayList<>();
    private final List<Pago> pagos = new ArrayList<>();

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

    /** Agrega un servicio antes del primer pago para no alterar deudas cobradas. */
    public void agregarServicio(ServicioAdicional servicio) {
        Objects.requireNonNull(servicio, "Servicio obligatorio");
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("No se permiten servicios en esta reserva");
        }
        if (!pagos.isEmpty()) {
            throw new IllegalStateException("No se pueden agregar servicios despues de registrar pagos");
        }
        for (ServicioAdicional actual : servicios) {
            if (actual.getCodigo().equalsIgnoreCase(servicio.getCodigo())) {
                throw new IllegalArgumentException("El servicio ya esta agregado");
            }
        }
        servicios.add(servicio);
    }

    public BigDecimal getCostoCancha() {
        return CalculadoraTarifas.costoCancha(fecha, horaInicio, duracionMinutos);
    }

    public BigDecimal getTotal() {
        BigDecimal total = getCostoCancha();
        for (ServicioAdicional servicio : servicios) total = total.add(servicio.getPrecio());
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPagado() {
        BigDecimal total = BigDecimal.ZERO;
        for (Pago pago : pagos) total = total.add(pago.getMonto());
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSaldo() { return getTotal().subtract(getPagado()); }

    public void registrarPago(Pago pago) {
        Objects.requireNonNull(pago, "Pago obligatorio");
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.FINALIZADA) {
            throw new IllegalStateException("No se aceptan pagos para esta reserva");
        }
        for (Pago actual : pagos) {
            if (actual.getCodigo().equalsIgnoreCase(pago.getCodigo())) {
                throw new IllegalArgumentException("El codigo del pago ya existe");
            }
        }
        if (pago.getMonto().compareTo(getSaldo()) > 0) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente");
        }
        pagos.add(pago);
    }

    public List<Pago> getPagos() { return Collections.unmodifiableList(pagos); }
    public List<ServicioAdicional> getServicios() { return Collections.unmodifiableList(servicios); }

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
