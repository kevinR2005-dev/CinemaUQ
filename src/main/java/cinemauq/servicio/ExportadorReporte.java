package cinemauq.servicio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportadorReporte {

    public void exportar(String contenido, Path ruta) {
        try {
            Path carpeta = ruta.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta); // crea las carpetas si faltan
            }
            Files.writeString(ruta, contenido, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}