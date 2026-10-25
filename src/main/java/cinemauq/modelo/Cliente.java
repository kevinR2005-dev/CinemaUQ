package cinemauq.modelo;

import cinemauq.modelo.tarjeta.TarjetaVirtual;

public class Cliente extends Usuario {
    private final TarjetaVirtual tarjeta;

    public Cliente(int id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, Rol.CLIENTE);
        this.tarjeta = new TarjetaVirtual(); // saldo 0, ACTIVA
    }

    public TarjetaVirtual getTarjeta() { return tarjeta; }
}