package cinemauq.servicio;

import cinemauq.excepcion.SaldoInsuficienteException;
import cinemauq.excepcion.TarjetaInactivaException;
import cinemauq.modelo.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PagoServicioTest {
    private PagoServicio pagos;
    private Cliente cliente;

    @BeforeEach
    void preparar() {
        pagos = new PagoServicio();
        cliente = new Cliente(1, "Ana", "ana@mail.com", "1234");
        cliente.getTarjeta().acreditar(30000, "Inicial");
    }

    @Test
    void verificarNoModificaElSaldo() {
        pagos.verificar(cliente, 10000);
        assertEquals(30000, cliente.getTarjeta().getSaldo());
    }

    @Test
    void cobrarDescuentaElSaldo() {
        pagos.cobrar(cliente, 10000, "Compra funcion 1");
        assertEquals(20000, cliente.getTarjeta().getSaldo());
    }

    @Test
    void verificarConSaldoInsuficienteLanzaExcepcion() {
        assertThrows(SaldoInsuficienteException.class, () -> pagos.verificar(cliente, 99999));
    }

    @Test
    void cobrarConTarjetaBloqueadaLanzaExcepcion() {
        cliente.getTarjeta().bloquear();
        assertThrows(TarjetaInactivaException.class, () -> pagos.cobrar(cliente, 1000, "x"));
        assertEquals(30000, cliente.getTarjeta().getSaldo());
    }
}
