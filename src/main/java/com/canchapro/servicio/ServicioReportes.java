package com.canchapro.servicio;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.Cliente;
import com.canchapro.modelo.EstadoReserva;
import com.canchapro.modelo.Pago;
import com.canchapro.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Agrupa, cuenta, suma y ordena reservas y pagos almacenados en memoria. */
public class ServicioReportes {
    private final DatosSistema datos;

    public ServicioReportes(DatosSistema datos) {
        this.datos = Objects.requireNonNull(datos);
    }

    public ResultadoReporte generar(TipoReporte tipo, LocalDate desde, LocalDate hasta,
                                    Cancha canchaFiltro, EstadoReserva estadoFiltro) {
        Objects.requireNonNull(tipo, "Seleccione un reporte");
        Objects.requireNonNull(desde, "Ingrese la fecha inicial");
        Objects.requireNonNull(hasta, "Ingrese la fecha final");
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede superar la final");
        }
        return switch (tipo) {
            case RESERVAS_FECHA -> reservasFecha(desde, hasta, canchaFiltro, estadoFiltro);
            case RESERVAS_CANCHA -> reservasCancha(desde, hasta, canchaFiltro, estadoFiltro);
            case INGRESOS_PERIODO -> ingresos(desde, hasta, canchaFiltro, estadoFiltro);
            case PAGOS_PENDIENTES -> pendientes(desde, hasta, canchaFiltro, estadoFiltro);
            case RESERVAS_CANCELADAS -> canceladas(desde, hasta, canchaFiltro, estadoFiltro);
            case CANCHA_MAS_UTILIZADA -> rankingCanchas(desde, hasta, canchaFiltro, estadoFiltro);
            case HORARIOS_DEMANDA -> horarios(desde, hasta, canchaFiltro, estadoFiltro);
            case CLIENTES_FRECUENTES -> clientes(desde, hasta, canchaFiltro, estadoFiltro);
        };
    }

    private boolean dentro(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private boolean filtros(Reserva r, Cancha canchaFiltro, EstadoReserva estadoFiltro) {
        return (canchaFiltro == null || r.getCancha().getCodigo().equals(canchaFiltro.getCodigo()))
            && (estadoFiltro == null || r.getEstado() == estadoFiltro);
    }

    private List<Reserva> reservasEntre(LocalDate desde, LocalDate hasta,
                                       Cancha cancha, EstadoReserva estado) {
        List<Reserva> encontrados = new ArrayList<>();
        for (Reserva r : datos.getReservas()) {
            if (dentro(r.getFecha(), desde, hasta) && filtros(r, cancha, estado)) encontrados.add(r);
        }
        return encontrados;
    }

    private boolean activa(Reserva r) {
        return r.getEstado() != EstadoReserva.CANCELADA;
    }

    private BigDecimal dinero(BigDecimal valor) { return valor.setScale(2); }

    private static class Conteo {
        int reservas;
        int canceladas;
        int minutos;
        BigDecimal total = BigDecimal.ZERO;
    }

    private ResultadoReporte reservasFecha(LocalDate desde, LocalDate hasta,
                                           Cancha cancha, EstadoReserva estado) {
        Map<LocalDate, Conteo> grupos = new TreeMap<>();
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            Conteo c = grupos.computeIfAbsent(r.getFecha(), x -> new Conteo());
            c.reservas++;
            if (activa(r)) c.total = c.total.add(r.getTotal());
            else c.canceladas++;
        }
        List<Object[]> filas = new ArrayList<>();
        int totalReservas = 0;
        for (var e : grupos.entrySet()) {
            Conteo c = e.getValue();
            filas.add(new Object[]{e.getKey(), c.reservas, c.canceladas, dinero(c.total)});
            totalReservas += c.reservas;
        }
        return new ResultadoReporte(new String[]{"Fecha de reserva", "Reservas", "Canceladas", "Total previsto Q"},
                filas, "Reservas encontradas: " + totalReservas,
                "El total previsto excluye reservas canceladas; no representa dinero cobrado.");
    }

    private ResultadoReporte reservasCancha(LocalDate desde, LocalDate hasta,
                                            Cancha cancha, EstadoReserva estado) {
        Map<String, Conteo> grupos = new TreeMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            String codigo = r.getCancha().getCodigo();
            nombres.put(codigo, r.getCancha().getNombre());
            Conteo c = grupos.computeIfAbsent(codigo, x -> new Conteo());
            c.reservas++;
            if (activa(r)) c.minutos += r.getDuracionMinutos();
            else c.canceladas++;
        }
        List<Object[]> filas = new ArrayList<>();
        for (var e : grupos.entrySet()) {
            Conteo c = e.getValue();
            filas.add(new Object[]{e.getKey(), nombres.get(e.getKey()), c.reservas, c.canceladas, c.minutos});
        }
        return new ResultadoReporte(new String[]{"Codigo", "Cancha", "Reservas", "Canceladas", "Minutos ocupados"},
                filas, "Canchas con reservas: " + grupos.size(),
                "Solo se cuentan minutos de reservas no canceladas.");
    }

    private ResultadoReporte ingresos(LocalDate desde, LocalDate hasta,
                                      Cancha cancha, EstadoReserva estado) {
        Map<LocalDate, Conteo> grupos = new TreeMap<>();
        for (Reserva r : datos.getReservas()) {
            if (!filtros(r, cancha, estado)) continue;
            for (Pago p : r.getPagos()) {
                LocalDate fechaPago = p.getFechaHora().toLocalDate();
                if (dentro(fechaPago, desde, hasta)) {
                    Conteo c = grupos.computeIfAbsent(fechaPago, x -> new Conteo());
                    c.reservas++;
                    c.total = c.total.add(p.getMonto());
                }
            }
        }
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int abonos = 0;
        for (var e : grupos.entrySet()) {
            Conteo c = e.getValue();
            filas.add(new Object[]{e.getKey(), c.reservas, dinero(c.total)});
            abonos += c.reservas;
            total = total.add(c.total);
        }
        return new ResultadoReporte(new String[]{"Fecha de pago", "Abonos", "Ingresos registrados Q"},
                filas, "Ingresos: Q" + dinero(total) + " | Abonos: " + abonos,
                "Suma pagos por su fecha de cobro, incluso de reservas luego canceladas. No hay devoluciones automatizadas.");
    }

    private ResultadoReporte pendientes(LocalDate desde, LocalDate hasta,
                                        Cancha cancha, EstadoReserva estado) {
        List<Object[]> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            if (activa(r) && r.getSaldo().signum() > 0) {
                filas.add(new Object[]{r.getCodigo(), r.getCliente().getNombre(), r.getCancha().getNombre(),
                        r.getFecha(), dinero(r.getTotal()), dinero(r.getPagado()), dinero(r.getSaldo())});
                total = total.add(r.getSaldo());
            }
        }
        filas.sort(Comparator.comparing(x -> (LocalDate) x[3]));
        return new ResultadoReporte(new String[]{"Reserva", "Cliente", "Cancha", "Fecha", "Total Q", "Pagado Q", "Saldo Q"},
                filas, "Reservas pendientes: " + filas.size() + " | Saldo total: Q" + dinero(total),
                "Filtrado por fecha de la reserva. Se excluyen las canceladas.");
    }

    private ResultadoReporte canceladas(LocalDate desde, LocalDate hasta,
                                        Cancha cancha, EstadoReserva estado) {
        List<Object[]> filas = new ArrayList<>();
        for (Reserva r : datos.getReservas()) {
            if (r.getEstado() != EstadoReserva.CANCELADA || r.getFechaCancelacion() == null) continue;
            if (!filtros(r, cancha, estado)) continue;
            if (!dentro(r.getFechaCancelacion().toLocalDate(), desde, hasta)) continue;
            filas.add(new Object[]{r.getCodigo(), r.getFecha(), r.getFechaCancelacion(),
                    r.getCancha().getNombre(), r.getMotivoCancelacion(), r.getOperadorCancelacion()});
        }
        filas.sort(Comparator.comparing(x -> x[2].toString()));
        return new ResultadoReporte(new String[]{"Reserva", "Fecha juego", "Fecha cancelacion", "Cancha", "Motivo", "Responsable"},
                filas, "Cancelaciones en el periodo: " + filas.size(),
                "Se filtra por la fecha en que se cancelo, no por la fecha del partido.");
    }

    private ResultadoReporte rankingCanchas(LocalDate desde, LocalDate hasta,
                                            Cancha cancha, EstadoReserva estado) {
        Map<String, Conteo> grupos = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            if (!activa(r)) continue;
            String codigo = r.getCancha().getCodigo();
            Conteo c = grupos.computeIfAbsent(codigo, x -> new Conteo());
            nombres.put(codigo, r.getCancha().getNombre());
            c.reservas++;
            c.minutos += r.getDuracionMinutos();
        }
        List<String> orden = new ArrayList<>(grupos.keySet());
        orden.sort(Comparator.<String>comparingInt(x -> grupos.get(x).reservas).reversed()
                 .thenComparing(Comparator.<String>comparingInt(x -> grupos.get(x).minutos).reversed())
                 .thenComparing(x -> x));
        List<Object[]> filas = new ArrayList<>();
        int puesto = 1;
        for (String codigo : orden) {
            Conteo c = grupos.get(codigo);
            filas.add(new Object[]{puesto++, codigo, nombres.get(codigo), c.reservas, c.minutos});
        }
        return new ResultadoReporte(new String[]{"Puesto", "Codigo", "Cancha", "Reservas", "Minutos"},
                filas, orden.isEmpty() ? "No hay canchas con reservas activas en el periodo" :
                    "Mas utilizada: " + nombres.get(orden.get(0)) + " (" + grupos.get(orden.get(0)).reservas + " reservas)",
                "Ranking por cantidad de reservas; en empate, por minutos de uso. No incluye canceladas.");
    }

    private ResultadoReporte horarios(LocalDate desde, LocalDate hasta,
                                      Cancha cancha, EstadoReserva estado) {
        Map<Integer, Integer> grupos = new TreeMap<>();
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            if (!activa(r)) continue;
            int hora = r.getHoraInicio().getHour();
            grupos.merge(hora, 1, Integer::sum);
        }
        List<Map.Entry<Integer, Integer>> orden = new ArrayList<>(grupos.entrySet());
        orden.sort(Comparator.<Map.Entry<Integer, Integer>>comparingInt(x -> x.getValue())
                 .reversed().thenComparingInt(Map.Entry::getKey));
        List<Object[]> filas = new ArrayList<>();
        for (var e : orden) {
            filas.add(new Object[]{String.format("%02d:00 - %02d:59", e.getKey(), e.getKey()), e.getValue()});
        }
        return new ResultadoReporte(new String[]{"Hora de inicio", "Reservas"}, filas,
                orden.isEmpty() ? "Sin horarios de demanda en el periodo" :
                        "Horario con mas reservas: " + String.format("%02d:00", orden.get(0).getKey()),
                "Agrupa por la hora en que comienza cada reserva (no por cada hora ocupada). Excluye canceladas.");
    }

    private ResultadoReporte clientes(LocalDate desde, LocalDate hasta,
                                      Cancha cancha, EstadoReserva estado) {
        Map<String, Integer> cuentas = new TreeMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (Reserva r : reservasEntre(desde, hasta, cancha, estado)) {
            if (!activa(r)) continue;
            Cliente c = r.getCliente();
            nombres.put(c.getCodigo(), c.getNombre());
            cuentas.merge(c.getCodigo(), 1, Integer::sum);
        }
        List<String> orden = new ArrayList<>(cuentas.keySet());
        orden.sort(Comparator.<String>comparingInt(cuentas::get).reversed().thenComparing(x -> x));
        List<Object[]> filas = new ArrayList<>();
        int puesto = 1;
        for (String codigo : orden) {
            filas.add(new Object[]{puesto++, codigo, nombres.get(codigo), cuentas.get(codigo)});
        }
        return new ResultadoReporte(new String[]{"Puesto", "Codigo cliente", "Cliente", "Reservas"}, filas,
                "Clientes con reservas: " + filas.size(),
                "Cuenta las reservas no canceladas de cada cliente.");
    }
}
