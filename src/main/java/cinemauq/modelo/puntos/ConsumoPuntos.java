package cinemauq.modelo.puntos;

import java.time.LocalDate;

public class ConsumoPuntos {

    private final int puntos;
    private final LocalDate fechaVencimiento;

    public ConsumoPuntos(int puntos, LocalDate fechaVencimiento) {
        this.puntos = puntos;
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getPuntos() { return puntos; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
}
