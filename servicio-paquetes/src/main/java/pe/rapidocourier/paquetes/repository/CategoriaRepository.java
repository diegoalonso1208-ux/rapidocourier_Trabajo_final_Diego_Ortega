package pe.rapidocourier.paquetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.rapidocourier.paquetes.entity.Categoria;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    Optional<Categoria> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}