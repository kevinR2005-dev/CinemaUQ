package cinemauq.modelo;

import cinemauq.modelo.confiteria.Combo;

import java.util.ArrayList;
import java.util.List;

public class Compra {
    private static final int MAX_ASIENTOS = 8;

    private final int numeroFactura;
    private final Cliente cliente;
    private final Funcion funcion;
    private final List<String> asientos;
    private final Combo combo;



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
