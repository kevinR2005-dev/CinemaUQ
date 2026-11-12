package test;

import cinemauq.evento.EventoCine;
import cinemauq.evento.NotificadorCliente;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.RegistroActividad;
import cinemauq.evento.TipoEvento;
import cinemauq.modelo.Cliente;
import cinemauq.servicio.TarjetaServicioReal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ObservadorTest {

    private PublicadorEventos publicador;
    private NotificadorCliente notificador;
    private RegistroActividad registro;

    @BeforeEach
    void preparar() {
        publicador = new PublicadorEventos();
        notificador = new NotificadorCliente();
        registro = new RegistroActividad();
        publicador.registrar(notificador);
        publicador.registrar(registro);
    }

    private Cliente crearCliente(int id, String nombre) {
        return new Cliente(id, nombre, nombre.toLowerCase() + "@correo.com", "1234");
    }

    @Test
    void publicarLlegaATodosLosObservadores() {
        Cliente c = crearCliente(1, "Nicolas");
        publicador.publicar(new EventoCine(TipoEvento.COMPRA, c, "Compra #1", LocalDateTime.now()));

        assertEquals(1, registro.getEventos().size());
        assertEquals(1, notificador.obtener(c).size());
        assertTrue(notificador.obtener(c).get(0).contains("Compra #1"));
    }

    @Test
    void unObservadorQueFallaNoAfectaALosDemas() {
        PublicadorEventos pub = new PublicadorEventos();
        RegistroActividad otroRegistro = new RegistroActividad();
        pub.registrar(ev -> { throw new RuntimeException("falla a proposito"); });
        pub.registrar(otroRegistro);

        assertDoesNotThrow(() ->
                pub.publicar(new EventoCine(TipoEvento.RECARGA, null, "x", LocalDateTime.now())));
        assertEquals(1, otroRegistro.getEventos().size());
    }

    @Test
    void clienteSinNotificacionesTieneListaVacia() {
        Cliente c = crearCliente(1, "Nicolas");
        assertTrue(notificador.obtener(c).isEmpty());
    }

    @Test
    void notificacionesSeSeparanPorCliente() {
        Cliente nicolas = crearCliente(1, "Nicolas");
        Cliente kevin = crearCliente(2, "Kevin");
        publicador.publicar(new EventoCine(TipoEvento.COMPRA, nicolas, "uno", LocalDateTime.now()));

        assertEquals(1, notificador.obtener(nicolas).size());
        assertTrue(notificador.obtener(kevin).isEmpty());
    }

    @Test
    void listasNoSeModificanDesdeAfuera() {
        Cliente c = crearCliente(1, "Nicolas");
        publicador.publicar(new EventoCine(TipoEvento.COMPRA, c, "uno", LocalDateTime.now()));

        assertThrows(UnsupportedOperationException.class, () -> notificador.obtener(c).add("x"));
        assertThrows(UnsupportedOperationException.class, () -> registro.getEventos().clear());
    }

    @Test
    void recargaPublicaEventoPorElServicioReal() {
        Cliente c = crearCliente(1, "Nicolas");
        new TarjetaServicioReal(publicador).recargar(c, c, 50000);

        assertEquals(TipoEvento.RECARGA, registro.getEventos().get(0).getTipo());
        assertEquals(50000, c.getTarjeta().getSaldo(), 0.001);
    }

    @Test
    void recargaInvalidaNoPublicaNada() {
        Cliente c = crearCliente(1, "Nicolas");
        assertThrows(IllegalArgumentException.class,
                () -> new TarjetaServicioReal(publicador).recargar(c, c, -5));
        assertTrue(registro.getEventos().isEmpty());
    }
}