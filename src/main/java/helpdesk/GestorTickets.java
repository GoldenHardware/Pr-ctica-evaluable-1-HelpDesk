package helpdesk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Gestiona la colección de incidencias: creación con identificadores
 * automáticos, búsqueda, listado y estadísticas. No interactúa con la consola.
 */
public class GestorTickets {

    private final List<Ticket> tickets = new ArrayList<>();
    private int siguienteId = 1;

    /** Gestor vacío; la numeración empieza en 1. */
    public GestorTickets() {
    }

    /**
     * Gestor a partir de incidencias ya existentes (por ejemplo, cargadas de
     * archivo). La numeración continúa desde el mayor identificador + 1.
     *
     * @throws IllegalArgumentException si hay elementos nulos o identificadores repetidos
     */
    public GestorTickets(List<Ticket> iniciales) {
        Objects.requireNonNull(iniciales, "La lista inicial no puede ser nula.");
        Set<Integer> vistos = new HashSet<>();
        int mayor = 0;
        for (Ticket ticket : iniciales) {
            if (ticket == null) {
                throw new IllegalArgumentException("La lista contiene un ticket nulo.");
            }
            if (!vistos.add(ticket.getId())) {
                throw new IllegalArgumentException("Identificador repetido: " + ticket.getId());
            }
            mayor = Math.max(mayor, ticket.getId());
        }
        tickets.addAll(iniciales);
        siguienteId = mayor + 1;
    }

    /**
     * Crea una incidencia abierta con el siguiente identificador.
     * Si la descripción no es válida se lanza la excepción ANTES de añadir
     * nada y de consumir el identificador.
     */
    public Ticket crearTicket(String descripcion) {
        Ticket nuevo = new Ticket(siguienteId, descripcion);
        tickets.add(nuevo);
        siguienteId++;
        return nuevo;
    }

    public Optional<Ticket> buscarPorId(int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return Optional.of(ticket);
            }
        }
        return Optional.empty();
    }

    /** Copia de la lista interna: la lista es nueva, los objetos son los mismos. */
    public List<Ticket> listarTickets() {
        return new ArrayList<>(tickets);
    }

    public int contarTotal() {
        return tickets.size();
    }

    public int contarAbiertos() {
        int abiertos = 0;
        for (Ticket ticket : tickets) {
            if (!ticket.estaCerrado()) {
                abiertos++;
            }
        }
        return abiertos;
    }

    public int contarCerrados() {
        return contarTotal() - contarAbiertos();
    }
}
