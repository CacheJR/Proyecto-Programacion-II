package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.servicio.CalculadoraTarifas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Pruebas simples: ejecutar este archivo desde NetBeans. */
public class PruebasPagos {
    private static int correctas = 0;
    private static void verificar(boolean condicion, String caso) {
        if (!condicion) throw new AssertionError("Fallo: " + caso);
        correctas++;
        System.out.println("OK " + correctas + " - " + caso);
    }
    private static void error(Runnable accion, String caso) {
        try { accion.run(); }
        catch (IllegalArgumentException | IllegalStateException esperado) {
            verificar(true, caso);
            return;
        }
        throw new AssertionError("Se esperaba un error: " + caso);
    }
    private static boolean igual(BigDecimal actual, String esperado) {
        return actual.compareTo(new BigDecimal(esperado)) == 0;
    }
    public static void main(String[] args) {
        LocalDate viernes = LocalDate.of(2026, 10, 30);
        LocalDate sabado = LocalDate.of(2026, 10, 31);
        LocalDate domingo = LocalDate.of(2026, 11, 1);
        verificar(igual(CalculadoraTarifas.tarifaPorHora(viernes, LocalTime.of(8, 0)), "150"), "Tarifa diurna");
        verificar(igual(CalculadoraTarifas.tarifaPorHora(viernes, LocalTime.of(16, 59)), "150"), "Antes de las 17:00");
        verificar(igual(CalculadoraTarifas.tarifaPorHora(viernes, LocalTime.of(17, 0)), "225"), "Tarifa nocturna desde 17:00");
        verificar(igual(CalculadoraTarifas.tarifaPorHora(sabado, LocalTime.of(9, 0)), "250"), "Tarifa sabado");
        verificar(igual(CalculadoraTarifas.tarifaPorHora(domingo, LocalTime.of(20, 0)), "250"), "Tarifa domingo");
        verificar(igual(CalculadoraTarifas.costoCancha(viernes, LocalTime.of(18, 0), 90), "337.50"), "90 minutos nocturnos");
        verificar(igual(CalculadoraTarifas.costoCancha(viernes, LocalTime.of(16, 30), 90), "225"), "Nivel medio: precio segun hora inicial");
        error(() -> CalculadoraTarifas.costoCancha(viernes, LocalTime.NOON, 0), "Duracion invalida");
        Cancha cancha = DatosEjemplo.crearCanchas().get(0);
        Cliente cliente = DatosEjemplo.crearClientes().get(0);
        Reserva reserva = new Reserva("RTEST", cliente, cancha, viernes, LocalTime.of(18, 0), 90);
        verificar(igual(reserva.getCostoCancha(), "337.50"), "Costo cancha");
        verificar(igual(reserva.getTotal(), "337.50"), "Total sin servicios");
        ServicioAdicional balon = new ServicioAdicional("S01", "Balon", new BigDecimal("25"));
        reserva.agregarServicio(balon);
        reserva.agregarServicio(new ServicioAdicional("S02", "Arbitro", new BigDecimal("80")));
        verificar(reserva.getServicios().size() == 2, "Dos servicios independientes");
        verificar(igual(reserva.getTotal(), "442.50"), "Suma tarifa y servicios");
        error(() -> reserva.agregarServicio(balon), "Servicio repetido");
        error(() -> new ServicioAdicional("S0", "Error", new BigDecimal("-1")), "Precio negativo");
        error(() -> new Pago("P0", BigDecimal.ZERO, MetodoPago.EFECTIVO, ""), "Pago en cero");
        error(() -> new Pago("P0", new BigDecimal("1.234"), MetodoPago.EFECTIVO, ""), "Monto con mas de dos decimales");
        reserva.registrarPago(new Pago("P01", new BigDecimal("100"), MetodoPago.EFECTIVO, "Anticipo"));
        verificar(igual(reserva.getPagado(), "100"), "Primer anticipo");
        verificar(igual(reserva.getSaldo(), "342.50"), "Saldo despues de anticipo");
        error(() -> reserva.agregarServicio(new ServicioAdicional("S03", "Luz", new BigDecimal("40"))), "No agregar servicios tras pago");
        error(() -> reserva.registrarPago(new Pago("P01", BigDecimal.ONE, MetodoPago.EFECTIVO, "")), "Codigo pago duplicado");
        error(() -> reserva.registrarPago(new Pago("P02", new BigDecimal("400"), MetodoPago.TARJETA, "")), "Pago superior al saldo");
        reserva.registrarPago(new Pago("P02", new BigDecimal("200"), MetodoPago.TRANSFERENCIA, "Segundo pago"));
        verificar(igual(reserva.getPagado(), "300"), "Dos pagos sumados");
        reserva.registrarPago(new Pago("P03", new BigDecimal("142.50"), MetodoPago.EFECTIVO, "Saldo"));
        verificar(igual(reserva.getSaldo(), "0"), "Pago completo: saldo cero");
        verificar(reserva.getPagos().size() == 3, "Se conserva historial de pagos");
        error(() -> reserva.registrarPago(new Pago("P04", BigDecimal.ONE, MetodoPago.EFECTIVO, "")), "Pago extra no permitido");
        reserva.confirmar();
        reserva.cancelar("Cliente no puede", "Operador");
        error(() -> reserva.registrarPago(new Pago("P05", BigDecimal.ONE, MetodoPago.EFECTIVO, "")), "No cobrar cancelada");
        System.out.println("TOTAL: " + correctas + " pruebas correctas.");
    }
}
