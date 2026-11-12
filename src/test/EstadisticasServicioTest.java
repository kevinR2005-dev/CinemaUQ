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
import cinemauq.modelo.tarjeta.TipoMovimiento;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.EstadisticasServicio;
import cinemauq.servicio.PagoServicio;
import cinemauq.servicio.PuntosServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EstadisticasServicioTest {

    private Repositorio<Compra, Integer> compras;
    private Repositorio<Funcion, Integer> funciones;
    private Repositorio<Usuario, Integer> usuarios;
    private PagoServicio pago;
    private PuntosServicio puntos;
    private EstadisticasServicio estadisticas;

    private Cliente nicolas;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        compras = new RepositorioMemoria<>(Compra::getNumeroFactura);
        funciones = new RepositorioMemoria<>(Funcion::getId);
        usuarios = new RepositorioMemoria<>(Usuario::getId);
        pago = new PagoServicio();
        puntos = new PuntosServicio(new ReglaPorValor(1000), new PublicadorEventos());
        estadisticas = new EstadisticasServicio(compras, funciones, usuarios);
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
    void ventasTotalesRespetaElRangoYExcluyeCanceladas() {
        assertEquals(70000, estadisticas.ventasTotales(hoy.minusDays(10), hoy.minusDays(5)), 0.001);
        assertEquals(90000, estadisticas.ventasTotales(hoy.minusDays(60), hoy), 0.001);
        assertEquals(0, estadisticas.ventasTotales(hoy.plusDays(1), hoy.plusDays(3)), 0.001);
    }

    @Test
    void peliculasMasVendidasVanDeMayorAMenor() {
        List<String> top = estadisticas.peliculasMasVendidas(5);

        assertEquals(2, top.size());
        assertEquals("Avengers (2 entradas)", top.get(0));
        assertEquals("Dune (1 entradas)", top.get(1)); // el contrato fija el formato "(N entradas)"
    }

    @Test
    void peliculasMasVendidasRespetaElLimite() {
        assertEquals(1, estadisticas.peliculasMasVendidas(1).size());
        assertTrue(estadisticas.peliculasMasVendidas(0).isEmpty());
    }

    @Test
    void sinCompraConConfiteriaNoHayProductos() {
        assertTrue(estadisticas.productosMasVendidos(3).isEmpty());
    }

    @Test
    void totalReembolsosSumaSoloMovimientosReembolso() {
        nicolas.getTarjeta().acreditar(50000, "Recarga", TipoMovimiento.RECARGA, hoy.minusDays(7).atTime(10, 0));
        nicolas.getTarjeta().acreditar(10000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(5).atTime(14, 0));
        nicolas.getTarjeta().acreditar(4000, "Reembolso", TipoMovimiento.REEMBOLSO, hoy.minusDays(30).atTime(9, 0));

        assertEquals(10000, estadisticas.totalReembolsos(hoy.minusDays(10), hoy), 0.001);
        assertEquals(14000, estadisticas.totalReembolsos(hoy.minusDays(60), hoy), 0.001);
    }

    @Test
    void ocupacionPorSalaEsPorcentaje() {
        Map<Integer, Double> ocupacion = estadisticas.ocupacionPorSala(hoy.minusDays(10), hoy.minusDays(5));

        assertEquals(1, ocupacion.size());
        assertEquals(4.0, ocupacion.get(1), 0.001); // 2 vendidos de 50 asientos
    }
}