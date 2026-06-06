package pe.rapidocourier.tarifas;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.rapidocourier.tarifas.document.TarifaConfig;
import pe.rapidocourier.tarifas.repository.TarifaConfigRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TarifaConfigRepository tarifaConfigRepository;

    @Override
    public void run(String... args) {
        if (tarifaConfigRepository.count() == 0) {
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "LIMA", "LIMA", 8.0, 0.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "LIMA", "AREQUIPA", 8.0, 25.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "AREQUIPA", "LIMA", 8.0, 25.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "LIMA", "CUSCO", 8.0, 35.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "CUSCO", "LIMA", 8.0, 35.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "AREQUIPA", "CUSCO", 8.0, 20.0, 0.01, true));
            tarifaConfigRepository.save(new TarifaConfig(
                    null, "CUSCO", "AREQUIPA", 8.0, 20.0, 0.01, true));
        }
    }
}