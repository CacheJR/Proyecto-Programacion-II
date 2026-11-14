package com.canchapro.tienda;

import java.math.BigDecimal;

public class DetalleVenta {
    private final String productoCodigo;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    public DetalleVenta(String productoCodigo, int cantidad, BigDecimal precioUnitario) {
        if (productoCodigo == null || productoCodigo.isBlank() || cantidad <= 0 || precioUnitario == null
                || precioUnitario.signum() < 0 || precioUnitario.scale() > 2)
            throw new IllegalArgumentException("Detalle de venta no valido");
        this.productoCodigo=productoCodigo.trim(); this.cantidad=cantidad;
        this.precioUnitario=precioUnitario.setScale(2);
    }
    public String getProductoCodigo() { return productoCodigo; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
