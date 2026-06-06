package pe.rapidocourier.paquetes.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "servicio-clientes")
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/dni/{dni}")
    ClienteResponse obtenerPorDni(@PathVariable("dni") String dni);
}