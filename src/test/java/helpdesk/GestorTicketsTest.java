package helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class GestorTicketsTest {

    @Test
    void coleccionInicialmenteVacia() {
        GestorTickets gestor = new GestorTickets();

        assertTrue(gestor.listarTickets().isEmpty());
        assertEquals(0, gestor.contarTotal());
    }

    @Test
    void identificadoresConsecutivosDesdeUno() {
        GestorTickets gestor = new GestorTickets();

        assertEquals(1, gestor.crearTicket("Uno").getId());
        assertEquals(2, gestor.crearTicket("Dos").getId());
        assertEquals(3, gestor.crearTicket("Tres").getId());
    }

    @Test
    void buscarDevuelveElMismoObjeto() {
        GestorTickets gestor = new GestorTickets();
        Ticket creado = gestor.crearTicket("Falla el teclado");

        assertTrue(gestor.buscarPorId(1).isPresent());
        assertSame(creado, gestor.buscarPorId(1).get());
    }

    @Test
    void buscarInexistenteDevuelveVacio() {
        GestorTickets gestor = new GestorTickets();
        gestor.crearTicket("Uno");

        assertTrue(gestor.buscarPorId(99).isEmpty());
    }

    @Test
    void creacionInvalidaNoAlteraColeccionNiContador() {
        GestorTickets gestor = new GestorTickets();
        gestor.crearTicket("Primera");

        assertThrows(IllegalArgumentException.class, () -> gestor.crearTicket("   "));
        assertThrows(IllegalArgumentException.class, () -> gestor.crearTicket(null));

        assertEquals(1, gestor.listarTickets().size());
        Ticket siguiente = gestor.crearTicket("Segunda");
        assertEquals(2, siguiente.getId(), "El fallo no debe consumir identificadores");
    }

    @Test
    void modificarLaListaDevueltaNoAfectaAlGestor() {
        GestorTickets gestor = new GestorTickets();
        gestor.crearTicket("Uno");
        gestor.crearTicket("Dos");

        List<Ticket> copia = gestor.listarTickets();
        copia.clear();

        assertEquals(2, gestor.listarTickets().size());
    }

    @Test
    void laCopiaConservaLosMismosObjetos() {
        GestorTickets gestor = new GestorTickets();
        Ticket creado = gestor.crearTicket("Uno");

        List<Ticket> lista = gestor.listarTickets();

        assertNotSame(lista, gestor.listarTickets());
        assertSame(creado, lista.get(0));
    }

    @Test
    void alConstruirDesdeListaContinuaDesdeElMayorMasUno() {
        GestorTickets gestor = new GestorTickets(List.of(
                new Ticket(2, "Dos"),
                new Ticket(7, "Siete", true),
                new Ticket(4, "Cuatro")));

        assertEquals(3, gestor.contarTotal());
        assertEquals(8, gestor.crearTicket("Nueva").getId());
    }

    @Test
    void rechazaIdentificadoresRepetidosAlConstruir() {
        List<Ticket> repetidos = List.of(new Ticket(1, "A"), new Ticket(1, "B"));

        assertThrows(IllegalArgumentException.class, () -> new GestorTickets(repetidos));
    }

    @Test
    void desdeListaVaciaLaNumeracionEmpiezaEnUno() {
        GestorTickets gestor = new GestorTickets(List.of());

        assertEquals(1, gestor.crearTicket("Primera").getId());
    }
}
