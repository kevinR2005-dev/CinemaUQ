package cinemauq.modelo;

public class Sala {

    private final int numero;
    private final int filas;
    private final int columnas;

    public Sala(int numero, int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("Filas y columnas deben ser mayores a 0");
        }
        this.numero = numero;
        this.filas = filas;
        this.columnas = columnas;
    }

    public int getNumero() {
        return numero;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    /** Total de asientos de la sala. */
    public int getCapacidad() {
        return filas * columnas;
    }
}