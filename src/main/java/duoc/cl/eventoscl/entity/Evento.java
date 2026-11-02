package duoc.cl.eventoscl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="EVENTOS")
public class Evento {

    public Evento() {
    }

    public Evento(String nombre, LocalDateTime fechaHora, BigDecimal precioEntrada) {
        this.nombre = nombre;
        this.fechaHora = fechaHora;
        this.precioEntrada = precioEntrada;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Para que se autogenere e incremente el Id 
    private Long id;
    
    @Column(nullable = false, length = 50) 
    private String nombre;

    @Column(nullable = false) 
    private LocalDateTime fechaHora;

    @Column(nullable = false) 
    private BigDecimal precioEntrada;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getPrecioEntrada() {
        return precioEntrada;
    }

    public void setPrecioEntrada(BigDecimal precioEntrada) {
        this.precioEntrada = precioEntrada;
    }
}
