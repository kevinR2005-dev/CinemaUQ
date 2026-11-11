package cinemauq.servicio;

import cinemauq.evento.EventoCine;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.TipoEvento;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.puntos.ConsumoPuntos;
import cinemauq.modelo.puntos.CuentaPuntos;
import cinemauq.modelo.puntos.ReglaAcumulacion;
import cinemauq.modelo.puntos.TipoBeneficio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PuntosServicio {

    private static final int DIAS_AVISO = 30;

    private final ReglaAcumulacion regla;
    private final PublicadorEventos publicador;

    public PuntosServicio(ReglaAcumulacion regla, PublicadorEventos publicador) {
        this.regla = regla;
        this.publicador = publicador;
    }


    public int acumular(Cliente cliente, double montoPagado, int origenFactura, LocalDate hoy) {
        int puntos = regla.calcular(montoPagado);
        cliente.getPuntos().acumular(puntos, hoy, origenFactura);
        return puntos;
    }


    public void verificarCanje(Cliente cliente, List<TipoBeneficio> canjes, LocalDate hoy) {
        int total = costoTotal(canjes);
        if (total > 0) {
            cliente.getPuntos().verificar(total, hoy);
        }
    }


    public List<ConsumoPuntos> redimir(Cliente cliente, List<TipoBeneficio> canjes, LocalDate hoy) {
        int total = costoTotal(canjes);
        if (total == 0) {
            return new ArrayList<>();
        }
        return cliente.getPuntos().redimir(total, hoy);
    }


    public void revertirCompra(Cliente cliente, Compra compra) {
        CuentaPuntos cuenta = cliente.getPuntos();
        cuenta.devolver(compra.getConsumos());
        cuenta.anular(compra.getNumeroFactura());
    }


    public void avisarVencimientos(Cliente cliente, LocalDate hoy) {
        CuentaPuntos cuenta = cliente.getPuntos();
        int vencidos = cuenta.puntosVencidos(hoy);
        int porVencer = cuenta.puntosPorVencer(hoy, DIAS_AVISO);

        if (vencidos == 0 && porVencer == 0) {
            return;
        }
        StringBuilder mensaje = new StringBuilder();
        if (porVencer > 0) {
            mensaje.append(porVencer).append(" puntos vencen en los proximos ")
                    .append(DIAS_AVISO).append(" dias. ");
        }
        if (vencidos > 0) {
            mensaje.append(vencidos).append(" puntos ya vencieron.");
        }
        publicador.publicar(new EventoCine(TipoEvento.VENCIMIENTO_PUNTOS, cliente,
                mensaje.toString().trim(), LocalDateTime.now()));
    }


    private int costoTotal(List<TipoBeneficio> canjes) {
        int total = 0;
        for (TipoBeneficio tipo : canjes) {
            total += tipo.getCosto();
        }
        return total;
    }
}
