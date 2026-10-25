package cinemauq.modelo;

public abstract class Usuario {
    private final int id;
    private final String nombre;
    private final String correo;
    private final String contrasena;
    private final Rol rol;

    public Usuario(int id, String nombre, String correo, String contrasena, Rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public Rol getRol() { return rol; }

    public boolean verificarContrasena(String c) {
        return contrasena.equals(c);
    }
}