package pe.rapidocourier.tarifas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.rapidocourier.tarifas.document.TarifaConfig;
import pe.rapidocourier.tarifas.dto.request.TarifaRequest;
import pe.rapidocourier.tarifas.dto.response.TarifaResponse;
import pe.rapidocourier.tarifas.exception.ResourceNotFoundException;
import pe.rapidocourier.tarifas.repository.TarifaConfigRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaConfigRepository tarifaConfigRepository;

    public TarifaResponse calcular(TarifaRequest request) {
        TarifaConfig config = tarifaConfigRepository
                .findByOrigenAndDestino(
                        request.getOrigen().toUpperCase(),
                        request.getDestino().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe tarifa para la ruta: " +
                                request.getOrigen() + " → " + request.getDestino()));

        double costoPeso = request.getPesoKg() * config.getCostoPorKg();
        double costoSeguro = request.getValorDeclarado() * config.getPorcentajeSeguro();
        double total = costoPeso + costoSeguro + config.getCostoBase();

        BigDecimal tarifa = BigDecimal.valueOf(total)
                .setScale(2, RoundingMode.HALF_UP);

        String detalle = String.format(
                "Peso: %.2f kg × S/%.2f + Seguro: %.2f%% + Base: S/%.2f",
                request.getPesoKg(), config.getCostoPorKg(),
                config.getPorcentajeSeguro() * 100, config.getCostoBase());

        return new TarifaResponse(
                request.getOrigen(), request.getDestino(),
                request.getPesoKg(), request.getValorDeclarado(),
                tarifa, detalle);
    }

    public List<TarifaConfig> listarConfiguraciones() {
        return tarifaConfigRepository.findAll();
    }

    public TarifaConfig crearConfiguracion(TarifaConfig config) {
        return tarifaConfigRepository.save(config);
    }
}