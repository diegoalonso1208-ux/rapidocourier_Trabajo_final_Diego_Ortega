package pe.rapidocourier.paquetes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "paquetes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String codigoRastreo;

    private String descripcion;

    @Column(nullable = false)
    private Double pesoKg;

    @Column(nullable = false)
    private BigDecimal valorDeclarado;

    @Column(nullable = false)
    private BigDecimal tarifa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sucursal sucursalOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sucursal sucursalDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPaquete estado = EstadoPaquete.REGISTRADO;

    @Column(nullable = false)
    private UUID remitenteId;

    @Column(nullable = false)
    private UUID destinatarioId;

    private String remitenteNombre;
    private String destinatarioNombre;

    @ManyToMany
    @JoinTable(
            name = "paquete_categorias",
            joinColumns = @JoinColumn(name = "paquete_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}