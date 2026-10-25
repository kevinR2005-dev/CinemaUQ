package cinemauq.servicio;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.repositorio.Repositorio;

import java.util.List;

public class AuthServicio {
    private final Repositorio<Usuario, Integer> usuarios;

    public AuthServicio(Repositorio<Usuario, Integer> usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario iniciarSesion(String correo, String contrasena) {
        return null; // se completa en el hito 2
    }

    public List<Cliente> listarClientes() {
        return null; // se completa en el hito 2
    }
}