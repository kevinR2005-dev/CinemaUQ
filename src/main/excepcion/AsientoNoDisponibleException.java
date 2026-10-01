package main.excepcion;

public class AsientoNoDisponibleException extends RuntimeException {
    public AsientoNoDisponibleException(String message) {
        super(message);
    }
}
