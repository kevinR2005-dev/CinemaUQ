package test;

import cinemauq.modelo.reporte.GeneradorCsv;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.GeneradorTxt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneradorReporteTest {

    // El dato de prueba ya trae su encabezado, como lo entrega ReporteServicio
    private final String datos = "Reporte: Ventas del periodo\n"
            + "Generado: 2026-11-11 10:00\n"
            + "Periodo: 2026-11-01 a 2026-11-11\n"
            + "Registros encontrados: 1\n"
            + "factura;cliente;pelicula;asientos;total\n"
            + "1;Nicolas Sanchez;Avengers;D7,D8;40000\n"
            + "TOTAL;;;;40000";

    @Test
    void mismosDatosDanFormatosDistintos() {
        GeneradorReporte txt = new GeneradorTxt();
        GeneradorReporte csv = new GeneradorCsv();

        assertNotEquals(txt.emitir(datos), csv.emitir(datos));
    }

    @Test
    void cadaFormatoTieneSuEstilo() {
        String salidaTxt = new GeneradorTxt().emitir(datos);
        String salidaCsv = new GeneradorCsv().emitir(datos);

        // TXT cambia ";" por " | "
        assertTrue(salidaTxt.contains("Nicolas Sanchez | Avengers"));
        assertFalse(salidaTxt.contains(";"));
        // CSV deja los datos tal cual, con su encabezado de columnas
        assertTrue(salidaCsv.contains("factura;cliente;pelicula;asientos;total"));
        assertTrue(salidaCsv.contains("1;Nicolas Sanchez;Avengers;D7,D8;40000"));
        assertFalse(salidaCsv.contains(" | "));
    }

    @Test
    void ambosIncluyenEncabezadoYPie() {
        GeneradorReporte[] generadores = { new GeneradorTxt(), new GeneradorCsv() };

        for (GeneradorReporte g : generadores) {
            String salida = g.emitir(datos);
            assertTrue(salida.startsWith("=== CinemaUQ ==="));
            assertTrue(salida.endsWith("=== fin ==="));
        }
    }

    @Test
    void conservanElEncabezadoDelReporte() {
        // Ninguno agrega título propio: el de los datos aparece una sola vez
        String salidaTxt = new GeneradorTxt().emitir(datos);
        String salidaCsv = new GeneradorCsv().emitir(datos);

        assertTrue(salidaTxt.contains("Reporte: Ventas del periodo"));
        assertTrue(salidaCsv.contains("Reporte: Ventas del periodo"));
        assertFalse(salidaTxt.contains("Reporte de ventas - CinemaUQ"));
    }
}