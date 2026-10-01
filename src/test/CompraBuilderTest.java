package test;

import cinemauq.modelo.Cliente;
import cinemauq.modelo.Combo;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
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
        cliente = new Cliente(1, "Kevin", "Kevin.robim@uqvirtual.edu.co");
        Pelicula pelicula = new Pelicula(1, "Dune", 150, "Ciencia ficción");
        Sala sala = new Sala(1, 10, 12);
        funcion = new Funcion(1, pelicula, sala, LocalDate.of(2026, 11, 1), LocalTime.of(18, 0), 15000);
    }

    @Test
    void construyeCompraValidaYCalculaTotalConCombo() {
        Compra compra = new Compra.Builder()
                .conCliente(cliente)
                .conFuncion(funcion)
                .conAsiento("A1")
                .conAsiento("A2")
                .conCombo(new Combo("Combo clásico", 8000))
                .conNumeroFactura(1)
                .build();

        assertEquals(1, compra.getNumeroFactura());
        assertEquals(2, compra.getAsientos().size());
        assertEquals(38000, compra.calcularTotal(), 0.001); // 15000*2 + 8000
    }

    @Test
    void totalSinComboSoloSumaEntradas() {
        Compra compra = new Compra.Builder()
                .conCliente(cliente)
                .conFuncion(funcion)
                .conAsiento("B1")
                .build();

        assertEquals(15000, compra.calcularTotal(), 0.001);
    }

    @Test
    void ochoAsientosEsElMaximoPermitido() {
        Compra.Builder b = new Compra.Builder().conCliente(cliente).conFuncion(funcion);
        for (int i = 1; i <= 8; i++) {
            b.conAsiento("C" + i);
        }
        assertEquals(8, b.build().getAsientos().size());
    }

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

    @Test
    void masDeOchoAsientosLanzaExcepcion() {
        Compra.Builder b = new Compra.Builder().conCliente(cliente).conFuncion(funcion);
        for (int i = 1; i <= 9; i++) {
            b.conAsiento("D" + i);
        }
        assertThrows(IllegalStateException.class, b::validar);
    }
}

