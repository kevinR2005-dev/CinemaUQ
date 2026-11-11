package cinemauq.modelo.estado;

import cinemauq.modelo.Compra;

public class Pagada implements EstadoCompra {

    @Override
    public void cancelar (Compra compra){
        compra.setEstado(new Cancelada());
    }

    @Override
    public String getNombre(){
        return "PAGADA";
    }
}
