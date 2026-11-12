package test;

import cinemauq.evento.*;
import cinemauq.modelo.Cliente;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ObservadorTest {

    private final Escenario e = new Escenario();

    @Test
    void publicarLlegaATodosLosObservadores() {
        Cliente c = e.cliente(1, "Nicolas", 0);
        e.publicador.publicar(new EventoCine(TipoEvento.COMPRA, c, "Compra #1", LocalDateTime.now()));

        assertEquals(1, e.registro.getEventos().size());
        assertEquals(1, e.notificador.obtener(c).size());
        assertTrue(e.notificador.obtener(c).get(0).contains("Compra #1"));
    }

    @Test
    void unObservadorQueFallaNoAfectaALosDemas() {
        PublicadorEventos pub = new PublicadorEventos();
        RegistroActividad registro = new RegistroActividad();
        pub.registrar(ev -> { throw new RuntimeException("falla a proposito"); });
        pub.registrar(registro);

        assertDoesNotThrow(() ->
                pub.publicar(new EventoCine(TipoEvento.RECARGA, null, "x", LocalDateTime.now())));
        assertEquals(1, registro.getEventos().size());
    }

    @Test
    void clienteSinNotificacionesTieneListaVacia() {
        Cliente c = e.cliente(1, "Nicolas", 0);
        assertTrue(e.notificador.obtener(c).isEmpty());
    }

    @Test
    void notificacionesSeSeparanPorCliente() {
        Cliente n = e.cliente(1, "Nicolas", 0);
        Cliente k = e.cliente(2, "Kevin", 0);
        e.publicador.publicar(new EventoCine(TipoEvento.COMPRA, n, "uno", LocalDateTime.now()));

        assertEquals(1, e.notificador.obtener(n).size());
        assertTrue(e.notificador.obtener(k).isEmpty());
    }

    @Test
    void listasNoSeModificanDesdeAfuera() {
        Cliente c = e.cliente(1, "Nicolas", 0);
        e.publicador.publicar(new EventoCine(TipoEvento.COMPRA, c, "uno", LocalDateTime.now()));

        assertThrows(UnsupportedOperationException.class, () -> e.notificador.obtener(c).add("x"));
        assertThrows(UnsupportedOperationException.class, () -> e.registro.getEventos().clear());
    }

    @Test
    void recargaPublicaEventoPorElServicioReal() {
        Cliente c = e.cliente(1, "Nicolas", 0);
        new cinemauq.servicio.TarjetaServicioReal(e.publicador).recargar(c, c, 50000);

        assertEquals(TipoEvento.RECARGA, e.registro.getEventos().get(0).getTipo());
        assertEquals(50000, c.getTarjeta().getSaldo(), 0.001);
    }

    @Test
    void recargaInvalidaNoPublicaNada() {
        Cliente c = e.cliente(1, "Nicolas", 0);
        assertThrows(IllegalArgumentException.class,
                () -> new cinemauq.servicio.TarjetaServicioReal(e.publicador).recargar(c, c, -5));
        assertTrue(e.registro.getEventos().isEmpty());
    }
}