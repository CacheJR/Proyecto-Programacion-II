package com.canchapro;

import com.canchapro.modelo.*;
import com.canchapro.persistencia.ArchivosCSV;
import com.canchapro.servicio.ServicioDisponibilidad;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Se ejecuta con Run File. Crea solo archivos temporales. */
public class PruebasArchivosCSV {
    private static int correctas;
    private static void comprobar(boolean resultado, String mensaje) {
        if (!resultado) throw new AssertionError("FALLO: " + mensaje);
        correctas++;
        System.out.println("OK " + correctas + " - " + mensaje);
    }
    private static void errorLectura(ArchivosCSV archivos, String descripcion) {
        try { archivos.cargarOIniciar(); }
        catch (IOException esperado) { comprobar(true, descripcion); return; }
        throw new AssertionError("Se esperaba IOException: " + descripcion);
    }
    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory("prueba_canchapro_");
        ArchivosCSV archivos = new ArchivosCSV(temp);
        DatosSistema inicial = archivos.cargarOIniciar();
        comprobar(inicial.getClientes().size() == 2, "Primer uso crea dos clientes de muestra");
        comprobar(inicial.getCanchas().size() == 3, "Primer uso crea tres canchas");
        comprobar(inicial.getReservas().size() == 2, "Primer uso crea dos reservas");
        comprobar(Files.exists(temp.resolve("reserva_servicios.csv")), "Guarda relaciones de servicios");
        comprobar(Files.exists(temp.resolve("pagos.csv")), "Guarda pagos en archivo");
        DatosSistema leido = archivos.cargarOIniciar();
        comprobar(leido.getReservas().get(0).getCliente() == leido.getClientes().get(0), "Reconstruye referencia Cliente");
        comprobar(leido.getReservas().get(0).getCancha() == leido.getCanchas().get(0), "Reconstruye referencia Cancha");
        comprobar(leido.getReservas().get(0).getServicios().size() == 2, "Recupera servicios vinculados");
        comprobar(leido.getReservas().get(0).getPagos().size() == 1, "Recupera abonos");
        comprobar(leido.getReservas().get(0).getEstado() == EstadoReserva.CONFIRMADA, "Conserva estado confirmado");
        LocalDate fechaCliente = leido.getClientes().get(0).getFechaRegistro();
        LocalDateTime horaPago = leido.getReservas().get(0).getPagos().get(0).getFechaHora();
        leido.getReservas().get(1).agregarServicio(leido.getServicios().get(0));
        leido.getReservas().get(1).registrarPago(new Pago("PXX", new BigDecimal("100.00"),
                MetodoPago.TRANSFERENCIA, "Transferencia; por \"prueba\""));
        archivos.guardar(leido);
        DatosSistema vuelto = archivos.cargar();
        comprobar(vuelto.getReservas().get(1).getPagos().size() == 1, "Conserva abono nuevo tras guardar y cargar");
        comprobar(vuelto.getReservas().get(1).getServicios().size() == 2, "Conserva servicio nuevo");
        comprobar(vuelto.getReservas().get(1).getSaldo().compareTo(leido.getReservas().get(1).getSaldo()) == 0,
                "Conserva saldo despues de reinicio");
        comprobar(vuelto.getReservas().get(1).getPagos().get(0).getObservacion().equals("Transferencia; por \"prueba\""),
                "Soporta punto y coma y comillas en CSV");
        comprobar(vuelto.getClientes().get(0).getFechaRegistro().equals(fechaCliente), "Conserva fecha de registro del cliente");
        comprobar(vuelto.getReservas().get(0).getPagos().get(0).getFechaHora().equals(horaPago),
                "Conserva fecha y hora original del pago");
        vuelto.getReservas().get(1).cancelar("Cambio de horario", "Encargado");
        vuelto.getCanchas().get(1).cambiarEstado(EstadoCancha.MANTENIMIENTO);
        archivos.guardar(vuelto);
        DatosSistema restaurado = archivos.cargar();
        comprobar(restaurado.getReservas().get(1).getEstado() == EstadoReserva.CANCELADA,
                "No pierde cancelacion al reiniciar");
        comprobar("Cambio de horario".equals(restaurado.getReservas().get(1).getMotivoCancelacion()),
                "Conserva motivo de cancelacion");
        comprobar(restaurado.getReservas().get(1).getFechaCancelacion() != null,
                "Conserva fecha de cancelacion");
        comprobar(restaurado.getCanchas().get(1).getEstado() == EstadoCancha.MANTENIMIENTO,
                "Recupera cancha en mantenimiento e historial de reservas");
        ServicioDisponibilidad motor = new ServicioDisponibilidad(restaurado.getCanchas(), restaurado.getReservas());
        comprobar(!motor.estaDisponible(restaurado.getCanchas().get(1), LocalDate.now().plusDays(1),
                LocalTime.of(20, 0), 60), "Mantenimiento bloquea nuevas reservas");
        Path pagos = temp.resolve("pagos.csv");
        String antes = Files.readString(pagos, StandardCharsets.UTF_8);
        Files.writeString(pagos, "mal;cabecera\n", StandardCharsets.UTF_8);
        errorLectura(archivos, "Detecta encabezado de pagos corrupto");
        comprobar(Files.readString(pagos, StandardCharsets.UTF_8).equals("mal;cabecera\n"),
                "No sobreescribe archivo corrupto");
        Files.writeString(pagos, antes, StandardCharsets.UTF_8);
        Files.delete(temp.resolve("canchas.csv"));
        errorLectura(archivos, "Detecta cuando falta un archivo del conjunto");
        comprobar(Files.exists(temp.resolve("clientes.csv")), "No borra clientes ante archivos incompletos");
        System.out.println("TOTAL " + correctas + " pruebas exitosas. Datos de prueba en " + temp);
    }
}
