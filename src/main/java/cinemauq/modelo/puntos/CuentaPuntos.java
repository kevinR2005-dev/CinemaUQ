package cinemauq.modelo.puntos;

import cinemauq.excepcion.PuntosInsuficientesException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CuentaPuntos {

    private final List<LotePuntos> lotes = new ArrayList<>();

    public void acumular(int puntos, LocalDate fecha, int origenFactura) {
        if (puntos <= 0) {
            return;
        }
        lotes.add(new LotePuntos(puntos, fecha, origenFactura));
    }


    public int puntosVigentes(LocalDate hoy) {
        int suma = 0;
        for (LotePuntos lote : lotes) {
            if (lote.estaVigente(hoy)) {
                suma += lote.getPuntosRestantes();
            }
        }
        return suma;
    }

    public void verificar(int puntos, LocalDate hoy) {
        int disponibles = puntosVigentes(hoy);
        if (disponibles < puntos) {
            throw new PuntosInsuficientesException(
                    "Puntos insuficientes: tiene " + disponibles + " y necesita " + puntos);
        }
    }


    public List<ConsumoPuntos> redimir(int puntos, LocalDate hoy) {
        verificar(puntos, hoy);   // primero se verifica, despues se modifica

        List<LotePuntos> candidatos = new ArrayList<>();
        for (LotePuntos lote : lotes) {
            if (lote.estaVigente(hoy) && lote.getPuntosRestantes() > 0) {
                candidatos.add(lote);
            }
        }
        candidatos.sort(Comparator.comparing(LotePuntos::getFechaVencimiento));

        List<ConsumoPuntos> consumos = new ArrayList<>();
        int pendiente = puntos;
        for (LotePuntos lote : candidatos) {
            if (pendiente == 0) {
                break;
            }
            int usar = Math.min(pendiente, lote.getPuntosRestantes());
            lote.consumir(usar);
            consumos.add(new ConsumoPuntos(usar, lote.getFechaVencimiento()));
            pendiente -= usar;
        }
        return consumos;
    }


    public void devolver(List<ConsumoPuntos> consumos) {
        for (ConsumoPuntos c : consumos) {
            lotes.add(new LotePuntos(c.getPuntos(),
                    c.getFechaVencimiento().minusYears(1), c.getFechaVencimiento(), 0));
        }
    }


    public void anular(int origenFactura) {
        for (LotePuntos lote : lotes) {
            if (origenFactura != 0 && lote.getOrigenFactura() == origenFactura) {
                lote.anular();
            }
        }
    }

    public int puntosVencidos(LocalDate hoy) {
        int suma = 0;
        for (LotePuntos lote : lotes) {
            if (!lote.estaVigente(hoy)) {
                suma += lote.getPuntosRestantes();
            }
        }
        return suma;
    }

    public int puntosPorVencer(LocalDate hoy, int dias) {
        LocalDate limite = hoy.plusDays(dias);
        int suma = 0;
        for (LotePuntos lote : lotes) {
            if (lote.estaVigente(hoy) && !lote.getFechaVencimiento().isAfter(limite)) {
                suma += lote.getPuntosRestantes();
            }
        }
        return suma;
    }

    public List<LotePuntos> getLotes() {
        return Collections.unmodifiableList(lotes);
    }
}
