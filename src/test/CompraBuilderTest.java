package test;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.modelo.confiteria.CategoriaProducto;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompraBuilderTest {

    private Cliente cliente;
    private Funcion funcion;

    @BeforeEach
    void preparar() {
        cliente = new Cliente(1, "Ana", "ana@correo.com", "1234");
        Pelicula pelicula = new Pelicula(1, "Avengers", 143, "Accion");
        funcion = new Funcion(1, pelicula, new Sala(1, 8, 12),
                LocalDate.now().plusDays(1), LocalTime.of(19, 0), 20000);
    }

    // RN04: faltan datos obligatorios
    @Test
    void sinClienteLanzaExcepcion() {
        Compra.Builder b = new Compra.Builder().conFuncion(funcion).conAsiento("A1");
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test
    void sinFuncionLanzaExcepcion() {
        Compra.Builder b = new Compra.Builder().conCliente(cliente).conAsiento("A1");
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test
    void sinAsientosLanzaExcepcion() {
        Compra.Builder b = new Compra.Builder().conCliente(cliente).conFuncion(funcion);
        assertThrows(IllegalStateException.class, b::build);
    }

    // RN03: maximo 8 asientos
    @Test
    void conNueveAsientosLanzaExcepcion() {
        Compra.Builder b = new Compra.Builder().conCliente(cliente).conFuncion(funcion);
        for (int i = 1; i <= 9; i++) {
            b.conAsiento("C" + i);
        }
        assertThrows(IllegalStateException.class, b::build);
    }

    // Compra minima: solo obligatorios
    @Test
    void compraMinimaSeCreaCorrectamente() {
        Compra compra = new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion).conAsiento("A1").build();

        assertEquals(1, compra.getAsientos().size());
        assertEquals(0, compra.getConfiteria().size());
        assertEquals(20000, compra.calcularSubtotal(), 0.001);
    }

    // Compra completa: obligatorios + confiteria (producto suelto y combo)
    @Test
    void compraCompletaIncluyeConfiteria() {
        Combo combo = new Combo("Combo Clasico");
        combo.agregar(new Producto("Gaseosa", 5000, CategoriaProducto.BEBIDA));
        combo.agregar(new Producto("Crispetas", 12000, CategoriaProducto.COMIDA));
        Producto chocolate = new Producto("Chocolate", 3000, CategoriaProducto.DULCE);

        Compra compra = new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion)
                .conAsiento("D7").conAsiento("D8")
                .conConfiteria(combo).conConfiteria(chocolate)
                .build();

        // 2 x 20000 + 17000 + 3000
        assertEquals(60000, compra.calcularSubtotal(), 0.001);
        assertEquals(2, compra.getConfiteria().size());
    }

    // La compra es inmutable: sus listas no se pueden modificar
    @Test
    void laListaDeAsientosNoSePuedeModificar() {
        Compra compra = new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion).conAsiento("A1").build();
        assertThrows(UnsupportedOperationException.class,
                () -> compra.getAsientos().add("A2"));
    }
}

