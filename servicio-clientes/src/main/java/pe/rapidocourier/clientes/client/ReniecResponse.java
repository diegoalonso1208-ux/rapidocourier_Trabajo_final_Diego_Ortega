package pe.rapidocourier.clientes.client;

import lombok.Data;

@Data
public class ReniecResponse {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String dni;
}