package com.canchapro.ui;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.*;
import com.canchapro.servicio.CalculadoraTarifas;
import com.canchapro.servicio.GestionRegistros;
import com.canchapro.servicio.ServicioDisponibilidad;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;

/** Una sola ventana para buscar cancha, elegir servicios, cobrar y confirmar. */
public class FormularioNuevaReserva {
    private final JFrame ventana;
    private final DatosSistema datos;
    private final GestionRegistros gestion;
    private final ServicioDisponibilidad disponibilidad;

    public FormularioNuevaReserva(JFrame ventana, DatosSistema datos, GestionRegistros gestion) {
        this.ventana = ventana;
        this.datos = datos;
        this.gestion = gestion;
        this.disponibilidad = new ServicioDisponibilidad(datos.getCanchas(), datos.getReservas());
    }

    public boolean mostrar() {
        if (datos.getClientes().isEmpty() || datos.getCanchas().isEmpty()) {
            JOptionPane.showMessageDialog(ventana, "Primero registre clientes y canchas.");
            return false;
        }
        JTextField codigo = new JTextField();
        JComboBox<String> clientes = new JComboBox<>();
        for (Cliente c : datos.getClientes()) clientes.addItem(c.getCodigo() + " - " + c.getNombre());
        JTextField fecha = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField inicio = new JTextField("18:00");
        JTextField minutos = new JTextField("60");
        JComboBox<TipoCancha> tipo = new JComboBox<>(TipoCancha.values());
        JComboBox<Cancha> canchas = new JComboBox<>();
        canchas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean foco) {
                String texto = valor instanceof Cancha c ? c.getCodigo() + " - " + c.getNombre() : "Seleccione cancha";
                return super.getListCellRendererComponent(lista, texto, indice, seleccionado, foco);
            }
        });
        JButton consultar = new JButton("Buscar canchas libres");
        JList<ServicioAdicional> servicios = new JList<>(datos.getServicios().toArray(new ServicioAdicional[0]));
        servicios.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane listaServicios = new JScrollPane(servicios);
        listaServicios.setPreferredSize(new Dimension(250, 68));
        JTextField anticipo = new JTextField("0.00");
        JComboBox<MetodoPago> metodo = new JComboBox<>(MetodoPago.values());
        JButton calcular = new JButton("Calcular total");
        JLabel total = new JLabel("Total: Q0.00 | Anticipo opcional");

        JPanel formulario = new JPanel(new GridLayout(0, 2, 9, 9));
        formulario.add(new JLabel("Codigo de reserva:")); formulario.add(codigo);
        formulario.add(new JLabel("Cliente:")); formulario.add(clientes);
        formulario.add(new JLabel("Fecha AAAA-MM-DD:")); formulario.add(fecha);
        formulario.add(new JLabel("Hora de inicio HH:mm:")); formulario.add(inicio);
        formulario.add(new JLabel("Duracion (minutos):")); formulario.add(minutos);
        formulario.add(new JLabel("Tipo de cancha:")); formulario.add(tipo);
        formulario.add(consultar); formulario.add(canchas);
        formulario.add(new JLabel("Servicios (Ctrl para varios):")); formulario.add(listaServicios);
        formulario.add(calcular); formulario.add(total);
        formulario.add(new JLabel("Primer abono Q:")); formulario.add(anticipo);
        formulario.add(new JLabel("Metodo de pago:")); formulario.add(metodo);

        consultar.addActionListener(evento -> {
            try {
                canchas.removeAllItems();
                List<Cancha> libres = disponibilidad.buscarDisponibles(
                        (TipoCancha) tipo.getSelectedItem(), LocalDate.parse(fecha.getText().trim()),
                        LocalTime.parse(inicio.getText().trim()), Integer.parseInt(minutos.getText().trim()));
                for (Cancha cancha : libres) canchas.addItem(cancha);
                if (libres.isEmpty()) JOptionPane.showMessageDialog(ventana, "No hay canchas libres para esta solicitud.");
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(ventana, error.getMessage(), "Revise los datos", JOptionPane.ERROR_MESSAGE);
            }
        });
        calcular.addActionListener(evento -> {
            try {
                BigDecimal precio = calcularTotal(fecha.getText(), inicio.getText(), minutos.getText(), servicios.getSelectedValuesList());
                total.setText("Total: Q" + precio);
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(ventana, error.getMessage(), "Calculo no valido", JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane desplazamiento = new JScrollPane(formulario);
        desplazamiento.setPreferredSize(new Dimension(580, 460));
        while (true) {
            int opcion = JOptionPane.showConfirmDialog(ventana, desplazamiento,
                    "Nueva reserva completa", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) return false;
            try {
                Cancha elegida = (Cancha) canchas.getSelectedItem();
                if (elegida == null) throw new IllegalArgumentException("Primero consulte y seleccione una cancha libre");
                if (elegida.getTipo() != tipo.getSelectedItem()) {
                    throw new IllegalArgumentException("El tipo de cancha cambio. Consulte la disponibilidad otra vez");
                }
                Reserva creada = gestion.registrarReservaCompleta(codigo.getText(),
                        datos.getClientes().get(clientes.getSelectedIndex()), elegida,
                        LocalDate.parse(fecha.getText().trim()), LocalTime.parse(inicio.getText().trim()),
                        Integer.parseInt(minutos.getText().trim()), servicios.getSelectedValuesList(),
                        new BigDecimal(anticipo.getText().trim()), (MetodoPago) metodo.getSelectedItem());
                JOptionPane.showMessageDialog(ventana, "Reserva " + creada.getCodigo() +
                        " confirmada y guardada. Total: Q" + creada.getTotal() +
                        " | Saldo: Q" + creada.getSaldo());
                return true;
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(ventana, error.getMessage(), "No se pudo confirmar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BigDecimal calcularTotal(String fecha, String inicio, String minutos,
                                    List<ServicioAdicional> servicios) {
        BigDecimal total = CalculadoraTarifas.costoCancha(LocalDate.parse(fecha.trim()),
                LocalTime.parse(inicio.trim()), Integer.parseInt(minutos.trim()));
        for (ServicioAdicional servicio : servicios) total = total.add(servicio.getPrecio());
        return total;
    }
}
