package cinemauq.modelo;

import cinemauq.excepcion.EntidadNoEncontradaException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Funcion {

    private int id;
    private final Pelicula pelicula;
    private final Sala sala;
    private LocalDate fecha;
    private LocalTime hora;
    private final double precio;
    private final Map<String, AsientoFuncion> asientos;

    public Funcion(int id, Pelicula pelicula, Sala sala, LocalDate fecha, LocalTime hora, double precio) {

        if (pelicula == null || sala == null || fecha == null || hora == null) {
            throw new IllegalArgumentException("La función requiere película, sala, fecha y hora");
        }
        if (sala.getFilas() > 26) {
            throw new IllegalArgumentException("Máximo 26 filas (letras A-Z)");
        }
        this.id = id;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.asientos = new LinkedHashMap<>();


        for (int f = 0; f < sala.getFilas(); f++) {
            char letra = (char) ('A' + f);
            for (int c = 1; c <= sala.getColumnas(); c++) {
                String codigo = letra + String.valueOf(c);
                asientos.put(codigo, new AsientoFuncion(codigo));
            }
        }
    }


    private Funcion(Funcion original) {
        this.id = original.id;
        this.pelicula = original.pelicula;   // Película y sala no cambian, se comparten
        this.sala = original.sala;
        this.fecha = original.fecha;
        this.hora = original.hora;
        this.precio = original.precio;
        this.asientos = new LinkedHashMap<>();   // Mapa NUEVO
        for (AsientoFuncion asiento : original.asientos.values()) {
            this.asientos.put(asiento.getCodigo(), asiento.clone());   // Cada asiento CLONADO (LIBRE)
        }
    }

    public int getId() {
        return id;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public double getPrecio() {
        return precio;
    }

    // En Funcion.java (import java.util.Collections y java.util.List)
    public List<AsientoFuncion> getAsientos() {
        return Collections.unmodifiableList(new ArrayList<>(asientos.values()));
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }


    public AsientoFuncion getAsiento(String codigo) {
        AsientoFuncion asiento = asientos.get(codigo);
        if (asiento == null) {
            throw new EntidadNoEncontradaException("No existe el asiento " + codigo + " en la función " + id);
        }
        return asiento;
    }


    public List<AsientoFuncion> asientosLibres() {
        List<AsientoFuncion> libres = new ArrayList<>();
        for (AsientoFuncion asiento : asientos.values()) {
            if (asiento.estaLibre()) {
                libres.add(asiento);
            }
        }
        return libres;
    }


    @Override
    public Funcion clone() {
        return new Funcion(this);
    }
}