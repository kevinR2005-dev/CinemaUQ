package test;
import cinemauq.excepcion.PuntosInsuficientesException;
import cinemauq.modelo.puntos.ConsumoPuntos;
import cinemauq.modelo.puntos.CuentaPuntos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CuentaPuntosTest {

    private final LocalDate hoy = LocalDate.of(2026, 7, 1);
    private CuentaPuntos cuenta;

    @BeforeEach
    void preparar() {
        cuenta = new CuentaPuntos();
    }

    // RN14: el lote vence un año despues de obtenerse
    @Test
    void elLoteVenceUnAnioDespuesDeObtenerse() {
        cuenta.acumular(45, LocalDate.of(2026, 1, 10), 1);

        assertEquals(LocalDate.of(2027, 1, 10), cuenta.getLotes().get(0).getFechaVencimiento());
    }

    @Test
    void acumularCeroPuntosNoCreaLote() {
        cuenta.acumular(0, hoy, 1);

        assertEquals(0, cuenta.getLotes().size());
    }

    @Test
    void puntosVigentesSumaTodosLosLotes() {
        cuenta.acumular(45, LocalDate.of(2026, 5, 1), 1);
        cuenta.acumular(30, LocalDate.of(2026, 6, 1), 2);

        assertEquals(75, cuenta.puntosVigentes(hoy));
    }

    // RN14: los puntos vencidos no cuentan
    @Test
    void unLoteVencidoNoCuentaComoVigente() {
        cuenta.acumular(500, LocalDate.of(2025, 1, 1), 1);   // vencio el 2026-01-01

        assertEquals(0, cuenta.puntosVigentes(hoy));
        assertEquals(500, cuenta.puntosVencidos(hoy));
    }

    // El dia del vencimiento todavia es valido
    @Test
    void elDiaDelVencimientoTodaviaEsVigente() {
        cuenta.acumular(10, LocalDate.of(2026, 1, 10), 1);

        assertEquals(10, cuenta.puntosVigentes(LocalDate.of(2027, 1, 10)));
        assertEquals(0, cuenta.puntosVigentes(LocalDate.of(2027, 1, 11)));
    }

    // RN15: se consume primero el lote que vence antes
    @Test
    void redimirConsumePrimeroElLoteQueVenceAntes() {
        // Se agregan al reves a proposito: el que vence despues entra primero
        cuenta.acumular(100, LocalDate.of(2026, 6, 1), 2);   // vence 2027-06-01
        cuenta.acumular(100, LocalDate.of(2026, 1, 1), 1);   // vence 2027-01-01

        List<ConsumoPuntos> consumos = cuenta.redimir(150, hoy);

        assertEquals(2, consumos.size());
        assertEquals(100, consumos.get(0).getPuntos());
        assertEquals(LocalDate.of(2027, 1, 1), consumos.get(0).getFechaVencimiento());
        assertEquals(50, consumos.get(1).getPuntos());
        assertEquals(LocalDate.of(2027, 6, 1), consumos.get(1).getFechaVencimiento());
        assertEquals(50, cuenta.puntosVigentes(hoy));
    }

    // Verificar antes de modificar: si faltan puntos, no se toca nada
    @Test
    void redimirSinPuntosSuficientesLanzaExcepcionYNoModifica() {
        cuenta.acumular(100, LocalDate.of(2026, 6, 1), 1);

        assertThrows(PuntosInsuficientesException.class, () -> cuenta.redimir(150, hoy));
        assertEquals(100, cuenta.puntosVigentes(hoy));
    }

    // RN14: con puntos vencidos no alcanza, aunque la suma total sea suficiente
    @Test
    void losPuntosVencidosNoSirvenParaRedimir() {
        cuenta.acumular(500, LocalDate.of(2025, 1, 1), 1);   // vencido
        cuenta.acumular(100, LocalDate.of(2026, 6, 1), 2);   // vigente

        assertThrows(PuntosInsuficientesException.class, () -> cuenta.redimir(200, hoy));
    }

    // RN20: los puntos devueltos conservan su vencimiento ORIGINAL
    @Test
    void devolverRegresaLosPuntosConSuVencimientoOriginal() {
        cuenta.acumular(100, LocalDate.of(2026, 1, 1), 1);   // vence 2027-01-01
        List<ConsumoPuntos> consumos = cuenta.redimir(100, hoy);
        assertEquals(0, cuenta.puntosVigentes(hoy));

        cuenta.devolver(consumos);

        assertEquals(100, cuenta.puntosVigentes(hoy));
        int ultimo = cuenta.getLotes().size() - 1;
        assertEquals(LocalDate.of(2027, 1, 1), cuenta.getLotes().get(ultimo).getFechaVencimiento());
    }

    // RN20: se anulan solo los puntos ganados por esa compra
    @Test
    void anularQuitaSoloLosPuntosDeEsaCompra() {
        cuenta.acumular(45, LocalDate.of(2026, 6, 1), 7);
        cuenta.acumular(30, LocalDate.of(2026, 6, 2), 8);

        cuenta.anular(7);

        assertEquals(30, cuenta.puntosVigentes(hoy));
    }

    @Test
    void puntosPorVencerCuentaSoloLosQueVencenDentroDelPlazo() {
        cuenta.acumular(10, LocalDate.of(2025, 7, 21), 1);   // vence 2026-07-21 (en 20 dias)
        cuenta.acumular(50, LocalDate.of(2026, 6, 1), 2);    // vence 2027-06-01

        assertEquals(10, cuenta.puntosPorVencer(hoy, 30));
    }
}
