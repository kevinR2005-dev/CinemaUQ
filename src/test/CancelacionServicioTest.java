package test;

import cinemauq.evento.NotificadorCliente;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.RegistroActividad;
import cinemauq.evento.TipoEvento;
import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.excepcion.CancelacionNoPermitidaException;
import cinemauq.excepcion.EntidadNoEncontradaException;
import cinemauq.modelo.Administrador;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.EstadoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.puntos.ReglaPorValor;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.CancelacionServicio;
import cinemauq.servicio.PagoServicio;
import cinemauq.servicio.PuntosServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CancelacionServicioTest {

    private Repositorio<Compra, Integer> compras;
    private Repositorio<Funcion, Integer> funciones;
    private PublicadorEventos publicador;
    private NotificadorCliente notificador;
    private RegistroActividad registro;
    private PagoServicio pago;
    private PuntosServicio puntos;
    private CancelacionServicio cancelacion;

    private Cliente nicolas;
    private LocalDateTime ahora;

    @BeforeEach
    void preparar() {
        compras = new RepositorioMemoria<>(Compra::getNumeroFactura);
        funciones = new RepositorioMemoria<>(Funcion::getId);
        publicador = new PublicadorEventos();
        notificador = new NotificadorCliente();
        registro = new RegistroActividad();
        publicador.registrar(notificador);
        publicador.registrar(registro);
        pago = new PagoServicio();
        puntos = new PuntosServicio(new ReglaPorValor(1000), publicador);
        cancelacion = new CancelacionServicio(compras, funciones, pago, puntos, publicador);

        nicolas = crearCliente(1, "Nicolas", 500000);
        ahora = LocalDateTime.now();
    }

    // ---------- armado de datos ----------

    private Cliente crearCliente(int id, String nombre, double saldo) {
        Cliente c = new Cliente(id, nombre, nombre.toLowerCase() + "@correo.com", "1234");
        c.getTarjeta().acreditar(saldo, "Saldo inicial");
        return c;
    }

    private Funcion crearFuncion(int id, LocalDateTime cuando) {
        Funcion f = new Funcion(id, new Pelicula(1, "Avengers", 143, "Accion"), new Sala(1, 5, 5),
                cuando.toLocalDate(), cuando.toLocalTime(), 20000);
        funciones.guardar(f);
        return f;
    }

    // Compra ya pagada: reserva, cobra, acumula puntos y guarda
    private Compra crearCompra(Cliente c, Funcion f, int factura, String asiento, double total) {
        f.getAsiento(asiento).reservar();
        pago.cobrar(c, total, "Compra funcion " + f.getId());
        int ganados = puntos.acumular(c, total, factura, ahora.toLocalDate());
        Compra compra = new Compra.Builder()
                .conCliente(c).conFuncion(f).conAsiento(asiento)
                .conNumeroFactura(factura).conTotal(total)
                .conPuntosGanados(ganados).conFecha(ahora)
                .build();
        compras.guardar(compra);
        return compra;
    }

    private Usuario crearAdmin() {
        return new Administrador(900, "Admin", "admin@cinemauq.com", "admin123");
    }

    // ---------- pruebas ----------

    @Test
    void masDe24HorasReembolsa100() {
        Funcion f = crearFuncion(11, ahora.plusHours(30));
        crearCompra(nicolas, f, 1, "A1", 40000);
        double saldoAntes = nicolas.getTarjeta().getSaldo();

        double reembolso = cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(40000, reembolso, 0.001);
        assertEquals(saldoAntes + 40000, nicolas.getTarjeta().getSaldo(), 0.001);
        assertTrue(f.getAsiento("A1").estaLibre());
        assertEquals("CANCELADA", compras.buscarPorId(1).getEstado().getNombre());
    }

    @Test
    void entre2y24HorasReembolsa80() {
        Funcion f = crearFuncion(12, ahora.plusHours(5));
        crearCompra(nicolas, f, 1, "A1", 40000);

        assertEquals(32000, cancelacion.cancelarCompra(nicolas, 1, ahora), 0.001);
    }

    @Test
    void menosDe2HorasSeRechazaYNoCambiaNada() {
        Funcion f = crearFuncion(13, ahora.plusHours(1));
        crearCompra(nicolas, f, 1, "A1", 40000);
        double saldoAntes = nicolas.getTarjeta().getSaldo();
        int movimientosAntes = nicolas.getTarjeta().getMovimientos().size();

        assertThrows(CancelacionNoPermitidaException.class,
                () -> cancelacion.cancelarCompra(nicolas, 1, ahora));

        // Verificar antes de cambiar: todo queda igual
        assertEquals("PAGADA", compras.buscarPorId(1).getEstado().getNombre());
        assertFalse(f.getAsiento("A1").estaLibre());
        assertEquals(saldoAntes, nicolas.getTarjeta().getSaldo(), 0.001);
        assertEquals(movimientosAntes, nicolas.getTarjeta().getMovimientos().size());
        assertEquals(40, nicolas.getPuntos().puntosVigentes(LocalDate.now()));
    }

    @Test
    void cancelarAnulaLosPuntosGanados() {
        Funcion f = crearFuncion(11, ahora.plusHours(30));
        crearCompra(nicolas, f, 1, "A1", 40000);
        assertEquals(40, nicolas.getPuntos().puntosVigentes(LocalDate.now()));

        cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(0, nicolas.getPuntos().puntosVigentes(LocalDate.now()));
    }

    @Test
    void cancelarDosVecesLanzaExcepcionDelState() {
        Funcion f = crearFuncion(11, ahora.plusHours(30));
        crearCompra(nicolas, f, 1, "A1", 40000);
        cancelacion.cancelarCompra(nicolas, 1, ahora);
        double saldo = nicolas.getTarjeta().getSaldo();

        CancelacionNoPermitidaException ex = assertThrows(CancelacionNoPermitidaException.class,
                () -> cancelacion.cancelarCompra(nicolas, 1, ahora));

        assertEquals("La compra ya esta cancelada", ex.getMessage());
        assertEquals(saldo, nicolas.getTarjeta().getSaldo(), 0.001); // reembolso una sola vez (RN19)
    }

    @Test
    void otroClienteNoPuedeCancelar() {
        Cliente kevin = crearCliente(2, "Kevin", 100000);
        Funcion f = crearFuncion(11, ahora.plusHours(30));
        crearCompra(nicolas, f, 1, "A1", 40000);

        assertThrows(AccesoDenegadoException.class, () -> cancelacion.cancelarCompra(kevin, 1, ahora));
        assertEquals("PAGADA", compras.buscarPorId(1).getEstado().getNombre());
    }

    @Test
    void facturaInexistenteLanzaExcepcion() {
        assertThrows(EntidadNoEncontradaException.class,
                () -> cancelacion.cancelarCompra(nicolas, 999, ahora));
    }

    @Test
    void cancelarCompraPublicaEventoYNotifica() {
        Funcion f = crearFuncion(11, ahora.plusHours(30));
        crearCompra(nicolas, f, 1, "A1", 40000);

        cancelacion.cancelarCompra(nicolas, 1, ahora);

        assertEquals(1, registro.getEventos().stream()
                .filter(ev -> ev.getTipo() == TipoEvento.CANCELACION).count());
        assertEquals(1, notificador.obtener(nicolas).size());
    }

    @Test
    void adminCancelaFuncionYReembolsaATodosAl100() {
        Cliente kevin = crearCliente(2, "Kevin", 500000);
        Funcion f = crearFuncion(13, ahora.plusHours(1)); // a 1 hora: si cancela el cine, igual es 100 %
        crearCompra(nicolas, f, 1, "A1", 40000);
        crearCompra(kevin, f, 2, "A2", 20000);
        double saldoN = nicolas.getTarjeta().getSaldo();
        double saldoK = kevin.getTarjeta().getSaldo();

        int cantidad = cancelacion.cancelarFuncion(crearAdmin(), 13, ahora);

        assertEquals(2, cantidad);
        assertEquals(EstadoFuncion.CANCELADA, f.getEstado());
        assertEquals(saldoN + 40000, nicolas.getTarjeta().getSaldo(), 0.001);
        assertEquals(saldoK + 20000, kevin.getTarjeta().getSaldo(), 0.001);
        assertTrue(f.getAsiento("A1").estaLibre());
    }

    @Test
    void soloElAdministradorCancelaFunciones() {
        Funcion f = crearFuncion(11, ahora.plusHours(30));

        assertThrows(AccesoDenegadoException.class,
                () -> cancelacion.cancelarFuncion(nicolas, 11, ahora));
        assertEquals(EstadoFuncion.PROGRAMADA, f.getEstado());
    }

    @Test
    void funcionSinComprasDevuelveCero() {
        crearFuncion(11, ahora.plusHours(30));
        assertEquals(0, cancelacion.cancelarFuncion(crearAdmin(), 11, ahora));
    }
}