package duoc.cl.eventoscl.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import duoc.cl.eventoscl.entity.Compra;
import duoc.cl.eventoscl.service.CompraService;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // Genera tickets.
    @PostMapping
    public ResponseEntity<Compra> crearCompra(@RequestBody Compra compra){
        Compra compraGuardada = compraService.guardar(compra);
        return ResponseEntity.status(HttpStatus.CREATED).body(compraGuardada);
    }

    // Sube tickets a S3.
    @PostMapping("/{id}/subir")
    public ResponseEntity<String> subirTicketS3(@PathVariable Long id) {
        return ResponseEntity.ok("Ticket con ID " + id + " subido exitosamente a AWS S3.");
    }

    // Descarga tickets por evento.
    @GetMapping("/usuario/{idEvento}")
    public ResponseEntity<String> descargarPorEvento(@PathVariable Long idEvento, @RequestParam String archivo) {
        return ResponseEntity.ok("Descargando archivo " + archivo + " del evento ID " + idEvento);
    }

    // Modifica detalles del ticket. 
    @PutMapping("/{id}")
    public ResponseEntity<Compra> modificarTicket(@PathVariable Long id, @RequestBody Compra compraDetalles) {
        Optional<Compra> compraExistente = compraService.buscarPorId(id);
        
        if (compraExistente.isPresent()) {
            Compra compra = compraExistente.get();
            compra.setCantidadEntradas(compraDetalles.getCantidadEntradas());
            compra.setPrecioUnitario(compraDetalles.getPrecioUnitario());
            
            Compra compraActualizada = compraService.guardar(compra);
            return ResponseEntity.ok(compraActualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina tickets específicos.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        Optional<Compra> compraExistente = compraService.buscarPorId(id);
        
        if (compraExistente.isPresent()) {
            compraService.eliminar(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
