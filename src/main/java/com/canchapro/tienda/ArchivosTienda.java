package com.canchapro.tienda;

import com.canchapro.modelo.MetodoPago;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** Almacena el modulo opcional en tres CSV independientes del core. */
public class ArchivosTienda {
    private final Path carpeta;
    private static final String[] ARCHIVOS={"productos.csv", "ventas.csv", "detalle_ventas.csv"};
    public ArchivosTienda(Path carpeta) { this.carpeta=Objects.requireNonNull(carpeta); }

    private List<String[]> leer(String archivo, String encabezado, int columnas) throws IOException {
        List<String> lineas=Files.readAllLines(carpeta.resolve(archivo), StandardCharsets.UTF_8);
        if (lineas.isEmpty() || !lineas.get(0).equals(encabezado))
            throw new IOException("Encabezado incorrecto en " + archivo);
        List<String[]> filas=new ArrayList<>();
        for (int i=1;i<lineas.size();i++) {
            if (lineas.get(i).isEmpty()) continue;
            String[] campos=lineas.get(i).split(";",-1);
            if (campos.length!=columnas) throw new IOException("Registro incorrecto en " +archivo+" linea "+(i+1));
            filas.add(campos);
        }
        return filas;
    }
    private static void texto(String valor) {
        if (valor==null || valor.contains(";") || valor.contains("\n") || valor.contains("\r"))
            throw new IllegalArgumentException("Texto con separadores CSV no permitido");
    }

    public DatosTienda cargarOIniciar() throws IOException {
        int presentes=0;
        for (String n : ARCHIVOS) if (Files.exists(carpeta.resolve(n))) presentes++;
        if (presentes==0) {
            DatosTienda base=new DatosTienda();
            base.getProductos().add(new Producto("PR01","Agua pura","Bebidas",new BigDecimal("5.00"),30,5,true));
            base.getProductos().add(new Producto("PR02","Galletas","Snacks",new BigDecimal("8.00"),20,4,true));
            guardar(base);
            return base;
        }
        if (presentes!=ARCHIVOS.length) throw new IOException("Faltan archivos CSV del modulo tienda");
        DatosTienda d=new DatosTienda();
        Set<String> codigosProductos=new HashSet<>(), codigosVentas=new HashSet<>();
        try {
            for (String[] a : leer("productos.csv","codigo;nombre;categoria;precio;existencia;minimo;activo",7)) {
                Producto p=new Producto(a[0],a[1],a[2],new BigDecimal(a[3]),Integer.parseInt(a[4]),Integer.parseInt(a[5]),Boolean.parseBoolean(a[6]));
                if (!codigosProductos.add(p.getCodigo())) throw new IllegalArgumentException("Producto duplicado");
                d.getProductos().add(p);
            }
            Map<String,List<DetalleVenta>> detalle=new HashMap<>();
            for (String[] a : leer("detalle_ventas.csv","venta;producto;cantidad;precioUnitario;subtotal",5)) {
                if (!codigosProductos.contains(a[1])) throw new IllegalArgumentException("Detalle con producto inexistente");
                DetalleVenta det=new DetalleVenta(a[1],Integer.parseInt(a[2]),new BigDecimal(a[3]));
                if (det.getSubtotal().compareTo(new BigDecimal(a[4]))!=0)
                    throw new IllegalArgumentException("Subtotal de detalle incorrecto");
                detalle.computeIfAbsent(a[0],x->new ArrayList<>()).add(det);
            }
            for (String[] a : leer("ventas.csv","codigo;fechaHora;reserva;total;metodo",5)) {
                if (!codigosVentas.add(a[0])) throw new IllegalArgumentException("Venta duplicada");
                Venta v=new Venta(a[0],LocalDateTime.parse(a[1]),a[2],MetodoPago.valueOf(a[4]),
                        detalle.getOrDefault(a[0],List.of()));
                if (v.getTotal().compareTo(new BigDecimal(a[3]))!=0)
                    throw new IllegalArgumentException("Total de venta incorrecto");
                d.getVentas().add(v);
            }
            if (!codigosVentas.containsAll(detalle.keySet()))
                throw new IllegalArgumentException("Detalles sin venta");
        } catch (RuntimeException e) { throw new IOException("Error en CSV de tienda: "+e.getMessage(),e); }
        return d;
    }

    public void guardar(DatosTienda datos) throws IOException {
        Files.createDirectories(carpeta);
        Map<String,List<String>> lineas=new LinkedHashMap<>();
        lineas.put("productos.csv",new ArrayList<>(List.of("codigo;nombre;categoria;precio;existencia;minimo;activo")));
        lineas.put("ventas.csv",new ArrayList<>(List.of("codigo;fechaHora;reserva;total;metodo")));
        lineas.put("detalle_ventas.csv",new ArrayList<>(List.of("venta;producto;cantidad;precioUnitario;subtotal")));
        for (Producto p : datos.getProductos()) {
            for (String x : List.of(p.getCodigo(),p.getNombre(),p.getCategoria())) texto(x);
            lineas.get("productos.csv").add(String.join(";",p.getCodigo(),p.getNombre(),p.getCategoria(),
                    p.getPrecio().toString(),String.valueOf(p.getExistencia()),String.valueOf(p.getMinimo()),String.valueOf(p.isActivo())));
        }
        for (Venta v : datos.getVentas()) {
            texto(v.getCodigo()); texto(v.getReservaCodigo());
            lineas.get("ventas.csv").add(String.join(";",v.getCodigo(),v.getFechaHora().toString(),v.getReservaCodigo(),
                    v.getTotal().toString(),v.getMetodo().name()));
            for (DetalleVenta d : v.getDetalles()) {
                texto(d.getProductoCodigo());
                lineas.get("detalle_ventas.csv").add(String.join(";",v.getCodigo(),d.getProductoCodigo(),
                        String.valueOf(d.getCantidad()),d.getPrecioUnitario().toString(),d.getSubtotal().toString()));
            }
        }
        Map<Path,Path> temps=new LinkedHashMap<>();
        try {
            for (var e : lineas.entrySet()) {
                Path tmp=Files.createTempFile(carpeta,"tienda_",".tmp");
                temps.put(carpeta.resolve(e.getKey()),tmp);
                Files.write(tmp,e.getValue(),StandardCharsets.UTF_8);
            }
            for (var e : temps.entrySet()) {
                try { Files.move(e.getValue(),e.getKey(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
                catch (AtomicMoveNotSupportedException ex) { Files.move(e.getValue(),e.getKey(),StandardCopyOption.REPLACE_EXISTING); }
            }
        } finally { for (Path tmp : temps.values()) Files.deleteIfExists(tmp); }
    }
}
