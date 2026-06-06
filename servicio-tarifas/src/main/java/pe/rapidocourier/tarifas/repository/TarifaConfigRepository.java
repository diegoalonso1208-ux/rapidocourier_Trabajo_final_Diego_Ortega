package pe.rapidocourier.tarifas.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pe.rapidocourier.tarifas.document.TarifaConfig;
import java.util.Optional;

public interface TarifaConfigRepository extends MongoRepository<TarifaConfig, String> {
    Optional<TarifaConfig> findByOrigenAndDestino(String origen, String destino);
}