package com.canchapro.servicio;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Indicadores obtenidos de las reservas y pagos reales. */
public class ServicioDashboard {
    private final DatosSistema datos;

    public ServicioDashboard(DatosSistema datos) { this.datos = Objects.requireNonNull(datos); }

    public Resumen calcular(LocalDate hoy) {
        Objects.requireNonNull(hoy);
        int delDia = 0;
        int enCurso = 0;
        int pendientes = 0;
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal saldos = BigDecimal.ZERO;
        for (Reserva r : datos.getReservas()) {
            if (r.getEstado() == EstadoReserva.CANCELADA) {
                // Los pagos de canceladas siguen siendo ingresos si no hubo devolucion.
                for (Pago p : r.getPagos())
                    if (p.getFechaHora().toLocalDate().equals(hoy)) ingresos = ingresos.add(p.getMonto());
                continue;
            }
            if (r.getFecha().equals(hoy)) {
                delDia++;
                if (r.getEstado() == EstadoReserva.EN_CURSO) enCurso++;
                if (r.getEstado() == EstadoReserva.PENDIENTE) pendientes++;
            }
            saldos = saldos.add(r.getSaldo());
            for (Pago p : r.getPagos()) {
                if (p.getFechaHora().toLocalDate().equals(hoy)) ingresos = ingresos.add(p.getMonto());
            }
        }
        return new Resumen(delDia, enCurso, pendientes, ingresos, saldos);
    }

    public record Resumen(int reservasHoy, int enCurso, int pendientesHoy,
                          BigDecimal ingresosHoy, BigDecimal saldosPendientes) { }
}
