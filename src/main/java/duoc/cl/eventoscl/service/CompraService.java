package duoc.cl.eventoscl.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import duoc.cl.eventoscl.entity.Compra;
import duoc.cl.eventoscl.entity.Evento;
import duoc.cl.eventoscl.repository.CompraRepository;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final EventoService eventoService;

    public CompraService(CompraRepository compraRepository, EventoService eventoService) {
        this.compraRepository = compraRepository;
        this.eventoService = eventoService;
    }
    
    // Guardar nuevas compras
    public Compra guardar(Compra compra){

    Long eventoId = compra.getEvento().getId();
    Evento evento = eventoService.buscarPorId(eventoId)
            .orElseThrow(() -> new RuntimeException("Id de Evento: " + eventoId + "no encontrado. "));
            
    compra.setEvento(evento);

    BigDecimal precioUnitario = evento.getPrecioEntrada();
    compra.setPrecioUnitario(precioUnitario);

    // Calculo del precio total de entradas
    BigDecimal precioTotal = precioUnitario.multiply(BigDecimal.valueOf(compra.getCantidadEntradas()));
    compra.setPrecioTotal(precioTotal);

    // Guarda la compra en la base de datos
        return compraRepository.save(compra);
    }

    // Modificar archivo de resumen de compra
    public Optional<Compra> actualizarCompra(Long id, Compra compraAcrualizada){
        Optional<Compra> encontrada = compraRepository.findById(id);
        if(encontrada.isEmpty()){
            return Optional.empty();
        }
        
        Compra compra = encontrada.get();
        compra.setEvento(compraAcrualizada.getEvento());
        compra.setCantidadEntradas(compraAcrualizada.getCantidadEntradas());
        
        return Optional.of(compraRepository.save(compra));

    }

    // Eliminar archivo de resumen de compra
    public boolean eliminar(Long id){
        if(!compraRepository.existsById(id)){
            return false;
        }
        compraRepository.deleteById(id);
        return true;
    }
}
