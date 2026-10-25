package cinemauq.app;

import cinemauq.modelo.*;
import cinemauq.modelo.confiteria.CategoriaProducto;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.confiteria.Producto;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatosIniciales {

    private final List<Cliente> clientes = new ArrayList<>();
    private Administrador administrador;
    private final List<Producto> productos = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();

    public void cargar(Repositorio<Pelicula, Integer> peliculas,
                       Repositorio<Funcion, Integer> funciones,
                       Repositorio<Usuario, Integer> usuarios) {

        // Peliculas
        Pelicula avengers = new Pelicula(1, "Avengers", 143, "Accion");
        Pelicula interestelar = new Pelicula(2, "Interestelar", 169, "Ciencia ficcion");
        Pelicula dune = new Pelicula(3, "Dune", 155, "Ciencia ficcion");
        peliculas.guardar(avengers);
        peliculas.guardar(interestelar);
        peliculas.guardar(dune);

        // Salas y funciones
        Sala sala1 = new Sala(1, 8, 12);
        Sala sala2 = new Sala(2, 6, 10);
        LocalDate hoy = LocalDate.now();
        funciones.guardar(new Funcion(1, avengers, sala1, hoy.plusDays(1), LocalTime.of(19, 0), 20000));
        funciones.guardar(new Funcion(2, interestelar, sala1, hoy.plusDays(1), LocalTime.of(21, 30), 20000));
        funciones.guardar(new Funcion(3, dune, sala2, hoy.plusDays(2), LocalTime.of(18, 0), 18000));
        funciones.guardar(new Funcion(4, avengers, sala2, hoy.plusDays(2), LocalTime.of(20, 30), 18000));

        // Clientes: uno con saldo alto y otro con saldo bajo (para probar saldo insuficiente)
        Cliente nicolas = new Cliente(1, "Nicolas Sanchez", "nicolas@correo.com", "1234");
        nicolas.getTarjeta().acreditar(500000, "Saldo inicial");
        Cliente kevin = new Cliente(2, "Kevin Robin", "kevin@correo.com", "1234");
        kevin.getTarjeta().acreditar(5000, "Saldo inicial");
        clientes.add(nicolas);
        clientes.add(kevin);

        // Administrador
        administrador = new Administrador(3, "Administrador", "admin@cinemauq.com", "admin123");

        usuarios.guardar(nicolas);
        usuarios.guardar(kevin);
        usuarios.guardar(administrador);

        // Productos
        Producto gaseosa = new Producto("Gaseosa", 5000, CategoriaProducto.BEBIDA);
        Producto crispetas = new Producto("Crispetas", 12000, CategoriaProducto.COMIDA);
        Producto perro = new Producto("Perro caliente", 9000, CategoriaProducto.COMIDA);
        Producto chocolate = new Producto("Chocolate", 3000, CategoriaProducto.DULCE);
        Producto agua = new Producto("Agua", 3500, CategoriaProducto.BEBIDA);
        productos.add(gaseosa);
        productos.add(crispetas);
        productos.add(perro);
        productos.add(chocolate);
        productos.add(agua);

        // Combos armados con Composite
        Combo clasico = new Combo("Combo Clasico");
        clasico.agregar(gaseosa);
        clasico.agregar(crispetas);

        Combo familiar = new Combo("Combo Familiar");   // contiene otro combo
        familiar.agregar(clasico);
        familiar.agregar(perro);
        familiar.agregar(chocolate);

        combos.add(clasico);
        combos.add(familiar);
    }

    public List<Cliente> getClientes() { return clientes; }

    public Administrador getAdministrador() { return administrador; }

    public List<Producto> getProductos() { return productos; }

    public List<Combo> getCombos() { return combos; }

    // Productos + combos, para el catalogo de la confiteria
    public List<ItemConfiteria> getCatalogo() {
        List<ItemConfiteria> catalogo = new ArrayList<>(productos);
        catalogo.addAll(combos);
        return catalogo;
    }
}
