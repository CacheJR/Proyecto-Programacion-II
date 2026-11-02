# CanchaPro

Proyecto de Programacion 2 en Java (NetBeans y Swing).

## Avances
1. Ventana principal y menu.
2. Clases Cliente, Cancha y Reserva.
3. Consulta de horarios disponibles.
4. Tarifas, servicios y pagos.
5. Guardado y recuperacion de datos CSV.

## Ejecutar
Abrir como proyecto Maven en NetBeans (Java 17 o superior) y ejecutar `Main.java`.

En el primer inicio se crea una carpeta **datos/** con registros de ejemplo. Al volver a iniciar se cargan esos archivos en vez de crear registros nuevos. Los servicios y abonos que se registren desde el menu Pagos se guardan al momento. En Configuracion se puede ver la ruta y guardar manualmente.

Archivos: `clientes.csv`, `canchas.csv`, `servicios.csv`, `reservas.csv`, `reserva_servicios.csv`, `pagos.csv`.

Para hacer una prueba: entrar a Pagos, seleccionar R002, agregar Balon y un abono de Q100, cerrar la aplicacion y abrirla de nuevo. Los cambios deberian permanecer. No editar manualmente los CSV mientras el programa esta abierto.

Ejecutar las clases `PruebasDisponibilidad`, `PruebasPagos` y `PruebasArchivosCSV` desde NetBeans para verificar el proyecto.

**Pendiente:** formularios para agregar y editar clientes, canchas y reservas; reportes; configuracion de tarifas desde archivo. Las tarifas de esta fase siguen definidas en Java. Si alguno de los CSV esta danado o falta, el inicio mostrara un error en lugar de sobreescribir los datos.
