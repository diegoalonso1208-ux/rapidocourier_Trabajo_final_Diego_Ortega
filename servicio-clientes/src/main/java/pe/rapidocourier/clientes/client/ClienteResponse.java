package pe.rapidocourier.paquetes.client;

import lombok.Data;
import java.util.UUID;

@Data
public class ClienteResponse {
    private UUID id;
    private String dni;
    private String nombreCompleto;
    private String email;
    private String telefono;
}