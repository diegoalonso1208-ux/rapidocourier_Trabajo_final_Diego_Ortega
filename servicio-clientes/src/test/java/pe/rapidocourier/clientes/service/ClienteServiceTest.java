package pe.rapidocourier.clientes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.rapidocourier.clientes.client.ReniecClient;
import pe.rapidocourier.clientes.client.ReniecResponse;
import pe.rapidocourier.clientes.dto.request.ClienteRequest;
import pe.rapidocourier.clientes.dto.response.ClienteResponse;
import pe.rapidocourier.clientes.entity.Cliente;
import pe.rapidocourier.clientes.exception.DuplicateResourceException;
import pe.rapidocourier.clientes.exception.ExternalServiceException;
import pe.rapidocourier.clientes.exception.ResourceNotFoundException;
import pe.rapidocourier.clientes.repository.ClienteRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ReniecClient reniecClient;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void registrar_HappyPath() {
        ClienteRequest request = new ClienteRequest();
        request.setDni("12345678");
        request.setEmail("test@test.com");
        request.setTelefono("987654321");

        ReniecResponse reniec = new ReniecResponse();
        reniec.setNombreCompleto("Juan Perez Lopez");

        Cliente cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setDni("12345678");
        cliente.setNombreCompleto("Juan Perez Lopez");
        cliente.setEmail("test@test.com");

        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(clienteRepository.existsByDni(any())).thenReturn(false);
        when(reniecClient.consultarDni(any(), any())).thenReturn(reniec);
        when(clienteRepository.save(any())).thenReturn(cliente);

        ClienteResponse response = clienteService.registrar(request);

        assertNotNull(response);
        assertEquals("Juan Perez Lopez", response.getNombreCompleto());
        assertEquals("12345678", response.getDni());
    }

    @Test
    void registrar_EmailDuplicado_LanzaExcepcion() {
        ClienteRequest request = new ClienteRequest();
        request.setEmail("duplicado@test.com");
        request.setDni("12345678");

        when(clienteRepository.existsByEmail("duplicado@test.com")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> clienteService.registrar(request)
        );

        assertTrue(ex.getMessage().contains("duplicado@test.com"));
    }

    @Test
    void registrar_DniDuplicado_LanzaExcepcion() {
        ClienteRequest request = new ClienteRequest();
        request.setEmail("nuevo@test.com");
        request.setDni("12345678");

        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(clienteRepository.existsByDni("12345678")).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> clienteService.registrar(request)
        );

        assertTrue(ex.getMessage().contains("12345678"));
    }

    @Test
    void registrar_ReniecFalla_LanzaExcepcion() {
        ClienteRequest request = new ClienteRequest();
        request.setEmail("nuevo@test.com");
        request.setDni("12345678");

        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(clienteRepository.existsByDni(any())).thenReturn(false);
        when(reniecClient.consultarDni(any(), any()))
                .thenThrow(new RuntimeException("RENIEC no disponible"));

        assertThrows(ExternalServiceException.class,
                () -> clienteService.registrar(request));
    }

    @Test
    void obtenerPorId_NoExiste_LanzaExcepcion() {
        UUID id = UUID.randomUUID();
        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> clienteService.obtenerPorId(id));
    }

    @Test
    void listarTodos_ListaVacia() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponse> result = clienteService.listarTodos();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}