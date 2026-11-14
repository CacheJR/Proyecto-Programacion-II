package com.canchapro.tienda;

import java.math.BigDecimal;
import java.util.Objects;

/** Producto de la tienda. El stock cambia al confirmar una venta. */
public class Producto {
    private final String codigo, nombre, categoria;
    private final BigDecimal precio;
    private int existencia;
    private final int minimo;
    private final boolean activo;

    public Producto(String codigo, String nombre, String categoria, BigDecimal precio,
                    int existencia, int minimo, boolean activo) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()
                || categoria == null || categoria.isBlank())
            throw new IllegalArgumentException("Codigo, nombre y categoria son obligatorios");
        if (precio == null || precio.signum() < 0 || precio.scale() > 2 || existencia < 0 || minimo < 0)
            throw new IllegalArgumentException("Precio o inventario incorrecto");
        this.codigo=codigo.trim(); this.nombre=nombre.trim(); this.categoria=categoria.trim();
        this.precio=precio.setScale(2); this.existencia=existencia;
        this.minimo=minimo; this.activo=activo;
    }
    public void vender(int cantidad) {
        if (!activo) throw new IllegalStateException("Producto inactivo");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad invalida");
        if (cantidad > existencia) throw new IllegalArgumentException("Existencia insuficiente de " + nombre);
        existencia -= cantidad;
    }
    public void restaurar(int cantidad) { existencia += cantidad; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getExistencia() { return existencia; }
    public int getMinimo() { return minimo; }
    public boolean isActivo() { return activo; }
    public boolean isStockBajo() { return existencia <= minimo; }
    @Override public String toString() { return nombre + " (" + existencia + ") - Q" + precio; }
}
