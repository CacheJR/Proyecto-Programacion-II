package com.canchapro.servicio;

/** Opciones de reporteria solicitadas en el proyecto. */
public enum TipoReporte {
    RESERVAS_FECHA("Reservas por fecha"),
    RESERVAS_CANCHA("Reservas por cancha"),
    INGRESOS_PERIODO("Ingresos por periodo"),
    PAGOS_PENDIENTES("Pagos pendientes"),
    RESERVAS_CANCELADAS("Reservas canceladas"),
    CANCHA_MAS_UTILIZADA("Cancha mas utilizada"),
    HORARIOS_DEMANDA("Horarios de mayor demanda"),
    CLIENTES_FRECUENTES("Clientes con mas reservas");

    private final String titulo;
    TipoReporte(String titulo) { this.titulo = titulo; }
    @Override public String toString() { return titulo; }
}
