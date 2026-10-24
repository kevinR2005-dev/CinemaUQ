package cinemauq.modelo.precio;

public class CargoServicio implements CalculadorPrecio {

    private final CalculadorPrecio envuelto;
    private final double cargo;

    public CargoServicio (CalculadorPrecio envuelto, double cargo){
        this.envuelto = envuelto;
        this.cargo = cargo;
    }

    @Override
    public double calcular(){
        return envuelto.calcular() + cargo;
    }
}
