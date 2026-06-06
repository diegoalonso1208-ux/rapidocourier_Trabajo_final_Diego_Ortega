package pe.rapidocourier.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.rapidocourier.auth.entity.Role;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(Role.RoleName name);
}