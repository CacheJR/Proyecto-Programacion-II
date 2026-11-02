package com.canchapro.persistencia;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** Guarda y lee los objetos utilizando CSV con ; y UTF-8, sin base de datos. */
public class ArchivosCSV {
    private static final String[] NOMBRES = {
        "clientes.csv", "canchas.csv", "servicios.csv", "reservas.csv",
        "reserva_servicios.csv", "pagos.csv"
    };
    private final Path carpeta;

    public ArchivosCSV(Path carpeta) {
        this.carpeta = Objects.requireNonNull(carpeta);
    }

    public Path getCarpeta() { return carpeta.toAbsolutePath().normalize(); }

    /** Primer inicio: crea archivos de ejemplo. Inicios siguientes: lee todo. */
    public DatosSistema cargarOIniciar() throws IOException {
        int encontrados = 0;
        for (String nombre : NOMBRES) if (Files.exists(carpeta.resolve(nombre))) encontrados++;
        if (encontrados == 0) {
            // Si la carpeta tiene otros archivos, no asumir que es una instalacion nueva.
            if (Files.exists(carpeta)) {
                try (var lista = Files.list(carpeta)) {
                    if (lista.findAny().isPresent()) {
                        throw new IOException("La carpeta de datos ya existe, pero faltan los CSV");
                    }
                }
            }
            DatosSistema ejemplos = DatosSistema.ejemplos();
            guardar(ejemplos);
            return ejemplos;
        }
        if (encontrados != NOMBRES.length) {
            throw new IOException("Falta uno o varios archivos CSV en " + getCarpeta());
        }
        return cargar();
    }

    /** Reconstruye Cliente, Cancha, Reserva, sus servicios y pagos. */
    public DatosSistema cargar() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        List<Cancha> canchas = new ArrayList<>();
        List<ServicioAdicional> servicios = new ArrayList<>();
        List<Reserva> reservas = new ArrayList<>();
        Map<String, Cliente> mapaClientes = new HashMap<>();
        Map<String, Cancha> mapaCanchas = new HashMap<>();
        Map<String, ServicioAdicional> mapaServicios = new HashMap<>();
        Map<String, List<ServicioAdicional>> serviciosPorReserva = new HashMap<>();
        Map<String, List<Pago>> pagosPorReserva = new HashMap<>();

        for (String[] f : leer("clientes.csv", "codigo", "nombre", "telefono", "correo", "fechaRegistro")) {
            try {
                Cliente c = new Cliente(f[0], f[1], f[2], f[3], LocalDate.parse(f[4]));
                agregarUnico(mapaClientes, c.getCodigo(), c, "cliente");
                clientes.add(c);
            } catch (RuntimeException e) { throw errorFila("clientes.csv", f, e); }
        }
        for (String[] f : leer("canchas.csv", "codigo", "nombre", "tipo", "capacidad", "precioBase", "estado", "apertura", "cierre")) {
            try {
                Cancha c = new Cancha(f[0], f[1], TipoCancha.valueOf(f[2]), Integer.parseInt(f[3]),
                    new BigDecimal(f[4]), EstadoCancha.valueOf(f[5]), LocalTime.parse(f[6]), LocalTime.parse(f[7]));
                agregarUnico(mapaCanchas, c.getCodigo(), c, "cancha");
                canchas.add(c);
            } catch (RuntimeException e) { throw errorFila("canchas.csv", f, e); }
        }
        for (String[] f : leer("servicios.csv", "codigo", "nombre", "precio")) {
            try {
                ServicioAdicional s = new ServicioAdicional(f[0], f[1], new BigDecimal(f[2]));
                agregarUnico(mapaServicios, s.getCodigo(), s, "servicio");
                servicios.add(s);
            } catch (RuntimeException e) { throw errorFila("servicios.csv", f, e); }
        }
        List<String[]> lineasReservas = leer("reservas.csv", "codigo", "cliente", "cancha", "fecha",
                "inicio", "minutos", "estado", "fechaCancelacion", "motivo", "operador");
        Set<String> codigosReservas = new HashSet<>();
        for (String[] f : lineasReservas) {
            if (!codigosReservas.add(f[0].toUpperCase(Locale.ROOT)))
                throw new IOException("Reserva repetida: " + f[0]);
            if (!mapaClientes.containsKey(f[1]) || !mapaCanchas.containsKey(f[2]))
                throw new IOException("Reserva " + f[0] + " tiene cliente o cancha inexistente");
        }
        for (String[] f : leer("reserva_servicios.csv", "reserva", "servicio")) {
            if (!codigosReservas.contains(f[0].toUpperCase(Locale.ROOT)) || !mapaServicios.containsKey(f[1]))
                throw new IOException("Relacion reserva-servicio inexistente: " + Arrays.toString(f));
            serviciosPorReserva.computeIfAbsent(f[0], k -> new ArrayList<>()).add(mapaServicios.get(f[1]));
        }
        for (String[] f : leer("pagos.csv", "reserva", "codigo", "fechaHora", "monto", "metodo", "observacion")) {
            if (!codigosReservas.contains(f[0].toUpperCase(Locale.ROOT)))
                throw new IOException("Pago de reserva inexistente: " + f[0]);
            try {
                Pago pago = new Pago(f[1], new BigDecimal(f[3]), MetodoPago.valueOf(f[4]), f[5], LocalDateTime.parse(f[2]));
                pagosPorReserva.computeIfAbsent(f[0], k -> new ArrayList<>()).add(pago);
            } catch (RuntimeException e) { throw errorFila("pagos.csv", f, e); }
        }
        for (String[] f : lineasReservas) {
            try {
                Reserva r = Reserva.recuperar(f[0], mapaClientes.get(f[1]), mapaCanchas.get(f[2]),
                    LocalDate.parse(f[3]), LocalTime.parse(f[4]), Integer.parseInt(f[5]),
                    EstadoReserva.valueOf(f[6]), f[7].isEmpty() ? null : LocalDateTime.parse(f[7]),
                    f[8], f[9], serviciosPorReserva.getOrDefault(f[0], List.of()),
                    pagosPorReserva.getOrDefault(f[0], List.of()));
                reservas.add(r);
            } catch (RuntimeException e) { throw errorFila("reservas.csv", f, e); }
        }
        return new DatosSistema(clientes, canchas, servicios, reservas);
    }

    /** Escribe primero archivos temporales y luego reemplaza los originales. */
    public void guardar(DatosSistema datos) throws IOException {
        Files.createDirectories(carpeta);
        Map<String, List<String>> salida = new LinkedHashMap<>();
        salida.put("clientes.csv", new ArrayList<>(List.of(fila("codigo", "nombre", "telefono", "correo", "fechaRegistro"))));
        salida.put("canchas.csv", new ArrayList<>(List.of(fila("codigo", "nombre", "tipo", "capacidad", "precioBase", "estado", "apertura", "cierre"))));
        salida.put("servicios.csv", new ArrayList<>(List.of(fila("codigo", "nombre", "precio"))));
        salida.put("reservas.csv", new ArrayList<>(List.of(fila("codigo", "cliente", "cancha", "fecha", "inicio", "minutos", "estado", "fechaCancelacion", "motivo", "operador"))));
        salida.put("reserva_servicios.csv", new ArrayList<>(List.of(fila("reserva", "servicio"))));
        salida.put("pagos.csv", new ArrayList<>(List.of(fila("reserva", "codigo", "fechaHora", "monto", "metodo", "observacion"))));
        for (Cliente c : datos.getClientes()) {
            salida.get("clientes.csv").add(fila(c.getCodigo(), c.getNombre(), c.getTelefono(), c.getCorreo(), c.getFechaRegistro()));
        }
        for (Cancha c : datos.getCanchas()) {
            salida.get("canchas.csv").add(fila(c.getCodigo(), c.getNombre(), c.getTipo().name(),
                    c.getCapacidad(), c.getPrecioBase(), c.getEstado().name(), c.getHoraApertura(), c.getHoraCierre()));
        }
        for (ServicioAdicional s : datos.getServicios()) {
            salida.get("servicios.csv").add(fila(s.getCodigo(), s.getNombre(), s.getPrecio()));
        }
        for (Reserva r : datos.getReservas()) {
            salida.get("reservas.csv").add(fila(r.getCodigo(), r.getCliente().getCodigo(), r.getCancha().getCodigo(),
                    r.getFecha(), r.getHoraInicio(), r.getDuracionMinutos(), r.getEstado().name(),
                    r.getFechaCancelacion(), r.getMotivoCancelacion(), r.getOperadorCancelacion()));
            for (ServicioAdicional s : r.getServicios()) {
                salida.get("reserva_servicios.csv").add(fila(r.getCodigo(), s.getCodigo()));
            }
            for (Pago p : r.getPagos()) {
                salida.get("pagos.csv").add(fila(r.getCodigo(), p.getCodigo(), p.getFechaHora(),
                        p.getMonto(), p.getMetodo().name(), p.getObservacion()));
            }
        }
        Map<Path, Path> temporales = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, List<String>> entrada : salida.entrySet()) {
                Path destino = carpeta.resolve(entrada.getKey());
                Path tmp = Files.createTempFile(carpeta, "canchapro_", ".tmp");
                temporales.put(destino, tmp);
                Files.write(tmp, entrada.getValue(), StandardCharsets.UTF_8);
            }
            for (Map.Entry<Path, Path> e : temporales.entrySet()) {
                try {
                    Files.move(e.getValue(), e.getKey(), StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(e.getValue(), e.getKey(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } finally {
            for (Path tmp : temporales.values()) Files.deleteIfExists(tmp);
        }
    }

    private List<String[]> leer(String archivo, String... encabezados) throws IOException {
        Path ruta = carpeta.resolve(archivo);
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        if (lineas.isEmpty()) throw new IOException("Archivo vacio: " + archivo);
        if (!Arrays.equals(separar(lineas.get(0)), encabezados)) {
            throw new IOException("Encabezado incorrecto: " + archivo);
        }
        List<String[]> resultado = new ArrayList<>();
        for (int i = 1; i < lineas.size(); i++) {
            if (lineas.get(i).isEmpty()) continue;
            try {
                String[] campos = separar(lineas.get(i));
                if (campos.length != encabezados.length) throw new IllegalArgumentException("Cantidad incorrecta de columnas");
                resultado.add(campos);
            } catch (RuntimeException e) {
                throw new IOException("Error en " + archivo + " linea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return resultado;
    }

    private static String fila(Object... datos) {
        List<String> partes = new ArrayList<>();
        for (Object dato : datos) {
            String valor = dato == null ? "" : dato.toString();
            if (valor.contains("\n") || valor.contains("\r")) {
                throw new IllegalArgumentException("No se admiten saltos de linea en los CSV");
            }
            if (valor.contains(";") || valor.contains("\""))
                valor = "\"" + valor.replace("\"", "\"\"") + "\"";
            partes.add(valor);
        }
        return String.join(";", partes);
    }

    private static String[] separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        boolean cerrado = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"'); i++;
                } else if (!entreComillas && (actual.length() != 0 || cerrado)) {
                    throw new IllegalArgumentException("Comillas mal colocadas");
                } else {
                    entreComillas = !entreComillas;
                    if (!entreComillas) cerrado = true;
                }
            } else if (c == ';' && !entreComillas) {
                campos.add(actual.toString()); actual.setLength(0); cerrado = false;
            } else {
                if (cerrado && !entreComillas) throw new IllegalArgumentException("Caracter tras las comillas");
                actual.append(c);
            }
        }
        if (entreComillas) throw new IllegalArgumentException("Comillas sin cerrar");
        campos.add(actual.toString());
        return campos.toArray(String[]::new);
    }

    private static IOException errorFila(String nombre, String[] f, RuntimeException ex) {
        return new IOException("Dato incorrecto en " + nombre + " (codigo " + f[0] + "): " + ex.getMessage(), ex);
    }

    private static <T> void agregarUnico(Map<String, T> mapa, String codigo, T objeto, String tipo) {
        if (mapa.putIfAbsent(codigo, objeto) != null)
            throw new IllegalArgumentException("Codigo duplicado de " + tipo + ": " + codigo);
    }
}
