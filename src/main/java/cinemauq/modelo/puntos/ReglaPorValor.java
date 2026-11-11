package cinemauq.modelo.puntos;

public class ReglaPorValor implements ReglaAcumulacion{

    private final double valorPorPunto;

    public ReglaPorValor(double valorPorPunto) {
        if (valorPorPunto <= 0) {
            throw new IllegalArgumentException("El valor por punto debe ser positivo");
        }
        this.valorPorPunto = valorPorPunto;
    }

    @Override
    public int calcular(double montoPagado) {
        return (int) Math.floor(montoPagado / valorPorPunto);
    }
}
