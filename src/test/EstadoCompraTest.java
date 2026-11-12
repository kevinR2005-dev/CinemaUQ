package test;
import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.modelo.estado.Utilizada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadoCompraTest {

    // La funcion es el 10 de julio de 2026 a las 19:00; el "reloj" se pasa a mano
    private final LocalDateTime antesDeLaFuncion = LocalDateTime.of(2026, 7, 10, 12, 0);
    private final LocalDateTime despuesDeLaFuncion = LocalDateTime.of(2026, 7, 11, 12, 0);

    private Funcion funcion;
    private Compra compra;

    @BeforeEach
    void preparar() {
        Cliente cliente = new Cliente(1, "Ana", "ana@correo.com", "1234");
        Pelicula pelicula = new Pelicula(1, "Avengers", 143, "Accion");
        funcion = new Funcion(1, pelicula, new Sala(1, 8, 12),
                LocalDate.of(2026, 7, 10), LocalTime.of(19, 0), 20000);
        compra = new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion).conAsiento("A1").build();
    }

    // Toda compra nueva nace PAGADA
    @Test
    void laCompraNaceEnEstadoPagada() {
        assertEquals("PAGADA", compra.getEstado().getNombre());
    }

    // Pagada -> Cancelada
    @Test
    void cancelarUnaCompraPagadaLaPasaACancelada() {
        compra.cancelar();

        assertEquals("CANCELADA", compra.getEstado().getNombre());
    }

    // RN19: no se puede cancelar dos veces
    @Test
    void cancelarDosVecesLanzaExcepcion() {
        compra.cancelar();

        assertThrows(CancelacionNoPermitidaException.class, () -> compra.cancelar());
    }

    // RN21: una compra utilizada no se puede cancelar
    @Test
    void cancelarUnaCompraUtilizadaLanzaExcepcion() {
        compra.setEstado(new Utilizada());

        assertThrows(CancelacionNoPermitidaException.class, () -> compra.cancelar());
    }

    // RN21: si la funcion ya se realizo, la compra pagada pasa a UTILIZADA
    @Test
    void actualizarEstadoDespuesDeLaFuncionLaPasaAUtilizada() {
        compra.actualizarEstado(despuesDeLaFuncion);

        assertEquals("UTILIZADA", compra.getEstado().getNombre());
    }

    // Si la funcion todavia no ocurre, sigue PAGADA
    @Test
    void actualizarEstadoAntesDeLaFuncionNoLaCambia() {
        compra.actualizarEstado(antesDeLaFuncion);

        assertEquals("PAGADA", compra.getEstado().getNombre());
    }

    // Una funcion cancelada por el cine no se considera "realizada"
    @Test
    void siLaFuncionFueCanceladaLaCompraNoPasaAUtilizada() {
        funcion.cancelar();

        compra.actualizarEstado(despuesDeLaFuncion);

        assertEquals("PAGADA", compra.getEstado().getNombre());
    }

    // Una compra ya cancelada no cambia de estado aunque pase la funcion
    @Test
    void unaCompraCanceladaNoPasaAUtilizada() {
        compra.cancelar();

        compra.actualizarEstado(despuesDeLaFuncion);

        assertEquals("CANCELADA", compra.getEstado().getNombre());
    }

    // Despues de pasar la funcion, ya no se puede cancelar
    @Test
    void despuesDeLaFuncionYaNoSePuedeCancelar() {
        compra.actualizarEstado(despuesDeLaFuncion);

        assertThrows(CancelacionNoPermitidaException.class, () -> compra.cancelar());
    }
}