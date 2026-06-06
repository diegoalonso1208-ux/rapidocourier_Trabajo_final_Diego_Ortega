package pe.rapidocourier.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.rapidocourier.clientes.entity.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    @Query(value = "SELECT * FROM clientes WHERE LOWER(nombre_completo) LIKE LOWER(CONCAT('%', :texto, '%'))", nativeQuery = true)
    List<Cliente> buscarPorNombre(@Param("texto") String texto);
}