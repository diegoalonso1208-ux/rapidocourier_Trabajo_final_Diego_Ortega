package pe.rapidocourier.clientes.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReniecClient reniecClient;

    @Value("${reniec.token}")
    private String reniecToken;

    @CircuitBreaker(name = "reniec", fallbackMethod = "fallbackReniec")
    public ClienteResponse registrar(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "El email ya está registrado: " + request.getEmail());
        }
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new DuplicateResourceException(
                    "El DNI ya está registrado: " + request.getDni());
        }

        ReniecResponse reniec;
        try {
            reniec = reniecClient.consultarDni(request.getDni(), reniecToken);
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Error al consultar RENIEC: " + e.getMessage());
        }

        if (reniec == null || reniec.getNombreCompleto() == null) {
            throw new ExternalServiceException("RENIEC no retornó datos válidos");
        }

        Cliente cliente = new Cliente();
        cliente.setDni(request.getDni());
        cliente.setNombreCompleto(reniec.getNombreCompleto());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());

        return toResponse(clienteRepository.save(cliente));
    }

    public ClienteResponse fallbackReniec(ClienteRequest request, Exception ex) {
        throw new ExternalServiceException(
                "Servicio RENIEC no disponible: " + ex.getMessage());
    }

    public ClienteResponse obtenerPorId(UUID id) {
        return toResponse(clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con id: " + id)));
    }

    public ClienteResponse obtenerPorDni(String dni) {
        return toResponse(clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado con DNI: " + dni)));
    }

    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ClienteResponse> buscarPorNombre(String texto) {
        return clienteRepository.buscarPorNombre(texto)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void eliminar(UUID id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setDni(cliente.getDni());
        response.setNombreCompleto(cliente.getNombreCompleto());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        response.setCreatedAt(cliente.getCreatedAt());
        return response;
    }
}