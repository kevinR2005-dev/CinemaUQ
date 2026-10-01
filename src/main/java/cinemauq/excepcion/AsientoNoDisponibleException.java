package cinemauq.excepcion;

public class AsientoNoDisponibleException extends RuntimeException {
    public AsientoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
