package test;

import cinemauq.evento.PublicadorEventos;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.estado.Cancelada;
import cinemauq.modelo.puntos.ReglaPorValor;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.modelo.tarjeta.TipoMovimiento;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.PagoServicio;
import cinemauq.servicio.PuntosServicio;
import cinemauq.servicio.ReporteServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReporteServicioTest {

    private Repositorio<Compra, Integer> compras;
    private Repositorio<Funcion, Integer> funciones;
    private Repositorio<Usuario, Integer> usuarios;
    private PagoServicio pago;
    private PuntosServicio puntos;
    private ReporteServicio reportes;

    private Cliente nicolas;
    private LocalDate hoy;

    // A: 40000 (hoy-10) | B: 30000 (hoy-5) | C: 20000 (hoy-40, fuera del rango) | D: 10000 (hoy-5, CANCELADA)
    @BeforeEach
    void preparar() {
        compras = new RepositorioMemoria<>(Compra::getNumeroFactura);
        funciones = new RepositorioMemoria<>(Funcion::getId);
        usuarios = new RepositorioMemoria<>(Usuario::getId);
        pago = new PagoServicio();
        puntos = new PuntosServicio(new ReglaPorValor(1000), new PublicadorEventos());
        reportes = new ReporteServicio(compras, funciones, usuarios);
        hoy = LocalDate.now();

        nicolas = new Cliente(1, "Nicolas", "nicolas@correo.com", "1234");
        nicolas.getTarjeta().acreditar(1000000, "Saldo inicial");
        usuarios.guardar(nicolas);

        Funcion f1 = crearFuncion(101, 1, "Avengers", hoy.minusDays(10).atTime(18, 0));
        Funcion f2 = crearFuncion(102, 1, "Avengers", hoy.minusDays(5).atTime(18, 0));
        Funcion f3 = crearFuncion(103, 2, "Dune", hoy.minusDays(40).atTime(18, 0));
        crearCompra(f1, 1001, "A1", 40000, hoy.minusDays(10).atTime(12, 0));
        crearCompra(f2, 1002, "A1", 30000, hoy.minusDays(5).atTime(12, 0));
        crearCompra(f3, 1003, "A1", 20000, hoy.minusDays(40).atTime(12, 0));
        Compra d = crearCompra(f2, 1004, "A2", 10000, hoy.minusDays(5).atTime(13, 0));
        d.setEstado(new Cancelada());
        f2.getAsiento("A2").liberar();
    }

    // ---------- armado de datos ----------

    private Funcion crearFuncion(int id, int numeroSala, String titulo, LocalDateTime cuando) {
        Funcion f = new Funcion(id, new Pelicula(id, titulo, 120, "Accion"), new Sala(numeroSala, 5, 5),
                cuando.toLocalDate(), cuando.toLocalTime(), 20000);
        funciones.guardar(f);
        return f;
    }

    private Compra crearCompra(Funcion f, int factura, String asiento, double total, LocalDateTime fecha) {
        f.getAsiento(asiento).reservar();
        pago.cobrar(nicolas, total, "Compra funcion " + f.getId());
        int ganados = puntos.acumular(nicolas, total, factura, fecha.toLocalDate());
        Compra compra = new Compra.Builder()
                .conCliente(nicolas).conFuncion(f).conAsiento(asiento)
                .conNumeroFactura(factura).conTotal(total)
                .conPuntosGanados(ganados).conFecha(fecha)
                .build();
        compras.guardar(compra);
        return compra;
    }

    // ---------- pruebas ----------

    @Test
    void ventasTieneElFormatoDelContrato() {
        String[] l = reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(5)).split("\n");

        assertTrue(l[0].startsWith("Reporte: "));
        assertTrue(l[1].startsWith("Generado: "));
        assertEquals("Periodo: " + hoy.minusDays(10) + " a " + hoy.minusDays(5), l[2]);
        assertEquals("Registros encontrados: 2", l[3]);
        assertEquals("factura;fecha;cliente;pelicula;total", l[4]);
        assertEquals("TOTAL;;;;70000", l[l.length - 1]);
    }

    @Test
    void elRangoIncluyeLasDosFechas() {
        // La compra cae justo en hoy-10 (RN22)
        String texto = reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(10));
        assertTrue(texto.contains("Registros encontrados: 1"));
        assertTrue(texto.contains("1001;"));
    }

    @Test
    void lasCompradasCanceladasNoCuentanEnVentas() {
        String texto = reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.minusDays(60), hoy);
        assertFalse(texto.contains("1004;"));
        assertTrue(texto.contains("TOTAL;;;;90000"));
    }

    @Test
    void rangoSinDatosDaCeroRegistros() {
        String texto = reportes.datos(TipoReporte.VENTAS_PERIODO, hoy.plusDays(1), hoy.plusDays(2));
        assertTrue(texto.contains("Registros encontrados: 0"));
        assertTrue(texto.endsWith("TOTAL;;;;0"));
    }

    @Test
    void entradasPorPelicula() {
        String texto = reportes.datos(TipoReporte.ENTRADAS_PELICULA, hoy.minusDays(10), hoy.minusDays(5));
        assertTrue(texto.contains("pelicula;entradas;total"));
        assertTrue(texto.contains("Avengers;2;70000"));
    }

    @Test
    void serieDeVentasEsTotalPorDia() {
        Map<String, Double> serie = reportes.serie(TipoReporte.VENTAS_PERIODO, hoy.minusDays(10), hoy.minusDays(5));

        assertEquals(2, serie.size());
        assertEquals(40000, serie.get(hoy.minusDays(10).toString()), 0.001);
        assertEquals(30000, serie.get(hoy.minusDays(5).toString()), 0.001);
    }

    @Test
    void ocupacionUsaLosAsientosVendidosDeFuncionesProgramadas() {
        int capacidad = 25; // sala de 5 x 5
        Map<String, Double> serie = reportes.serie(TipoReporte.OCUPACION_SALAS, hoy.minusDays(10), hoy.minusDays(5));

        // funciones 101 y 102 (sala 1): 2 vendidos de 50
        assertEquals(2 * 100.0 / (2 * capacidad), serie.get("Sala 1"), 0.001);
        assertFalse(serie.containsKey("Sala 2")); // la función 103 está fuera del rango
    }

    @Test
    void recargasYReembolsosSalenDeLosMovimientos() {
        nicolas.getTarjeta().acreditar(50000, "Recarga", TipoMovimiento.RECARGA, hoy.minusDays(7).atTime(10, 0));
        nicolas.getTarjeta().acreditar(10000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(5).atTime(14, 0));

        // El rango termina en hoy-1 para no incluir el saldo inicial, que quedó fechado hoy
        String recargas = reportes.datos(TipoReporte.RECARGAS, hoy.minusDays(10), hoy.minusDays(1));
        String reembolsos = reportes.datos(TipoReporte.CANCELACIONES_REEMBOLSOS, hoy.minusDays(10), hoy.minusDays(1));

        assertTrue(recargas.contains(hoy.minusDays(7) + ";Nicolas;50000"));
        assertFalse(recargas.contains("10000"));
        assertTrue(reembolsos.contains(hoy.minusDays(5) + ";Nicolas;10000"));
    }

    @Test
    void puntosSumaAcumulados() {
        Map<String, Double> serie = reportes.serie(TipoReporte.PUNTOS, hoy.minusDays(60), hoy);
        // 40 + 30 + 20 (la cancelada no cuenta) = 90 acumulados
        assertEquals(90, serie.get("Acumulados"), 0.001);
        assertEquals(0, serie.get("Redimidos"), 0.001);
    }
}