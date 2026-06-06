package pe.rapidocourier.paquetes.dto.response;

import lombok.Data;
import pe.rapidocourier.paquetes.entity.EstadoPaquete;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class HistorialResponse {
    private UUID id;
    private EstadoPaquete estado;
    private LocalDateTime fechaCambio;
    private String usuarioResponsable;
    private String observacion;
}