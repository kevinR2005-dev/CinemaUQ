package cinemauq.modelo;

import java.util.ArrayList;
import java.util.List;

public class Compra {
    private static final int MAX_ASIENTOS = 8;

    private final int numeroFactura;
    private final Cliente cliente;
    private final Funcion funcion;
    private final List<String> asientos;
    private final Combo combo;

    //Patron Builder:

    private Compra(Builder b) {
        this.numeroFactura = b.numeroFactura;
        this.cliente = b.cliente;
        this.funcion = b.funcion;
        this.asientos = List.copyOf(b.asientos);
        this.combo = b.combo;
    }

    public int getNumeroFactura(){
        return numeroFactura;

    }

    public Cliente getCliente(){
        return cliente;
    }

    public Funcion getFuncion(){
        return funcion;
    }

    public List<String> getAsientos(){
        return asientos;

    }

    public Combo getCombo(){
        return combo;
    }

    // Aqui va el precio de la función por asiento, ademas del precio del combo (si hay).

    public double calcularTotal(){
        return funcion.getPrecio() * asientos.size() + (combo != null ?
                combo.getPrecio() : 0);
    }

    public static class Builder{
        private int numeroFactura;
        private Cliente cliente;
        private Funcion funcion;
        private final List<String> asientos = new ArrayList<>();
        private Combo combo;

        public Builder conCliente(Cliente cliente){
            this.cliente = cliente;
            return this;
        }

        public Builder conFuncion(Funcion funcion) {
            this.funcion = funcion;
            return this;
        }

        public Builder conAsiento(String codigo){
            this.asientos.add(codigo);
            return this;
        }

        public Builder conCombo(Combo combo){
            this.combo = combo;
            return this;
        }

        public Builder conNumeroFactura(int numeroFactura){
            this.numeroFactura = numeroFactura;
            return this;
        }

        // Regla de negocio 04: Toda compra requiere un cliente, función y al menos un asiento.

        public void validar(){
            if(cliente==null){
                throw new IllegalStateException("La compra requiere un cliente.");

            }

            if (funcion == null){
                throw new IllegalStateException("La compra requiere una función.");

            }

            if (asientos.isEmpty()){
                throw new IllegalStateException("La compra requiere al menos un asiento.");
            }
            //Regla de negocio 03: máximo 8 entradas por compra.
            if (asientos.size() >  MAX_ASIENTOS){
                throw new IllegalStateException("Máximo" + MAX_ASIENTOS + " asientos por compra.");
            }

        }

        public Compra build(){
            validar();
            return new Compra(this);
        }

    }
}
