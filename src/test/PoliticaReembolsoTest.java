package test;

import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.modelo.cancelacion.PoliticaReembolso;
import cinemauq.modelo.cancelacion.ReembolsoParcial;
import cinemauq.modelo.cancelacion.ReembolsoTotal;
import cinemauq.modelo.cancelacion.SinReembolso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PoliticaReembolsoTest {

    @Test
    void reembolsoTotalDevuelveTodo() {
        assertEquals(50000, new ReembolsoTotal().calcular(50000), 0.001);
    }

    @Test
    void reembolsoParcialAplicaElPorcentaje() {
        assertEquals(40000, new ReembolsoParcial(80).calcular(50000), 0.001);
        assertEquals(25000, new ReembolsoParcial(50).calcular(50000), 0.001);
    }

    @Test
    void sinReembolsoLanzaExcepcion() {
        PoliticaReembolso p = new SinReembolso();
        CancelacionNoPermitidaException e = assertThrows(CancelacionNoPermitidaException.class,
                () -> p.calcular(50000));
        assertEquals("No se permite cancelar con menos de 2 horas de anticipacion", e.getMessage());
    }

    @Test
    void todasSeUsanPorLaMismaInterfaz() {
        // Esencia del Strategy: quien usa la política no sabe cuál es
        PoliticaReembolso[] politicas = { new ReembolsoTotal(), new ReembolsoParcial(80) };
        assertEquals(10000, politicas[0].calcular(10000), 0.001);
        assertEquals(8000, politicas[1].calcular(10000), 0.001);
    }
}