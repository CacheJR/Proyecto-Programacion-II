package com.canchapro;

import com.canchapro.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Datos ficticios para observar las clases; todavia no se guardan en archivos. */
public final class DatosEjemplo {
    private DatosEjemplo() {}

    public static List<Cliente> crearClientes() {
        List<Cliente> clientes = new ArrayList<>();
        clientes.add(new Cliente("CL01", "Rosa Lopez", "55550101", "rosa@ejemplo.com"));
        clientes.add(new Cliente("CL02", "Luis Perez", "55550202", ""));
        return clientes;
    }

    public static List<Cancha> crearCanchas() {
        List<Cancha> canchas = new ArrayList<>();
        LocalTime abre = LocalTime.of(8, 0);
        LocalTime cierra = LocalTime.of(23, 0);
        canchas.add(new Cancha("C01", "La Bombonera", TipoCancha.FUTBOL_5, 10,
            new BigDecimal("150.00"), EstadoCancha.DISPONIBLE, abre, cierra));
        canchas.add(new Cancha("C02", "El Maracana", TipoCancha.FUTBOL_5, 10,
            new BigDecimal("150.00"), EstadoCancha.DISPONIBLE, abre, cierra));
        canchas.add(new Cancha("C03", "El Monumental", TipoCancha.FUTBOL_7, 14,
            new BigDecimal("200.00"), EstadoCancha.MANTENIMIENTO, abre, cierra));
        return canchas;
    }

    public static List<ServicioAdicional> crearServicios() {
        List<ServicioAdicional> servicios = new ArrayList<>();
        servicios.add(new ServicioAdicional("S01", "Balon", new BigDecimal("25.00")));
        servicios.add(new ServicioAdicional("S02", "Arbitro", new BigDecimal("80.00")));
        servicios.add(new ServicioAdicional("S03", "Chalecos", new BigDecimal("35.00")));
        servicios.add(new ServicioAdicional("S04", "Iluminacion", new BigDecimal("40.00")));
        return servicios;
    }

    public static List<Reserva> crearReservas(List<Cliente> clientes, List<Cancha> canchas) {
        List<Reserva> reservas = new ArrayList<>();
        LocalDate fecha = LocalDate.now().plusDays(1);
        Reserva reserva1 = new Reserva("R001", clientes.get(0), canchas.get(0),
            fecha, LocalTime.of(18, 0), 90);
        List<ServicioAdicional> servicios = crearServicios();
        reserva1.agregarServicio(servicios.get(0));
        reserva1.agregarServicio(servicios.get(1));
        reserva1.registrarPago(new Pago("P001", new BigDecimal("100.00"),
                MetodoPago.EFECTIVO, "Anticipo de ejemplo"));
        reserva1.confirmar();
        reservas.add(reserva1);
        Reserva reserva2 = new Reserva("R002", clientes.get(1), canchas.get(1),
            fecha, LocalTime.of(20, 0), 60);
        reserva2.agregarServicio(servicios.get(2));
        reservas.add(reserva2);
        return reservas;
    }
}
