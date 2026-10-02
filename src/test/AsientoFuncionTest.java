package test;

import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.EstadoAsiento;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class AsientoFuncionTest {

    @Test
    void asientoNuevoEstaLibre() {
        AsientoFuncion asiento = new AsientoFuncion("A1");

        assertEquals("A1", asiento.getCodigo());
        assertTrue(asiento.estaLibre());
        assertEquals(EstadoAsiento.LIBRE, asiento.getEstado());
    }

    @Test
    void reservarCambiaEstadoAVendido() {
        AsientoFuncion asiento = new AsientoFuncion("B3");

        asiento.reservar();

        assertFalse(asiento.estaLibre());
        assertEquals(EstadoAsiento.VENDIDO, asiento.getEstado());
    }

    @Test
    void reservarDosVecesLanzaExcepcion() {
        AsientoFuncion asiento = new AsientoFuncion("C5");
        asiento.reservar();

        // RN-02: un asiento no se vende dos veces
        assertThrows(AsientoNoDisponibleException.class, asiento::reservar);
    }

    @Test
    void cloneDeAsientoVendidoNaceLibreConMismoCodigo() {
        AsientoFuncion original = new AsientoFuncion("D7");
        original.reservar();

        AsientoFuncion copia = original.clone();

        assertEquals("D7", copia.getCodigo());
        assertTrue(copia.estaLibre());
        assertNotSame(original, copia);
    }

    @Test
    void reservarLaCopiaNoAfectaAlOriginal() {
        AsientoFuncion original = new AsientoFuncion("E2");
        AsientoFuncion copia = original.clone();

        copia.reservar();

        assertTrue(original.estaLibre());   // El original sigue libre
        assertFalse(copia.estaLibre());
    }
}