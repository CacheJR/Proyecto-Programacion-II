package com.canchapro.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Tarifas configurables; el precio se fija al crear cada reserva. */
public class ConfiguracionTarifas {
    private final BigDecimal diurna;
    private final BigDecimal nocturna;
    private final BigDecimal finSemana;

    public ConfiguracionTarifas(BigDecimal diurna, BigDecimal nocturna, BigDecimal finSemana) {
        this.diurna = validar(diurna);
        this.nocturna = validar(nocturna);
        this.finSemana = validar(finSemana);
    }
    private static BigDecimal validar(BigDecimal valor) {
        Objects.requireNonNull(valor, "Tarifa obligatoria");
        if (valor.signum() < 0 || valor.scale() > 2)
            throw new IllegalArgumentException("Las tarifas deben ser positivas o cero y tener maximo 2 decimales");
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
    public static ConfiguracionTarifas valoresIniciales() {
        return new ConfiguracionTarifas(new BigDecimal("150.00"),
                new BigDecimal("225.00"), new BigDecimal("250.00"));
    }
    public BigDecimal getDiurna() { return diurna; }
    public BigDecimal getNocturna() { return nocturna; }
    public BigDecimal getFinSemana() { return finSemana; }
}
