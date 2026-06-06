package pe.rapidocourier.paquetes.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String estadoActual, String estadoNuevo) {
        super("Transición inválida: no se puede pasar de " +
                estadoActual + " a " + estadoNuevo);
    }
}