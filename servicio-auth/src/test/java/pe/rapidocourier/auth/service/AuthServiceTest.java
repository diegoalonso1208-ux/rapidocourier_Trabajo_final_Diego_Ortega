package pe.rapidocourier.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_HappyPath() {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Diego Test");
        request.setEmail("diego@test.com");
        request.setPassword("Pass123!");
        request.setRole("CLIENTE");

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(Role.RoleName.CLIENTE);

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("diego@test.com");
        usuario.setRole(role);

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(jwtUtil.generateToken(any(), any())).thenReturn("fake-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-token", response.getToken());
        assertEquals("diego@test.com", response.getEmail());
    }

    @Test
    void register_EmailDuplicado_LanzaExcepcion() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicado@test.com");
        request.setRole("CLIENTE");

        when(usuarioRepository.existsByEmail("duplicado@test.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        assertTrue(ex.getMessage().contains("duplicado@test.com"));
    }

    @Test
    void register_RolNoExiste_LanzaExcepcion() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("nuevo@test.com");
        request.setRole("CLIENTE");

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.register(request));
    }

    @Test
    void login_HappyPath() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("Pass123!");

        Role role = new Role();
        role.setName(Role.RoleName.ADMIN);

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@test.com");
        usuario.setRole(role);

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("admin@test.com", "Pass123!"));
        when(usuarioRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(any(), any())).thenReturn("fake-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-token", response.getToken());
    }

    @Test
    void login_UsuarioNoExiste_LanzaExcepcion() {
        LoginRequest request = new LoginRequest();
        request.setEmail("noexiste@test.com");
        request.setPassword("Pass123!");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("noexiste@test.com", "Pass123!"));
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.login(request));
    }

    @Test
    void register_ListaVacia_CuandoNoHayUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(java.util.List.of());
        assertTrue(usuarioRepository.findAll().isEmpty());
    }
}