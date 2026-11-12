package cinemauq.servicio;

import cinemauq.evento.EventoCine;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.TipoEvento;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.tarjeta.Movimiento;

import java.time.LocalDateTime;
import java.util.List;

public class TarjetaServicioReal implements TarjetaServicio {

    // Sujeto del Observer: a quien se le avisa cuando hay una recarga
    private final PublicadorEventos publicador;

    public TarjetaServicioReal(PublicadorEventos publicador) {
        this.publicador = publicador;
    }

    @Override
    public void recargar(Usuario solicitante, Cliente destino, double monto) {
        destino.getTarjeta().acreditar(monto, "Recarga"); // los roles los revisa el Proxy
        // Solo se publica si acreditar no lanzó excepción
        publicador.publicar(new EventoCine(TipoEvento.RECARGA, destino,
                "Recarga de $" + monto, LocalDateTime.now()));
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