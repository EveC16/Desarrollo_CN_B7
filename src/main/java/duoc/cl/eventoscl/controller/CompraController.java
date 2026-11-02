package duoc.cl.eventoscl.controller;

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

    @PostMapping
    public ResponseEntity<Compra> crearCompra(@RequestBody Compra compra){
        Compra compraGuardada = compraService.guardar(compra);
        return ResponseEntity.status(HttpStatus.CREATED).body(compraGuardada);
    }
}
