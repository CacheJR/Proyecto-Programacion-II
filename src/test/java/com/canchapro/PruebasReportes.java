package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.servicio.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Comprobaciones sencillas de agrupacion, sumas y filtros de reportes. */
public class PruebasReportes {
    private static int correctas;
    private static void comprobar(boolean bien, String nombre) {
        if (!bien) throw new AssertionError("Fallo: " + nombre);
        correctas++;
        System.out.println("OK " + correctas + " - " + nombre);
    }
    private static void error(Runnable accion, String nombre) {
        try { accion.run(); }
        catch (IllegalArgumentException esperado) { comprobar(true, nombre); return; }
        throw new AssertionError("Se esperaba error: " + nombre);
    }
    private static boolean monto(Object numero, String esperado) {
        return ((BigDecimal) numero).compareTo(new BigDecimal(esperado)) == 0;
    }
    private static ResultadoReporte crear(ServicioReportes s, TipoReporte tipo,
                          LocalDate desde, LocalDate hasta) {
        return s.generar(tipo, desde, hasta, null, null);
    }
    public static void main(String[] args) throws Exception {
        LocalDate d1 = LocalDate.of(2026, 11, 3);
        LocalDate d2 = LocalDate.of(2026, 11, 4);
        LocalDate d3 = LocalDate.of(2026, 11, 5);
        List<Cliente> clientes = DatosEjemplo.crearClientes();
        List<Cancha> canchas = DatosEjemplo.crearCanchas();
        List<Reserva> reservas = new ArrayList<>();
        Cancha c1 = canchas.get(0), c2 = canchas.get(1);
        Cliente ana = clientes.get(0), luis = clientes.get(1);

        Reserva r1 = new Reserva("Q01", ana, c1, d1, LocalTime.of(10, 0), 60);
        r1.registrarPago(new Pago("P01", new BigDecimal("80.00"), MetodoPago.EFECTIVO,
                 "Anticipo", d1.atTime(12, 0)));
        r1.confirmar();
        reservas.add(r1);
        Reserva r2 = new Reserva("Q02", ana, c1, d1, LocalTime.of(17, 0), 60);
        r2.agregarServicio(new ServicioAdicional("S01", "Balon", new BigDecimal("25.00")));
        r2.registrarPago(new Pago("P02", new BigDecimal("100.00"), MetodoPago.TARJETA,
                 "Anticipo", d3.atTime(10, 0)));
        reservas.add(r2);
        Reserva r3 = new Reserva("Q03", luis, c2, d1, LocalTime.of(11, 0), 120);
        r3.registrarPago(new Pago("P03", new BigDecimal("300.00"), MetodoPago.TRANSFERENCIA,
                 "Completo", d2.atTime(8, 0)));
        reservas.add(r3);
        Reserva cancelada = new Reserva("Q04", luis, c2, d2, LocalTime.of(10, 0), 60);
        cancelada.cancelar("Cambio de fecha", "Recepcion");
        reservas.add(cancelada);
        Reserva r5 = new Reserva("Q05", ana, c2, d2, LocalTime.of(12, 0), 60);
        r5.registrarPago(new Pago("P05", new BigDecimal("50.00"), MetodoPago.EFECTIVO,
                 "Abono", d2.atTime(10, 0)));
        reservas.add(r5);
        Reserva r6 = new Reserva("Q06", luis, c2, d2, LocalTime.of(17, 0), 60);
        reservas.add(r6);
        DatosSistema datos = new DatosSistema(clientes, canchas,
                 new ArrayList<>(List.of(new ServicioAdicional("S01", "Balon", new BigDecimal("25.00")))), reservas);
        ServicioReportes s = new ServicioReportes(datos);

        ResultadoReporte fecha = crear(s, TipoReporte.RESERVAS_FECHA, d1, d2);
        comprobar(fecha.getFilas().size() == 2, "Dos fechas con reservas");
        comprobar((int) fecha.getFilas().get(0)[1] == 3, "Tres reservas el primer dia");
        comprobar((int) fecha.getFilas().get(1)[1] == 3, "Tres reservas el segundo dia");
        comprobar((int) fecha.getFilas().get(1)[2] == 1, "Una cancelacion el segundo dia");
        comprobar(monto(fecha.getFilas().get(0)[3], "700.00"), "Previsto Q700 el primer dia");
        comprobar(monto(fecha.getFilas().get(1)[3], "375.00"), "Excluye cancelada en previsto");

        ResultadoReporte cancha = crear(s, TipoReporte.RESERVAS_CANCHA, d1, d2);
        comprobar(cancha.getFilas().size() == 2, "Agrupa dos canchas");
        comprobar((int) cancha.getFilas().get(0)[4] == 120, "C01 cuenta 120 minutos");
        comprobar((int) cancha.getFilas().get(1)[4] == 240, "C02 cuenta 240 minutos");

        ResultadoReporte ingresos = crear(s, TipoReporte.INGRESOS_PERIODO, d1, d3);
        comprobar(ingresos.getFilas().size() == 3, "Ingresos agrupados en tres dias");
        comprobar(monto(ingresos.getFilas().get(0)[2], "80.00"), "Ingreso de martes Q80");
        comprobar(monto(ingresos.getFilas().get(1)[2], "350.00"), "Ingreso de miercoles Q350");
        comprobar(monto(ingresos.getFilas().get(2)[2], "100.00"), "Ingreso de jueves Q100");
        comprobar(ingresos.getResumen().contains("530.00"), "Ingreso total cobrado Q530");
        comprobar(crear(s, TipoReporte.INGRESOS_PERIODO, d3, d3).getFilas().size() == 1,
                  "El filtro de ingresos usa fecha de pago");

        ResultadoReporte pendientes = crear(s, TipoReporte.PAGOS_PENDIENTES, d1, d2);
        comprobar(pendientes.getFilas().size() == 4, "Cuatro reservas con saldo");
        comprobar(pendientes.getResumen().contains("545.00"), "Saldos suman Q545");
        comprobar(pendientes.getFilas().stream().noneMatch(x -> x[0].equals("Q04")),
                  "Cancelada excluida de saldos");

        ResultadoReporte cancelaciones = crear(s, TipoReporte.RESERVAS_CANCELADAS,
                  LocalDate.now(), LocalDate.now());
        comprobar(cancelaciones.getFilas().size() == 1, "Cancelaciones por fecha de cancelacion");
        comprobar(cancelaciones.getFilas().get(0)[4].equals("Cambio de fecha"), "Conserva motivo");
        comprobar(crear(s, TipoReporte.RESERVAS_CANCELADAS, d1, d2).getFilas().isEmpty()
                  || LocalDate.now().equals(d1) || LocalDate.now().equals(d2),
                  "Distingue fecha del partido y cancelacion");

        ResultadoReporte ranking = crear(s, TipoReporte.CANCHA_MAS_UTILIZADA, d1, d2);
        comprobar(ranking.getFilas().size() == 2, "Ranking tiene dos canchas");
        comprobar(ranking.getFilas().get(0)[1].equals("C02"), "C02 gana con tres reservas activas");
        comprobar((int) ranking.getFilas().get(0)[3] == 3, "Ranking excluye cancelacion");
        ResultadoReporte horario = crear(s, TipoReporte.HORARIOS_DEMANDA, d1, d2);
        comprobar(horario.getFilas().get(0)[0].equals("17:00 - 17:59"), "17 h es la hora mas demandada");
        comprobar((int) horario.getFilas().get(0)[1] == 2, "Dos comienzos a las 17 h");
        ResultadoReporte frecuentes = crear(s, TipoReporte.CLIENTES_FRECUENTES, d1, d2);
        comprobar(frecuentes.getFilas().get(0)[1].equals(ana.getCodigo()), "Cliente mas frecuente Ana");
        comprobar((int) frecuentes.getFilas().get(0)[3] == 3, "Ana tiene tres reservas activas");

        comprobar(s.generar(TipoReporte.RESERVAS_FECHA, d1, d2, c1, null).getFilas().get(0)[1].equals(2),
                  "Filtro por cancha C01");
        comprobar(s.generar(TipoReporte.RESERVAS_FECHA, d1, d2, null, EstadoReserva.CANCELADA)
                  .getFilas().size() == 1, "Filtro por estado CANCELADA");
        comprobar(s.generar(TipoReporte.RESERVAS_CANCELADAS, LocalDate.now(), LocalDate.now(),
                  null, EstadoReserva.CONFIRMADA).getFilas().isEmpty(), "Cancelada no aparece al filtrar CONFIRMADA");
        comprobar(s.generar(TipoReporte.PAGOS_PENDIENTES, d1, d2, c1, null)
                  .getFilas().size() == 2, "Saldos pendientes por cancha C01");
        comprobar(s.generar(TipoReporte.INGRESOS_PERIODO, d1, d3, c1, null)
                  .getResumen().contains("180.00"), "Ingreso filtrado de C01 Q180");
        comprobar(crear(s, TipoReporte.RESERVAS_FECHA, d1.minusYears(1), d1.minusMonths(1))
                  .getFilas().isEmpty(), "Periodo vacio no falla");
        error(() -> crear(s, TipoReporte.RESERVAS_FECHA, d2, d1), "Rechaza fechas invertidas");

        // Comprobar que el reporte funciona tambien con datos leidos de CSV.
        Path carpeta = Files.createTempDirectory("canchapro07-").resolve("datos");
        ArchivosCSV csv = new ArchivosCSV(carpeta);
        csv.guardar(datos);
        ServicioReportes recuperado = new ServicioReportes(csv.cargar());
        comprobar(crear(recuperado, TipoReporte.INGRESOS_PERIODO, d1, d3)
                  .getResumen().contains("530.00"), "Ingresos se conservan despues de guardar CSV");
        comprobar(crear(recuperado, TipoReporte.CANCHA_MAS_UTILIZADA, d1, d2)
                  .getFilas().get(0)[1].equals("C02"), "Ranking se reconstruye desde CSV");
        comprobar(crear(recuperado, TipoReporte.RESERVAS_CANCELADAS, LocalDate.now(), LocalDate.now())
                  .getFilas().size() == 1, "Se recupera historial de cancelaciones");
        System.out.println("TOTAL REPORTES: " + correctas + " pruebas correctas.");
    }
}
