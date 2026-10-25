package cinemauq.servicio;

import cinemauq.excepcion.CredencialesInvalidasException;
import cinemauq.modelo.Administrador;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServicioTest {
    private AuthServicio auth;

    @BeforeEach
    void preparar() {
        // AJUSTA el constructor/metodos a tu RepositorioMemoria de la Entrega 1
        Repositorio<Usuario, Integer> repo = new RepositorioMemoria<>(Usuario::getId);
        repo.guardar(new Cliente(1, "Ana", "ana@mail.com", "1234"));
        repo.guardar(new Administrador(2, "Admin", "admin@cinemauq.com", "admin123"));
        auth = new AuthServicio(repo);
    }

    @Test
    void loginCorrectoDevuelveUsuario() {
        Usuario u = auth.iniciarSesion("ana@mail.com", "1234");
        assertEquals("Ana", u.getNombre());
    }

    @Test
    void contrasenaIncorrectaLanzaExcepcion() {
        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> auth.iniciarSesion("ana@mail.com", "mala"));
        assertEquals("Correo o contrasena incorrectos", ex.getMessage());
    }

    @Test
    void correoInexistenteLanzaExcepcion() {
        assertThrows(CredencialesInvalidasException.class,
                () -> auth.iniciarSesion("nadie@mail.com", "1234"));
    }

    @Test
    void listarClientesNoIncluyeAdministrador() {
        assertEquals(1, auth.listarClientes().size());
    }
}