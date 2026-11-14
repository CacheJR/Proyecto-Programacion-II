package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.servicio.GestionRegistros;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Se ejecuta con Run File desde NetBeans. No necesita interfaz grafica. */
public class PruebasFlujoReserva {
    private static int correctas;

    private static void verificar(boolean cumple, String descripcion) {
        if (!cumple) throw new AssertionError("Fallo: " + descripcion);
        correctas++;
        System.out.println("OK " + correctas + " - " + descripcion);
    }

    private static void rechazar(Runnable accion, String descripcion) {
        boolean error = false;
        try { accion.run(); }
        catch (IllegalArgumentException | IllegalStateException e) { error = true; }
        verificar(error, descripcion);
    }

    private static boolean dinero(BigDecimal actual, String esperado) {
        return actual.compareTo(new BigDecimal(esperado)) == 0;
    }

    public static void main(String[] args) throws Exception {
        Path carpeta = Files.createTempDirectory("canchapro08-").resolve("datos");
        ArchivosCSV csv = new ArchivosCSV(carpeta);
        DatosSistema datos = csv.cargarOIniciar();
        GestionRegistros gestion = new GestionRegistros(datos, csv);
        Cliente cliente = datos.getClientes().get(0);
        Cancha cancha = datos.getCanchas().get(0);
        LocalDate dia = LocalDate.of(2027, 1, 4); // lunes
        ServicioAdicional balon = datos.getServicios().get(0);
        ServicioAdicional arbitro = datos.getServicios().get(1);
        int antes = datos.getReservas().size();

        Reserva r = gestion.registrarReservaCompleta("R800", cliente, cancha, dia,
            LocalTime.of(18, 0), 90, List.of(balon, arbitro), new BigDecimal("100.00"), MetodoPago.EFECTIVO);
        verificar(r.getEstado() == EstadoReserva.CONFIRMADA, "Reserva completa queda confirmada");
        verificar(r.getServicios().size() == 2, "Se guardan dos servicios");
        verificar(dinero(r.getCostoCancha(), "337.50"), "Tarifa nocturna 90 minutos");
        verificar(dinero(r.getTotal(), "442.50"), "Total incluye servicios");
        verificar(dinero(r.getPagado(), "100.00"), "Se registra primer anticipo");
        verificar(dinero(r.getSaldo(), "342.50"), "Se calcula el saldo");
        verificar(datos.getReservas().size() == antes + 1, "Lista contiene la reserva completa");
        DatosSistema leidos = csv.cargar();
        Reserva copia = leidos.getReservas().stream().filter(x -> x.getCodigo().equals("R800")).findFirst().orElseThrow();
        verificar(copia.getEstado() == EstadoReserva.CONFIRMADA, "El estado persiste en CSV");
        verificar(copia.getServicios().size() == 2, "Los servicios persisten en CSV");
        verificar(dinero(copia.getPagado(), "100.00"), "El anticipo persiste en CSV");
        verificar(dinero(copia.getSaldo(), "342.50"), "El saldo persiste en CSV");

        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(19, 0), 60, List.of(), BigDecimal.ZERO, null), "Traslape rechazado");
        rechazar(() -> gestion.registrarReservaCompleta("R800", cliente, cancha, dia.plusDays(1),
            LocalTime.of(18, 0), 60, List.of(), BigDecimal.ZERO, null), "Codigo duplicado rechazado");
        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(balon), new BigDecimal("500"), MetodoPago.EFECTIVO), "Anticipo superior al total rechazado");
        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(), new BigDecimal("-1"), MetodoPago.EFECTIVO), "Anticipo negativo rechazado");
        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(), new BigDecimal("5"), null), "Metodo de pago obligatorio para anticipo");
        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(balon, balon), BigDecimal.ZERO, null), "Servicio duplicado rechazado");
        rechazar(() -> gestion.registrarReservaCompleta("R801", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(new ServicioAdicional("Z01", "Inventado", BigDecimal.ONE)),
            BigDecimal.ZERO, null), "Servicio fuera del catalogo rechazado");
        verificar(datos.getReservas().size() == antes + 1, "Errores no agregan reservas a memoria");

        Reserva sinPago = gestion.registrarReservaCompleta("R802", cliente, cancha, dia,
            LocalTime.of(12, 0), 60, List.of(), BigDecimal.ZERO, null);
        verificar(sinPago.getEstado() == EstadoReserva.CONFIRMADA, "Sin anticipo tambien puede confirmarse");
        verificar(sinPago.getPagos().isEmpty(), "Anticipo cero no crea pago ficticio");
        gestion.registrarPagoReserva(sinPago, new Pago("P001", new BigDecimal("50"), MetodoPago.TRANSFERENCIA, "Abono"));
        verificar(dinero(sinPago.getPagado(), "50"), "Abono posterior actualiza objeto original");
        verificar(dinero(csv.cargar().getReservas().stream().filter(x -> x.getCodigo().equals("R802")).findFirst().orElseThrow().getPagado(), "50"), "Abono posterior permanece en CSV");
        rechazar(() -> gestion.registrarPagoReserva(sinPago, new Pago("P002", new BigDecimal("999"), MetodoPago.EFECTIVO, "")), "No cobrar mas del saldo");
        verificar(sinPago.getPagos().size() == 1, "Pago rechazado no modifica la lista");
        rechazar(() -> gestion.agregarServicioReserva(sinPago, balon), "No agregar servicio despues de un pago");
        gestion.cambiarEstadoReserva(sinPago, EstadoReserva.EN_CURSO);
        verificar(sinPago.getEstado() == EstadoReserva.EN_CURSO, "CONFIRMADA pasa a EN_CURSO");
        gestion.cambiarEstadoReserva(sinPago, EstadoReserva.FINALIZADA);
        verificar(sinPago.getEstado() == EstadoReserva.FINALIZADA, "EN_CURSO pasa a FINALIZADA");
        rechazar(() -> gestion.cambiarEstadoReserva(sinPago, EstadoReserva.CONFIRMADA), "FINALIZADA no vuelve a CONFIRMADA");
        rechazar(() -> gestion.cancelarReserva(sinPago, "No", "Recepcion"), "No cancelar finalizada");

        Reserva pendiente = gestion.registrarReserva("R803", cliente, cancha, dia,
            LocalTime.of(20, 0), 60);
        verificar(pendiente.getEstado() == EstadoReserva.PENDIENTE, "Registro basico inicia PENDIENTE");
        rechazar(() -> gestion.cambiarEstadoReserva(pendiente, EstadoReserva.EN_CURSO), "No iniciar sin confirmar");
        gestion.cambiarEstadoReserva(pendiente, EstadoReserva.CONFIRMADA);
        verificar(pendiente.getEstado() == EstadoReserva.CONFIRMADA, "Confirmar reserva pendiente");
        gestion.cancelarReserva(pendiente, "Cliente aviso", "Recepcion");
        verificar(pendiente.getEstado() == EstadoReserva.CANCELADA, "Cancelacion conserva referencia del objeto");
        verificar(pendiente.getMotivoCancelacion().equals("Cliente aviso"), "Motivo conservado");
        verificar(csv.cargar().getReservas().stream().anyMatch(x -> x.getCodigo().equals("R803") && x.getEstado() == EstadoReserva.CANCELADA), "Cancelacion permanece al reiniciar");
        Reserva liberada = gestion.registrarReserva("R804", cliente, cancha, dia, LocalTime.of(20, 0), 60);
        verificar(liberada != null, "Horario cancelado vuelve a estar libre");

        // Una ruta que existe como ARCHIVO no admite crear los seis CSV dentro.
        Path bloqueo = Files.createTempFile("canchapro08-bloqueo", ".txt");
        GestionRegistros sinDisco = new GestionRegistros(datos, new ArchivosCSV(bloqueo));
        int cuenta = datos.getReservas().size();
        rechazar(() -> sinDisco.registrarReservaCompleta("R805", cliente, cancha, dia.plusDays(1),
                LocalTime.of(18, 0), 60, List.of(), BigDecimal.ZERO, null), "Falla de guardado al crear reserva controlada");
        verificar(datos.getReservas().size() == cuenta, "Falla de archivo no agrega reserva en memoria");
        int totalPagos = liberada.getPagos().size();
        rechazar(() -> sinDisco.registrarPagoReserva(liberada,
            new Pago("P001", new BigDecimal("1"), MetodoPago.EFECTIVO, "")), "Falla de guardado de pago controlada");
        verificar(liberada.getPagos().size() == totalPagos, "Falla de archivo no crea abono en memoria");
        rechazar(() -> sinDisco.cambiarEstadoReserva(liberada, EstadoReserva.CONFIRMADA), "Falla al guardar estado controlada");
        verificar(liberada.getEstado() == EstadoReserva.PENDIENTE, "Falla de archivo conserva estado original");
        System.out.println("TOTAL AVANCE 08: " + correctas + " pruebas correctas.");
    }
}
