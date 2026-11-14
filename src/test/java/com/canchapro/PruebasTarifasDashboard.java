package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.servicio.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Regresion de tarifas configurables, precio historico y dashboard. */
public class PruebasTarifasDashboard {
    private static int pruebas;
    private static void ok(boolean condicion, String caso) {
        if (!condicion) throw new AssertionError(caso);
        System.out.println("OK " + ++pruebas + " " + caso);
    }
    public static void main(String[] args) throws Exception {
        Path base = Files.createTempDirectory("canchapro9-");
        ArchivosCSV archivos = new ArchivosCSV(base.resolve("datos"));
        DatosSistema datos = archivos.cargarOIniciar();
        ok(Files.exists(base.resolve("datos/tarifas.csv")), "tarifas CSV iniciales");
        ok(CalculadoraTarifas.getConfiguracion().getDiurna().compareTo(new BigDecimal("150.00")) == 0, "tarifa inicial");
        Reserva antigua = datos.getReservas().get(0);
        BigDecimal precioAnterior = antigua.getCostoCancha();
        ConfiguracionTarifas nuevas = new ConfiguracionTarifas(new BigDecimal("200.00"),new BigDecimal("250.00"),new BigDecimal("275.00"));
        archivos.guardarTarifas(nuevas);
        CalculadoraTarifas.aplicarConfiguracion(nuevas);
        ok(antigua.getCostoCancha().compareTo(precioAnterior) == 0, "reserva historica conserva costo");
        Reserva nueva = new Reserva("RNEW", datos.getClientes().get(0), datos.getCanchas().get(0),
           LocalDate.of(2026,11,17), LocalTime.of(18,0), 60);
        ok(nueva.getCostoCancha().compareTo(new BigDecimal("250.00"))==0, "reserva nueva usa tarifa configurada");
        datos.getReservas().add(nueva);
        archivos.guardar(datos);
        DatosSistema recuperados = archivos.cargar();
        ok(recuperados.getReservas().get(0).getCostoCancha().compareTo(precioAnterior)==0, "costo historico persiste");
        ok(recuperados.getReservas().stream().anyMatch(r->r.getCodigo().equals("RNEW") && r.getCostoCancha().compareTo(new BigDecimal("250.00"))==0), "tarifa nueva persiste");
        ok(archivos.cargarTarifas().getNocturna().compareTo(new BigDecimal("250.00"))==0, "configuracion recargable");
        ServicioDashboard d = new ServicioDashboard(recuperados);
        ServicioDashboard.Resumen indicadores = d.calcular(LocalDate.of(2026,11,17));
        ok(indicadores.reservasHoy() >= 1, "reserva de fecha mostrada");
        ok(indicadores.ingresosHoy().signum()>=0, "ingresos reales no negativos");
        ok(indicadores.saldosPendientes().signum()>=0, "saldos pendientes no negativos");
        try { new ConfiguracionTarifas(new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ONE);
              throw new AssertionError("No rechazo precio negativo");
        } catch (IllegalArgumentException esperado) {ok(true,"tarifa negativa rechazada");}
        CalculadoraTarifas.aplicarConfiguracion(ConfiguracionTarifas.valoresIniciales());
        System.out.println("TOTAL: "+ pruebas +" pruebas correctas.");
    }
}
