package pe.rapidocourier.paquetes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.rapidocourier.paquetes.dto.request.CategoriaRequest;
import pe.rapidocourier.paquetes.dto.request.EstadoRequest;
import pe.rapidocourier.paquetes.dto.request.PaqueteRequest;
import pe.rapidocourier.paquetes.dto.response.ApiResponse;
import pe.rapidocourier.paquetes.dto.response.HistorialResponse;
import pe.rapidocourier.paquetes.dto.response.PaqueteResponse;
import pe.rapidocourier.paquetes.service.PaqueteService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaqueteResponse>> registrar(
            @Valid @RequestBody PaqueteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paquete registrado",
                        paqueteService.registrar(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaqueteResponse>> obtenerPorId(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Paquete encontrado",
                paqueteService.obtenerPorId(id)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<PaqueteResponse>> actualizarEstado(
            @PathVariable("id") UUID id,
            @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Estado actualizado",
                paqueteService.actualizarEstado(id, request)));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<HistorialResponse>>> obtenerHistorial(
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Historial obtenido",
                paqueteService.obtenerHistorial(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaqueteResponse>>> buscar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String sucursal,
            @RequestParam(required = false) String estado) {
        if (busqueda != null) {
            return ResponseEntity.ok(ApiResponse.success("Resultados",
                    paqueteService.buscarPorTexto(busqueda)));
        }
        if (sucursal != null) {
            return ResponseEntity.ok(ApiResponse.success("Resultados",
                    paqueteService.filtrarPorSucursalYEstado(sucursal, estado)));
        }
        return ResponseEntity.ok(ApiResponse.success("Resultados", List.of()));
    }

    @PostMapping("/{id}/categorias/{categoriaId}")
    public ResponseEntity<ApiResponse<PaqueteResponse>> asignarCategoria(
            @PathVariable("id") UUID id,
            @PathVariable("categoriaId") UUID categoriaId) {
        return ResponseEntity.ok(ApiResponse.success("Categoría asignada",
                paqueteService.asignarCategoria(id, categoriaId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") UUID id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}