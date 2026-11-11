package cinemauq.modelo;

import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.estado.EstadoCompra;
import cinemauq.modelo.estado.Pagada;
import cinemauq.modelo.estado.Utilizada;
import cinemauq.modelo.puntos.ConsumoPuntos;
import cinemauq.modelo.puntos.TipoBeneficio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Compra {
    private static final int MAX_ASIENTOS = 8;

    private final int numeroFactura;
    private final Cliente cliente;
    private final Funcion funcion;
    private final List<String> asientos;
    private final List<ItemConfiteria> confiteria;
    private final double total;                      // lo pagado en dinero
    private final LocalDateTime fecha;
    private final int puntosGanados;
    private final double valorCanjeado;
    private final List<TipoBeneficio> canjes;
    private final List<ConsumoPuntos> consumos;

    // Lo UNICO que cambia despues de creada la compra
    private EstadoCompra estado = new Pagada();

    // Constructor PRIVADO: la unica puerta es Builder.build()
    private Compra(Builder b) {
        this.numeroFactura = b.numeroFactura;
        this.cliente = b.cliente;
        this.funcion = b.funcion;
        this.asientos = Collections.unmodifiableList(new ArrayList<>(b.asientos));
        this.confiteria = Collections.unmodifiableList(new ArrayList<>(b.confiteria));
        this.total = b.total;
        this.fecha = (b.fecha != null) ? b.fecha : LocalDateTime.now();
        this.puntosGanados = b.puntosGanados;
        this.valorCanjeado = b.valorCanjeado;
        this.canjes = Collections.unmodifiableList(new ArrayList<>(b.canjes));
        this.consumos = Collections.unmodifiableList(new ArrayList<>(b.consumos));
    }

    // Entradas + confiteria (todavia sin descuentos ni cargo)
    private static double subtotal(Funcion funcion, List<String> asientos,
                                   List<ItemConfiteria> confiteria) {
        double suma = funcion.getPrecio() * asientos.size();
        for (ItemConfiteria item : confiteria) {
            suma += item.getPrecio();
        }
        return suma;
    }

    public double calcularSubtotal() { return subtotal(funcion, asientos, confiteria); }

    // Devuelve el total pagado en dinero (ya calculado con los decoradores)
    public double calcularTotal() { return total; }

    // ---------- STATE: la compra delega en su estado ----------
    public void cancelar() {
        estado.cancelar(this);
    }

    public void setEstado(EstadoCompra nuevo) { this.estado = nuevo; }

    public EstadoCompra getEstado() { return estado; }

    // Si la funcion ya se realizo, una compra PAGADA pasa a UTILIZADA
    public void actualizarEstado(LocalDateTime ahora) {
        if (estado.getNombre().equals("PAGADA")
                && funcion.getEstado() != EstadoFuncion.CANCELADA
                && funcion.getFechaHora().isBefore(ahora)) {
            estado = new Utilizada();
        }
    }

    public int getNumeroFactura() { return numeroFactura; }
    public Cliente getCliente() { return cliente; }
    public Funcion getFuncion() { return funcion; }
    public List<String> getAsientos() { return asientos; }
    public List<ItemConfiteria> getConfiteria() { return confiteria; }
    public LocalDateTime getFecha() { return fecha; }
    public int getPuntosGanados() { return puntosGanados; }
    public double getValorCanjeado() { return valorCanjeado; }
    public List<TipoBeneficio> getCanjes() { return canjes; }
    public List<ConsumoPuntos> getConsumos() { return consumos; }

    // Puntos que el cliente gasto en esta compra
    public int getPuntosRedimidos() {
        int suma = 0;
        for (ConsumoPuntos c : consumos) {
            suma += c.getPuntos();
        }
        return suma;
    }

    public static class Builder {

        private Cliente cliente;
        private Funcion funcion;
        private final List<String> asientos = new ArrayList<>();
        private final List<ItemConfiteria> confiteria = new ArrayList<>();
        private int numeroFactura;
        private double total;
        private LocalDateTime fecha;
        private int puntosGanados;
        private double valorCanjeado;
        private final List<TipoBeneficio> canjes = new ArrayList<>();
        private final List<ConsumoPuntos> consumos = new ArrayList<>();

        public Builder conCliente(Cliente cliente) { this.cliente = cliente; return this; }
        public Builder conFuncion(Funcion funcion) { this.funcion = funcion; return this; }
        public Builder conAsiento(String codigo) { this.asientos.add(codigo); return this; }
        public Builder conConfiteria(ItemConfiteria item) { this.confiteria.add(item); return this; }
        public Builder conNumeroFactura(int numero) { this.numeroFactura = numero; return this; }
        public Builder conTotal(double total) { this.total = total; return this; }
        public Builder conFecha(LocalDateTime fecha) { this.fecha = fecha; return this; }
        public Builder conPuntosGanados(int puntos) { this.puntosGanados = puntos; return this; }

        // Cada canje agrega su tipo y suma su valor al descuento por puntos
        public Builder conCanje(TipoBeneficio tipo, double valor) {
            this.canjes.add(tipo);
            this.valorCanjeado += valor;
            return this;
        }

        public Builder conConsumos(List<ConsumoPuntos> consumos) {
            this.consumos.clear();
            this.consumos.addAll(consumos);
            return this;
        }

        public Cliente getCliente() { return cliente; }
        public Funcion getFuncion() { return funcion; }
        public List<String> getAsientos() { return Collections.unmodifiableList(asientos); }
        public List<TipoBeneficio> getCanjes() { return Collections.unmodifiableList(canjes); }
        public double getValorCanjeado() { return valorCanjeado; }

        public double calcularSubtotal() { return subtotal(funcion, asientos, confiteria); }

        // RN03 y RN04: se valida con throw, nunca con println
        public void validar() {
            if (cliente == null) {
                throw new IllegalStateException("La compra requiere un cliente");
            }
            if (funcion == null) {
                throw new IllegalStateException("La compra requiere una funcion");
            }
            if (asientos.isEmpty()) {
                throw new IllegalStateException("La compra requiere al menos un asiento");
            }
            if (asientos.size() > 8) {
                throw new IllegalStateException("Maximo 8 asientos por compra");
            }
            int entradasCanjeadas = 0;
            for (TipoBeneficio t : canjes) {
                if (t == TipoBeneficio.ENTRADA) {
                    entradasCanjeadas++;
                }
            }
            if (entradasCanjeadas > asientos.size()) {
                throw new IllegalStateException("No se pueden canjear mas entradas que asientos");
            }
        }

        public Compra build() {
            validar();
            return new Compra(this);
        }
    }
}