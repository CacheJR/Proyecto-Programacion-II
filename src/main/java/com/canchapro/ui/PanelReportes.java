package com.canchapro.ui;

import com.canchapro.DatosSistema;
import com.canchapro.modelo.Cancha;
import com.canchapro.modelo.EstadoReserva;
import com.canchapro.servicio.ResultadoReporte;
import com.canchapro.servicio.ServicioReportes;
import com.canchapro.servicio.TipoReporte;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Panel para consultar los ocho reportes sin modificar datos ni CSV. */
public class PanelReportes extends JPanel {
    private static final Color FONDO = new Color(15, 30, 48);
    private static final Color PANEL = new Color(21, 43, 67);
    private static final Color CLARO = new Color(36, 75, 111);
    private static final Color BLANCO = new Color(245, 248, 252);

    private final ServicioReportes reportes;
    private final JComboBox<TipoReporte> selectorReporte = new JComboBox<>(TipoReporte.values());
    private final JTextField desde = new JTextField(LocalDate.now().minusDays(7).toString(), 10);
    private final JTextField hasta = new JTextField(LocalDate.now().plusDays(30).toString(), 10);
    private final JComboBox<String> selectorCancha = new JComboBox<>();
    private final JComboBox<String> selectorEstado = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel resumen = new JLabel("Selecciona un reporte y presiona Consultar.");
    private final JLabel nota = new JLabel("Los reportes se calculan con los datos guardados en el sistema.");
    private final DatosSistema datos;

    public PanelReportes(DatosSistema datos) {
        super(new BorderLayout(0, 12));
        this.datos = datos;
        this.reportes = new ServicioReportes(datos);
        setBackground(FONDO);
        crearFormulario();
        tabla.setBackground(PANEL);
        tabla.setForeground(BLANCO);
        tabla.setGridColor(CLARO);
        tabla.setRowHeight(29);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new GridLayout(2, 1, 0, 4));
        pie.setBackground(FONDO);
        resumen.setForeground(BLANCO);
        nota.setForeground(new Color(182, 200, 217));
        pie.add(resumen);
        pie.add(nota);
        add(pie, BorderLayout.SOUTH);
        consultar();
    }

    private JLabel etiqueta(String titulo) {
        JLabel l = new JLabel(titulo);
        l.setForeground(BLANCO);
        return l;
    }

    private void crearFormulario() {
        JPanel filtros = new JPanel(new GridLayout(2, 1, 0, 5));
        filtros.setBackground(PANEL);
        filtros.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JPanel primera = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        primera.setBackground(PANEL);
        primera.add(etiqueta("Reporte:"));
        primera.add(selectorReporte);
        primera.add(etiqueta("Desde:"));
        primera.add(desde);
        primera.add(etiqueta("Hasta:"));
        primera.add(hasta);
        filtros.add(primera);

        selectorCancha.addItem("Todas las canchas");
        for (Cancha c : datos.getCanchas()) selectorCancha.addItem(c.getCodigo() + " - " + c.getNombre());
        selectorEstado.addItem("Todos los estados");
        for (EstadoReserva e : EstadoReserva.values()) selectorEstado.addItem(e.name());
        JButton consultar = new JButton("Consultar");
        consultar.addActionListener(e -> consultar());
        JPanel segunda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        segunda.setBackground(PANEL);
        segunda.add(etiqueta("Cancha:"));
        segunda.add(selectorCancha);
        segunda.add(etiqueta("Estado:"));
        segunda.add(selectorEstado);
        segunda.add(consultar);
        filtros.add(segunda);
        add(filtros, BorderLayout.NORTH);
    }

    private void consultar() {
        try {
            LocalDate fechaInicio = LocalDate.parse(desde.getText().trim());
            LocalDate fechaFin = LocalDate.parse(hasta.getText().trim());
            Cancha cancha = null;
            if (selectorCancha.getSelectedIndex() > 0) {
                cancha = datos.getCanchas().get(selectorCancha.getSelectedIndex() - 1);
            }
            EstadoReserva estado = selectorEstado.getSelectedIndex() == 0 ? null :
                    EstadoReserva.valueOf((String) selectorEstado.getSelectedItem());
            ResultadoReporte resultado = reportes.generar((TipoReporte) selectorReporte.getSelectedItem(),
                    fechaInicio, fechaFin, cancha, estado);
            modelo.setDataVector(resultado.getFilas().toArray(new Object[0][]), resultado.getColumnas());
            resumen.setForeground(BLANCO);
            resumen.setText(resultado.getResumen());
            nota.setText(resultado.getNota());
        } catch (DateTimeParseException | IllegalArgumentException error) {
            modelo.setRowCount(0);
            resumen.setForeground(new Color(255, 175, 175));
            resumen.setText("Revise las fechas o filtros: " + error.getMessage());
            nota.setText("Fechas en formato AAAA-MM-DD, por ejemplo: 2026-11-01");
        }
    }
}
