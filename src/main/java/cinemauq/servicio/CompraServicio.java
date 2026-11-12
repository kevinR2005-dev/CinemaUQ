package cinemauq.servicio;

import cinemauq.evento.EventoCine;
import cinemauq.evento.PublicadorEventos;
import cinemauq.evento.TipoEvento;
import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.modelo.*;
import cinemauq.modelo.precio.*;
import cinemauq.modelo.puntos.ConsumoPuntos;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


public class CompraServicio {

    public static final double CARGO_SERVICIO = 2000;
    public static final double PROMOCION_PORCENTAJE = 10;

    private final Repositorio<Compra, Integer> compras;
    private final ConsecutivoFactura consecutivo;
    private final PagoServicio pagoServicio;
    private final PuntosServicio puntosServicio;
    private final PublicadorEventos publicador;


    public CompraServicio(Repositorio<Compra, Integer> compras,
                          ConsecutivoFactura consecutivo,
                          PagoServicio pagoServicio,
                          PuntosServicio puntosServicio,
                          PublicadorEventos publicador) {
        this.compras = compras;
        this.consecutivo = consecutivo;
        this.pagoServicio = pagoServicio;
        this.puntosServicio = puntosServicio;
        this.publicador = publicador;
    }
    public Compra confirmarCompra(Compra.Builder b) {
        return confirmarCompra(b, 0);
    }

    // RN10: todas las verificaciones van ANTES de cualquier cambio
    public Compra confirmarCompra(Compra.Builder b, double descuentoPorcentaje) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();

        // 1) La compra debe ser valida (cliente, funcion, 1 a 8 asientos)
        b.validar();
        Cliente cliente = b.getCliente();
        Funcion funcion = b.getFuncion();
        if (funcion.getEstado() == EstadoFuncion.CANCELADA) {
            throw new IllegalStateException("La funcion fue cancelada");
        }
        if (funcion.getFechaHora().isBefore(ahora)) {
            throw new IllegalStateException("La funcion ya se realizo");
        }

        // 2) Puntos suficientes y vigentes para los canjes
        puntosServicio.verificarCanje(cliente, b.getCanjes(), hoy);

        // 3) Total con la cadena de decoradores: base -> promocion -> puntos -> cargo
        CalculadorPrecio precio = new PrecioBase(b.calcularSubtotal());
        if (descuentoPorcentaje > 0) {
            precio = new DescuentoPromocion(precio, descuentoPorcentaje);
        }
        if (b.getValorCanjeado() > 0) {
            precio = new DescuentoPuntos(precio, b.getValorCanjeado());
        }
        precio = new CargoServicio(precio, CARGO_SERVICIO);
        double total = precio.calcular();

        // 4) Tarjeta activa y con saldo (todavia no descuenta nada)
        pagoServicio.verificar(cliente, total);

        // 5) Asientos libres y sin repetir
        Set<String> vistos = new HashSet<>();
        for (String codigo : b.getAsientos()) {
            if (!vistos.add(codigo) || !funcion.getAsiento(codigo).estaLibre()) {
                throw new AsientoNoDisponibleException("El asiento " + codigo + " no esta disponible");
            }
        }


        // 6) Reservar los asientos
        for (String codigo : b.getAsientos()) {
            funcion.getAsiento(codigo).reservar();
        }

        // 7) Gastar los puntos del canje
        List<ConsumoPuntos> consumos = puntosServicio.redimir(cliente, b.getCanjes(), hoy);

        // 8) Cobrar: descuenta el saldo y registra el movimiento
        pagoServicio.cobrar(cliente, total, "Compra funcion " + funcion.getId());

        // 9) Recien ahora se gasta un numero de factura y se ganan los puntos
        int factura = consecutivo.siguiente();
        int ganados = puntosServicio.acumular(cliente, total, factura, hoy);

        // 10) Crear la compra, guardarla y avisar
        Compra compra = b.conTotal(total)
                .conNumeroFactura(factura)
                .conConsumos(consumos)
                .conPuntosGanados(ganados)
                .conFecha(ahora)
                .build();
        compras.guardar(compra);

        publicador.publicar(new EventoCine(TipoEvento.COMPRA, cliente,
                "Compra #" + factura + " por $" + Math.round(total), ahora));
        return compra;
    }


    // Historial de un cliente
    public List<Compra> historial(Cliente cliente) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Compra> resultado = new ArrayList<>();
        for (Compra c : compras.listar()) {
            if (c.getCliente().getId() == cliente.getId()) {
                c.actualizarEstado(ahora);
                resultado.add(c);
            }
        }
        resultado.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        return resultado;
    }

    // Una linea por compra: factura;cliente;pelicula;asientos;total (sin espacios)
    public String datosVentas() {
        StringBuilder sb = new StringBuilder();
        for (Compra c : compras.listar()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(c.getNumeroFactura()).append(";")
                    .append(c.getCliente().getNombre()).append(";")
                    .append(c.getFuncion().getPelicula().getTitulo()).append(";")
                    .append(String.join(",", c.getAsientos())).append(";")
                    .append(Math.round(c.calcularTotal()));
        }
        return sb.toString();
    }
    }
}