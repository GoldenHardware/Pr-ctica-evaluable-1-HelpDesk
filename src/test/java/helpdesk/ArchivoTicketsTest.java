package helpdesk;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Ampliación opcional: automatiza la comprobación de la persistencia. */
class ArchivoTicketsTest {

    @TempDir
    Path directorio;

    @Test
    void archivoInexistenteDaColeccionVacia() throws Exception {
        ArchivoTickets archivo = new ArchivoTickets(directorio.resolve("no_existe.txt"));

        assertTrue(archivo.cargar().isEmpty());
    }

    @Test
    void guardarYCargarConservaIdsEstadosYDescripciones() throws Exception {
        ArchivoTickets archivo = new ArchivoTickets(directorio.resolve("tickets.txt"));
        Ticket abierto = new Ticket(1, "Falla el teclado");
        Ticket cerrado = new Ticket(2, "Proyector; no enciende", true);

        archivo.guardar(List.of(abierto, cerrado));
        List<Ticket> cargados = archivo.cargar();

        assertEquals(2, cargados.size());
        assertEquals(1, cargados.get(0).getId());
        assertFalse(cargados.get(0).estaCerrado());
        assertEquals("Falla el teclado", cargados.get(0).getDescripcion());
        assertEquals(2, cargados.get(1).getId());
        assertTrue(cargados.get(1).estaCerrado());
        assertEquals("Proyector; no enciende", cargados.get(1).getDescripcion());
    }

    @Test
    void guardarSustituyeElContenidoAnterior() throws Exception {
        Path ruta = directorio.resolve("tickets.txt");
        ArchivoTickets archivo = new ArchivoTickets(ruta);
        archivo.guardar(List.of(new Ticket(1, "Vieja"), new Ticket(2, "Otra vieja")));

        archivo.guardar(List.of(new Ticket(5, "Nueva")));

        assertEquals(List.of("5;false;Nueva"), Files.readAllLines(ruta, StandardCharsets.UTF_8));
    }

    @Test
    void despuesDeCargarLaNumeracionContinuaDesdeElMayor() throws Exception {
        Path ruta = directorio.resolve("tickets.txt");
        Files.writeString(ruta, "1;false;Falla el teclado\n2;true;Sin conexión a Internet\n");

        GestorTickets gestor = new GestorTickets(new ArchivoTickets(ruta).cargar());

        assertEquals(3, gestor.crearTicket("Nueva").getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1;false;Correcta\nabc;false;Id no numérico",
            "1;false;Correcta\n0;false;Id cero",
            "1;false;Correcta\n2;quizas;Estado raro",
            "1;false;Correcta\n2;false;   ",
            "1;false;Correcta\n2;false",
            "1;false;Correcta\n1;true;Id repetido"
    })
    void datosInvalidosDetienenLaCargaSinResultadoParcialNiModificarElArchivo(String contenido)
            throws IOException {
        Path ruta = directorio.resolve("tickets.txt");
        Files.writeString(ruta, contenido);
        ArchivoTickets archivo = new ArchivoTickets(ruta);

        assertThrows(ArchivoTicketsException.class, archivo::cargar);
        assertEquals(contenido, Files.readString(ruta), "El archivo no debe modificarse");
    }
}
