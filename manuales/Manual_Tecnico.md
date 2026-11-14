# Manual Técnico — CanchaPro

**Proyecto final de Programación 2** · Java 17 · Swing · Maven · Noviembre de 2026

## 1. Objetivo

Explicar cómo está organizado CanchaPro y cómo trabaja desde la ventana hasta los objetos y archivos. La aplicación es local: no usa servidor, base de datos ni inicio de sesión.

## 2. Requisitos de ejecución

- JDK **17 o superior**.
- Apache NetBeans con soporte Maven.
- Permisos para crear la carpeta `datos/` en el directorio de ejecución.

Abrir el directorio que contiene `pom.xml` y ejecutar `com.canchapro.Main`. Para compilar con Maven: `mvn clean package`. El JAR incluye el punto de entrada `com.canchapro.Main`.

## 3. Paquetes

| Paquete | Función |
| --- | --- |
| `com.canchapro` | `Main`, `DatosSistema` y `DatosEjemplo` |
| `modelo` | `Cliente`, `Cancha`, `Reserva`, `Pago`, servicios y enums |
| `servicio` | disponibilidad, registros, tarifas, Dashboard y reportes |
| `persistencia` | `ArchivosCSV`, lectura y escritura del núcleo |
| `ui` | ventanas y formularios Swing, reportes y tienda |
| `tienda` | productos, ventas, detalle, inventario, configuración y archivos propios |

El diagrama UML del código se encuentra en `diagrama_clases.puml`.

## 4. Clases importantes

| Clase | Responsabilidad principal |
| --- | --- |
| `Main` | Arrancar Swing en el hilo de eventos con `invokeLater` |
| `VentanaPrincipal` | Menu, Dashboard, Disponibilidad, Pagos y Configuración |
| `PanelesGestion` | Formularios de Clientes, Canchas y Reservas |
| `FormularioNuevaReserva` | Flujo completo con cancha, servicios, anticipo y confirmación |
| `DatosSistema` | Listas de objetos compartidos |
| `Cliente` y `Cancha` | Datos de clientes y canchas |
| `Reserva` | Horario, estado, costo histórico, servicios, pagos y saldo |
| `ServicioDisponibilidad` | Reglas para evitar horarios traslapados |
| `GestionRegistros` | Altas, cambios, pagos y guardado con copia/retroceso en memoria |
| `CalculadoraTarifas` | Seleccionar tarifa y calcular alquiler por minutos |
| `ConfiguracionTarifas` | Tres valores configurables con validación |
| `ServicioDashboard` | Indicadores reales del día y saldo pendiente |
| `ServicioReportes` | Ocho reportes agrupados y filtrados |
| `ArchivosCSV` | Persistir y reconstruir el núcleo en siete CSV |
| `PanelTienda` | Pantalla opcional de inventario y ventas |
| `Producto`, `Venta` y `DetalleVenta` | Entidades del módulo opcional |
| `ServicioTienda` | Comprobar stock, registrar ventas y descontar inventario |
| `ArchivosTienda` | Lectura/escritura de tres CSV independientes |
| `ConfiguracionModulos` | Activación de tienda por archivo de propiedades |

Se usa encapsulamiento con atributos privados; `Reserva` tiene asociaciones con `Cliente` y `Cancha`, y colecciones de `Pago` y `ServicioAdicional`. `VentanaPrincipal` hereda de `JFrame`. Los enums evitan valores arbitrarios para estados y tipos.

## 5. Recorrido de una operación

**Inicio:** `Main.main()` → `SwingUtilities.invokeLater` → `VentanaPrincipal` → `ArchivosCSV.cargarOIniciar()` → `DatosSistema` → pantalla.

**Nueva reserva:** `FormularioNuevaReserva.mostrar()` → `ServicioDisponibilidad.buscarDisponibles()` → `GestionRegistros.registrarReservaCompleta()` → objeto `Reserva` → `ArchivosCSV.guardar()`.

**Venta opcional:** `PanelTienda` → `ServicioTienda.vender()` → objetos `Producto`, `Venta` y `DetalleVenta` → `ArchivosTienda.guardar()`.

La interfaz no escribe directamente las filas CSV: las clases de servicio y persistencia se encargan de ello.

## 6. Disponibilidad y estados

Existe traslape si se cumple:

```
inicioNuevo < finExistente  Y  finNuevo > inicioExistente
```

Se comprueba el mismo día y la misma cancha. No se considera conflicto cuando una reserva empieza exactamente al finalizar otra. Se rechazan las canchas `MANTENIMIENTO` o `INACTIVA`, las solicitudes fuera de apertura y duraciones no válidas. Las reservas canceladas no bloquean el horario.

Estados de reserva: `PENDIENTE` → `CONFIRMADA` → `EN_CURSO` → `FINALIZADA`. Las pendientes y confirmadas pueden pasar a `CANCELADA` registrando fecha, motivo y operador. La interfaz solo permite transiciones válidas.

## 7. Tarifas, pagos y saldo

Tarifa original: Q150/h diurna entre semana, Q225/h nocturna entre semana y Q250/h sábados y domingos. Se utiliza la franja correspondiente a la **hora inicial**.

```
Costo de cancha = Tarifa inicial × (minutos / 60)
Total = Costo de cancha + Servicios adicionales
Pagado = Suma de los pagos registrados
Saldo = Total - Pagado
```

`BigDecimal` evita errores de precisión con dinero. `ConfiguracionTarifas` valida los valores de `tarifas.csv`. Al crear la reserva, el costo de cancha queda guardado como **costo histórico** en `reservas.csv`; por eso los cambios de tarifa no alteran importes anteriores. En una instalación anterior de seis CSV, el programa admite el encabezado antiguo y conserva los valores de referencia en la migración.

`Reserva.registrarPago()` rechaza duplicados, importes no positivos y pagos mayores al saldo. `GestionRegistros` modifica una copia de la reserva y mantiene el estado anterior en memoria si el guardado falla; aun así **los seis archivos no forman una transacción atómica global**.

## 8. Archivos del núcleo

| Archivo | Datos almacenados |
| --- | --- |
| `clientes.csv` | Código, nombre, teléfono, correo y fecha de registro |
| `canchas.csv` | Código, nombre, tipo, capacidad, precio base, estado y horario |
| `servicios.csv` | Servicios y precios |
| `tarifas.csv` | Tres tarifas configurables |
| `reservas.csv` | Cliente, cancha, fecha, hora, minutos, estado, cancelación y costo histórico |
| `reserva_servicios.csv` | Relaciones entre reservas y servicios |
| `pagos.csv` | Importe, fecha, método, observación y reserva asociada |

Se usa **UTF-8** y **punto y coma** como separador. `ArchivosCSV` valida encabezados, relaciones y formatos; escribe archivos temporales que reemplazan los anteriores. Si un archivo está malformado, muestra un error controlado. La carpeta de datos debe respaldarse completa mientras el programa está cerrado.

## 9. Tienda/Cafetería configurable

El núcleo de reservas funciona sin la tienda. En `datos/configuracion.properties` el campo `modulo_tienda=true` habilita el botón. El valor `false` lo oculta y **no elimina los registros**. El menú también permite cambiar esta opción sin recompilar.

Archivos opcionales: `productos.csv`, `ventas.csv` y `detalle_ventas.csv`. `ServicioTienda.vender()` rechaza cantidades superiores a la existencia disponible. Una venta guarda los precios unitarios de la operación, sus cantidades, un total calculado y una referencia opcional a una reserva. La tienda tiene su propia pantalla de ventas con historial e indicador de stock bajo.

## 10. Reportería y Dashboard

`ServicioReportes.generar(...)` devuelve un objeto `ResultadoReporte` con tabla y resumen. Procesa reservas por fecha, por cancha, ingresos por período, pagos pendientes, cancelaciones, cancha más utilizada, horas de demanda y clientes frecuentes. Los ingresos se agrupan por **fecha del pago** y la demanda excluye las reservas canceladas.

`ServicioDashboard.calcular(fecha)` obtiene conteos e importes desde los objetos. Los ingresos de hoy pueden incluir pagos de partidos futuros.

## 11. Pruebas

En `src/test/java/com/canchapro/` están las pruebas ejecutables individualmente desde NetBeans: `PruebasDisponibilidad`, `PruebasPagos`, `PruebasArchivosCSV`, `PruebasFormularios`, `PruebasReportes`, `PruebasFlujoReserva`, `PruebasTarifasDashboard`, `PruebasTienda`, `PruebasInterfaz` y `PruebasInterfazReportes`. Las dos últimas requieren pantalla.

Los resultados de la revisión final están en `documentacion/resultado_pruebas.txt`. Repetir las pruebas en NetBeans antes de la defensa, porque las instalaciones Java y la ubicación de los datos pueden variar.

## 12. Riesgos y límites conocidos

- No hay copias de seguridad automáticas ni una transacción única para todos los CSV.
- No hay facturación electrónica ni integración de pagos en línea.
- Se usa la tarifa de hora inicial, no el cálculo avanzado por tramos.
- El precio base de `Cancha` es un atributo registrado, pero las tarifas generales configurables gobiernan el costo de nuevas reservas.
- La tienda es adicional y no debe alterar las operaciones del núcleo.
- No se exportan reportes directamente a PDF desde la interfaz; el documento académico se entrega por separado.

## 13. Mantenimiento

Antes de cambiar una clase, hacer respaldo del código y de la carpeta `datos`. Si se agrega un atributo persistente, actualizar la clase, los archivos CSV, las pruebas, el UML y los manuales. Si el profesor pide agregar limpieza entre reservas, modificar `ServicioDisponibilidad`; si pide un recargo dominical, modificar `CalculadoraTarifas`; si pide un anticipo mínimo, modificar `GestionRegistros`.
