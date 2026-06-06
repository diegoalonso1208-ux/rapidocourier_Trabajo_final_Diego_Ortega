package pe.rapidocourier.clientes.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo números")
    private String dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene formato válido")
    private String email;

    @Size(min = 9, max = 9, message = "El teléfono debe tener 9 dígitos")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe contener solo números")
    private String telefono;
}