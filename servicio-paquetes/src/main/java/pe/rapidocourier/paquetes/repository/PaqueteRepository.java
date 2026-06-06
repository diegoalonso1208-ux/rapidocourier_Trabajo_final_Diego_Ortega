package pe.rapidocourier.paquetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.rapidocourier.paquetes.entity.EstadoPaquete;
import pe.rapidocourier.paquetes.entity.Paquete;
import pe.rapidocourier.paquetes.entity.Sucursal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaqueteRepository extends JpaRepository<Paquete, UUID> {

    Optional<Paquete> findByCodigoRastreo(String codigoRastreo);

    @Query(value = """
        SELECT * FROM paquetes 
        WHERE LOWER(codigo_rastreo) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(remitente_nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(destinatario_nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
        """, nativeQuery = true)
    List<Paquete> buscarPorTexto(@Param("texto") String texto);

    @Query(value = """
        SELECT * FROM paquetes 
        WHERE (sucursal_origen = :sucursal OR sucursal_destino = :sucursal)
        AND (:estado IS NULL OR estado = :estado)
        """, nativeQuery = true)
    List<Paquete> filtrarPorSucursalYEstado(
            @Param("sucursal") String sucursal,
            @Param("estado") String estado
    );

    @Query(value = """
        SELECT p.* FROM paquetes p
        JOIN paquete_categorias pc ON p.id = pc.paquete_id
        JOIN categorias c ON pc.categoria_id = c.id
        WHERE c.nombre = :categoria
        """, nativeQuery = true)
    List<Paquete> findByCategoria(@Param("categoria") String categoria);

    List<Paquete> findByRemitenteId(UUID remitenteId);
    List<Paquete> findByDestinatarioId(UUID destinatarioId);
}