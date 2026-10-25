package cinemauq.servicio;

import cinemauq.excepcion.AccesoDenegadoException;
import cinemauq.modelo.Administrador;
import cinemauq.modelo.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TarjetaServicioProxyTest {
    private TarjetaServicio proxy;
    private Cliente cliente;
    private Administrador admin;

    @BeforeEach
    void preparar() {
        proxy = new TarjetaServicioProxy(new TarjetaServicioReal());
        cliente = new Cliente(1, "Ana", "ana@mail.com", "1234");
        admin = new Administrador(2, "Admin", "admin@cinemauq.com", "admin123");
    }

    @Test
    void adminPuedeRecargar() {
        proxy.recargar(admin, cliente, 20000);
        assertEquals(20000, proxy.consultarSaldo(cliente));
        assertEquals(1, proxy.consultarMovimientos(cliente).size());
    }

    @Test
    void clienteNoPuedeRecargar() {
        AccesoDenegadoException ex = assertThrows(AccesoDenegadoException.class,
                () -> proxy.recargar(cliente, cliente, 20000));
        assertEquals("Solo el administrador puede recargar", ex.getMessage());
        assertEquals(0, proxy.consultarSaldo(cliente)); // no se recargo
    }

    @Test
    void consultasFuncionanParaCualquierCliente() {
        assertEquals(0, proxy.consultarSaldo(cliente));
        assertTrue(proxy.consultarMovimientos(cliente).isEmpty());
    }
}
