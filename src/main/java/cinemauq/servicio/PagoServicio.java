package cinemauq.servicio;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.tarjeta.TipoMovimiento;

public class PagoServicio {

    // Solo verifica, no modifica nada (RN07)
    public void verificar(Cliente c, double monto) {
        c.getTarjeta().verificarPago(monto);
    }

    public void cobrar(Cliente c, double monto, String detalle) {
        c.getTarjeta().debitar(monto, detalle);
    }

    // Devuelve dinero a la tarjeta del cliente (RN19)
    public void reembolsar(Cliente c, double monto, String detalle) {
        c.getTarjeta().acreditar(monto, detalle, TipoMovimiento.REEMBOLSO);
    }
}