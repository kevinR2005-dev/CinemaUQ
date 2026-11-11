package cinemauq.excepcion;

public class CancelacionNoPermitidaException extends RuntimeException {
    public CancelacionNoPermitidaException(String message) {
        super(message);
    }
}
