package cinemauq.servicio;

import cinemauq.excepcion.CredencialesInvalidasException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Usuario;
import cinemauq.repositorio.Repositorio;

import java.util.ArrayList;
import java.util.List;

public class AuthServicio {
    private final Repositorio<Usuario, Integer> usuarios;

    public AuthServicio(Repositorio<Usuario, Integer> usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario iniciarSesion(String correo, String contrasena) {
        for (Usuario u : usuarios.listar()) {
            if (u.getCorreo().equals(correo) && u.verificarContrasena(contrasena)) {
                return u;
            }
        }
        throw new CredencialesInvalidasException("Correo o contrasena incorrectos");
    }

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        for (Usuario u : usuarios.listar()) {
            if (u instanceof Cliente) {
                clientes.add((Cliente) u);
            }
        }
        return clientes;
    }
}