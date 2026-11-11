package cinemauq.modelo.estado;

import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.modelo.Compra;

public class Cancelada implements EstadoCompra{

    @Override
    public void cancelar(Compra compra) {
        throw new CancelacionNoPermitidaException("La compra ya esta cancelada");
    }

    @Override
    public String getNombre() {
        return "CANCELADA";
    }
}
