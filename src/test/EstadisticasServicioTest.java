package test;

import cinemauq.modelo.*;
import cinemauq.modelo.estado.Cancelada;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EstadisticasServicioTest {

    private Escenario e;
    private Cliente nicolas;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        e = new Escenario();
        hoy = LocalDate.now();
        nicolas = e.cliente(1, "Nicolas", 1000000);
        Funcion f1 = e.funcion(101, 1, "Avengers", hoy.minusDays(10).atTime(18, 0));
        Funcion f2 = e.funcion(102, 1, "Avengers", hoy.minusDays(5).atTime(18, 0));
        Funcion f3 = e.funcion(103, 2, "Dune", hoy.minusDays(40).atTime(18, 0));
        e.compra(nicolas, f1, 1001, "A1", 40000, hoy.minusDays(10).atTime(12, 0));
        e.compra(nicolas, f2, 1002, "A1", 30000, hoy.minusDays(5).atTime(12, 0));
        e.compra(nicolas, f3, 1003, "A1", 20000, hoy.minusDays(40).atTime(12, 0));
        Compra d = e.compra(nicolas, f2, 1004, "A2", 10000, hoy.minusDays(5).atTime(13, 0));
        d.setEstado(new Cancelada());
        f2.getAsiento("A2").liberar();
    }

    @Test
    void ventasTotalesRespetaElRangoYExcluyeCanceladas() {
        assertEquals(70000, e.estadisticas.ventasTotales(hoy.minusDays(10), hoy.minusDays(5)), 0.001);
        assertEquals(90000, e.estadisticas.ventasTotales(hoy.minusDays(60), hoy), 0.001);
        assertEquals(0, e.estadisticas.ventasTotales(hoy.plusDays(1), hoy.plusDays(3)), 0.001);
    }

    @Test
    void peliculasMasVendidasVanDeMayorAMenor() {
        List<String> top = e.estadisticas.peliculasMasVendidas(5);

        assertEquals(2, top.size());
        assertEquals("Avengers (2 entradas)", top.get(0));
        assertEquals("Dune (1 entradas)", top.get(1));
    }

    @Test
    void peliculasMasVendidasRespetaElLimite() {
        assertEquals(1, e.estadisticas.peliculasMasVendidas(1).size());
        assertTrue(e.estadisticas.peliculasMasVendidas(0).isEmpty());
    }

    @Test
    void sinCompraConConfiteriaNoHayProductos() {
        assertTrue(e.estadisticas.productosMasVendidos(3).isEmpty());
    }

    @Test
    void totalReembolsosSumaSoloMovimientosReembolso() {
        nicolas.getTarjeta().acreditar(50000, "Recarga", TipoMovimiento.RECARGA, hoy.minusDays(7).atTime(10, 0));
        nicolas.getTarjeta().acreditar(10000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(5).atTime(14, 0));
        nicolas.getTarjeta().acreditar(4000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(30).atTime(9, 0));

        assertEquals(10000, e.estadisticas.totalReembolsos(hoy.minusDays(10), hoy), 0.001);
        assertEquals(14000, e.estadisticas.totalReembolsos(hoy.minusDays(60), hoy), 0.001);
    }

    @Test
    void ocupacionPorSalaEsPorcentaje() {
        Map<Integer, Double> ocupacion = e.estadisticas.ocupacionPorSala(hoy.minusDays(10), hoy.minusDays(5));

        assertEquals(1, ocupacion.size());
        assertEquals(4.0, ocupacion.get(1), 0.001); // 2 vendidos de 50 asientos
    }
}