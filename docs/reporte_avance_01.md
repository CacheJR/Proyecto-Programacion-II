# Reporte de estudio - Avance 01 de CanchaPro

**Fecha:** 14/10/2026  
**Curso:** Programacion 2  
**Repositorio:** CacheJR/Proyecto-Programacion-II  
**Caso:** Complejo Deportivo ficticio La 10

## 1. Objetivo

Preparar una base ejecutable para NetBeans utilizando Java, Maven y Swing, con una ventana y navegacion entre modulos. **No estan implementadas las reservas, clientes, canchas, pagos ni archivos CSV.** El enunciado exige desarrollarlos en los proximos avances.

## 2. Archivos creados

| Archivo | Para que sirve |
| --- | --- |
| `pom.xml` | Configura Maven, Java 17 y el JAR ejecutable. |
| `Main.java` | Inicia el programa. |
| `VentanaPrincipal.java` | Construye la interfaz y responde a los clics del menu. |
| `.gitignore` | Ignora archivos compilados y temporales. |
| `README.md` | Instrucciones de apertura y ejecucion. |
| `docs/plan_entregas.md` | Cronograma de siete etapas propuestas. |
| `docs/bitacora_actualizaciones.md` | Registra los cambios. |
| `docs/reporte_avance_01.md` | Material para estudiar esta entrega. |

## 3. Clase Main

**Paquete:** `com.canchapro`. **Funcion:** arrancar el sistema.

- `private Main()`: impide crear instancias innecesarias de la clase.
- `public static void main(String[] args)`: punto de entrada que ejecuta Java.
- `SwingUtilities.invokeLater(...)`: crea la interfaz en el hilo de eventos de Swing (EDT).
- `new VentanaPrincipal()`: construye un objeto de la ventana.
- `setVisible(true)`: la hace visible.

Ejemplo real:

```java
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.setVisible(true);
    });
}
```

## 4. Clase VentanaPrincipal

**Paquete:** `com.canchapro.ui`. **Hereda:** `JFrame`.

Tiene constantes `Color`, una matriz `MODULOS`, un `CardLayout tarjetas`, un `JPanel panelPantallas` y un `JLabel tituloVista`.

| Metodo | Para que sirve |
| --- | --- |
| `VentanaPrincipal()` | Constructor: configura titulo, tamaño, cierre y posicion de ventana; llama a la construccion visual. |
| `construirInterfaz()` | Une encabezado, menu y centro en el panel principal. |
| `crearEncabezado()` | Muestra CANCHAPRO y la descripcion. |
| `crearMenu()` | Recorre `MODULOS` y coloca cada boton verticalmente. |
| `crearBotonMenu(texto, identificador)` | Crea y configura un boton; conecta su clic a `mostrarPantalla`. |
| `crearContenido()` | Prepara area central, titulo y pantallas de `CardLayout`. |
| `crearPanelInicio()` | Crea la bienvenida y cuatro tarjetas de estado pendiente. |
| `crearTarjeta(titulo, subtitulo)` | Reutiliza el diseño de una tarjeta informativa. |
| `crearPanelPendiente(modulo)` | Muestra un aviso para un modulo sin implementar. |
| `mostrarPantalla(identificador, nombre)` | Cambia la tarjeta visible y actualiza el titulo. |

## 5. Componentes y conceptos utilizados

- **Java:** lenguaje de programacion.
- **Swing:** biblioteca para interfaz grafica.
- **Maven:** compila y empaqueta el proyecto.
- **JFrame:** ventana principal.
- **JPanel:** contenedor de componentes.
- **JLabel:** textos informativos.
- **JButton:** boton clicable.
- **BorderLayout:** distribuye encabezado, menu y centro.
- **BoxLayout:** apila verticalmente los botones.
- **GridLayout:** distribuye las cuatro tarjetas.
- **CardLayout:** intercambia vistas sin abrir otras ventanas.
- **addActionListener:** define la accion al hacer clic.
- **setText:** cambia un texto visible.
- **Color, Font y BorderFactory:** diseño y espaciado visual.
- **private:** protege detalles internos.
- **final:** impide reasignar variables o constantes indicadas.

## 6. Recorrido para explicar frente al ingeniero

1. Java ejecuta `Main.main()`.
2. El hilo de Swing ejecuta la construccion de `VentanaPrincipal`.
3. El constructor configura la ventana y ejecuta `construirInterfaz()`.
4. Se muestran el encabezado, el menu y la vista de Inicio.
5. Al presionar Clientes, su `addActionListener` llama a `mostrarPantalla("CLIENTES", "Clientes")`.
6. `CardLayout.show(...)` cambia a la vista Clientes y el titulo se actualiza. Como el modulo esta pendiente, la vista solo muestra un aviso.

No hay solicitudes a un servidor, login, contraseñas, tokens ni autentificacion: en esta etapa es una interfaz de escritorio local.

## 7. Como probar

1. Abrir la carpeta que tiene `pom.xml` en NetBeans.
2. Utilizar Java 17 o superior y ejecutar el proyecto.
3. Verificar que aparezca la ventana y que los ocho botones funcionen.
4. Confirmar que las pantallas pendientes estan identificadas.

**Pruebas realizadas al preparar este avance:** compilacion con `javac --release 17` exitosa; inicio de ventana en pantalla virtual sin excepciones. No se ha probado Maven en este entorno ni ningun comportamiento de reservas.

## 8. Preguntas de defensa

**¿Por que usar Maven?** Para organizar, compilar y generar el proyecto de manera repetible.

**¿Para que sirve main?** Es el punto de entrada de Java.

**¿Que diferencia existe entre JFrame y JPanel?** JFrame es la ventana y JPanel contiene elementos dentro de ella.

**¿Para que sirve CardLayout?** Permite cambiar entre secciones dentro de la misma ventana.

**¿Por que SwingUtilities.invokeLater?** Para crear y manipular la interfaz en el hilo de eventos correcto.

**¿Se pueden reservar canchas?** Aun no; esta es la estructura inicial para los siguientes avances.

**¿Por que no se incluye base de datos?** El enunciado exige guardar y recuperar informacion por archivos CSV.

## 9. Siguiente trabajo pendiente

Definir las clases de dominio (Cliente, Cancha, Reserva, estados), realizar UML, implementar disponibilidad y conflictos; luego tarifas, pagos, persistencia CSV, formularios, reportes y pruebas.

## 10. Explicacion breve para practicar

> En el primer avance cree una estructura de proyecto Java con Maven, compatible con NetBeans. La clase Main inicia la aplicacion y VentanaPrincipal muestra la interfaz Swing. Dividi la ventana en encabezado, menu lateral y panel central. Use CardLayout para cambiar de vista cuando presiono botones. Por ahora las pantallas muestran mensajes de funciones pendientes; la logica de reservas, pagos y CSV se desarrollara despues. Subi el codigo y su documentacion a GitHub para conservar el historial de avances.

## Referencia al enunciado

El PDF de CanchaPro exige CORE de clientes, canchas, disponibilidad y reservas (pagina 2); archivos y UML (pagina 5); interfaz Swing y reportes (pagina 6); entregables y README (pagina 8); y defensa individual del codigo (pagina 10). Este avance no pretende cumplir los requisitos del sistema completo.
