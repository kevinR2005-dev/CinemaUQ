package test;

import cinemauq.modelo.confiteria.CategoriaProducto;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.confiteria.Producto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ConfiteriaCompositeTest {
    private final Producto gaseosa = new Producto("Gaseosa", 5000, CategoriaProducto.BEBIDA);
    private final Producto crispetas = new Producto("Crispetas", 12000, CategoriaProducto.COMIDA);
    private final Producto chocolate = new Producto("Chocolate", 3000, CategoriaProducto.DULCE);


    @Test
    void productoDevuelveSuPrecio() {
        assertEquals(5000, gaseosa.getPrecio(), 0.001);
    }


    @Test
    void comboSumaLosPreciosDeSusProductos() {
        Combo combo = new Combo("Combo Clasico");
        combo.agregar(gaseosa);
        combo.agregar(crispetas);

        assertEquals(17000, combo.getPrecio(), 0.001);
    }


    @Test
    void comboAnidadoSumaTambienLoDelComboInterno() {
        Combo interno = new Combo("Combo Clasico");
        interno.agregar(gaseosa);
        interno.agregar(crispetas);

        Combo externo = new Combo("Combo Familiar");
        externo.agregar(interno);
        externo.agregar(chocolate);

        assertEquals(20000, externo.getPrecio(), 0.001);
    }


    @Test
    void productoYComboSeUsanIgualComoItemConfiteria() {
        Combo combo = new Combo("Combo Clasico");
        combo.agregar(gaseosa);
        combo.agregar(crispetas);

        List<ItemConfiteria> items = List.of(chocolate, combo);
        double total = 0;
        for (ItemConfiteria item : items) {
            total += item.getPrecio();
        }

        assertEquals(20000, total, 0.001);
    }


    @Test
    void comboNoPuedeContenerseASiMismo() {
        Combo combo = new Combo("Combo Loco");
        assertThrows(IllegalArgumentException.class, () -> combo.agregar(combo));
    }
}
