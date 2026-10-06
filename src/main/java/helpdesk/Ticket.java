package helpdesk;

/**
 * Incidencia del centro. Protege su propia validez: no se puede construir con
 * un identificador no positivo ni con una descripción nula, vacía, en blanco o
 * de varias líneas. El identificador y la descripción son inmutables; lo único
 * que cambia es el estado, y solo mediante {@link #cerrar()}.
 */
public class Ticket {

    private final int id;
    private final String descripcion;
    private boolean cerrado;

    /** Crea un ticket nuevo, que comienza abierto. */
    public Ticket(int id, String descripcion) {
        this(id, descripcion, false);
    }

    /** Crea un ticket con un estado concreto (usado al recuperar datos guardados). */
    public Ticket(int id, String descripcion, boolean cerrado) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador debe ser positivo: " + id);
        }
        this.id = id;
        this.descripcion = validarDescripcion(descripcion);
        this.cerrado = cerrado;
    }

    private static String validarDescripcion(String descripcion) {
        if (descripcion == null) {
            throw new IllegalArgumentException("La descripción no puede ser nula.");
        }
        String limpia = descripcion.strip();
        if (limpia.isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía ni en blanco.");
        }
        if (limpia.indexOf('\n') >= 0 || limpia.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("La descripción debe ser de una sola línea.");
        }
        return limpia;
    }

    public int getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean estaCerrado() {
        return cerrado;
    }

    /**
     * Cierra el ticket.
     *
     * @return {@code true} si estaba abierto y ahora queda cerrado;
     *         {@code false} si ya estaba cerrado (no hay cambios).
     */
    public boolean cerrar() {
        if (cerrado) {
            return false;
        }
        cerrado = true;
        return true;
    }

    @Override
    public String toString() {
        return "#" + id + " | " + (cerrado ? "CERRADA" : "ABIERTA") + " | " + descripcion;
    }
}
