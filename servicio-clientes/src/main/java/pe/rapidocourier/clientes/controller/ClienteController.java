package pe.rapidocourier.clientes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.rapidocourier.clientes.dto.request.ClienteRequest;
import pe.rapidocourier.clientes.dto.response.ApiResponse;
import pe.rapidocourier.clientes.dto.response.ClienteResponse;
import pe.rapidocourier.clientes.service.ClienteService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> registrar(
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cliente registrado",
                        clienteService.registrar(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.success("Clientes obtenidos",
                clienteService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Cliente encontrado",
                clienteService.obtenerPorId(id)));
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorDni(
            @PathVariable String dni) {
        return ResponseEntity.ok(ApiResponse.success("Cliente encontrado",
                clienteService.obtenerPorDni(dni)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> buscar(
            @RequestParam String nombre) {
        return ResponseEntity.ok(ApiResponse.success("Resultados de búsqueda",
                clienteService.buscarPorNombre(nombre)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}