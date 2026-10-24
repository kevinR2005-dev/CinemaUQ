package cinemauq.modelo.precio;

public class DescuentoPromocion implements CalculadorPrecio {

    private final CalculadorPrecio envuelto;
    private final double porcentaje;

    public DescuentoPromocion (CalculadorPrecio envuelto, double porcentaje) {

        if (porcentaje < 0 || porcentaje > 100){
            throw new IllegalStateException("El porcentaje debe estar entre 0 y 100");
        }
        this.envuelto = envuelto;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcular(){
        return envuelto.calcular() * (1 - porcentaje / 100);
    }
}
