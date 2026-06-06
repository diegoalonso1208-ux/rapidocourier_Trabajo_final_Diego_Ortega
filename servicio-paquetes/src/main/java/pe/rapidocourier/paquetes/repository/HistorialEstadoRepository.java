package pe.rapidocourier.paquetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.rapidocourier.paquetes.entity.HistorialEstado;
import pe.rapidocourier.paquetes.entity.Paquete;
import java.util.List;
import java.util.UUID;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, UUID> {
    List<HistorialEstado> findByPaqueteOrderByFechaCambioAsc(Paquete paquete);
}