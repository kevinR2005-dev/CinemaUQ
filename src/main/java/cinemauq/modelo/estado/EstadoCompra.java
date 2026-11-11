package cinemauq.modelo.estado;

import cinemauq.modelo.Compra;

public interface EstadoCompra {

    void cancelar (Compra compra);
    String getNombre();
}
