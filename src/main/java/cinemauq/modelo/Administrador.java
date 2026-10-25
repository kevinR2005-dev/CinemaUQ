package cinemauq.modelo;

public class Administrador extends Usuario {
    public Administrador(int id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, Rol.ADMINISTRADOR);
    }
}