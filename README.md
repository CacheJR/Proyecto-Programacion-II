# CanchaPro - Proyecto final de Programacion 2

**Repositorio:** `CacheJR/Proyecto-Programacion-II`  
**Caso de estudio:** Complejo Deportivo ficticio **La 10** (Mazatenango, Suchitepequez).  
**Tecnologias:** Java, Swing, Maven y, en avances posteriores, archivos CSV.  
**Estado actual:** Avance 01: base ejecutable con menu y pantallas de ejemplo. **El sistema de reservas todavia no esta implementado.**

## Abrir en Apache NetBeans

1. Abrir **NetBeans**.
2. Elegir **File > Open Project** (Archivo > Abrir proyecto).
3. Seleccionar esta carpeta, que contiene `pom.xml`.
4. Ejecutar `Main.java` o usar **Run Project**.

La configuracion solicita **Java 17 o superior** (por ejemplo, JDK 21 o 24). Al usar NetBeans con Maven, el IDE descargara los plugins de compilacion que hagan falta.

## Ejecutar desde una terminal con Maven

```bash
mvn clean package
java -jar target/CanchaPro-1.0-SNAPSHOT.jar
```

## Estructura actual

```text
Proyecto-Programacion-II/
|-- pom.xml
|-- README.md
|-- .gitignore
|-- docs/
|   |-- plan_entregas.md
|   `-- bitacora_actualizaciones.md
`-- src/main/java/com/canchapro/
    |-- Main.java
    `-- ui/VentanaPrincipal.java
```

## Avance 01: funciones terminadas

- Iniciar una aplicacion Java Swing.
- Abrir una ventana con cabecera y menu lateral.
- Navegar visualmente entre **Inicio, Clientes, Canchas, Disponibilidad, Reservas, Pagos, Reportes y Configuracion**.
- Mostrar avisos claros de funcionalidad aun no implementada.

No se registran datos, no se calculan tarifas ni se guardan CSV en este avance. El documento base no solicita un inicio de sesion obligatorio.

## Seguimiento de avances

Consultar [`docs/bitacora_actualizaciones.md`](docs/bitacora_actualizaciones.md) para conocer los avances del codigo, y [`docs/plan_entregas.md`](docs/plan_entregas.md) para revisar las proximas funciones.

Los reportes de estudio personales se entregan por separado y **no se incluyen en este repositorio**. La documentacion tecnica exigida para la entrega academica se preparara de forma independiente.

## Referencia academica

La implementacion sigue el documento **CanchaPro - Sistema de Gestion y Reservas para Complejos Deportivos**, proyecto final de Programacion 2. El nucleo de reservas, disponibilidad, tarifas, pagos, persistencia y reportes se incorporara en avances posteriores.
