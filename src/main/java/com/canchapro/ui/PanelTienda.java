package com.canchapro.ui;

import com.canchapro.modelo.MetodoPago;
import com.canchapro.tienda.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Pantalla independiente del CORE. Disponible solo si modulo_tienda=true. */
public class PanelTienda extends JPanel {
    private final ServicioTienda tienda;
    private final JFrame ventana;
    private final DefaultTableModel modeloProductos;
    private final DefaultTableModel modeloVentas;

    public PanelTienda(JFrame ventana, ServicioTienda tienda) {
        this.tienda=tienda; this.ventana=ventana;
        setLayout(new BorderLayout(10,12));
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton agregar=new JButton("Agregar producto");
        JButton vender=new JButton("Nueva venta");
        acciones.add(agregar); acciones.add(vender);
        add(acciones,BorderLayout.NORTH);

        modeloProductos=new DefaultTableModel(new String[]{"Codigo","Producto","Categoria","Precio Q","Stock","Minimo","Estado"},0) {
            @Override public boolean isCellEditable(int a,int b) {return false;}
        };
        modeloVentas=new DefaultTableModel(new String[]{"Venta","Fecha","Reserva","Articulos","Total Q","Metodo"},0) {
            @Override public boolean isCellEditable(int a,int b) {return false;}
        };
        JTable tablaProductos=new JTable(modeloProductos), tablaVentas=new JTable(modeloVentas);
        tablaProductos.setRowHeight(26); tablaVentas.setRowHeight(26);
        JSplitPane secciones=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaProductos),new JScrollPane(tablaVentas));
        secciones.setResizeWeight(0.60); secciones.setDividerLocation(260);
        add(secciones,BorderLayout.CENTER);
        add(new JLabel("Tienda opcional: stock minimo visible; no afecta el sistema de reservas."),BorderLayout.SOUTH);
        agregar.addActionListener(e->nuevoProducto());
        vender.addActionListener(e->nuevaVenta());
        actualizar();
    }
    private void actualizar() {
        modeloProductos.setRowCount(0);
        for (Producto p:tienda.getDatos().getProductos()) {
            modeloProductos.addRow(new Object[]{p.getCodigo(),p.getNombre(),p.getCategoria(),p.getPrecio(),
                    p.getExistencia(),p.getMinimo(),p.isActivo() ? (p.isStockBajo()? "STOCK BAJO":"ACTIVO") : "INACTIVO"});
        }
        modeloVentas.setRowCount(0);
        for (Venta v:tienda.getDatos().getVentas()) {
            int cantidad=v.getDetalles().stream().mapToInt(DetalleVenta::getCantidad).sum();
            modeloVentas.addRow(new Object[]{v.getCodigo(),v.getFechaHora(),v.getReservaCodigo(),cantidad,v.getTotal(),v.getMetodo()});
        }
    }
    private static void campo(JPanel p,String etiqueta,JComponent componente) {
        p.add(new JLabel(etiqueta)); p.add(componente);
    }
    private void nuevoProducto() {
        JPanel f=new JPanel(new GridLayout(0,2,8,8));
        JTextField codigo=new JTextField(), nombre=new JTextField(), categoria=new JTextField("Bebidas");
        JTextField precio=new JTextField("5.00"), stock=new JTextField("20"), minimo=new JTextField("5");
        campo(f,"Codigo:",codigo); campo(f,"Nombre:",nombre); campo(f,"Categoria:",categoria);
        campo(f,"Precio Q:",precio); campo(f,"Existencia:",stock); campo(f,"Stock minimo:",minimo);
        if(JOptionPane.showConfirmDialog(ventana,f,"Nuevo producto",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try {
            tienda.registrarProducto(new Producto(codigo.getText(),nombre.getText(),categoria.getText(),
                    new BigDecimal(precio.getText().trim()),Integer.parseInt(stock.getText().trim()),
                    Integer.parseInt(minimo.getText().trim()),true));
            actualizar(); JOptionPane.showMessageDialog(ventana,"Producto guardado");
        }catch(IOException|RuntimeException error) {mostrarError(error);}
    }
    private void nuevaVenta() {
        List<Producto> disponibles=tienda.getDatos().getProductos().stream().filter(Producto::isActivo).toList();
        if(disponibles.isEmpty()) {
            JOptionPane.showMessageDialog(ventana,"No hay productos activos");return;
        }
        JPanel f=new JPanel(new GridLayout(0,2,8,8));
        JTextField codigo=new JTextField("V"+String.format("%03d",tienda.getDatos().getVentas().size()+1));
        JTextField reserva=new JTextField();
        JComboBox<MetodoPago> metodo=new JComboBox<>(MetodoPago.values());
        JList<Producto> lista=new JList<>(disponibles.toArray(new Producto[0]));
        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll=new JScrollPane(lista);scroll.setPreferredSize(new java.awt.Dimension(280,120));
        campo(f,"Codigo venta:",codigo);
        campo(f,"Reserva opcional (codigo):",reserva);
        campo(f,"Metodo pago:",metodo);
        campo(f,"Productos (Ctrl para varios):",scroll);
        if (JOptionPane.showConfirmDialog(ventana,f,"Registrar venta",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        Map<Producto,Integer> carrito=new LinkedHashMap<>();
        for (Producto p:lista.getSelectedValuesList()) {
            String entrada=JOptionPane.showInputDialog(ventana,"Cantidad de "+p.getNombre()+" (stock "+p.getExistencia()+"):","1");
            if(entrada==null)return;
            try {carrito.put(p,Integer.parseInt(entrada.trim()));}
            catch(NumberFormatException error){mostrarError(error);return;}
        }
        try {
            Venta v=tienda.vender(codigo.getText(),reserva.getText(),(MetodoPago)metodo.getSelectedItem(),carrito);
            actualizar(); JOptionPane.showMessageDialog(ventana,"Venta "+v.getCodigo()+" registrada. Total: Q"+v.getTotal());
        }catch(IOException|RuntimeException error){mostrarError(error);}
    }
    private void mostrarError(Exception e) {
        JOptionPane.showMessageDialog(ventana,e.getMessage(),"Revisar datos",JOptionPane.ERROR_MESSAGE);
    }
}
