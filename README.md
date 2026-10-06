# HelpDesk · Gestión de incidencias

Aplicación de consola en Java para registrar, consultar, cerrar y conservar las incidencias del centro.

- **Práctica:** Evaluable 1 · Desarrollo Web en Entorno Servidor · 2.º DAW
- **Autor:** Javier Doñoro
- **Proyecto:** Reto1-Servidor (Maven · Java 17 · JUnit 5)

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior (o el Maven integrado de IntelliJ)

## Instrucciones de ejecución

```bash
# Compilar y ejecutar todas las pruebas
mvn clean test

# Ejecutar la aplicación
mvn compile exec:java

# Alternativa: generar el jar y ejecutarlo
mvn clean package
java -jar target/reto1-servidor-1.0.0.jar
```

En IntelliJ también se puede ejecutar directamente el método `main` de `AplicacionHelpDesk`.

### Archivo de datos

La aplicación lee y escribe `tickets.txt` en el **directorio desde el que se ejecuta** (la raíz del proyecto):

- Si el archivo **no existe**, arranca con una colección vacía. Se crea al guardar por primera vez (opción 6).
- Los cambios **no se guardan solos**: hay que usar la opción 6 antes de salir.
- El proyecto incluye un `tickets.txt` de ejemplo con los datos de la demostración. Para empezar desde cero, bórralo.

Formato, una incidencia por línea (`id;cerrado;descripcion`):

```
1;false;Falla el teclado
2;true;Sin conexión a Internet
```

`false` = abierta, `true` = cerrada. La descripción es de una sola línea y puede contener `;`.

## Menú

```
HELPDESK DEL CENTRO
1. Crear incidencia
2. Listar incidencias
3. Buscar incidencia por identificador
4. Cerrar incidencia
5. Mostrar estadísticas
6. Guardar incidencias
0. Salir
```

## Estructura del proyecto

```
Reto1-Servidor/
├── .idea/                           (IntelliJ; no se entrega)
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/helpdesk/
│   │   │   ├── AplicacionHelpDesk.java
│   │   │   ├── ArchivoTickets.java  (incluye ArchivoTicketsException)
│   │   │   ├── GestorTickets.java
│   │   │   └── Ticket.java
│   │   └── resources/
│   └── test/
│       └── java/helpdesk/
│           ├── EstadisticasTest.java
│           ├── GestorTicketsTest.java
│           └── TicketTest.java
├── target/                          (generada por Maven; no se entrega)
├── .gitignore
├── pom.xml
└── tickets.txt                      (ejemplo / generado al guardar)
```

Las carpetas `.idea` y `target` no forman parte de la entrega (están en `.gitignore`).

## Responsabilidades de las clases

| Clase | Responsabilidad |
|---|---|
| `Ticket` | Datos, validación y estado de una incidencia. `id` y `descripcion` son `final`; solo el estado cambia, mediante `cerrar()`. Valida en el constructor, por lo que el objeto es válido se cree desde donde se cree. |
| `GestorTickets` | Colección, generación de identificadores, creación, búsqueda, listado (copia defensiva) y estadísticas (`contarTotal`, `contarAbiertos`, `contarCerrados`). No usa consola. |
| `ArchivoTickets` | Lectura y escritura de `tickets.txt`. Carga «todo o nada» y guardado mediante archivo temporal. Contiene la excepción anidada `ArchivoTicketsException`, ya que solo se lanza desde esta clase. |
| `AplicacionHelpDesk` | Menú dividido en métodos, `Scanner`, mensajes y coordinación. Es la única clase con `Scanner` y `System.out`. |

### Decisiones de diseño

- **Creación sin efectos secundarios si falla:** `crearTicket` construye primero el `Ticket` (que valida) y solo después lo añade e incrementa `siguienteId`. Si lanza excepción, ni la colección ni el contador cambian.
- **`cerrar()` devuelve `boolean`:** `true` si cambió el estado, `false` si ya estaba cerrado. La aplicación distingue así inexistente / abierto / ya cerrado.
- **Lista protegida:** `listarTickets()` devuelve `new ArrayList<>(tickets)`: lista nueva, mismos objetos.
- **Búsqueda con `Optional`:** `buscarPorId` obliga a tratar el caso «no existe» sin devolver `null`.
- **Numeración tras cargar:** `GestorTickets(List<Ticket>)` calcula `mayor id + 1` y rechaza identificadores repetidos.
- **Carga atómica:** ante cualquier línea inválida (nº de campos, id no numérico o ≤ 0, estado distinto de `true`/`false`, descripción en blanco, id repetido) se lanza `ArchivoTicketsException` indicando la línea, el programa se detiene y el archivo **no se sobrescribe**.
- **Salida controlada:** si hay cambios sin guardar, se pide confirmación antes de salir.

## Pruebas automáticas (JUnit 5)

| Clase | Casos |
|---|---|
| `TicketTest` | Estado inicial abierto; cierre; cierre repetido; descripción en blanco y nula; identificador cero y negativo; una sola línea; recorte de espacios; ticket recuperado ya cerrado. |
| `GestorTicketsTest` | Colección vacía; identificadores consecutivos; búsqueda del mismo objeto (`assertSame`); búsqueda inexistente; creación inválida sin alterar colección ni contador; protección de la lista interna; numeración desde lista cargada. |
| `EstadisticasTest` | Gestor vacío; dos abiertas; dos con una cerrada. |

Cada prueba prepara sus propios datos, no usa `Scanner` y no depende del orden de ejecución. La persistencia se verifica con la demostración manual siguiente.

## Demostración (recorrido exigido)

1. Borrar `tickets.txt` y ejecutar: «Incidencias cargadas: 0».
2. Opción 1 dos veces (identificadores 1 y 2).
3. Opción 1 con descripción en blanco → mensaje de error, no se crea nada.
4. Opción 4 con identificador 2.
5. Opción 5 → total 2, abiertas 1, cerradas 1.
6. Opción 6 (guardar), opción 0 (salir) y volver a ejecutar.
7. Opción 2 → se conservan las dos incidencias con su estado.
8. Opción 1 → la nueva incidencia recibe el identificador 3.
9. `mvn clean test` → `BUILD SUCCESS`.

**Prueba extra:** estropear a mano una línea de `tickets.txt` (por ejemplo, `quizas` en lugar de `true`) → el programa informa del error, se detiene y el archivo queda intacto.

## Evidencias

En la carpeta `evidencias/`:

- Reinicio con datos recuperados (pasos 1-8 de la demostración y contenido de `tickets.txt`).
- `mvn clean test` con todas las pruebas superadas.
- Prueba fallida provocada (captura en rojo y de nuevo en verde):

  > _(Explicación breve. Ejemplo: cambié `siguienteId++` por `siguienteId += 2` en `crearTicket`; la prueba `identificadoresConsecutivosDesdeUno` falló porque esperaba 2 y recibió 3. Restauré el código y volvió a pasar.)_

## Limitaciones conocidas

- Pensada para una sola persona usando el archivo a la vez (sin control de concurrencia).
- Las descripciones son de una sola línea; se eliminan los espacios al principio y al final.
- No se pueden editar ni borrar incidencias; solo crear y cerrar.
- La ruta de `tickets.txt` es fija (directorio de trabajo).
- Se ignoran las líneas completamente vacías del archivo; cualquier otra línea mal formada detiene el arranque.
- No hay guardado automático: hay que usar la opción 6.
- La persistencia y la clase de consola no tienen pruebas automáticas; se verifican con la demostración manual.
