package cinemauq.modelo;

import cinemauq.excepcion.AsientoNoDisponibleException;

public class AsientoFuncion {

    private final String codigo;      // Ejemplo: "A1", "J12"
    private EstadoAsiento estado;

    public AsientoFuncion(String codigo) {
        this.codigo = codigo;
        this.estado = EstadoAsiento.LIBRE;   // Todo asiento nace libre
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }



    public boolean estaLibre() {
        return estado == EstadoAsiento.LIBRE;
    }


    public void reservar() {
        if (estado == EstadoAsiento.VENDIDO) {
            throw new AsientoNoDisponibleException("El asiento " + codigo + " ya está vendido");
        }
        estado = EstadoAsiento.VENDIDO;
    }


    @Override
    public AsientoFuncion clone() {
        return new AsientoFuncion(this.codigo);
    }
}