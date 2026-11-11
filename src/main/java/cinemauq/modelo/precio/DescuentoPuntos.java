package cinemauq.modelo.precio;

public class DescuentoPuntos implements CalculadorPrecio {

    private final CalculadorPrecio envuelto;
    private final double valorCanjeado;

    public DescuentoPuntos(CalculadorPrecio envuelto, double valorCanjeado) {
        if (valorCanjeado < 0) {
            throw new IllegalArgumentException("El valor canjeado no puede ser negativo");
        }
        this.envuelto = envuelto;
        this.valorCanjeado = valorCanjeado;
    }

    @Override
    public double calcular() {
        return Math.max(0, envuelto.calcular() - valorCanjeado);
    }
}
