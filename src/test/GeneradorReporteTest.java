package test;
import cinemauq.modelo.reporte.GeneradorCsv;
import cinemauq.modelo.reporte.GeneradorReporte;
import cinemauq.modelo.reporte.GeneradorTxt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class GeneradorReporteTest {


    private final String datos = "1;Nicolas Sanchez;Avengers;D7,D8;40000";


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

        assertTrue(salidaTxt.contains("Nicolas Sanchez | Avengers"));
        assertTrue(salidaCsv.contains("factura;cliente;pelicula;asientos;total"));
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
}
