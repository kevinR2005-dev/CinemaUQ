package cinemauq.modelo;

import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;

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
    private final double total;


    private Compra(Builder b) {
        this.numeroFactura = b.numeroFactura;
        this.cliente = b.cliente;
        this.funcion = b.funcion;
        this.asientos = Collections.unmodifiableList(new ArrayList<>(b.asientos));
        this.confiteria = Collections.unmodifiableList(new ArrayList<>(b.confiteria));
        this.total = b.total;
    }


    private static double subtotal(Funcion funcion, List<String> asientos,
                                   List<ItemConfiteria> confiteria) {
        double suma = funcion.getPrecio() * asientos.size();
        for (ItemConfiteria item : confiteria) {
            suma += item.getPrecio();
        }
        return suma;
    }

    public double calcularSubtotal() { return subtotal(funcion, asientos, confiteria); }


    public double calcularTotal() { return total; }

    public int getNumeroFactura() { return numeroFactura; }
    public Cliente getCliente() { return cliente; }
    public Funcion getFuncion() { return funcion; }
    public List<String> getAsientos() { return asientos; }
    public List<ItemConfiteria> getConfiteria() { return confiteria; }

    public static class Builder {

        private Cliente cliente;
        private Funcion funcion;
        private final List<String> asientos = new ArrayList<>();
        private final List<ItemConfiteria> confiteria = new ArrayList<>();
        private int numeroFactura;
        private double total;

        public Builder conCliente(Cliente cliente) {
            this.cliente = cliente; return this;
        }
        public Builder conFuncion(Funcion funcion) {
            this.funcion = funcion; return this;
        }
        public Builder conAsiento(String codigo) {
            this.asientos.add(codigo); return this;
        }
        public Builder conConfiteria(ItemConfiteria item) {
            this.confiteria.add(item); return this;
        }
        public Builder conNumeroFactura(int numero) {
            this.numeroFactura = numero; return this;
        }
        public Builder conTotal(double total) {
            this.total = total; return this;
        }

        public Cliente getCliente() { return cliente; }
        public Funcion getFuncion() { return funcion; }
        public List<String> getAsientos() { return Collections.unmodifiableList(asientos); }

        public double calcularSubtotal() { return subtotal(funcion, asientos, confiteria); }


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
        }

        public Compra build() {
            validar();
            return new Compra(this);
        }
    }
}
