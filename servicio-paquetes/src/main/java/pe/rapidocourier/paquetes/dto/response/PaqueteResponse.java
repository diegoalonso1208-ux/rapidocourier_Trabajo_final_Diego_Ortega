package pe.rapidocourier.paquetes.dto.response;

import lombok.Data;
import pe.rapidocourier.paquetes.entity.EstadoPaquete;
import pe.rapidocourier.paquetes.entity.Sucursal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class PaqueteResponse {
    private UUID id;
    private String codigoRastreo;
    private String descripcion;
    private Double pesoKg;
    private BigDecimal valorDeclarado;
    private BigDecimal tarifa;
    private Sucursal sucursalOrigen;
    private Sucursal sucursalDestino;
    private EstadoPaquete estado;
    private String remitenteNombre;
    private String destinatarioNombre;
    private Set<String> categorias;
    private LocalDateTime createdAt;
}