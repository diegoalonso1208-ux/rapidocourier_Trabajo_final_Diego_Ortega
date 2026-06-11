package pe.rapidocourier.paquetes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pe.rapidocourier.paquetes.dto.response.ApiResponse;

@FeignClient(name = "servicio-clientes")
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/dni/{dni}")
    ApiResponse<ClienteResponse> obtenerPorDni(@PathVariable("dni") String dni);
}