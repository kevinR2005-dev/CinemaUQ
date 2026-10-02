package test;

import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.excepcion.EntidadNoEncontradaException;
import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FuncionCloneTest {

    private Pelicula pelicula;
    private Sala sala;
    private Funcion original;

    @BeforeEach
    void preparar() {
        pelicula = new Pelicula(1, "Interestelar", 169, "Ciencia ficción");
        sala = new Sala(1, 10, 12);   // 120 asientos: A1..J12
        original = new Funcion(1, pelicula, sala, LocalDate.of(2026, 10, 15), LocalTime.of(19, 30), 15000);
    }

    @Test
    void funcionNuevaTieneTodosLosAsientosLibres() {
        assertEquals(120, original.asientosLibres().size());
        assertEquals("A1", original.getAsiento("A1").getCodigo());
        assertEquals("J12", original.getAsiento("J12").getCodigo());
    }

    @Test
    void cloneNaceConTodosLosAsientosLibres() {
        original.getAsiento("A1").reservar();
        original.getAsiento("B2").reservar();

        Funcion copia = original.clone();

        // RN-05: la copia nace con todos los asientos libres
        assertEquals(sala.getCapacidad(), copia.asientosLibres().size());
        assertTrue(copia.getAsiento("A1").estaLibre());
        assertTrue(copia.getAsiento("B2").estaLibre());
    }

    @Test
    void cloneNoAfectaAlOriginal() {
        original.getAsiento("A1").reservar();

        original.clone();

        assertFalse(original.getAsiento("A1").estaLibre());
        assertEquals(119, original.asientosLibres().size());
    }

    @Test
    void cloneEsCopiaProfundaConAsientosIndependientes() {
        Funcion copia = original.clone();

        assertNotSame(original, copia);
        // Cada asiento de la copia es un objeto distinto al del original
        AsientoFuncion asientoOriginal = original.getAsiento("C4");
        AsientoFuncion asientoCopia = copia.getAsiento("C4");
        assertNotSame(asientoOriginal, asientoCopia);

        // Vender en la copia no vende en el original (RN-05)
        asientoCopia.reservar();
        assertTrue(asientoOriginal.estaLibre());
        assertEquals(119, copia.asientosLibres().size());
        assertEquals(120, original.asientosLibres().size());
    }

    @Test
    void cloneConservaDatosYCambiarlosNoAfectaAlOriginal() {
        Funcion copia = original.clone();

        assertSame(pelicula, copia.getPelicula());
        assertSame(sala, copia.getSala());
        assertEquals(15000, copia.getPrecio());

        copia.setId(2);
        copia.setFecha(LocalDate.of(2026, 10, 16));
        copia.setHora(LocalTime.of(21, 0));

        assertEquals(1, original.getId());
        assertEquals(LocalDate.of(2026, 10, 15), original.getFecha());
        assertEquals(LocalTime.of(19, 30), original.getHora());
    }

    @Test
    void getAsientoInexistenteLanzaExcepcion() {
        assertThrows(EntidadNoEncontradaException.class, () -> original.getAsiento("Z99"));
    }

    @Test
    void reservarAsientoYaVendidoEnFuncionLanzaExcepcion() {
        original.getAsiento("D6").reservar();

        // RN-02 a nivel de función
        assertThrows(AsientoNoDisponibleException.class, () -> original.getAsiento("D6").reservar());
    }

    @Test
    void funcionSinPeliculaLanzaExcepcion() {
        // RN-01: toda función tiene película, sala, fecha y hora
        assertThrows(IllegalArgumentException.class,
                () -> new Funcion(3, null, sala, LocalDate.now(), LocalTime.NOON, 10000));
    }
}
