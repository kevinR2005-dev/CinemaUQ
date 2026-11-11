package cinemauq.modelo;

import cinemauq.modelo.puntos.CuentaPuntos;
import cinemauq.modelo.tarjeta.TarjetaVirtual;

public class Cliente extends Usuario {
    private final TarjetaVirtual tarjeta;
    private final CuentaPuntos puntos = new CuentaPuntos();

    public Cliente(int id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, Rol.CLIENTE);
        this.tarjeta = new TarjetaVirtual(); // saldo 0, ACTIVA
    }

    public TarjetaVirtual getTarjeta() { return tarjeta; }
    public CuentaPuntos getPuntos() {
        return puntos;
    }
}