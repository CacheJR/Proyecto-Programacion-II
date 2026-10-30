package com.canchapro.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Servicio que se puede agregar a una reserva. */
public class ServicioAdicional {
    private final String codigo;
    private final String nombre;
    private final BigDecimal precio;

    public ServicioAdicional(String codigo, String nombre, BigDecimal precio) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Codigo y nombre del servicio son obligatorios");
        }
        if (precio == null || precio.signum() < 0 || precio.scale() > 2) {
            throw new IllegalArgumentException("Precio del servicio incorrecto");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.precio = precio.setScale(2, RoundingMode.HALF_UP);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }

    @Override
    public String toString() { return nombre + " - Q" + precio; }
}
