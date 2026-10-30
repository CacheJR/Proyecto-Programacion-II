package com.canchapro.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Tarifas de referencia del enunciado, con la regla del nivel medio. */
public final class CalculadoraTarifas {
    private static final BigDecimal DIA = new BigDecimal("150.00");
    private static final BigDecimal NOCHE = new BigDecimal("225.00");
    private static final BigDecimal FIN_SEMANA = new BigDecimal("250.00");

    private CalculadoraTarifas() { }

    public static BigDecimal tarifaPorHora(LocalDate fecha, LocalTime inicio) {
        Objects.requireNonNull(fecha, "Fecha obligatoria");
        Objects.requireNonNull(inicio, "Hora obligatoria");
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) return FIN_SEMANA;
        return inicio.isBefore(LocalTime.of(17, 0)) ? DIA : NOCHE;
    }

    public static BigDecimal costoCancha(LocalDate fecha, LocalTime inicio, int minutos) {
        if (minutos <= 0) throw new IllegalArgumentException("Duracion incorrecta");
        return tarifaPorHora(fecha, inicio)
                .multiply(BigDecimal.valueOf(minutos))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}
