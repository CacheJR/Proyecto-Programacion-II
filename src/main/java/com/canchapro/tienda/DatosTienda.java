package com.canchapro.tienda;

import java.util.ArrayList;
import java.util.List;

/** Listas de la tienda, independientes del modulo obligatorio de reservas. */
public class DatosTienda {
    private final List<Producto> productos = new ArrayList<>();
    private final List<Venta> ventas = new ArrayList<>();
    public List<Producto> getProductos() { return productos; }
    public List<Venta> getVentas() { return ventas; }
}
