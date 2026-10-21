package com.canchapro.modelo;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

public class Cancha {
    private final String codigo;
    private final String nombre;
    private final TipoCancha tipo;
    private final int capacidad;
    private final BigDecimal precioBase;
    private EstadoCancha estado;
    private final LocalTime horaApertura;
    private final LocalTime horaCierre;

    public Cancha(String codigo, String nombre, TipoCancha tipo, int capacidad,
                  BigDecimal precioBase, EstadoCancha estado,
                  LocalTime horaApertura, LocalTime horaCierre) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Codigo y nombre son obligatorios");
        }
        if (capacidad <= 0) throw new IllegalArgumentException("Capacidad incorrecta");
        if (precioBase == null || precioBase.signum() < 0) {
            throw new IllegalArgumentException("Precio base incorrecto");
        }
        this.horaApertura = Objects.requireNonNull(horaApertura);
        this.horaCierre = Objects.requireNonNull(horaCierre);
        if (!horaApertura.isBefore(horaCierre)) {
            throw new IllegalArgumentException("Horario incorrecto");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.tipo = Objects.requireNonNull(tipo);
        this.capacidad = capacidad;
        this.precioBase = precioBase;
        this.estado = Objects.requireNonNull(estado);
    }

    public void cambiarEstado(EstadoCancha nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado);
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public TipoCancha getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public EstadoCancha getEstado() { return estado; }
    public LocalTime getHoraApertura() { return horaApertura; }
    public LocalTime getHoraCierre() { return horaCierre; }
}
