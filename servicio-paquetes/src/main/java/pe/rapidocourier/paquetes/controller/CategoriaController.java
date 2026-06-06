package pe.rapidocourier.paquetes.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.rapidocourier.paquetes.dto.request.CategoriaRequest;
import pe.rapidocourier.paquetes.dto.response.ApiResponse;
import pe.rapidocourier.paquetes.entity.Categoria;
import pe.rapidocourier.paquetes.exception.DuplicateResourceException;
import pe.rapidocourier.paquetes.repository.CategoriaRepository;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Categoria>> crear(
            @Valid @RequestBody CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException(
                    "Categoría ya existe: " + request.getNombre());
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Categoría creada",
                        categoriaRepository.save(categoria)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Categoria>>> listar() {
        return ResponseEntity.ok(ApiResponse.success("Categorías obtenidas",
                categoriaRepository.findAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}