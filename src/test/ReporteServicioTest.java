package test;

import cinemauq.modelo.*;
import cinemauq.modelo.estado.Cancelada;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReporteServicioTest {

    private Escenario e;
    private Cliente nicolas;
    private LocalDate hoy;

    // A: 40000 (hoy-10) | B: 30000 (hoy-5) | C: 20000 (hoy-40, fuera) | D: 10000 (hoy-5, CANCELADA)
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
    void ventasTieneElFormatoDelContrato() {
        String[] l = e.reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(5)).split("\n");

        assertTrue(l[0].startsWith("Reporte: "));
        assertTrue(l[1].startsWith("Generado: "));
        assertEquals("Periodo: " + hoy.minusDays(10) + " a " + hoy.minusDays(5), l[2]);
        assertEquals("Registros encontrados: 2", l[3]);
        assertEquals("factura;fecha;cliente;pelicula;total", l[4]);
        assertEquals("TOTAL;;;;70000", l[l.length - 1]);
    }

    @Test
    void elRangoIncluyeLasDosFechas() {
        // Las compras caen justo en hoy-10 y hoy-5 (RN22)
        String texto = e.reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(10));
        assertTrue(texto.contains("Registros encontrados: 1"));
        assertTrue(texto.contains("1001;"));
    }

    @Test
    void lasCompradasCanceladasNoCuentanEnVentas() {
        String texto = e.reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(60), hoy);
        assertFalse(texto.contains("1004;"));
        assertTrue(texto.contains("TOTAL;;;;90000"));
    }

    @Test
    void rangoSinDatosDaCeroRegistros() {
        String texto = e.reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.plusDays(1), hoy.plusDays(2));
        assertTrue(texto.contains("Registros encontrados: 0"));
        assertTrue(texto.endsWith("TOTAL;;;;0"));
    }

    @Test
    void entradasPorPelicula() {
        String texto = e.reportes.datos(TipoReporte.ENTRADAS_PELICULA, hoy.minusDays(10), hoy.minusDays(5));
        assertTrue(texto.contains("pelicula;entradas;total"));
        assertTrue(texto.contains("Avengers;2;70000"));
    }

    @Test
    void serieDeVentasEsTotalPorDia() {
        Map<String, Double> serie = e.reportes.serie(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(5));

        assertEquals(2, serie.size());
        assertEquals(40000, serie.get(hoy.minusDays(10).toString()), 0.001);
        assertEquals(30000, serie.get(hoy.minusDays(5).toString()), 0.001);
    }

    @Test
    void ocupacionUsaLosAsientosVendidosDeFuncionesProgramadas() {
        int capacidad = 25; // sala de 5 x 5 en Escenario
        Map<String, Double> serie = e.reportes.serie(TipoReporte.OCUPACION_SALAS, hoy.minusDays(10), hoy.minusDays(5));

        // funciones 101 y 102 (sala 1): 2 vendidos de 50
        assertEquals(2 * 100.0 / (2 * capacidad), serie.get("Sala 1"), 0.001);
        assertFalse(serie.containsKey("Sala 2")); // la función 103 está fuera del rango
    }

    @Test
    void recargasYReembolsosSalenDeLosMovimientos() {
        nicolas.getTarjeta().acreditar(50000, "Recarga", TipoMovimiento.RECARGA, hoy.minusDays(7).atTime(10, 0));
        nicolas.getTarjeta().acreditar(10000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(5).atTime(14, 0));

        String recargas = e.reportes.datos(TipoReporte.RECARGAS, hoy.minusDays(10), hoy);
        String reembolsos = e.reportes.datos(TipoReporte.CANCELACIONES_REEMBOLSOS, hoy.minusDays(10), hoy);

        assertTrue(recargas.contains(hoy.minusDays(7) + ";Nicolas;50000"));
        assertFalse(recargas.contains("10000"));
        assertTrue(reembolsos.contains(hoy.minusDays(5) + ";Nicolas;10000"));
    }

    @Test
    void puntosSumaAcumulados() {
        Map<String, Double> serie = e.reportes.serie(TipoReporte.PUNTOS, hoy.minusDays(60), hoy);
        // 40 + 30 + 20 (la cancelada no cuenta) = 90 acumulados
        assertEquals(90, serie.get("Acumulados"), 0.001);
        assertEquals(0, serie.get("Redimidos"), 0.001);
    }
}