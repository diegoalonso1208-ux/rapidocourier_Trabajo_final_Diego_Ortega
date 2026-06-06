package pe.rapidocourier.tarifas.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "tarifas_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaConfig {

    @Id
    private String id;

    private String origen;
    private String destino;
    private Double costoPorKg;
    private Double costoBase;
    private Double porcentajeSeguro;
    private Boolean activo;
}