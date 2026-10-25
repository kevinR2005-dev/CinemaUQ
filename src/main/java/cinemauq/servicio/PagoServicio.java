package cinemauq.servicio;

import cinemauq.modelo.Cliente;

public class PagoServicio {

    public void verificar(Cliente c, double monto) {
        c.getTarjeta().verificarPago(monto);
    }

    public void cobrar(Cliente c, double monto, String detalle) {
        c.getTarjeta().debitar(monto, detalle);
    }
}