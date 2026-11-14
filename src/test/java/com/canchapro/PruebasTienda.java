package com.canchapro;

import com.canchapro.modelo.MetodoPago;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.tienda.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PruebasTienda {
    private static int correctas;
    private static void ok(boolean valor,String texto) {
        if (!valor) throw new AssertionError(texto);
        System.out.println("OK "+(++correctas)+" "+texto);
    }
    private static void error(Runnable accion,String texto) {
        try {accion.run();} catch (IllegalArgumentException | IllegalStateException esperado) {ok(true,texto);return;}
        throw new AssertionError("Debio rechazar: "+texto);
    }
    public static void main(String[] args) throws Exception {
        Path base=Files.createTempDirectory("canchapro_tienda_");
        Path carpeta=base.resolve("datos");
        ConfiguracionModulos flag=new ConfiguracionModulos(carpeta);
        ok(!flag.isTiendaActiva(),"tienda desactivada por defecto");
        flag.configurarTienda(true);
        ok(new ConfiguracionModulos(carpeta).isTiendaActiva(),"activacion persiste sin recompilar");
        DatosSistema core=new ArchivosCSV(carpeta).cargarOIniciar();
        ServicioTienda s=new ServicioTienda(new ArchivosTienda(carpeta),core);
        ok(s.getDatos().getProductos().size()==2,"productos iniciales");
        Producto agua=s.getDatos().getProductos().get(0), galletas=s.getDatos().getProductos().get(1);
        int stockAgua=agua.getExistencia(), stockGalletas=galletas.getExistencia();
        error(()->agua.vender(stockAgua+1),"rechazar stock insuficiente");
        ok(agua.getExistencia()==stockAgua,"stock no cambia tras rechazo");
        Producto extra=new Producto("PR99","Jugo","Bebidas",new BigDecimal("12.50"),4,2,true);
        s.registrarProducto(extra);
        ok(s.getDatos().getProductos().size()==3,"nuevo producto guardado");
        try {s.registrarProducto(new Producto("PR99","Repetido","Otro",BigDecimal.ONE,2,1,true));
           throw new AssertionError("producto duplicado");}
        catch(IllegalArgumentException e){ok(true,"rechaza codigo producto repetido");}
        Map<Producto,Integer> carrito=new LinkedHashMap<>();
        carrito.put(agua,3);carrito.put(galletas,2);
        Venta venta=s.vender("V001",core.getReservas().get(0).getCodigo(),MetodoPago.EFECTIVO,carrito);
        ok(venta.getDetalles().size()==2,"venta con varios productos");
        ok(venta.getTotal().compareTo(new BigDecimal("31.00"))==0,"total calculado correctamente");
        ok(agua.getExistencia()==stockAgua-3,"disminuye stock agua");
        ok(galletas.getExistencia()==stockGalletas-2,"disminuye stock galletas");
        try {s.vender("V002","INEXISTENTE",MetodoPago.EFECTIVO,carrito);throw new AssertionError("sin reserva");}
        catch(IllegalArgumentException e){ok(true,"reserva asociada debe existir");}
        try {s.vender("V001","",MetodoPago.EFECTIVO,carrito);throw new AssertionError("duplicado");}
        catch(IllegalArgumentException e){ok(true,"venta duplicada rechazada");}
        try {s.vender("V002","",MetodoPago.EFECTIVO,Map.of(extra,5));throw new AssertionError("stock");}
        catch(IllegalArgumentException e){ok(true,"venta mayor al stock rechazada");}
        ok(extra.getExistencia()==4,"stock se conserva tras venta rechazada");
        DatosTienda recuperado=new ArchivosTienda(carpeta).cargarOIniciar();
        ok(recuperado.getProductos().size()==3,"productos recuperados de CSV");
        ok(recuperado.getVentas().size()==1,"ventas recuperadas de CSV");
        ok(recuperado.getVentas().get(0).getDetalles().size()==2,"detalle de venta recuperado");
        ok(recuperado.getProductos().get(0).getExistencia()==stockAgua-3,"stock recuperado");
        ok(recuperado.getVentas().get(0).getTotal().compareTo(venta.getTotal())==0,"importe preservado");
        flag.configurarTienda(false);
        ok(!new ConfiguracionModulos(carpeta).isTiendaActiva(),"tienda puede desactivarse");
        ok(Files.exists(carpeta.resolve("productos.csv")),"desactivar no borra inventario");
        ok(Files.exists(carpeta.resolve("ventas.csv")),"desactivar no borra ventas");
        ok(Files.exists(carpeta.resolve("detalle_ventas.csv")),"desactivar no borra detalle");
        System.out.println("TOTAL: "+correctas+" pruebas de tienda correctas.");
    }
}
