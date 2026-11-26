package duoc.cl.eventoscl.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import duoc.cl.eventoscl.dto.S3ObjectDto;
import duoc.cl.eventoscl.service.ResumenCompraService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class ResumenCompraController {

    @Autowired
    private ResumenCompraService resumenCompraService;

    // Lista los objetos generados en un bucket de AWS S3
    @GetMapping("/{bucket}/objects")
	public ResponseEntity<List<S3ObjectDto>> listObjects(@PathVariable String bucket) {

		List<S3ObjectDto> dtoList = resumenCompraService.listObjects(bucket);
		return ResponseEntity.ok(dtoList);
	}

    // Sube el resumen de compra generado a un bucket de AWS S3
    @PostMapping("/{bucket}/object")
	public ResponseEntity<Void> uploadObject(@PathVariable String bucket, @RequestParam String key,
			@RequestParam("file") MultipartFile file) {

		resumenCompraService.upload(bucket, key, file);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

    // Mueve un objeto dentro del bucket
    @PostMapping("/{bucket}/move")
	public ResponseEntity<Void> moveObject(@PathVariable String bucket, @RequestParam String sourceKey,
			@RequestParam String destKey) {

		resumenCompraService.moveObject(bucket, sourceKey, destKey);
		return ResponseEntity.ok().build();
	}

    // Permite modificar el archivo del resumen de compra

    // Permite descargar el archivo del resumen de compra
    @GetMapping("/{bucket}/object")
	public ResponseEntity<byte[]> downloadObject(@PathVariable String bucket, @RequestParam String key) {

		byte[] fileBytes = resumenCompraService.downloadAsBytes(bucket, key);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(fileBytes);
	}

    // Permite borrar el archivo del resumen de compra
    @DeleteMapping("/{bucket}/object")
	public ResponseEntity<Void> deleteObject(@PathVariable String bucket, @RequestParam String key) {

		resumenCompraService.deleteObject(bucket, key);
		return ResponseEntity.noContent().build();
	}

}
