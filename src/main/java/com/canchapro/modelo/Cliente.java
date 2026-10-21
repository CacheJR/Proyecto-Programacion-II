package com.canchapro.modelo;

import java.time.LocalDate;

public class Cliente {
    private final String codigo;
    private String nombre;
    private String telefono;
    private String correo;
    private final LocalDate fechaRegistro;

    public Cliente(String codigo, String nombre, String telefono, String correo) {
        this.codigo = validarTexto(codigo, "Codigo");
        this.nombre = validarTexto(nombre, "Nombre");
        this.fechaRegistro = LocalDate.now();
        actualizarContacto(telefono, correo);
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        return texto.trim();
    }

    public void actualizarContacto(String telefono, String correo) {
        String nuevoTelefono = validarTexto(telefono, "Telefono");
        String nuevoCorreo = correo == null ? "" : correo.trim();
        if (!nuevoCorreo.isEmpty() && !nuevoCorreo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Correo no valido");
        }
        this.telefono = nuevoTelefono;
        this.correo = nuevoCorreo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
}
