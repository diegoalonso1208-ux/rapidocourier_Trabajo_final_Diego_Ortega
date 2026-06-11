package pe.rapidocourier.paquetes.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.rapidocourier.paquetes.client.ClienteClient;
import pe.rapidocourier.paquetes.client.ClienteResponse;
import pe.rapidocourier.paquetes.dto.request.EstadoRequest;
import pe.rapidocourier.paquetes.dto.request.PaqueteRequest;
import pe.rapidocourier.paquetes.dto.response.HistorialResponse;
import pe.rapidocourier.paquetes.dto.response.PaqueteResponse;
import pe.rapidocourier.paquetes.entity.*;
import pe.rapidocourier.paquetes.exception.*;
import pe.rapidocourier.paquetes.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final HistorialEstadoRepository historialRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClienteClient clienteClient;

    private static final Map<EstadoPaquete, List<EstadoPaquete>> TRANSICIONES = Map.of(
            EstadoPaquete.REGISTRADO, List.of(EstadoPaquete.EN_ALMACEN),
            EstadoPaquete.EN_ALMACEN, List.of(EstadoPaquete.EN_TRANSITO),
            EstadoPaquete.EN_TRANSITO, List.of(EstadoPaquete.EN_REPARTO),
            EstadoPaquete.EN_REPARTO, List.of(EstadoPaquete.ENTREGADO, EstadoPaquete.NO_ENTREGADO),
            EstadoPaquete.NO_ENTREGADO, List.of(EstadoPaquete.EN_TRANSITO),
            EstadoPaquete.ENTREGADO, List.of()
    );

    @CircuitBreaker(name = "servicio-clientes", fallbackMethod = "fallbackCliente")
    public PaqueteResponse registrar(PaqueteRequest request) {
        ClienteResponse remitente;
        ClienteResponse destinatario;
        try {
            remitente = clienteClient.obtenerPorDni(request.getDniRemitente()).getData();
            destinatario = clienteClient.obtenerPorDni(request.getDniDestinatario()).getData();
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Error al consultar servicio-clientes: " + e.getMessage());
        }

        Sucursal origen = Sucursal.valueOf(request.getSucursalOrigen().toUpperCase());
        Sucursal destino = Sucursal.valueOf(request.getSucursalDestino().toUpperCase());

        BigDecimal tarifa = calcularTarifa(request.getPesoKg(),
                request.getValorDeclarado(), origen, destino);

        Paquete paquete = new Paquete();
        paquete.setCodigoRastreo(generarCodigo());
        paquete.setDescripcion(request.getDescripcion());
        paquete.setPesoKg(request.getPesoKg());
        paquete.setValorDeclarado(BigDecimal.valueOf(request.getValorDeclarado()));
        paquete.setTarifa(tarifa);
        paquete.setSucursalOrigen(origen);
        paquete.setSucursalDestino(destino);
        paquete.setEstado(EstadoPaquete.REGISTRADO);
        paquete.setRemitenteId(remitente.getId());
        paquete.setDestinatarioId(destinatario.getId());
        paquete.setRemitenteNombre(remitente.getNombreCompleto());
        paquete.setDestinatarioNombre(destinatario.getNombreCompleto());

        Paquete saved = paqueteRepository.save(paquete);

        HistorialEstado historial = new HistorialEstado();
        historial.setPaquete(saved);
        historial.setEstado(EstadoPaquete.REGISTRADO);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioResponsable("SISTEMA");
        historial.setObservacion("Paquete registrado");
        historialRepository.save(historial);

        return toResponse(saved);
    }

    public PaqueteResponse fallbackCliente(PaqueteRequest request, Exception ex) {
        throw new ExternalServiceException(
                "Servicio de clientes no disponible: " + ex.getMessage());
    }

    public PaqueteResponse actualizarEstado(UUID id, EstadoRequest request) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paquete no encontrado: " + id));

        EstadoPaquete estadoActual = paquete.getEstado();
        EstadoPaquete nuevoEstado = EstadoPaquete.valueOf(request.getNuevoEstado());

        List<EstadoPaquete> transicionesValidas = TRANSICIONES.get(estadoActual);
        if (!transicionesValidas.contains(nuevoEstado)) {
            throw new InvalidStateTransitionException(
                    estadoActual.name(), nuevoEstado.name());
        }

        paquete.setEstado(nuevoEstado);
        Paquete saved = paqueteRepository.save(paquete);

        HistorialEstado historial = new HistorialEstado();
        historial.setPaquete(saved);
        historial.setEstado(nuevoEstado);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioResponsable(request.getUsuario());
        historial.setObservacion(request.getObservacion());
        historialRepository.save(historial);

        return toResponse(saved);
    }

    public PaqueteResponse obtenerPorId(UUID id) {
        return toResponse(paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paquete no encontrado: " + id)));
    }

    public List<HistorialResponse> obtenerHistorial(UUID id) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paquete no encontrado: " + id));
        return historialRepository.findByPaqueteOrderByFechaCambioAsc(paquete)
                .stream().map(this::toHistorialResponse)
                .collect(Collectors.toList());
    }

    public List<PaqueteResponse> buscarPorTexto(String texto) {
        return paqueteRepository.buscarPorTexto(texto)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PaqueteResponse> filtrarPorSucursalYEstado(String sucursal, String estado) {
        return paqueteRepository.filtrarPorSucursalYEstado(sucursal, estado)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PaqueteResponse asignarCategoria(UUID paqueteId, UUID categoriaId) {
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paquete no encontrado: " + paqueteId));
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada: " + categoriaId));
        paquete.getCategorias().add(categoria);
        return toResponse(paqueteRepository.save(paquete));
    }

    public void eliminar(UUID id) {
        if (!paqueteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paquete no encontrado: " + id);
        }
        paqueteRepository.deleteById(id);
    }

    private BigDecimal calcularTarifa(Double peso, Double valorDeclarado,
                                      Sucursal origen, Sucursal destino) {
        double base = peso * 8.0;
        double seguro = valorDeclarado * 0.01;
        double distancia = calcularDistancia(origen, destino);
        return BigDecimal.valueOf(base + seguro + distancia)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private double calcularDistancia(Sucursal origen, Sucursal destino) {
        if (origen == destino) return 0.0;
        if ((origen == Sucursal.LIMA && destino == Sucursal.AREQUIPA) ||
                (origen == Sucursal.AREQUIPA && destino == Sucursal.LIMA)) return 25.0;
        if ((origen == Sucursal.LIMA && destino == Sucursal.CUSCO) ||
                (origen == Sucursal.CUSCO && destino == Sucursal.LIMA)) return 35.0;
        if ((origen == Sucursal.AREQUIPA && destino == Sucursal.CUSCO) ||
                (origen == Sucursal.CUSCO && destino == Sucursal.AREQUIPA)) return 20.0;
        return 0.0;
    }

    private String generarCodigo() {
        return "RC" + System.currentTimeMillis() +
                UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private PaqueteResponse toResponse(Paquete p) {
        PaqueteResponse r = new PaqueteResponse();
        r.setId(p.getId());
        r.setCodigoRastreo(p.getCodigoRastreo());
        r.setDescripcion(p.getDescripcion());
        r.setPesoKg(p.getPesoKg());
        r.setValorDeclarado(p.getValorDeclarado());
        r.setTarifa(p.getTarifa());
        r.setSucursalOrigen(p.getSucursalOrigen());
        r.setSucursalDestino(p.getSucursalDestino());
        r.setEstado(p.getEstado());
        r.setRemitenteNombre(p.getRemitenteNombre());
        r.setDestinatarioNombre(p.getDestinatarioNombre());
        r.setCategorias(p.getCategorias().stream()
                .map(Categoria::getNombre).collect(Collectors.toSet()));
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }

    private HistorialResponse toHistorialResponse(HistorialEstado h) {
        HistorialResponse r = new HistorialResponse();
        r.setId(h.getId());
        r.setEstado(h.getEstado());
        r.setFechaCambio(h.getFechaCambio());
        r.setUsuarioResponsable(h.getUsuarioResponsable());
        r.setObservacion(h.getObservacion());
        return r;
    }
}