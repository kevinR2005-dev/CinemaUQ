package main.modelo;

public class Combo {
    private final String nombre;
    private final double precio;

    public Combo( String nombre, double precio){
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getNombre(){
        return nombre;
    }

    public double getPrecio(){
        return precio;
    }
}
