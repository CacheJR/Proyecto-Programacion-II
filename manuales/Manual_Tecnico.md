# Manual tecnico - CanchaPro

**Proyecto:** Sistema de Gestion y Reservas para Complejos Deportivos  
**Caso:** Complejo Deportivo La 10  
**Curso:** Programacion 2  
**Version documentada:** Avance 07 (noviembre de 2026)

## 1. Objetivo

Explicar la estructura del codigo, las reglas de negocio, los archivos y las pruebas del sistema para poder darle mantenimiento y defender su funcionamiento.

CanchaPro es una aplicacion **de escritorio**, desarrollada en **Java 17**, **Swing** y **Maven**. No se utiliza servidor web, API ni base de datos. La informacion se guarda en archivos CSV de texto.

## 2. Como compilar y ejecutar

1. Instalar JDK 17 o superior y Apache NetBeans con soporte Maven.
2. Clonar o descargar `CacheJR/Proyecto-Programacion-II`.
3. Abrir el directorio que contiene `pom.xml`.
4. Ejecutar `src/main/java/com/canchapro/Main.java`.

Desde una consola con Maven instalado se puede compilar mediante:

```bash
mvn clean package
java -jar target/CanchaPro-1.0-SNAPSHOT.jar
```

El directorio de trabajo determina donde se encuentra la carpeta relativa `datos/`. Por eso es importante comprobar la ruta en **Configuracion**.

## 3. Estructura de carpetas

```text
src/main/java/com/canchapro/
  Main.java
  DatosSistema.java
  DatosEjemplo.java
  modelo/
    Cliente.java, Cancha.java, Reserva.java
    Pago.java, ServicioAdicional.java
    EstadoCancha.java, EstadoReserva.java
    TipoCancha.java, MetodoPago.java
  servicio/
    ServicioDisponibilidad.java
    CalculadoraTarifas.java
    GestionRegistros.java
    ServicioReportes.java
    TipoReporte.java
    ResultadoReporte.java
  persistencia/
    ArchivosCSV.java
  ui/
    VentanaPrincipal.java
    PanelesGestion.java
    PanelReportes.java
src/test/java/com/canchapro/
  PruebasDisponibilidad.java, PruebasPagos.java
  PruebasArchivosCSV.java, PruebasFormularios.java
  PruebasInterfaz.java, PruebasReportes.java
  PruebasInterfazReportes.java
pom.xml
diagrama_clases.puml
```

`diagrama_clases.puml` contiene el UML en formato PlantUML. Se debe revisar y actualizar cuando cambien las clases.

## 4. Funciones de las clases principales

| Clase | Responsabilidad |
| --- | --- |
| `Main` | Punto de entrada; inicia Swing con `SwingUtilities.invokeLater` |
| `VentanaPrincipal` | Ventana, menu lateral, Pagos, Disponibilidad y Configuracion |
| `PanelesGestion` | Formularios de Clientes, Canchas y Reservas |
| `PanelReportes` | Interfaz de seleccion, filtros y tabla de reportes |
| `DatosSistema` | Contiene listas de clientes, canchas, servicios y reservas |
| `Cliente` | Codigo, nombre, telefono, correo y fecha de registro |
| `Cancha` | Datos de la cancha, horarios y estado |
| `Reserva` | Cliente y cancha asociados, fechas, estado, servicios, pagos y saldo |
| `Pago` | Codigo, fecha y hora, monto, metodo y observacion |
| `ServicioAdicional` | Codigo, nombre y precio |
| `ServicioDisponibilidad` | Comprueba estados, horarios, traslapes y registro en memoria |
| `GestionRegistros` | Validaciones de altas/ediciones/cancelaciones y guardado |
| `CalculadoraTarifas` | Calcula costos de alquiler por fecha, hora y minutos |
| `ServicioReportes` | Agrupa, cuenta, filtra y suma objetos para obtener ocho reportes |
| `ArchivosCSV` | Lee, valida, escribe y reconstruye los objetos al iniciar |

Las clases `EstadoCancha`, `EstadoReserva`, `TipoCancha`, `MetodoPago` y `TipoReporte` son enumeraciones (`enum`) que evitan valores de texto inconsistentes.

## 5. Relaciones orientadas a objetos

Una **Reserva** pertenece a un **Cliente** y a una **Cancha**. Una reserva puede incluir varios **ServicioAdicional** y varios **Pago**. Las listas en `DatosSistema` conservan los objetos compartidos.

Se utiliza encapsulamiento al declarar atributos `private` y acceder a ellos mediante constructores, metodos y getters. `VentanaPrincipal extends JFrame` aplica herencia. El tipo `enum` limita los posibles estados y tipos.

## 6. Flujo de inicio

```text
Main.main()
    -> SwingUtilities.invokeLater()
    -> VentanaPrincipal()
    -> ArchivosCSV.cargarOIniciar()
    -> DatosSistema (objetos y listas)
    -> Construccion del menu y pantallas
```

Si no existe ninguno de los seis CSV y el directorio esta vacio o no existe, el programa genera datos de ejemplo. Si faltan archivos en un conjunto existente, presenta un error para evitar sobrescribir registros.

## 7. Flujo de una reserva

```text
Formulario Swing
    -> PanelesGestion.crearReservas()
    -> GestionRegistros.registrarReserva()
    -> ServicioDisponibilidad.registrarReserva()
    -> Reserva (objeto en memoria)
    -> ArchivosCSV.guardar()
    -> Archivos CSV
```

Primero se valida que el cliente este registrado, que la cancha exista y este disponible y que el codigo de reserva no se repita. Si pasa las validaciones, se crea la reserva y se guarda.

**Regla de traslape:** hay conflicto cuando se cumplen ambas condiciones:

```text
inicioNuevo < finExistente
Y
finNuevo > inicioExistente
```

Ejemplo: una reserva existente de 18:00 a 19:30 bloquea otra de 19:00 a 20:00 en la misma cancha. Una nueva de 19:30 a 20:30 puede iniciar porque no se superpone, siempre que no choque con otra reserva.

Las reservas canceladas no bloquean horarios.

## 8. Tarifa, servicios y pagos

`CalculadoraTarifas.tarifaPorHora(fecha, inicio)` selecciona **Q150**, **Q225** o **Q250** por hora, segun dia y horario. `costoCancha(fecha, inicio, minutos)` calcula la tarifa proporcional a los minutos.

```text
Costo cancha = Tarifa por hora x (minutos / 60)
Total reserva = Costo cancha + Suma de servicios
Pagado        = Suma de pagos registrados
Saldo         = Total reserva - Pagado
```

Se utiliza `BigDecimal` para manejar importes de dinero y evitar errores de precision binaria. `Reserva.registrarPago()` rechaza importes mayores al saldo, codigos repetidos y pagos para estados no permitidos. `Reserva.agregarServicio()` evita servicios duplicados y no permite agregar servicios despues del primer pago.

**Limite actual:** las tarifas estan escritas en `CalculadoraTarifas`, no se leen de `tarifas.csv`. Se usa la tarifa de la hora inicial cuando una reserva cruza franjas horarias.

## 9. Guardado y recuperacion CSV

La clase `ArchivosCSV` trabaja con seis archivos en `datos/`, codificacion **UTF-8** y separador **punto y coma (`;`)**.

| Archivo | Guarda |
| --- | --- |
| `clientes.csv` | Codigo, nombre, telefono, correo y fecha de registro |
| `canchas.csv` | Identificacion, tipo, capacidad, precio base, estado y horario |
| `servicios.csv` | Catalogo de servicios y precios |
| `reservas.csv` | Codigo, cliente, cancha, fecha, horario y estado |
| `reserva_servicios.csv` | Relacion entre cada reserva y sus servicios |
| `pagos.csv` | Codigo de reserva y pago, fecha, importe, metodo y observacion |

**Escritura:** `guardar(datos)` genera archivos temporales y luego sustituye los CSV originales.  
**Lectura:** `cargar()` lee y valida los encabezados, identifica registros duplicados y reconstruye las relaciones entre objetos.  
**Primer uso:** `cargarOIniciar()` diferencia una instalacion nueva de una carpeta de datos incompleta.

No hay transaccion unica entre los seis archivos; un corte de energia durante el reemplazo podria dejar datos inconsistentes. Para este proyecto academico se recomienda conservar respaldos de la carpeta completa.

## 10. Reportes

`ServicioReportes.generar(tipo, desde, hasta, canchaFiltro, estadoFiltro)` aplica filtros y selecciona uno de ocho reportes:

- Reservas por fecha.
- Reservas por cancha.
- Ingresos por periodo.
- Pagos pendientes.
- Reservas canceladas.
- Cancha mas utilizada.
- Horarios de mayor demanda.
- Clientes con mas reservas.

`ResultadoReporte` devuelve columnas, filas, resumen y nota. `PanelReportes` los presenta en una tabla Swing.

El reporte de ingresos utiliza la fecha del **Pago**, no la fecha de la **Reserva**. La ocupacion y los rankings no cuentan reservas canceladas. Los horarios de demanda se agrupan por la hora de inicio.

## 11. Validaciones y manejo de errores

- Clientes: campos obligatorios, codigo y telefono no duplicados.
- Canchas: capacidad positiva, estado valido y apertura anterior al cierre.
- Reservas: duracion positiva, hora final dentro del mismo dia y del horario de cancha, ausencia de traslape.
- Pagos: importe positivo, maximo dos decimales, no mayor al saldo.
- CSV: encabezados correctos, referencias existentes y registros bien formados.
- Interfaz: mensajes con `JOptionPane` cuando se rechaza una operacion o falla la lectura/guardado.

Las validaciones centrales estan en las clases de modelo y servicio, no unicamente en los formularios.

## 12. Pruebas del proyecto

Las clases de prueba estan en `src/test/java/com/canchapro/`. Se ejecutan con **Run File** desde NetBeans:

| Clase | Comprueba |
| --- | --- |
| `PruebasDisponibilidad` | Traslapes, canchas inactivas, horarios y limites |
| `PruebasPagos` | Tarifas, servicios, abonos y saldo |
| `PruebasArchivosCSV` | Guardado, reconstruccion y archivos invalidos |
| `PruebasFormularios` | Altas, ediciones, cancelaciones y persistencia |
| `PruebasInterfaz` | Navegacion y tablas de Swing |
| `PruebasReportes` | Calculos, agrupaciones y filtros |
| `PruebasInterfazReportes` | Pantalla y seleccion de reportes |

Las pruebas de Swing requieren un ambiente grafico. Antes de la entrega final deben ejecutarse nuevamente y registrarse sus resultados.

## 13. Limites y mejoras pendientes

- No se incluye el modulo opcional Tienda/Cafeteria.
- No se permite configurar tarifas desde archivos.
- No hay devolucion automatica de pagos cancelados.
- La gestion de canchas aun no edita todos los campos.
- No existe un flujo unico que cree la reserva, agregue servicios y cobre en una sola pantalla.
- No se exportan reportes a PDF ni se dispone de copias de seguridad automaticas.

## 14. Mantenimiento recomendado

Antes de modificar el proyecto, hacer una copia del codigo y de `datos/`. Si se agregan atributos nuevos a las clases, actualizar `ArchivosCSV`, pruebas, UML y manuales. No modificar los datos CSV mientras el programa este abierto.

**Referencia de trabajo:** enunciado "CanchaPro - Sistema de Gestion y Reservas para Complejos Deportivos", proyecto final de Programacion 2.
