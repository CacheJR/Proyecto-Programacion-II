package com.canchapro.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Columnas, registros y texto explicativo del reporte calculado. */
public class ResultadoReporte {
    private final String[] columnas;
    private final List<Object[]> filas;
    private final String resumen;
    private final String nota;

    public ResultadoReporte(String[] columnas, List<Object[]> filas, String resumen, String nota) {
        this.columnas = columnas.clone();
        this.filas = new ArrayList<>();
        for (Object[] fila : filas) this.filas.add(fila.clone());
        this.resumen = resumen;
        this.nota = nota;
    }

    public String[] getColumnas() { return columnas.clone(); }
    public List<Object[]> getFilas() {
        List<Object[]> copia = new ArrayList<>();
        for (Object[] fila : filas) copia.add(fila.clone());
        return Collections.unmodifiableList(copia);
    }
    public String getResumen() { return resumen; }
    public String getNota() { return nota; }
}
