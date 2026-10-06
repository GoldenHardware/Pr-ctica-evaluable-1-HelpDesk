package helpdesk;
import helpdesk.ArchivoTickets.ArchivoTicketsException;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interfaz de consola: menú, lectura de teclado, mensajes y coordinación entre
 * el gestor y el archivo. Es la única clase que usa Scanner y System.out.
 */
public class AplicacionHelpDesk {

    private final Scanner scanner;
    private final ArchivoTickets archivo;
    private GestorTickets gestor;
    private boolean cambiosSinGuardar = false;

    public AplicacionHelpDesk(Scanner scanner, ArchivoTickets archivo) {
        this.scanner = scanner;
        this.archivo = archivo;
    }

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            new AplicacionHelpDesk(teclado, new ArchivoTickets(Path.of("tickets.txt"))).ejecutar();
        }
    }

    /** Bucle principal: carga los datos y muestra el menú hasta que se elija salir. */
    public void ejecutar() {
        if (!cargarIncidencias()) {
            return; // arranque detenido; el archivo no se ha tocado
        }
        try {
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                salir = procesarOpcion(leerOpcion());
            }
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Entrada finalizada. Se cierra el programa.");
        }
        System.out.println("Programa finalizado.");
    }

    // ------------------------------------------------------------ arranque

    private boolean cargarIncidencias() {
        try {
            List<Ticket> cargados = archivo.cargar();
            gestor = new GestorTickets(cargados);
            System.out.println("Incidencias cargadas: " + cargados.size());
            return true;
        } catch (ArchivoTicketsException | IllegalArgumentException e) {
            System.out.println("ERROR al cargar las incidencias: " + e.getMessage());
            System.out.println("El programa se detiene y el archivo NO se ha modificado.");
            return false;
        }
    }

    // ---------------------------------------------------------------- menú

    private void mostrarMenu() {
        System.out.println();
        System.out.println("HELPDESK DEL CENTRO");
        System.out.println("1. Crear incidencia");
        System.out.println("2. Listar incidencias");
        System.out.println("3. Buscar incidencia por identificador");
        System.out.println("4. Cerrar incidencia");
        System.out.println("5. Mostrar estadísticas");
        System.out.println("6. Guardar incidencias");
        System.out.println("0. Salir");
        System.out.println("(Los cambios no se guardan solos: usa la opción 6 antes de salir.)");
    }

    /** @return la opción numérica, o -1 si lo escrito no es un número */
    private int leerOpcion() {
        System.out.print("Opción: ");
        String texto = scanner.nextLine().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** @return {@code true} si hay que terminar el programa */
    private boolean procesarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> crearIncidencia();
            case 2 -> listarIncidencias();
            case 3 -> buscarIncidencia();
            case 4 -> cerrarIncidencia();
            case 5 -> mostrarEstadisticas();
            case 6 -> guardarIncidencias();
            case 0 -> {
                return confirmarSalida();
            }
            default -> System.out.println("Opción incorrecta. Elige un número del menú (0-6).");
        }
        return false;
    }

    // ---------------------------------------------------------- operaciones

    private void crearIncidencia() {
        System.out.print("Descripción de la incidencia: ");
        String descripcion = scanner.nextLine();
        try {
            Ticket creado = gestor.crearTicket(descripcion);
            cambiosSinGuardar = true;
            System.out.println("Incidencia creada con identificador " + creado.getId() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("No se ha creado la incidencia: " + e.getMessage());
        }
    }

    private void listarIncidencias() {
        List<Ticket> lista = gestor.listarTickets();
        if (lista.isEmpty()) {
            System.out.println("No hay incidencias registradas.");
            return;
        }
        System.out.println("ID | ESTADO  | DESCRIPCIÓN");
        for (Ticket ticket : lista) {
            System.out.println(ticket);
        }
    }

    private void buscarIncidencia() {
        int id = leerIdentificador();
        Optional<Ticket> encontrado = gestor.buscarPorId(id);
        if (encontrado.isPresent()) {
            System.out.println(encontrado.get());
        } else {
            System.out.println("No existe ninguna incidencia con el identificador " + id + ".");
        }
    }

    private void cerrarIncidencia() {
        int id = leerIdentificador();
        Optional<Ticket> encontrado = gestor.buscarPorId(id);
        if (encontrado.isEmpty()) {
            System.out.println("No existe ninguna incidencia con el identificador " + id + ".");
        } else if (encontrado.get().cerrar()) {
            cambiosSinGuardar = true;
            System.out.println("La incidencia " + id + " ha sido cerrada.");
        } else {
            System.out.println("La incidencia " + id + " ya estaba cerrada.");
        }
    }

    private void mostrarEstadisticas() {
        System.out.println("Total de incidencias: " + gestor.contarTotal());
        System.out.println("Abiertas: " + gestor.contarAbiertos());
        System.out.println("Cerradas: " + gestor.contarCerrados());
    }

    private void guardarIncidencias() {
        try {
            archivo.guardar(gestor.listarTickets());
            cambiosSinGuardar = false;
            System.out.println("Incidencias guardadas correctamente (" + gestor.contarTotal() + ").");
        } catch (ArchivoTicketsException e) {
            System.out.println("ERROR: las incidencias NO se han guardado. " + e.getMessage());
        }
    }

    private boolean confirmarSalida() {
        if (!cambiosSinGuardar) {
            return true;
        }
        System.out.print("Hay cambios sin guardar. ¿Salir de todos modos? (s/n): ");
        return scanner.nextLine().trim().equalsIgnoreCase("s");
    }

    // ------------------------------------------------------------- teclado

    /** Repite la petición hasta que se escribe un número entero. */
    private int leerIdentificador() {
        while (true) {
            System.out.print("Identificador: ");
            String texto = scanner.nextLine().trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero válido.");
            }
        }
    }
}
