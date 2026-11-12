package test;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.RegistroActividad;
import cinemauq.evento.TipoEvento;
import cinemauq.excepcion.PuntosInsuficientesException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.modelo.puntos.ConsumoPuntos;
import cinemauq.modelo.puntos.ReglaPorValor;
import cinemauq.modelo.puntos.TipoBeneficio;
import cinemauq.servicio.PuntosServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuntosServicioTest {

    private final LocalDate hoy = LocalDate.of(2026, 7, 1);

    private Cliente cliente;
    private PuntosServicio servicio;
    private RegistroActividad registro;

    @BeforeEach
    void preparar() {
        cliente = new Cliente(1, "Ana", "ana@correo.com", "1234");
        PublicadorEventos publicador = new PublicadorEventos();
        registro = new RegistroActividad();
        publicador.registrar(registro);
        servicio = new PuntosServicio(new ReglaPorValor(1000), publicador);   // 1 punto por $1.000
    }

    // RN13: 45.000 pagados en dinero -> 45 puntos
    @Test
    void acumularCalculaUnPuntoPorCadaMil() {
        int ganados = servicio.acumular(cliente, 45000, 1, hoy);

        assertEquals(45, ganados);
        assertEquals(45, cliente.getPuntos().puntosVigentes(hoy));
    }

    // RN13: el redondeo es hacia abajo
    @Test
    void acumularRedondeaHaciaAbajo() {
        assertEquals(1, servicio.acumular(cliente, 1999, 1, hoy));
        assertEquals(0, servicio.acumular(cliente, 999, 2, hoy));
    }

    // RN16: una entrada cuesta 500 puntos
    @Test
    void verificarCanjeSinPuntosSuficientesLanzaExcepcion() {
        cliente.getPuntos().acumular(100, hoy, 1);

        assertThrows(PuntosInsuficientesException.class,
                () -> servicio.verificarCanje(cliente, List.of(TipoBeneficio.ENTRADA), hoy));
    }

    // verificarCanje solo mira, no gasta nada (RN10)
    @Test
    void verificarCanjeNoModificaLosPuntos() {
        cliente.getPuntos().acumular(600, hoy, 1);

        servicio.verificarCanje(cliente, List.of(TipoBeneficio.ENTRADA), hoy);

        assertEquals(600, cliente.getPuntos().puntosVigentes(hoy));
    }

    // RN16: entrada (500) + producto (150) = 650 puntos
    @Test
    void redimirDescuentaElCostoDeTodosLosCanjes() {
        cliente.getPuntos().acumular(700, hoy, 1);

        List<ConsumoPuntos> consumos = servicio.redimir(cliente,
                List.of(TipoBeneficio.ENTRADA, TipoBeneficio.PRODUCTO), hoy);

        assertEquals(650, consumos.stream().mapToInt(ConsumoPuntos::getPuntos).sum());
        assertEquals(50, cliente.getPuntos().puntosVigentes(hoy));
    }

    // Sin canjes no se gasta nada
    @Test
    void redimirSinCanjesNoHaceNada() {
        cliente.getPuntos().acumular(100, hoy, 1);

        List<ConsumoPuntos> consumos = servicio.redimir(cliente, List.of(), hoy);

        assertEquals(0, consumos.size());
        assertEquals(100, cliente.getPuntos().puntosVigentes(hoy));
    }

    // RN20: al reembolsar, se devuelven los puntos gastados y se anulan los ganados
    @Test
    void revertirCompraDevuelveLosGastadosYAnulaLosGanados() {
        servicio.acumular(cliente, 100000, 1, hoy.minusMonths(2));   // 100 puntos de otra compra
        List<ConsumoPuntos> consumos = cliente.getPuntos().redimir(50, hoy);   // la compra gasta 50
        int ganados = servicio.acumular(cliente, 45000, 5, hoy);               // y gana 45

        Cliente.class.getName();   // (solo para dejar claro que el cliente es el mismo)
        Funcion funcion = new Funcion(1, new Pelicula(1, "Avengers", 143, "Accion"),
                new Sala(1, 8, 12), hoy.plusDays(5), LocalTime.of(19, 0), 20000);
        Compra compra = new Compra.Builder()
                .conCliente(cliente).conFuncion(funcion).conAsiento("A1")
                .conNumeroFactura(5).conConsumos(consumos).conPuntosGanados(ganados).build();
        assertEquals(95, cliente.getPuntos().puntosVigentes(hoy));   // 100 - 50 + 45

        servicio.revertirCompra(cliente, compra);

        // quedan los 50 que no se gastaron + los 50 devueltos; los 45 ganados se anularon
        assertEquals(100, cliente.getPuntos().puntosVigentes(hoy));
    }

    // RF30: se avisa cuando hay puntos por vencer
    @Test
    void avisarVencimientosPublicaUnEventoSiHayPuntosPorVencer() {
        cliente.getPuntos().acumular(10, hoy.minusDays(350), 1);   // vence en 15 dias

        servicio.avisarVencimientos(cliente, hoy);

        assertEquals(1, registro.getEventos().size());
        assertEquals(TipoEvento.VENCIMIENTO_PUNTOS, registro.getEventos().get(0).getTipo());
    }

    @Test
    void avisarVencimientosNoPublicaNadaSiNoHayNada() {
        cliente.getPuntos().acumular(10, hoy, 1);   // vence en un año

        servicio.avisarVencimientos(cliente, hoy);

        assertTrue(registro.getEventos().isEmpty());
    }
}
