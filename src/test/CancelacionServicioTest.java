package test;

import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.excepcion.EntidadNoEncontradaException;
import cinemauq.evento.TipoEvento;
import cinemauq.modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CancelacionServicioTest {

    private Escenario e;
    private Cliente nicolas;
    private LocalDateTime ahora;

    @BeforeEach
    void preparar() {
        e = new Escenario();
        nicolas = e.cliente(1, "Nicolas", 500000);
        ahora = LocalDateTime.now();
    }

    @Test
    void masDe24HorasReembolsa100() {
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);
        double saldoAntes = nicolas.getTarjeta().getSaldo();

        double reembolso = e.cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(40000, reembolso, 0.001);
        assertEquals(saldoAntes + 40000, nicolas.getTarjeta().getSaldo(), 0.001);
        assertTrue(f.getAsiento("A1").estaLibre());
        assertEquals("CANCELADA", e.compras.buscarPorId(1).getEstado().getNombre());
    }

    @Test
    void entre2y24HorasReembolsa80() {
        Funcion f = e.funcion(12, 1, "Avengers", ahora.plusHours(5));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);

        assertEquals(32000, e.cancelacion.cancelarCompra(nicolas, 1, ahora), 0.001);
    }

    @Test
    void menosDe2HorasSeRechazaYNoCambiaNada() {
        Funcion f = e.funcion(13, 1, "Avengers", ahora.plusHours(1));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);
        double saldoAntes = nicolas.getTarjeta().getSaldo();
        int movimientosAntes = nicolas.getTarjeta().getMovimientos().size();

        assertThrows(CancelacionNoPermitidaException.class,
                () -> e.cancelacion.cancelarCompra(nicolas, 1, ahora));

        // Verificar antes de cambiar: todo igual
        assertEquals("PAGADA", e.compras.buscarPorId(1).getEstado().getNombre());
        assertFalse(f.getAsiento("A1").estaLibre());
        assertEquals(saldoAntes, nicolas.getTarjeta().getSaldo(), 0.001);
        assertEquals(movimientosAntes, nicolas.getTarjeta().getMovimientos().size());
        assertEquals(40, nicolas.getPuntos().puntosVigentes(LocalDate.now()));
    }

    @Test
    void cancelarAnulaLosPuntosGanados() {
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);
        assertEquals(40, nicolas.getPuntos().puntosVigentes(LocalDate.now()));

        e.cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(0, nicolas.getPuntos().puntosVigentes(LocalDate.now()));
    }

    @Test
    void cancelarDosVecesLanzaExcepcionDelState() {
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);
        e.cancelacion.cancelarCompra(nicolas, 1, ahora);
        double saldo = nicolas.getTarjeta().getSaldo();

        CancelacionNoPermitidaException ex = assertThrows(CancelacionNoPermitidaException.class,
                () -> e.cancelacion.cancelarCompra(nicolas, 1, ahora));

        assertEquals("La compra ya esta cancelada", ex.getMessage());
        assertEquals(saldo, nicolas.getTarjeta().getSaldo(), 0.001); // reembolso una sola vez (RN19)
    }

    @Test
    void otroClienteNoPuedeCancelar() {
        Cliente kevin = e.cliente(2, "Kevin", 100000);
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);

        assertThrows(AccesoDenegadoException.class, () -> e.cancelacion.cancelarCompra(kevin, 1, ahora));
        assertEquals("PAGADA", e.compras.buscarPorId(1).getEstado().getNombre());
    }

    @Test
    void facturaInexistenteLanzaExcepcion() {
        assertThrows(EntidadNoEncontradaException.class,
                () -> e.cancelacion.cancelarCompra(nicolas, 999, ahora));
    }

    @Test
    void cancelarCompraPublicaEventoYNotifica() {
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        e.compra(nicolas, f, 1, "A1", 40000, ahora);

        e.cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(1, e.registro.getEventos().stream()
                .filter(ev -> ev.getTipo() == TipoEvento.CANCELACION).count());
        assertEquals(1, e.notificador.obtener(nicolas).size());
    }

    @Test
    void adminCancelaFuncionYReembolsaATodosAl100() {
        Cliente kevin = e.cliente(2, "Kevin", 500000);
        Funcion f = e.funcion(13, 1, "Avengers", ahora.plusHours(1)); // a 1 hora: por cuenta del cine igual es 100 %
        e.compra(nicolas, f, 1, "A1", 40000, ahora);
        e.compra(kevin, f, 2, "A2", 20000, ahora);
        double saldoN = nicolas.getTarjeta().getSaldo();
        double saldoK = kevin.getTarjeta().getSaldo();

        int cantidad = e.cancelacion.cancelarFuncion(e.admin(), 13, ahora);

        assertEquals(2, cantidad);
        assertEquals(EstadoFuncion.CANCELADA, f.getEstado());
        assertEquals(saldoN + 40000, nicolas.getTarjeta().getSaldo(), 0.001);
        assertEquals(saldoK + 20000, kevin.getTarjeta().getSaldo(), 0.001);
        assertTrue(f.getAsiento("A1").estaLibre());
    }

    @Test
    void soloElAdministradorCancelaFunciones() {
        Funcion f = e.funcion(11, 1, "Avengers", ahora.plusHours(30));

        assertThrows(AccesoDenegadoException.class,
                () -> e.cancelacion.cancelarFuncion(nicolas, 11, ahora));
        assertEquals(EstadoFuncion.PROGRAMADA, f.getEstado());
    }

    @Test
    void funcionSinComprasDevuelveCero() {
        e.funcion(11, 1, "Avengers", ahora.plusHours(30));
        assertEquals(0, e.cancelacion.cancelarFuncion(e.admin(), 11, ahora));
    }
}