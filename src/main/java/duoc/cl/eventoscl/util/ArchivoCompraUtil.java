package duoc.cl.eventoscl.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import duoc.cl.eventoscl.entity.Compra;
import duoc.cl.eventoscl.entity.Evento;
import duoc.cl.eventoscl.exception.InvalidFileException;

@Component
public class ArchivoCompraUtil {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
     
    // Construye la key
    public String construirRutaS3(String numeroResumen, String nombreArchivo) {
            if (numeroResumen == null || numeroResumen.isBlank()) {
                throw new InvalidFileException("El numero de resumen es obligatorio");
            }
            if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new InvalidFileException("El nombre del archivo es obligatorio");
            }

            String carpeta = numeroResumen.trim().replaceAll("^/+|/+$", "");
            String archivoLimpio = nombreArchivo.trim().replaceAll("^/+", "");

            return carpeta + "/" + archivoLimpio;
    }

    public String generarNombreArchivo(String numeroResumen) {
        return generarNombreArchivo(numeroResumen, "txt");
    }

    // Genera el nombre del archivo físico para un resumen
    public String generarNombreArchivo(String numeroResumen, String extension) {
        if (numeroResumen == null || numeroResumen.isBlank()) {
            throw new InvalidFileException("El número de resumen es obligatorio");
        }
        String ext = (extension == null || extension.isBlank()) ? "txt" : extension.trim().replace(".", "").toLowerCase();
        return "resumen-" + numeroResumen.trim() + "." + ext;
    }

    // Genera los bytes del archivo resumen a partir de una compra para guardarlos en el archivo en el computador
    public byte[] generarContenidoFisico(String numeroResumen, Compra compra) {
        if (compra == null) {
            throw new InvalidFileException("La compra no puede ser nula");
        }
        String detalle = formatearDetalleCompra(compra);
        return generarContenidoDesdeTexto(numeroResumen, detalle);
    }
    // Formato archivo resumen compra
    public String formatearDetalleCompra(Compra compra) {
        Evento evento = compra.getEvento();
        String nombreEvento = (evento != null && evento.getNombre() != null && !evento.getNombre().isBlank())
                ? evento.getNombre()
                : "No especificado";
        String fechaEvento = (evento != null && evento.getFechaHora() != null)
                ? evento.getFechaHora().format(FORMATO_FECHA)
                : "No especificada";

        return """
                ID Compra: %s
                Evento: %s
                Fecha del evento: %s
                Cantidad de entradas: %d
                Precio unitario: $%s
                Precio total: $%s
                """.formatted(
                compra.getId(),
                nombreEvento,
                fechaEvento,
                compra.getCantidadEntradas(),
                compra.getPrecioUnitario(),
                compra.getPrecioTotal()
        ).strip();
    }

    // Construccion del archivo codificado a byte
    public byte[] generarContenidoDesdeTexto(String numeroResumen, String detalleCompra) {
        if (numeroResumen == null || numeroResumen.isBlank()) {
            throw new InvalidFileException("El número de resumen es obligatorio");
        }
        String fechaEmision = LocalDateTime.now().format(FORMATO_FECHA);

        return """
                ========================================
                        RESUMEN DE COMPRA N° %s
                ========================================
                Fecha de emisión: %s

                Detalle:
                %s
                ========================================
                """.formatted(
                numeroResumen.trim(),
                fechaEmision,
                (detalleCompra != null && !detalleCompra.isBlank()) ? detalleCompra : "Sin detalle registrado"
        ).getBytes(StandardCharsets.UTF_8);
    }

}
