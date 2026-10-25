package cinemauq.servicio;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.tarjeta.Movimiento;

import java.util.List;

public class TarjetaServicioReal implements TarjetaServicio {

    @Override
    public void recargar(Usuario solicitante, Cliente destino, double monto) {
        destino.getTarjeta().acreditar(monto, "Recarga"); // sin revisar roles
    }

    @Override
    public double consultarSaldo(Cliente c) {
        return c.getTarjeta().getSaldo();
    }

    @Override
    public List<Movimiento> consultarMovimientos(Cliente c) {
        return c.getTarjeta().getMovimientos();
    }
}