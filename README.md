# CanchaPro

Proyecto final de **Programación 2**. Sistema de reservas para el Complejo Deportivo ficticio "La 10".

**Tecnologías:** Java 17, Swing, Maven, Programación Orientada a Objetos y archivos CSV (sin base de datos).

## Cómo abrirlo en NetBeans

1. Instalar JDK 17 o superior y abrir Apache NetBeans.
2. **Archivo > Abrir proyecto** y elegir la carpeta que contiene `pom.xml`.
3. Ejecutar `Main.java` (paquete `com.canchapro`).
4. En el primer inicio se crean archivos CSV de ejemplo en `datos/`. En siguientes inicios se recuperan.

Para compilar por terminal, si tienes Maven instalado:

```bash
mvn clean package
java -jar target/CanchaPro-1.0-SNAPSHOT.jar
```

También se incluye un ejecutable JAR en el paquete de entrega. Para Windows, usar `ejecutable/ejecutar.bat` desde la carpeta del proyecto.

## Funciones

- Clientes: registrar, buscar y modificar.
- Canchas: registrar, cambiar estado y gestionar horario.
- Disponibilidad: detectar traslapes por fecha, hora, duración y tipo de cancha.
- Reservas: registrar, añadir servicios, cobrar anticipo, confirmar, iniciar, finalizar y cancelar.
- Pagos: distintos métodos, varios abonos, cálculo del saldo.
- Tarifas: se configuran en **Configuración**, se guardan en `datos/tarifas.csv` y afectan reservas nuevas, no las anteriores.
- Dashboard: reservas del día, en curso, pendientes e ingresos recibidos hoy.
- Reportes: ocho tipos con filtros.
- Tienda/Cafetería: módulo adicional que se activa desde Configuración, con productos, ventas e inventario.

## Archivos y copias

El programa utiliza `datos/` como carpeta local. Para respaldar la información, **cerrar CanchaPro y copiar toda la carpeta**. Los siete CSV del núcleo son `clientes.csv`, `canchas.csv`, `servicios.csv`, `tarifas.csv`, `reservas.csv`, `reserva_servicios.csv` y `pagos.csv`. La tienda utiliza `productos.csv`, `ventas.csv` y `detalle_ventas.csv`, además de `configuracion.properties` para activarse.

Si se abre una versión anterior que contiene seis CSV sin `tarifas.csv`, el programa crea las tarifas iniciales y lee sus reservas existentes. Los nuevos guardados incluyen el precio histórico de cada reserva.

## Documentación

- `diagrama_clases.puml`: diagrama UML del código.
- `manuales/Manual_de_Usuario.md`: explicación de las pantallas.
- `manuales/Manual_Tecnico.md`: clases, reglas, archivos y pruebas.
- `documentacion/Informe_Final.md`: informe académico con evidencias y conclusiones.
- `documentacion/capturas/`: capturas tomadas del programa real.

## Pruebas

Las pruebas están en `src/test/java/com/canchapro/`. Se pueden ejecutar individualmente con **Run File** desde NetBeans. La mayoría no necesita pantalla; las pruebas de interfaz requieren entorno gráfico.

**Notas:** las tarifas usan la franja de la **hora inicial** (nivel medio del enunciado). La cancelación conserva el historial, pero no genera devoluciones automáticas. Los CSV se sustituyen por archivo; no constituyen una transacción de base de datos y conviene conservar respaldos.
