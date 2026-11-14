# CanchaPro

Proyecto final de Programacion 2 con Java 17, Swing y Maven. La informacion se guarda en archivos CSV, sin usar base de datos.

## Avances
1. Ventana principal y navegacion.
2. Modelo Cliente, Cancha y Reserva.
3. Disponibilidad y deteccion de horarios traslapados.
4. Tarifas, servicios y pagos.
5. Guardado y recuperacion mediante CSV.
6. Formularios para clientes, canchas y reservas.
7. Reportes: reservas por fecha, por cancha, ingresos por periodo, pagos pendientes, cancelaciones, cancha mas usada, horarios de demanda y clientes frecuentes.
8. Formulario completo de reservas: consultar disponibilidad, elegir cancha y servicios, calcular total, registrar anticipo y confirmar. Botones para iniciar y finalizar reservas.

## Abrir en NetBeans
Abrir la carpeta como proyecto Maven con Java 17 o superior. Ejecutar `Main.java`. El sistema carga los CSV de la carpeta `datos/`; si no existen en el primer inicio se crean datos de ejemplo.

## Probar la fase 07
1. Entrar en **Reportes**.
2. Elegir **Reservas por cancha** o **Clientes con mas reservas** y presionar **Consultar**.
3. Cambiar fechas, cancha o estado para filtrar la informacion.
4. Elegir **Ingresos por periodo**: se suman los pagos efectivamente registrados **por fecha de cobro**, no el total de todas las reservas.
5. Elegir **Pagos pendientes** para comprobar las reservas con saldo mayor a cero.
6. Elegir **Reservas canceladas**: el filtro de fechas corresponde al dia de cancelacion.
7. En otros modulos, registrar un cliente o una reserva y regresar a Reportes para comprobar los cambios.

Para que se vean los datos de ejemplo, al abrir la aplicacion se proponen fechas desde 7 dias atras hasta 30 dias adelante; se pueden cambiar.

## Pruebas
Ejecutar **PruebasReportes.java** con Run File. Las pruebas antiguas de disponibilidad, pagos, CSV, formularios e interfaz se conservaron. `PruebasInterfaz` requiere entorno grafico.

## Probar el avance 08

1. Ir a **Reservas** y presionar **Nueva reserva**.
2. Elegir cliente, fecha, hora y tipo de cancha; consultar **Buscar canchas libres**.
3. Elegir la cancha disponible, los servicios adicionales, y presionar **Calcular total**.
4. Ingresar un primer anticipo o Q0 y confirmar.
5. La reserva queda **CONFIRMADA** y se guarda en los CSV.
6. Usar **Iniciar** y **Finalizar** sobre una reserva confirmada.
7. Cerrar y reabrir CanchaPro para comprobar el historial.

Ejecutar `PruebasFlujoReserva.java` con Run File: son 43 pruebas de la nueva fase. Los pagos y servicios ahora se gestionan a traves de GestionRegistros, que restaura en memoria los cambios rechazados por fallos de guardado.

**Limitacion:** cada CSV se reemplaza por separado; todavia no existe una transaccion unica para los seis archivos. Conviene conservar respaldos de la carpeta datos.

## Notas importantes
- Se excluyen las reservas canceladas de ocupacion, rankings y saldos pendientes.
- Una reserva cancelada puede conservar abonos cobrados: no existe devolucion automatica, por lo que esos pagos siguen apareciendo en ingresos.
- Los horarios de demanda se cuentan por la **hora de inicio** de cada reserva, no por horas completas ocupadas.
- El reporte de ingresos se filtra por fecha de pago; los demas reportes usan fecha de la reserva, excepto cancelaciones.
- El precio base de la cancha aun no reemplaza la tarifa general de `CalculadoraTarifas`.
- Pendiente: configuracion de tarifas desde CSV, mejoras de captura y modulo opcional de tienda/cafeteria.

## Manuales

- [Manual de Usuario](manuales/Manual_de_Usuario.md): instalacion, uso de pantallas, reservas, pagos y errores frecuentes.
- [Manual Tecnico](manuales/Manual_Tecnico.md): clases, metodos, CSV, reglas, UML y pruebas.

Los manuales corresponden al Avance 07. Los reportes personales de estudio se entregan por separado y no se publican aqui.
