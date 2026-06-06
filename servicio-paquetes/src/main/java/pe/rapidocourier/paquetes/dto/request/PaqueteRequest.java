package pe.rapidocourier.paquetes.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaqueteRequest {

    private String descripcion;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor a 0")
    private Double pesoKg;

    @NotNull(message = "El valor declarado es obligatorio")
    @Positive(message = "El valor declarado debe ser mayor a 0")
    private Double valorDeclarado;

    @NotBlank(message = "La sucursal de origen es obligatoria")
    private String sucursalOrigen;

    @NotBlank(message = "La sucursal de destino es obligatoria")
    private String sucursalDestino;

    @NotBlank(message = "El DNI del remitente es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dniRemitente;

    @NotBlank(message = "El DNI del destinatario es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dniDestinatario;
}