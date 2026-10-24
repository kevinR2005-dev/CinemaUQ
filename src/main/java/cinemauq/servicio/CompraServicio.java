package cinemauq.servicio;

import cinemauq.excepcion.AsientoNoDisponibleException;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.ConsecutivoFactura;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.precio.CalculadorPrecio;
import cinemauq.modelo.precio.CargoServicio;
import cinemauq.modelo.precio.DescuentoPromocion;
import cinemauq.modelo.precio.PrecioBase;
import cinemauq.repositorio.Repositorio;

import java.util.*;


public class CompraServicio {

    public static final double CARGO_SERVICIO = 2000;
    public static final double PROMOCION_PORCENTAJE = 10;

    private final Repositorio<Compra, Integer> compras;
    private final ConsecutivoFactura consecutivo;   // se RECIBE, no se llama getInstancia() aqui
    private final PagoServicio pagoServicio;

    public CompraServicio(Repositorio<Compra, Integer> compras,
                          ConsecutivoFactura consecutivo,
                          PagoServicio pagoServicio) {
        this.compras = compras;
        this.consecutivo = consecutivo;
        this.pagoServicio = pagoServicio;
    }

    public Compra confirmarCompra(Compra.Builder b) {
        return confirmarCompra(b, 0);
    }

    // RN10: todas las verificaciones van ANTES de cualquier cambio
    public Compra confirmarCompra(Compra.Builder b, double descuentoPorcentaje) {

        // 1) La compra debe ser valida (cliente, funcion, 1 a 8 asientos)
        b.validar();

        // 2) Total con la cadena de decoradores: base -> descuento -> cargo
        CalculadorPrecio precio = new PrecioBase(b.calcularSubtotal());
        if (descuentoPorcentaje > 0) {
            precio = new DescuentoPromocion(precio, descuentoPorcentaje);
        }
        precio = new CargoServicio(precio, CARGO_SERVICIO);
        double total = precio.calcular();

        // 3) La tarjeta debe estar activa y con saldo (todavia no descuenta nada)
        Cliente cliente = b.getCliente();
        pagoServicio.verificar(cliente, total);

        // 4) Todos los asientos deben estar libres y no repetidos
        Funcion funcion = b.getFuncion();
        Set<String> vistos = new HashSet<>();
        for (String codigo : b.getAsientos()) {
            if (!vistos.add(codigo) || !funcion.getAsiento(codigo).estaLibre()) {
                throw new AsientoNoDisponibleException("El asiento " + codigo + " no esta disponible");
            }
        }

        // 5) Reservar los asientos
        for (String codigo : b.getAsientos()) {
            funcion.getAsiento(codigo).reservar();
        }

        // 6) Cobrar: descuenta el saldo y registra el movimiento
        pagoServicio.cobrar(cliente, total, "Compra funcion " + funcion.getId());

        // 7) Recien ahora se gasta un numero de factura
        // 8) Se crea la compra y se guarda
        Compra compra = b.conTotal(total)
                .conNumeroFactura(consecutivo.siguiente())
                .build();
        compras.guardar(compra);
        return compra;
    }

    // Historial de un cliente
    public List<Compra> historial(Cliente cliente) {
        List<Compra> resultado = new ArrayList<>();
        for (Compra c : compras.listar()) {
            if (c.getCliente().getId() == cliente.getId()) {
                resultado.add(c);
            }
        }
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