# CanchaPro

Proyecto de Programacion 2 en Java para un complejo deportivo.

## Herramientas
Java 17, NetBeans, Maven y Swing.

## Avances
1. Ventana principal y menu.
2. Clases Cliente, Cancha y Reserva.
3. Busqueda de disponibilidad y deteccion de traslapes.
4. Calculo de tarifas, servicios adicionales, pagos y saldos.

## Ejecutar
Abrir el proyecto Maven en NetBeans y ejecutar `Main.java`.

En el menu **Pagos** se pueden seleccionar reservas, agregar servicios y registrar abonos.
Las tarifas de ejemplo son Q150/h (lunes a viernes antes de las 17:00), Q225/h (lunes a viernes desde las 17:00) y Q250/h (sabado y domingo). Para reservas que cruzan horarios se usa la tarifa de la hora de inicio (nivel medio del enunciado).

Los datos siguen siendo de prueba y estan en memoria: **no se guardan al cerrar el programa**. No se usan bases de datos.

## Pruebas
Ejecutar `PruebasDisponibilidad.java` y `PruebasPagos.java` desde NetBeans con **Run File**.
