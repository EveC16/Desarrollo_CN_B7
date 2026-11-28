package duoc.cl.eventoscl.controller;

import java.nio.file.Path;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import duoc.cl.eventoscl.dto.S3ObjectDto;
import duoc.cl.eventoscl.service.ResumenCompraService;
import duoc.cl.eventoscl.util.ArchivoCompraUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class ResumenCompraController {

    private final ResumenCompraService resumenCompraService;
	private final ArchivoCompraUtil archivoCompraUtil;

    // Lista los objetos generados en un bucket de AWS S3
    @GetMapping("/{bucket}/objects")
	public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {
		return ResponseEntity.ok(resumenCompraService.listObjects(bucket));
    }

	// Genera el archivo físico .txt en el computador a partir del ID de compra
    @PostMapping("/resumenes/{compraId}/archivo-fisico")
    public ResponseEntity<String> generarArchivoFisico(@PathVariable Long compraId) {
        Path rutaGenerada = resumenCompraService.generarArchivoFisicoPorCompraId(compraId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Archivo físico generado en: " + rutaGenerada);
    }

    // Sube el resumen de compra generado a un bucket de AWS S3
    @PostMapping("/{bucket}/resumenes/{compraId}")
	public ResponseEntity<String> subirResumen(
            @PathVariable String bucket,
            @PathVariable Long compraId) {
        String key = resumenCompraService.subirResumenAS3(bucket, compraId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Resumen subido a S3 con key: " + key);
    }

	// Modifica el resumen en S3
    @PutMapping("/{bucket}/resumenes/{numeroResumen}")
    public ResponseEntity<String> modificarResumen(
            @PathVariable String bucket,
            @PathVariable String numeroResumen,
            @RequestBody String nuevoDetalle) {
        String key = resumenCompraService.modificarResumenEnS3(bucket, numeroResumen, nuevoDetalle);
        return ResponseEntity.ok("Resumen modificado en S3 con key: " + key);
    }

	// Descarga el archivo del resumen desde S3
    @GetMapping("/{bucket}/resumenes/{numeroResumen}/descargar")
    public ResponseEntity<byte[]> descargarResumen(
            @PathVariable String bucket,
            @PathVariable String numeroResumen) {
        byte[] fileBytes = resumenCompraService.descargarResumenDesdeS3(bucket, numeroResumen);
        String nombreArchivo = archivoCompraUtil.generarNombreArchivo(numeroResumen);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(fileBytes);
    }

    // Permite borrar el archivo del resumen de compra
	@DeleteMapping("/{bucket}/resumenes/{numeroResumen}")
    public ResponseEntity<String> borrarResumen(
            @PathVariable String bucket,
            @PathVariable String numeroResumen) {
        String key = resumenCompraService.borrarResumenEnS3(bucket, numeroResumen);
        return ResponseEntity.ok("Resumen eliminado de S3 con key: " + key);
    }

}
