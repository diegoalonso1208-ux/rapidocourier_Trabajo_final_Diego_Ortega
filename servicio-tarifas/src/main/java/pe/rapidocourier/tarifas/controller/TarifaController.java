package pe.rapidocourier.tarifas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.rapidocourier.tarifas.document.TarifaConfig;
import pe.rapidocourier.tarifas.dto.request.TarifaRequest;
import pe.rapidocourier.tarifas.dto.response.ApiResponse;
import pe.rapidocourier.tarifas.dto.response.TarifaResponse;
import pe.rapidocourier.tarifas.service.TarifaService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @PostMapping("/calcular")
    public ResponseEntity<ApiResponse<TarifaResponse>> calcular(
            @Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tarifa calculada",
                tarifaService.calcular(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TarifaConfig>>> listar() {
        return ResponseEntity.ok(ApiResponse.success("Configuraciones obtenidas",
                tarifaService.listarConfiguraciones()));
    }

    @PostMapping("/config")
    public ResponseEntity<ApiResponse<TarifaConfig>> crearConfig(
            @RequestBody TarifaConfig config) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Configuración creada",
                        tarifaService.crearConfiguracion(config)));
    }
}