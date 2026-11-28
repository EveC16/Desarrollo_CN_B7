package duoc.cl.eventoscl.service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import duoc.cl.eventoscl.dto.S3ObjectDto;
import duoc.cl.eventoscl.entity.Compra;
import duoc.cl.eventoscl.exception.InvalidFileException;
import duoc.cl.eventoscl.exception.S3AccessDeniedException;
import duoc.cl.eventoscl.exception.S3BucketNotFoundException;
import duoc.cl.eventoscl.exception.S3ObjectNotFoundException;
import duoc.cl.eventoscl.exception.S3OperationException;
import duoc.cl.eventoscl.exception.S3UploadException;
import duoc.cl.eventoscl.repository.CompraRepository;
import duoc.cl.eventoscl.util.ArchivoCompraUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumenCompraService {

    private final S3Client s3Client;
	private final CompraRepository compraRepository;
	private final ArchivoCompraUtil archivoCompraUtil;
	private final ArchivoCompraLocalService archivoCompraLocalService;

	@Value("${aws.s3.bucket-name}")
    private String defaultBucket;

    private String resolverBucket(String bucket) {
        return (bucket == null || bucket.isBlank()) ? defaultBucket : bucket.trim();
    }

    private Compra buscarCompra(Long compraId) {
        if (compraId == null || compraId <= 0) {
            throw new InvalidFileException("El ID de compra debe ser mayor a cero");
        }
        return compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la compra con ID: " + compraId));
    }

	// Genera el archivo segun la compra
    public Path generarArchivoFisicoPorCompraId(Long compraId) {
        Compra compra = buscarCompra(compraId);
        String numeroResumen = String.valueOf(compra.getId());
        String nombreArchivo = archivoCompraUtil.generarNombreArchivo(numeroResumen);
        byte[] contenido = archivoCompraUtil.generarContenidoFisico(numeroResumen, compra);
        return archivoCompraLocalService.guardarArchivoFisico(nombreArchivo, contenido);
    }

	// Sube a S3 los objetos
    public String subirResumenAS3(String bucket, Long compraId) {
        String targetBucket = resolverBucket(bucket);
        Path archivoLocal = generarArchivoFisicoPorCompraId(compraId);
        Compra compra = buscarCompra(compraId);
        String numeroResumen = String.valueOf(compra.getId());
        String key = archivoCompraUtil.construirRutaS3(numeroResumen, archivoLocal.getFileName().toString());

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .contentType("text/plain; charset=UTF-8")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(archivoLocal));
            log.info("Resumen subido a S3 en bucket {} con key {}", targetBucket, key);
            return key;
        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(targetBucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("subir resumen al bucket: " + targetBucket, e);
            }
            throw new S3UploadException("Error al subir el resumen a S3: " + e.getMessage(), e);
        }
    }

	// Modifica los resumenes de compra
    public String modificarResumenEnS3(String bucket, String numeroResumen, String nuevoDetalle) {
        if (numeroResumen == null || numeroResumen.isBlank()) {
            throw new InvalidFileException("El número de resumen es obligatorio");
        }
        String targetBucket = resolverBucket(bucket);
        String nombreArchivo = archivoCompraUtil.generarNombreArchivo(numeroResumen);
        String key = archivoCompraUtil.construirRutaS3(numeroResumen, nombreArchivo);
        byte[] contenidoActualizado = archivoCompraUtil.generarContenidoDesdeTexto(numeroResumen, nuevoDetalle);

        archivoCompraLocalService.guardarArchivoFisico(nombreArchivo, contenidoActualizado);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .contentType("text/plain; charset=UTF-8")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(contenidoActualizado));
            log.info("Resumen modificado en S3 en bucket {} con key {}", targetBucket, key);
            return key;
        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(targetBucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("modificar resumen en el bucket: " + targetBucket, e);
            }
            throw new S3OperationException("Error al modificar el resumen en S3: " + key, e);
        }
    }

	// Descarga los resumenes de compra
    public byte[] descargarResumenDesdeS3(String bucket, String numeroResumen) {
        if (numeroResumen == null || numeroResumen.isBlank()) {
            throw new InvalidFileException("El número de resumen es obligatorio");
        }
        String targetBucket = resolverBucket(bucket);
        String nombreArchivo = archivoCompraUtil.generarNombreArchivo(numeroResumen);
        String key = archivoCompraUtil.construirRutaS3(numeroResumen, nombreArchivo);

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return responseBytes.asByteArray();
        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(targetBucket, e);
        } catch (NoSuchKeyException e) {
            throw new S3ObjectNotFoundException(key, targetBucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("descargar resumen del bucket: " + targetBucket, e);
            }
            throw new S3OperationException("Error al descargar el resumen: " + key, e);
        }
    }

	// Elimina un resumen de compra
    public String borrarResumenEnS3(String bucket, String numeroResumen) {
        if (numeroResumen == null || numeroResumen.isBlank()) {
            throw new InvalidFileException("El número de resumen es obligatorio");
        }
        String targetBucket = resolverBucket(bucket);
        String nombreArchivo = archivoCompraUtil.generarNombreArchivo(numeroResumen);
        String key = archivoCompraUtil.construirRutaS3(numeroResumen, nombreArchivo);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Resumen eliminado de S3 en bucket {} con key {}", targetBucket, key);
            return key;
        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(targetBucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("eliminar resumen del bucket: " + targetBucket, e);
            }
            throw new S3OperationException("Error al eliminar el resumen: " + key, e);
        }
    }

	// Lista los resumenes de compra
    public List<S3ObjectDto> listObjects(String bucket) {
        String targetBucket = resolverBucket(bucket);
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(targetBucket).build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);

            return response.contents().stream()
                    .map(obj -> new S3ObjectDto(
                            obj.key(),
                            obj.size(),
                            obj.lastModified() != null ? obj.lastModified().toString() : null))
                    .collect(Collectors.toList());
        } catch (NoSuchBucketException e) {
            throw new S3BucketNotFoundException(targetBucket, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 403) {
                throw new S3AccessDeniedException("listar objetos del bucket: " + targetBucket, e);
            }
            throw new S3OperationException("Error al listar objetos del bucket: " + targetBucket, e);
        }
    }
}
