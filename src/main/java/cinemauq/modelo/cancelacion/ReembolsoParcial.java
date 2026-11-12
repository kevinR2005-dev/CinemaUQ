package cinemauq.modelo.cancelacion;

public class ReembolsoParcial implements PoliticaReembolso {
    private final double porcentaje;

    public ReembolsoParcial(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcular(double valorPagado) {
        return valorPagado * porcentaje / 100;
    }
}
