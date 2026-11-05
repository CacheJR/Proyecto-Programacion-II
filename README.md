# CanchaPro

Proyecto final de Programacion 2 con Java 17, Swing y Maven. Los datos se guardan en archivos CSV y no se usa base de datos.

## Avances

1. Ventana principal y navegacion.
2. Modelo Cliente, Cancha y Reserva.
3. Consulta de disponibilidad y conflictos de horarios.
4. Tarifas, servicios adicionales y pagos.
5. Lectura y escritura de archivos CSV.
6. Formularios para registrar, buscar y editar clientes, agregar canchas, cambiar su estado, crear y cancelar reservas.

## Ejecutar en NetBeans

Abrir este directorio como proyecto Maven y ejecutar `Main.java`.

En el primer inicio se crean los datos de ejemplo en la carpeta `datos/`. Al cerrar y abrir el programa, los registros guardados se vuelven a cargar.

## Probar el avance 06

1. En **Clientes**, registrar un cliente de prueba. Usar **Buscar** por codigo, nombre o telefono. Seleccionar el cliente y elegir **Editar seleccionado**.
2. En **Canchas**, registrar una cancha o cambiar su estado a MANTENIMIENTO.
3. En **Reservas**, seleccionar un cliente y una cancha, indicar fecha, hora y minutos. Si la cancha esta libre se crea la reserva; si hay conflicto se rechaza.
4. Seleccionar una reserva pendiente o confirmada y usar **Cancelar seleccionada**, indicando motivo y responsable.
5. Cerrar y abrir CanchaPro para verificar que los cambios permanecen.

En **Pagos** continuan disponibles los servicios y abonos de los avances anteriores.

## Pruebas

Ejecutar con **Run File** las clases `PruebasDisponibilidad`, `PruebasPagos`, `PruebasArchivosCSV`, `PruebasFormularios` y `PruebasInterfaz`. La ultima necesita un entorno grafico.

## Pendiente

Reportes agrupados, configuracion de tarifas desde CSV, formularios para editar todos los campos de cancha y mejoras del flujo completo de reserva con servicios y pagos en un solo formulario.

Nota: el precio base almacenado en cada cancha no modifica todavia la tarifa calculada; se usan las tarifas generales segun dia y hora. Guardar CSV no equivale a una transaccion de base de datos: para uso real conviene anadir copias de seguridad.
