package pe.rapidocourier.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.rapidocourier.auth.dto.request.LoginRequest;
import pe.rapidocourier.auth.dto.request.RegisterRequest;
import pe.rapidocourier.auth.dto.response.AuthResponse;
import pe.rapidocourier.auth.entity.Role;
import pe.rapidocourier.auth.entity.Usuario;
import pe.rapidocourier.auth.exception.DuplicateResourceException;
import pe.rapidocourier.auth.exception.ResourceNotFoundException;
import pe.rapidocourier.auth.repository.RoleRepository;
import pe.rapidocourier.auth.repository.UsuarioRepository;
import pe.rapidocourier.auth.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "El email ya está registrado: " + request.getEmail());
        }

        Role.RoleName roleName = Role.RoleName.valueOf(request.getRole());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rol no encontrado: " + request.getRole()));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(role);

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getEmail(),
                usuario.getRole().getName().name());

        return new AuthResponse(token, usuario.getEmail(),
                usuario.getRole().getName().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"));

        String token = jwtUtil.generateToken(usuario.getEmail(),
                usuario.getRole().getName().name());

        return new AuthResponse(token, usuario.getEmail(),
                usuario.getRole().getName().name());
    }
}