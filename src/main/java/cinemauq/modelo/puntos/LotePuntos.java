package cinemauq.modelo.puntos;

import java.time.LocalDate;

public class LotePuntos {

    private final int puntosObtenidos;
    private int puntosRestantes;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;
    private final int origenFactura;

    public LotePuntos (int puntos, LocalDate fechaObtencion, int origenFactura) {
        this(puntos, fechaObtencion, fechaObtencion.plusYears(1), origenFactura);
    }

    public LotePuntos (int puntos, LocalDate fechaObtencion, LocalDate fechaVencimiento, int origenFactura){

        this.puntosObtenidos = puntos;
        this.puntosRestantes = puntos;
        this.fechaObtencion = fechaObtencion;
        this.fechaVencimiento = fechaVencimiento;
        this.origenFactura = origenFactura;
    }

    public boolean estaVigente (LocalDate hoy){
        return !hoy.isAfter (fechaVencimiento);
    }

    public void consumir (int puntos){
        if (puntos <= 0 || puntos > puntosRestantes){
            throw new IllegalStateException("Cantidad de puntos invalida");
        }
        puntosRestantes -= puntos;
    }

    public void anular(){
        puntosRestantes = 0;
    }

    public int getPuntosObtenidos() {
        return puntosObtenidos;
    }
    public int getPuntosRestantes() {
        return puntosRestantes;
    }
    public LocalDate getFechaObtencion() {
        return fechaObtencion;
    }
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }
    public int getOrigenFactura() {
        return origenFactura;
    }
}
