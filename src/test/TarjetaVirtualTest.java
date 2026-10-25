package cinemauq.modelo.tarjeta;

import cinemauq.excepcion.SaldoInsuficienteException;
import cinemauq.excepcion.TarjetaInactivaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TarjetaVirtualTest {

    @Test
    void estadoInicialActivaYSaldoCero() {
        TarjetaVirtual t = new TarjetaVirtual();
        assertEquals(EstadoTarjeta.ACTIVA, t.getEstado());
        assertEquals(0, t.getSaldo());
    }

    @Test
    void debitarDescuentaYRegistraMovimiento() {
        TarjetaVirtual t = new TarjetaVirtual(50000);
        t.debitar(20000, "Compra");
        assertEquals(30000, t.getSaldo());
        assertEquals(1, t.getMovimientos().size());
        assertEquals(TipoMovimiento.PAGO, t.getMovimientos().get(0).getTipo());
        assertEquals(30000, t.getMovimientos().get(0).getSaldoResultante());
    }

    @Test
    void acreditarSumaYRegistraMovimiento() {
        TarjetaVirtual t = new TarjetaVirtual();
        t.acreditar(10000, "Recarga");
        assertEquals(10000, t.getSaldo());
        assertEquals(TipoMovimiento.RECARGA, t.getMovimientos().get(0).getTipo());
    }

    @Test
    void saldoInsuficienteLanzaExcepcionYNoCambiaNada() {
        TarjetaVirtual t = new TarjetaVirtual(1000);
        assertThrows(SaldoInsuficienteException.class, () -> t.debitar(5000, "x"));
        assertEquals(1000, t.getSaldo());
        assertTrue(t.getMovimientos().isEmpty());
    }

    @Test
    void tarjetaBloqueadaNoPuedePagar() {
        TarjetaVirtual t = new TarjetaVirtual(50000);
        t.bloquear();
        assertThrows(TarjetaInactivaException.class, () -> t.verificarPago(1000));
    }

    @Test
    void acreditarMontoInvalidoLanzaExcepcion() {
        TarjetaVirtual t = new TarjetaVirtual();
        assertThrows(IllegalArgumentException.class, () -> t.acreditar(0, "x"));
        assertThrows(IllegalArgumentException.class, () -> t.acreditar(-5, "x"));
    }

    @Test
    void listaDeMovimientosNoSePuedeModificar() {
        TarjetaVirtual t = new TarjetaVirtual();
        assertThrows(UnsupportedOperationException.class, () -> t.getMovimientos().clear());
    }
}