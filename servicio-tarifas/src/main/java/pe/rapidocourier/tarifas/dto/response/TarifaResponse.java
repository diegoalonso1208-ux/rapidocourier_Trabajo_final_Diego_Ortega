package pe.rapidocourier.tarifas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarifaResponse {
    private String origen;
    private String destino;
    private Double pesoKg;
    private Double valorDeclarado;
    private BigDecimal tarifaCalculada;
    private String detalle;
}