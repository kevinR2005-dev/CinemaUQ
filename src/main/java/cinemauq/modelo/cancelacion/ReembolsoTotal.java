package cinemauq.modelo.cancelacion;

public class ReembolsoTotal implements PoliticaReembolso {
    @Override
    public double calcular(double valorPagado) {
        return valorPagado;
    }
}
