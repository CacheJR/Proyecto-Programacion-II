package com.canchapro.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/** Cada abono queda como un objeto separado. */
public class Pago {
    private final String codigo;
    private final LocalDateTime fechaHora;
    private final BigDecimal monto;
    private final MetodoPago metodo;
    private final String observacion;

    public Pago(String codigo, BigDecimal monto, MetodoPago metodo, String observacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo de pago obligatorio");
        }
        if (monto == null || monto.signum() <= 0 || monto.scale() > 2) {
            throw new IllegalArgumentException("El monto debe ser positivo y tener maximo 2 decimales");
        }
        this.codigo = codigo.trim();
        this.monto = monto.setScale(2, RoundingMode.HALF_UP);
        this.metodo = Objects.requireNonNull(metodo, "Metodo de pago obligatorio");
        this.observacion = observacion == null ? "" : observacion.trim();
        this.fechaHora = LocalDateTime.now();
    }

    public String getCodigo() { return codigo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public BigDecimal getMonto() { return monto; }
    public MetodoPago getMetodo() { return metodo; }
    public String getObservacion() { return observacion; }
}
