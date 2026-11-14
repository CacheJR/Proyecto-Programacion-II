package com.canchapro.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** Usa tarifas configurables; cada reserva guarda una copia de su costo original. */
public final class CalculadoraTarifas {
    private static ConfiguracionTarifas configuracion = ConfiguracionTarifas.valoresIniciales();
    private CalculadoraTarifas() { }

    public static void aplicarConfiguracion(ConfiguracionTarifas nueva) {
        configuracion = Objects.requireNonNull(nueva);
    }
    public static ConfiguracionTarifas getConfiguracion() { return configuracion; }

    public static BigDecimal tarifaPorHora(LocalDate fecha, LocalTime inicio) {
        return tarifaPorHora(fecha, inicio, configuracion);
    }
    public static BigDecimal tarifaPorHora(LocalDate fecha, LocalTime inicio, ConfiguracionTarifas tarifas) {
        Objects.requireNonNull(fecha, "Fecha obligatoria");
        Objects.requireNonNull(inicio, "Hora obligatoria");
        Objects.requireNonNull(tarifas, "Tarifas obligatorias");
        DayOfWeek dia = fecha.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) return tarifas.getFinSemana();
        return inicio.isBefore(LocalTime.of(17, 0)) ? tarifas.getDiurna() : tarifas.getNocturna();
    }
    public static BigDecimal costoCancha(LocalDate fecha, LocalTime inicio, int minutos) {
        return costoCancha(fecha, inicio, minutos, configuracion);
    }
    public static BigDecimal costoCancha(LocalDate fecha, LocalTime inicio, int minutos,
                                         ConfiguracionTarifas tarifas) {
        if (minutos <= 0) throw new IllegalArgumentException("Duracion incorrecta");
        return tarifaPorHora(fecha, inicio, tarifas)
                .multiply(BigDecimal.valueOf(minutos))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
    public static BigDecimal costoHistoricoInicial(LocalDate fecha, LocalTime inicio, int minutos) {
        return costoCancha(fecha, inicio, minutos, ConfiguracionTarifas.valoresIniciales());
    }
}
