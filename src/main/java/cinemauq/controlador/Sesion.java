package cinemauq.controlador;

import cinemauq.modelo.Funcion;
import cinemauq.modelo.Usuario;
import cinemauq.modelo.confiteria.ItemConfiteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Guarda el estado de la sesion: usuario logueado y la compra en curso.
 * Es una clase normal (NO es Singleton): CinemaUQApp crea una sola instancia
 * y se la pasa a cada controlador por constructor.
 */
public class Sesion {

    private Usuario usuario;
    private Funcion funcion;
    private List<String> asientos = new ArrayList<>();
    private final List<ItemConfiteria> confiteria = new ArrayList<>();

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public void setFuncion(Funcion funcion) {
        this.funcion = funcion;
    }

    public List<String> getAsientos() {
        return Collections.unmodifiableList(asientos);
    }

    public void setAsientos(List<String> asientos) {

        this.asientos = new ArrayList<>(asientos);
    }

    public List<ItemConfiteria> getConfiteria() {
        return Collections.unmodifiableList(confiteria);
    }

    public void agregarItem(ItemConfiteria item) {
        confiteria.add(item);
    }

    public void quitarItem(ItemConfiteria item) {
        confiteria.remove(item);
    }

    /** Borra funcion, asientos y confiteria (no toca el usuario). */
    public void limpiarCompra() {
        funcion = null;
        asientos = new ArrayList<>();
        confiteria.clear();
    }
}