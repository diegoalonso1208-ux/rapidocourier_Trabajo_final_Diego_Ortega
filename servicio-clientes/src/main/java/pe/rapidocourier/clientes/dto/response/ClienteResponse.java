package pe.rapidocourier.clientes.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClienteResponse {
    private UUID id;
    private String dni;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private LocalDateTime createdAt;
}