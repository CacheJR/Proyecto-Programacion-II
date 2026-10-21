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

    public static List<Reserva> crearReservas(List<Cliente> clientes, List<Cancha> canchas) {
        List<Reserva> reservas = new ArrayList<>();
        LocalDate fecha = LocalDate.now().plusDays(1);
        Reserva reserva1 = new Reserva("R001", clientes.get(0), canchas.get(0),
            fecha, LocalTime.of(18, 0), 90);
        reserva1.confirmar();
        reservas.add(reserva1);
        reservas.add(new Reserva("R002", clientes.get(1), canchas.get(1),
            fecha, LocalTime.of(20, 0), 60));
        return reservas;
    }
}
