package duoc.cl.eventoscl.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import duoc.cl.eventoscl.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ArchivoCompraLocalService {

    private final Path directorioBase;

    public ArchivoCompraLocalService(
        @Value("${app.resumen.directorio-local}") String directorioLocal) {
        if (directorioLocal == null || directorioLocal.isBlank()) {
            throw new InvalidFileException("La ruta local para resúmenes no está configurada");
        }
        this.directorioBase = Paths.get(directorioLocal.trim()).toAbsolutePath().normalize();
    }

    public Path guardarArchivoFisico(String nombreArchivo, byte[] contenido) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new InvalidFileException("El nombre del archivo físico es obligatorio");
        }
        if (contenido == null || contenido.length == 0) {
            throw new InvalidFileException("El contenido del archivo físico está vacío");
        }

        try {
            Files.createDirectories(directorioBase);
            Path archivoDestino = directorioBase.resolve(nombreArchivo).normalize();

            if (!archivoDestino.startsWith(directorioBase)) {
                throw new InvalidFileException("Ruta de archivo no permitida: " + nombreArchivo);
            }

            Files.write(
                    archivoDestino,
                    contenido,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            log.info("Archivo físico guardado en: {}", archivoDestino);
            return archivoDestino;
        } catch (IOException e) {
            throw new InvalidFileException("Error al guardar el archivo físico en disco: " + e.getMessage());
        }
    }
}
