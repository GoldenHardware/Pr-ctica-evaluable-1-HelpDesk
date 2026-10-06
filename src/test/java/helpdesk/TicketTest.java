package helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void ticketNuevoComienzaAbierto() {
        Ticket ticket = new Ticket(1, "Falla el teclado");

        assertFalse(ticket.estaCerrado());
        assertEquals(1, ticket.getId());
        assertEquals("Falla el teclado", ticket.getDescripcion());
    }

    @Test
    void cerrarCambiaElEstadoYIndicaQueHuboCambio() {
        Ticket ticket = new Ticket(1, "Falla el teclado");

        assertTrue(ticket.cerrar());
        assertTrue(ticket.estaCerrado());
    }

    @Test
    void cerrarDosVecesNoHaceNadaLaSegundaVez() {
        Ticket ticket = new Ticket(1, "Falla el teclado");
        ticket.cerrar();

        assertFalse(ticket.cerrar());
        assertTrue(ticket.estaCerrado());
    }

    @Test
    void rechazaDescripcionEnBlanco() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(1, ""));
        assertThrows(IllegalArgumentException.class, () -> new Ticket(1, "     "));
        assertThrows(IllegalArgumentException.class, () -> new Ticket(1, "\t "));
    }

    @Test
    void rechazaDescripcionNula() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(1, null));
    }

    @Test
    void rechazaIdentificadorCero() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(0, "Descripción válida"));
    }

    @Test
    void rechazaIdentificadorNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(-5, "Descripción válida"));
    }

    @Test
    void rechazaDescripcionDeVariasLineas() {
        assertThrows(IllegalArgumentException.class, () -> new Ticket(1, "linea1\nlinea2"));
    }

    @Test
    void eliminaEspaciosSobrantesYAceptaPuntoYComa() {
        Ticket ticket = new Ticket(2, "  Proyector; no enciende  ");

        assertEquals("Proyector; no enciende", ticket.getDescripcion());
    }

    @Test
    void permiteRecuperarUnTicketYaCerrado() {
        Ticket ticket = new Ticket(7, "Sin red", true);

        assertTrue(ticket.estaCerrado());
        assertFalse(ticket.cerrar());
    }
}
