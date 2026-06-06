package pe.rapidocourier.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.rapidocourier.auth.entity.Role;
import pe.rapidocourier.auth.entity.Usuario;
import pe.rapidocourier.auth.repository.RoleRepository;
import pe.rapidocourier.auth.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }

        crearUsuario("Admin Sistema", "admin@rapidocourier.pe", "Admin123!", Role.RoleName.ADMIN);
        crearUsuario("Operador Lima", "operador@rapidocourier.pe", "Operador123!", Role.RoleName.OPERADOR);
        crearUsuario("Cliente Demo", "cliente@rapidocourier.pe", "Cliente123!", Role.RoleName.CLIENTE);
    }

    private void crearUsuario(String nombre, String email, String password, Role.RoleName roleName) {
        if (!usuarioRepository.existsByEmail(email)) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setRole(role);
            usuarioRepository.save(usuario);
        }
    }
}