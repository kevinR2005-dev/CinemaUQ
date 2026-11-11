package cinemauq.modelo.estado;

import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.modelo.Compra;

public class Utilizada implements EstadoCompra {

    @Override
    public void cancelar(Compra compra) {
        throw new CancelacionNoPermitidaException("La funcion ya se realizo");
    }

    @Override
    public String getNombre() {
        return "UTILIZADA";
    }
}
