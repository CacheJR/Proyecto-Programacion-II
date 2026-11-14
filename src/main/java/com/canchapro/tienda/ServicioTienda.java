package com.canchapro.tienda;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.MetodoPago;
import com.canchapro.modelo.Reserva;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Gestiona stock, ventas, relaciones opcionales y persistencia de tienda. */
public class ServicioTienda {
    private final ArchivosTienda archivos;
    private final DatosSistema core;
    private final DatosTienda datos;

    public ServicioTienda(ArchivosTienda archivos, DatosSistema core) throws IOException {
        this.archivos=Objects.requireNonNull(archivos);
        this.core=Objects.requireNonNull(core);
        this.datos=archivos.cargarOIniciar();
    }
    public DatosTienda getDatos() { return datos; }
    public void registrarProducto(Producto p) throws IOException {
        Objects.requireNonNull(p);
        for (Producto actual : datos.getProductos()) if(actual.getCodigo().equalsIgnoreCase(p.getCodigo()))
            throw new IllegalArgumentException("Codigo de producto duplicado");
        datos.getProductos().add(p);
        try { archivos.guardar(datos); }
        catch (IOException | RuntimeException error) { datos.getProductos().remove(p); throw error; }
    }
    public Venta vender(String codigo, String reservaCodigo, MetodoPago metodo, Map<Producto,Integer> carrito)
            throws IOException {
        if (codigo==null || codigo.isBlank() || carrito==null || carrito.isEmpty())
            throw new IllegalArgumentException("Codigo de venta y productos obligatorios");
        for (Venta actual : datos.getVentas()) if (actual.getCodigo().equalsIgnoreCase(codigo.trim()))
            throw new IllegalArgumentException("Codigo de venta duplicado");
        if (reservaCodigo!=null && !reservaCodigo.isBlank()) {
            boolean existe=false;
            for (Reserva r:core.getReservas()) if (r.getCodigo().equals(reservaCodigo.trim())) existe=true;
            if (!existe) throw new IllegalArgumentException("La reserva asociada no existe");
        }
        List<DetalleVenta> lineas=new ArrayList<>();
        for (var e:carrito.entrySet()) {
            Producto producto=e.getKey(); int cantidad=e.getValue();
            if(!datos.getProductos().contains(producto)) throw new IllegalArgumentException("Producto no registrado");
            if (!producto.isActivo() || cantidad<=0 || cantidad>producto.getExistencia())
                throw new IllegalArgumentException("Producto inactivo o existencia insuficiente");
            lineas.add(new DetalleVenta(producto.getCodigo(),cantidad,producto.getPrecio()));
        }
        Venta v=new Venta(codigo,LocalDateTime.now(),reservaCodigo,metodo,lineas);
        for (var e:carrito.entrySet()) e.getKey().vender(e.getValue());
        datos.getVentas().add(v);
        try { archivos.guardar(datos); }
        catch (IOException | RuntimeException error) {
            datos.getVentas().remove(v);
            for (var e:carrito.entrySet()) e.getKey().restaurar(e.getValue());
            throw error;
        }
        return v;
    }
}
