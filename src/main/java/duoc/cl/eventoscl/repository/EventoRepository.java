package duoc.cl.eventoscl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import duoc.cl.eventoscl.entity.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long>{

}
