package test;

import cinemauq.modelo.precio.CalculadorPrecio;
import cinemauq.modelo.precio.CargoServicio;
import cinemauq.modelo.precio.DescuentoPromocion;
import cinemauq.modelo.precio.PrecioBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class CalculadorPrecioTest {

    @Test
    void precioBaseDevuelveElSubtotal() {
        assertEquals(40000, new PrecioBase(40000).calcular(), 0.001);
    }

    @Test
    void soloCargoSumaElCargoAlSubtotal() {
        CalculadorPrecio precio = new CargoServicio(new PrecioBase(40000), 2000);
        assertEquals(42000, precio.calcular(), 0.001);
    }

    @Test
    void descuentoRestaElPorcentaje() {
        CalculadorPrecio precio = new DescuentoPromocion(new PrecioBase(40000), 10);
        assertEquals(36000, precio.calcular(), 0.001);
    }

    @Test
    void descuentoYCargoSeApilan() {
        CalculadorPrecio precio = new PrecioBase(40000);
        precio = new DescuentoPromocion(precio, 10);
        precio = new CargoServicio(precio, 2000);

        assertEquals(38000, precio.calcular(), 0.001);
    }

    @Test
    void elOrdenDeLasCapasCambiaElResultado() {
        CalculadorPrecio precio = new PrecioBase(40000);
        precio = new CargoServicio(precio, 2000);
        precio = new DescuentoPromocion(precio, 10);

        assertEquals(37800, precio.calcular(), 0.001);
    }

    @Test
    void descuentoConPorcentajeInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> new DescuentoPromocion(new PrecioBase(40000), 150));
        assertThrows(IllegalArgumentException.class,
                () -> new DescuentoPromocion(new PrecioBase(40000), -5));
    }
}
