package test;

import cinemauq.modelo.precio.CalculadorPrecio;
import cinemauq.modelo.precio.CargoServicio;
import cinemauq.modelo.precio.DescuentoPuntos;
import cinemauq.modelo.precio.PrecioBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DescuentoPuntosTest {

    @Test
    void descuentoPuntosNoDejaElTotalNegativo() {
        CalculadorPrecio precio = new DescuentoPuntos(new PrecioBase(20000), 30000);
        assertEquals(0, precio.calcular(), 0.001);
    }

    @Test
    void canjeAntesDelCargoConservaElCargo() {
        CalculadorPrecio precio = new PrecioBase(20000);
        precio = new DescuentoPuntos(precio, 20000);
        precio = new CargoServicio(precio, 2000);
        assertEquals(2000, precio.calcular(), 0.001);
    }
}
