package cinemauq.modelo.precio;

public class PrecioBase implements CalculadorPrecio {

    private final double subtotal;

    public PrecioBase (double subtotal){
        this.subtotal = subtotal;
    }

    @Override
    public double calcular (){
        return subtotal;
    }
}
