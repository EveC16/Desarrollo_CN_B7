package duoc.cl.eventoscl.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import duoc.cl.eventoscl.entity.Evento;
import duoc.cl.eventoscl.repository.EventoRepository;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    // Lista de eventos disponibles
    public List<Evento> listar(){
        return eventoRepository.findAll();
    }

    // Guardar nuevos eventos
    public Evento guardar(Evento evento){
        return eventoRepository.save(evento);
    }

    // Buscar evento por ID
    public Optional<Evento> buscarPorId(Long id){
        return eventoRepository.findById(id);
    }

}
