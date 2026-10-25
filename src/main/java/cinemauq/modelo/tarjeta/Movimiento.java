package cinemauq.modelo.tarjeta;

import java.time.LocalDateTime;

// Inmutable: todos los atributos son final y no hay setters
public class Movimiento {
    private final LocalDateTime fecha;
    private final TipoMovimiento tipo;
    private final double monto;
    private final double saldoResultante;
    private final String detalle;

    public Movimiento(LocalDateTime fecha, TipoMovimiento tipo, double monto,
                      double saldoResultante, String detalle) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() { return fecha; }
    public TipoMovimiento getTipo() { return tipo; }
    public double getMonto() { return monto; }
    public double getSaldoResultante() { return saldoResultante; }
    public String getDetalle() { return detalle; }
}