package cinemauq.modelo.confiteria;

public class Producto implements ItemConfiteria {

    private final String nombre;
    private final double precio;
    private final CategoriaProducto categoria;

    public Producto (String nombre, double precio, CategoriaProducto categoria){
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }

    @Override
    public String getNombre(){
        return nombre;
    }

    @Override
    public double getPrecio(){
        return precio;
    }

    public CategoriaProducto getCategoria(){
        return categoria;
    }



}
