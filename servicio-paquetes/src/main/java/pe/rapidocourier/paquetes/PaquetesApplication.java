package pe.rapidocourier.paquetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaquetesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaquetesApplication.class, args);
    }
}