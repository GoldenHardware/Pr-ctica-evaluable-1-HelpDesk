package helpdesk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstadisticasTest {

    @Test
    void gestorVacioTodoACero() {
        GestorTickets gestor = new GestorTickets();

        assertEquals(0, gestor.contarTotal());
        assertEquals(0, gestor.contarAbiertos());
        assertEquals(0, gestor.contarCerrados());
    }

    @Test
    void dosIncidenciasAbiertas() {
        GestorTickets gestor = new GestorTickets();
        gestor.crearTicket("Uno");
        gestor.crearTicket("Dos");

        assertEquals(2, gestor.contarTotal());
        assertEquals(2, gestor.contarAbiertos());
        assertEquals(0, gestor.contarCerrados());
    }

    @Test
    void dosIncidenciasUnaCerrada() {
        GestorTickets gestor = new GestorTickets();
        gestor.crearTicket("Uno");
        gestor.crearTicket("Dos").cerrar();

        assertEquals(2, gestor.contarTotal());
        assertEquals(1, gestor.contarAbiertos());
        assertEquals(1, gestor.contarCerrados());
    }

    @Test
    void cerrarDosVecesNoCuentaDosCerradas() {
        GestorTickets gestor = new GestorTickets();
        Ticket ticket = gestor.crearTicket("Uno");
        gestor.crearTicket("Dos");

        ticket.cerrar();
        ticket.cerrar();

        assertEquals(1, gestor.contarCerrados());
        assertEquals(1, gestor.contarAbiertos());
    }
}
