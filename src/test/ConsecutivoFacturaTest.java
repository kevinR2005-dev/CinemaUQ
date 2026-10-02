package test;
import cinemauq.modelo.ConsecutivoFactura;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ConsecutivoFacturaTest {

    @Test
    void getInstanciaDevuelveSiempreLaMismaInstancia() {
        ConsecutivoFactura a = ConsecutivoFactura.getInstancia();
        ConsecutivoFactura b = ConsecutivoFactura.getInstancia();
        assertSame(a, b);
    }

    @Test
    void siguienteEntregaNumerosConsecutivos() {
        ConsecutivoFactura c = ConsecutivoFactura.getInstancia();
        int primero = c.siguiente();
        int segundo = c.siguiente();
        int tercero = c.siguiente();
        assertEquals(primero + 1, segundo);
        assertEquals(segundo + 1, tercero);
    }

    @Test
    void siguienteDesdeDiezHilosNoRepiteNumeros() throws Exception {
        ConsecutivoFactura c = ConsecutivoFactura.getInstancia();
        int hilos = 10;
        int porHilo = 100;

        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        List<Callable<List<Integer>>> tareas = new ArrayList<>();
        for (int i = 0; i < hilos; i++) {
            tareas.add(() -> {
                List<Integer> numeros = new ArrayList<>();
                for (int j = 0; j < porHilo; j++) {
                    numeros.add(c.siguiente());
                }
                return numeros;
            });
        }
        Set<Integer> todos = new HashSet<>();
        int total = 0;
        for (Future<List<Integer>> f : pool.invokeAll(tareas)) {
            List<Integer> lista = f.get();
            total += lista.size();
            todos.addAll(lista);
        }
        pool.shutdown();

        assertEquals(1000, total);
        assertEquals(1000, todos.size());
    }

}

