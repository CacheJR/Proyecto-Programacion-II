# Manual de usuario - CanchaPro

**Proyecto:** CanchaPro, Complejo Deportivo La 10  
**Curso:** Programacion 2  
**Version documentada:** Avance 07 (noviembre de 2026)

## 1. Para que sirve

CanchaPro es un programa de escritorio hecho en Java Swing. Permite llevar el control de clientes, canchas, reservas, pagos y reportes del complejo deportivo. Guarda los datos en archivos CSV, por lo que no necesita una base de datos.

## 2. Requisitos y como abrirlo

- Tener Apache NetBeans y Java JDK 17 o superior.
- Descargar o clonar el repositorio `CacheJR/Proyecto-Programacion-II`.
- En NetBeans elegir **File > Open Project** y seleccionar la carpeta donde esta `pom.xml`.
- Ejecutar la clase `com.canchapro.Main` (por ejemplo, clic derecho en `Main.java` y **Run File**).

En el primer inicio, si no hay archivos de datos, el sistema crea la carpeta `datos/` con registros de ejemplo. En los siguientes inicios se cargan los datos guardados. La ruta depende de la carpeta desde la que se ejecuta el programa; puede consultarse en **Configuracion**.

## 3. Pantalla principal

El menu lateral contiene las secciones **Inicio, Clientes, Canchas, Disponibilidad, Reservas, Pagos, Reportes y Configuracion**. Basta con hacer clic en una para abrirla.

## 4. Clientes

### Agregar un cliente
1. Abrir **Clientes** y presionar **Agregar cliente**.
2. Escribir codigo, nombre, telefono y correo (opcional).
3. Confirmar. El sistema valida los campos y guarda el cliente.

### Buscar y modificar
1. Escribir el codigo, nombre o telefono en la caja de busqueda y presionar **Buscar**.
2. Para mostrar todos, presionar **Mostrar todos**.
3. Para modificar, seleccionar una fila y usar **Editar seleccionado**.
4. Cambiar nombre, telefono o correo y confirmar.

No se admiten codigos repetidos ni el mismo telefono en dos clientes.

## 5. Canchas

1. Abrir **Canchas** y presionar **Agregar cancha**.
2. Indicar codigo, nombre, tipo, capacidad, precio base, estado, apertura y cierre.
3. Confirmar para guardar.
4. Para cambiar el estado de una cancha, seleccionar su fila y elegir **Cambiar estado**.

Los estados son **DISPONIBLE, MANTENIMIENTO e INACTIVA**. Las canchas en mantenimiento o inactivas no pueden recibir nuevas reservas. El precio base almacenado no reemplaza todavia las tarifas generales del sistema.

## 6. Consultar disponibilidad

1. Abrir **Disponibilidad**.
2. Ingresar fecha en formato `AAAA-MM-DD` y hora en formato `HH:mm`.
3. Elegir duracion (30, 60, 90 o 120 minutos) y tipo de cancha.
4. Presionar **Consultar**.

La tabla muestra las canchas que cumplen las condiciones y no tienen otra reserva en ese horario. Si dos reservas se tocan exactamente en el limite de hora, no existe conflicto; si se superponen, se rechaza la nueva reserva.

## 7. Crear o cancelar reservas

### Crear una reserva
1. Abrir **Reservas** y presionar **Nueva reserva**.
2. Ingresar un codigo y seleccionar un cliente y una cancha.
3. Indicar fecha, hora inicial y duracion en minutos.
4. Confirmar. Si el horario esta libre, la nueva reserva se registra con estado **PENDIENTE**.

Se comprueban la existencia del cliente, el estado de la cancha, el horario de apertura y cierre y los traslapes con reservas anteriores. El sistema calcula la hora final.

### Cancelar una reserva
1. En **Reservas**, seleccionar la fila correspondiente.
2. Presionar **Cancelar seleccionada**.
3. Escribir el motivo y el operador responsable.
4. Confirmar. La reserva se conserva en el historial como **CANCELADA**.

No es posible cancelar una reserva que ya este finalizada, en curso o cancelada. La cancelacion no hace automaticamente devoluciones de dinero.

## 8. Agregar servicios y registrar abonos

1. Abrir **Pagos**.
2. Para un servicio adicional, seleccionar reserva y servicio, y pulsar **Agregar servicio**.
3. Para un abono, seleccionar reserva, indicar el monto y metodo de pago, y presionar **Registrar abono**.
4. Revisar la tabla de **Total Q, Pagado Q y Saldo Q**.

Un abono no puede ser negativo, cero ni mayor que el saldo pendiente. El sistema permite varios pagos por reserva. En esta version, los servicios se agregan **antes del primer pago**, no despues.

**Tarifas generales de referencia:**

| Horario | Precio |
| --- | ---: |
| Lunes a viernes, antes de las 17:00 | Q150 por hora |
| Lunes a viernes, desde las 17:00 | Q225 por hora |
| Sabado y domingo | Q250 por hora |

Si una reserva cruza franjas se utiliza la tarifa correspondiente a **la hora de inicio**.

## 9. Consultar reportes

1. Abrir **Reportes**.
2. Escoger el reporte deseado, el rango de fechas y opcionalmente cancha y estado.
3. Presionar **Consultar** y revisar los resultados.

Los reportes son: **reservas por fecha, reservas por cancha, ingresos por periodo, pagos pendientes, reservas canceladas, cancha mas utilizada, horarios de mayor demanda y clientes con mas reservas**.

Importante: los ingresos reflejan **pagos efectivamente registrados por fecha de cobro**. Las cancelaciones se filtran por fecha de cancelacion. El horario de demanda se mide por la hora de inicio de las reservas.

## 10. Guardar datos y hacer copias de seguridad

Los cambios de clientes, canchas, reservas y pagos se guardan en `datos/`. En **Configuracion** se puede ver la ruta real y presionar **Guardar datos ahora**. Para hacer una copia de seguridad, cerrar CanchaPro y copiar toda la carpeta `datos/` a otra ubicacion.

No eliminar ni modificar manualmente los CSV mientras el programa este abierto. Si falta un archivo o tiene datos incorrectos, CanchaPro puede mostrar un error al iniciar; se debe restaurar una copia valida, no crear archivos vacios para ocultar el problema.

## 11. Problemas frecuentes

| Situacion | Que revisar |
| --- | --- |
| NetBeans no inicia | JDK 17 o superior y proyecto abierto desde `pom.xml` |
| Fecha u hora invalida | Formato `AAAA-MM-DD` y `HH:mm` |
| No aparece cancha disponible | Fecha, tipo, horario, mantenimiento y reservas existentes |
| No permite reservar | Codigo duplicado, cancha no disponible o traslape |
| Pago rechazado | El monto debe ser mayor que cero y no superar el saldo |
| Error al cargar CSV | Restaurar la carpeta `datos/` desde una copia completa |

## 12. Funciones que todavia estan pendientes

La version 07 no incluye tienda/cafeteria, devoluciones automaticas, edicion completa de todos los datos de una cancha, exportacion de reportes a PDF ni configuracion de tarifas desde CSV. El sistema funciona como proyecto academico y todavia puede recibir mejoras.
