package helpdesk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lee y escribe las incidencias. Formato, una por línea: {@code id;cerrado;descripcion}.
 * La descripción puede contener ';' porque se separa en 3 partes como máximo.
 * La carga es "todo o nada": ante cualquier dato inválido lanza una excepción
 * y no devuelve resultados parciales. Cargar nunca modifica el archivo.
 */
public class ArchivoTickets {

    /** Error al leer o escribir el archivo de incidencias. */
    public static class ArchivoTicketsException extends Exception {

        public ArchivoTicketsException(String mensaje) {
            super(mensaje);
        }

        public ArchivoTicketsException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    private static final String SEPARADOR = ";";

    private final Path ruta;

    public ArchivoTickets(Path ruta) {
        this.ruta = ruta;
    }

    /**
     * @return las incidencias guardadas, o una lista vacía si el archivo no existe
     * @throws ArchivoTicketsException si no se puede leer o contiene datos inválidos
     */
    public List<Ticket> cargar() throws ArchivoTicketsException {
        List<Ticket> resultado = new ArrayList<>();
        if (!Files.exists(ruta)) {
            return resultado;
        }

        List<String> lineas;
        try {
            lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ArchivoTicketsException("No se pudo leer " + ruta + ": " + e.getMessage(), e);
        }

        Set<Integer> idsVistos = new HashSet<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i);
            if (linea.isBlank()) {
                continue; // se toleran líneas totalmente vacías (p. ej. al final)
            }
            Ticket ticket = interpretarLinea(linea, i + 1);
            if (!idsVistos.add(ticket.getId())) {
                throw errorLinea(i + 1, "identificador repetido (" + ticket.getId() + ")");
            }
            resultado.add(ticket);
        }
        return resultado;
    }

    private Ticket interpretarLinea(String linea, int numeroLinea) throws ArchivoTicketsException {
        String[] partes = linea.split(SEPARADOR, 3);
        if (partes.length != 3) {
            throw errorLinea(numeroLinea, "se esperaban 3 campos (id;cerrado;descripcion)");
        }

        int id;
        try {
            id = Integer.parseInt(partes[0].trim());
        } catch (NumberFormatException e) {
            throw errorLinea(numeroLinea, "identificador no numérico (" + partes[0] + ")");
        }

        String estado = partes[1].trim();
        boolean cerrado;
        if (estado.equals("true")) {
            cerrado = true;
        } else if (estado.equals("false")) {
            cerrado = false;
        } else {
            throw errorLinea(numeroLinea, "estado inválido (" + partes[1] + "); debe ser true o false");
        }

        try {
            return new Ticket(id, partes[2], cerrado);
        } catch (IllegalArgumentException e) {
            throw errorLinea(numeroLinea, e.getMessage());
        }
    }

    private ArchivoTicketsException errorLinea(int numeroLinea, String motivo) {
        return new ArchivoTicketsException("Datos inválidos en " + ruta + ", línea " + numeroLinea + ": " + motivo);
    }

    /**
     * Sustituye el contenido del archivo por la colección completa. Escribe
     * primero en un archivo temporal y lo mueve al final, de modo que un fallo
     * a mitad de escritura no destruye el archivo anterior.
     */
    public void guardar(List<Ticket> tickets) throws ArchivoTicketsException {
        List<String> lineas = new ArrayList<>();
        for (Ticket t : tickets) {
            lineas.add(t.getId() + SEPARADOR + t.estaCerrado() + SEPARADOR + t.getDescripcion());
        }
        Path temporal = ruta.resolveSibling(ruta.getFileName() + ".tmp");
        try {
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, ruta, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException ignorada) {
                // nada más que hacer
            }
            throw new ArchivoTicketsException("No se pudo guardar en " + ruta + ": " + e.getMessage(), e);
        }
    }
}