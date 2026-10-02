package cinemauq.app;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Combo;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatosIniciales {

    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Combo> combos = new ArrayList<>();

    // Crea los datos de demostración y los guarda en los repositorios
    public void cargar(Repositorio<Pelicula, Integer> peliculas,
                       Repositorio<Funcion, Integer> funciones) {

        // Películas
        Pelicula avengers = new Pelicula(1, "Avengers", 143, "Accion");
        Pelicula interestelar = new Pelicula(2, "Interestelar", 169, "Ciencia ficcion");
        Pelicula dune = new Pelicula(3, "Dune", 155, "Ciencia ficcion");
        peliculas.guardar(avengers);
        peliculas.guardar(interestelar);
        peliculas.guardar(dune);

        // Salas (número, filas, columnas)
        Sala sala1 = new Sala(1, 8, 12);
        Sala sala2 = new Sala(2, 6, 10);

        // Funciones (id, película, sala, fecha, hora, precio)
        LocalDate hoy = LocalDate.now();
        funciones.guardar(new Funcion(1, avengers, sala1, hoy.plusDays(1), LocalTime.of(19, 0), 20000));
        funciones.guardar(new Funcion(2, interestelar, sala1, hoy.plusDays(1), LocalTime.of(21, 30), 20000));
        funciones.guardar(new Funcion(3, dune, sala2, hoy.plusDays(2), LocalTime.of(18, 0), 18000));
        funciones.guardar(new Funcion(4, avengers, sala2, hoy.plusDays(2), LocalTime.of(20, 30), 18000));

        // Clientes
        clientes.add(new Cliente(1, "Nicolas Sanchez", "nicolas@correo.com"));
        clientes.add(new Cliente(2, "Kevin Robin", "kevin@correo.com"));

        // Combos
        combos.add(new Combo("Combo Clasico", 15000));
        combos.add(new Combo("Combo Familiar", 28000));
    }

    public List<Cliente> getClientes() { return clientes; }

    public List<Combo> getCombos() { return combos; }
}
