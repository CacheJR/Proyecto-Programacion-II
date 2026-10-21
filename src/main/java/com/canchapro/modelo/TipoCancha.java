package com.canchapro.modelo;

public enum TipoCancha {
    FUTBOL_5("Futbol 5"),
    FUTBOL_7("Futbol 7");

    private final String nombre;

    TipoCancha(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
