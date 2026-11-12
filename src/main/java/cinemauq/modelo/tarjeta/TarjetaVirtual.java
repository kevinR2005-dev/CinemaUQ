package cinemauq.modelo.tarjeta;

import cinemauq.excepcion.SaldoInsuficienteException;
import cinemauq.excepcion.TarjetaInactivaException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TarjetaVirtual {
    private double saldo;
    private EstadoTarjeta estado;
    private final List<Movimiento> movimientos = new ArrayList<>();

    public TarjetaVirtual() {
        this(0);
    }

    public TarjetaVirtual(double saldoInicial) {
        this.saldo = saldoInicial;
        this.estado = EstadoTarjeta.ACTIVA;
    }

    // Solo verifica, no modifica nada (RN07)
    public void verificarPago(double monto) {
        if (estado != EstadoTarjeta.ACTIVA) {
            throw new TarjetaInactivaException("La tarjeta esta bloqueada");
        }
        if (saldo < monto) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }
    }

    public void debitar(double monto, String detalle) {
        verificarPago(monto);
        saldo -= monto;
        movimientos.add(new Movimiento(LocalDateTime.now(), TipoMovimiento.PAGO,
                monto, saldo, detalle)); // RN08
    }

    // Existente: delega con RECARGA
    public void acreditar(double monto, String detalle) {
        acreditar(monto, detalle, TipoMovimiento.RECARGA);
    }

    // Tipo RECARGA o REEMBOLSO, con la fecha de ahora
    public void acreditar(double monto, String detalle, TipoMovimiento tipo) {
        acreditar(monto, detalle, tipo, LocalDateTime.now());
    }

    // Con fecha, para datos históricos. Aquí están las validaciones y el cambio de saldo
    public void acreditar(double monto, String detalle, TipoMovimiento tipo, LocalDateTime fecha) {
        if (tipo != TipoMovimiento.RECARGA && tipo != TipoMovimiento.REEMBOLSO) {
            throw new IllegalArgumentException("Solo se acredita por RECARGA o REEMBOLSO");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        saldo += monto;
        movimientos.add(new Movimiento(fecha, tipo, monto, saldo, detalle)); // RN08
    }

    public void bloquear() { estado = EstadoTarjeta.BLOQUEADA; }
    public void activar() { estado = EstadoTarjeta.ACTIVA; }

    public double getSaldo() { return saldo; }
    public EstadoTarjeta getEstado() { return estado; }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}