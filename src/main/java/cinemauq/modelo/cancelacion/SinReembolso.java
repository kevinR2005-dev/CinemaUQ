package cinemauq.modelo.cancelacion;

import cinemauq.excepcion.CancelacionNoPermitidaException;

public class SinReembolso implements PoliticaReembolso {
    @Override
    public double calcular(double valorPagado) {
        throw new CancelacionNoPermitidaException(
                "No se permite cancelar con menos de 2 horas de anticipacion");
    }
}