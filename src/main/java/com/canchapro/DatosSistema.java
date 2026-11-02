package com.canchapro;

import com.canchapro.modelo.*;
import java.util.List;

/** Colecciones del programa; los objetos se relacionan mediante referencias. */
public class DatosSistema {
    private final List<Cliente> clientes;
    private final List<Cancha> canchas;
    private final List<ServicioAdicional> servicios;
    private final List<Reserva> reservas;

    public DatosSistema(List<Cliente> clientes, List<Cancha> canchas,
                       List<ServicioAdicional> servicios, List<Reserva> reservas) {
        this.clientes = clientes;
        this.canchas = canchas;
        this.servicios = servicios;
        this.reservas = reservas;
    }

    public static DatosSistema ejemplos() {
        List<Cliente> clientes = DatosEjemplo.crearClientes();
        List<Cancha> canchas = DatosEjemplo.crearCanchas();
        return new DatosSistema(clientes, canchas, DatosEjemplo.crearServicios(),
                DatosEjemplo.crearReservas(clientes, canchas));
    }

    public List<Cliente> getClientes() { return clientes; }
    public List<Cancha> getCanchas() { return canchas; }
    public List<ServicioAdicional> getServicios() { return servicios; }
    public List<Reserva> getReservas() { return reservas; }
}
