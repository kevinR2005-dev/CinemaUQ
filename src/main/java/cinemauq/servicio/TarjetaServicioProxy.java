package cinemauq.servicio;

import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Rol;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.tarjeta.Movimiento;

import java.util.List;

public class TarjetaServicioProxy implements TarjetaServicio {
    private final TarjetaServicio real; // el objeto que protege

    public TarjetaServicioProxy(TarjetaServicio real) {
        this.real = real;
    }

    @Override
    public void recargar(Usuario solicitante, Cliente destino, double monto) {
        // RN09: solo el administrador recarga
        if (solicitante.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo el administrador puede recargar");
        }
        real.recargar(solicitante, destino, monto);
    }

    @Override
    public double consultarSaldo(Cliente c) {
        return real.consultarSaldo(c);
    }

    @Override
    public List<Movimiento> consultarMovimientos(Cliente c) {
        return real.consultarMovimientos(c);
    }
}
