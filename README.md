# CanchaPro

Proyecto de Programacion 2 para organizar un complejo deportivo.

## Herramientas
- Java 17 o superior
- NetBeans y Maven
- Java Swing

## Avances
1. Ventana principal y menu de navegacion.
2. Clases Cliente, Cancha y Reserva con tablas de ejemplo.
3. Consulta de disponibilidad segun fecha, hora, duracion y tipo de cancha.

## Como probar el avance 03
1. Abrir el proyecto en NetBeans y ejecutar `Main.java`.
2. Entrar al menu **Disponibilidad**.
3. Dejar la fecha que aparece (manana) y la hora 18:00, 60 minutos y Futbol 5.
4. La Bombonera esta ocupada de 18:00 a 19:30, asi que solo deberia aparecer El Maracana.
5. Cambiar a 19:30: ambas canchas Futbol 5 deben aparecer.
6. Elegir Futbol 7: no debe aparecer El Monumental porque esta en mantenimiento.

Este avance aun utiliza datos de ejemplo en memoria. No guarda archivos CSV, no realiza pagos ni permite crear reservas desde la pantalla. La validacion para registrar reservas existe en `ServicioDisponibilidad`, pero la interfaz de registro se hara despues.

## Pruebas
Desde NetBeans, ejecutar `PruebasDisponibilidad.java` (Run File) para comprobar automaticamente diferentes casos de horarios y errores.
