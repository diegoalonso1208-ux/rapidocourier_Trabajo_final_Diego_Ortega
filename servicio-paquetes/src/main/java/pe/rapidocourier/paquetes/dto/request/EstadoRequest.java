package pe.rapidocourier.paquetes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstadoRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    private String observacion;

    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;
}