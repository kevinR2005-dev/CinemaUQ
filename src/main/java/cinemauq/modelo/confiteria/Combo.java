package cinemauq.modelo.confiteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Combo implements ItemConfiteria{
    private final String nombre;
    private final List<ItemConfiteria> items = new ArrayList<>();

    public Combo(String nombre) {
        this.nombre = nombre;
    }


    public void agregar(ItemConfiteria item) {
        if (item == null || item == this) {
            throw new IllegalArgumentException("Item invalido para el combo");
        }
        items.add(item);
    }

    @Override
    public String getNombre() { return nombre; }


    @Override
    public double getPrecio() {
        double suma = 0;
        for (ItemConfiteria item : items) {
            suma += item.getPrecio();
        }
        return suma;
    }

    public List<ItemConfiteria> getItems() {
        return Collections.unmodifiableList(items);
    }
}

