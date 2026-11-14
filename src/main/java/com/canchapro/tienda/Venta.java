package com.canchapro.tienda;

import com.canchapro.modelo.MetodoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Venta {
    private final String codigo;
    private final LocalDateTime fechaHora;
    private final String reservaCodigo;
    private final MetodoPago metodo;
    private final List<DetalleVenta> detalles;
    private final BigDecimal total;

    public Venta(String codigo, LocalDateTime fechaHora, String reservaCodigo,
                 MetodoPago metodo, List<DetalleVenta> detalles) {
        if (codigo == null || codigo.isBlank() || detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("Codigo y productos son obligatorios");
        this.codigo = codigo.trim(); this.fechaHora=Objects.requireNonNull(fechaHora);
        this.reservaCodigo=reservaCodigo == null ? "" : reservaCodigo.trim();
        this.metodo=Objects.requireNonNull(metodo); this.detalles=List.copyOf(detalles);
        BigDecimal suma=BigDecimal.ZERO;
        for (DetalleVenta d : detalles) suma=suma.add(d.getSubtotal());
        this.total=suma.setScale(2);
    }
    public String getCodigo() { return codigo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getReservaCodigo() { return reservaCodigo; }
    public MetodoPago getMetodo() { return metodo; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public BigDecimal getTotal() { return total; }
}
