package cinemauq.servicio;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.tarjeta.Movimiento;

import java.util.List;

// Interfaz comun que implementan el servicio real y el Proxy
public interface TarjetaServicio {
    void recargar(Usuario solicitante, Cliente destino, double monto);
    double consultarSaldo(Cliente c);
    List<Movimiento> consultarMovimientos(Cliente c);
}
