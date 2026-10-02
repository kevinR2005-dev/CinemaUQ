package cinemauq.servicio;

import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Sala;
import cinemauq.repositorio.Repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class CarteleraServicio {

    private final Repositorio<Funcion, Integer> funcionRepo;
    private final Repositorio<Pelicula, Integer> peliculaRepo;

    public CarteleraServicio(Repositorio<Funcion, Integer> funcionRepo,
                             Repositorio<Pelicula, Integer> peliculaRepo) {
        this.funcionRepo = funcionRepo;
        this.peliculaRepo = peliculaRepo;
    }


    public List<Funcion> listarFunciones() {
        return funcionRepo.listar();
    }


    public Funcion programarFuncion(int id, int idPelicula, Sala sala,
                                    LocalDate fecha, LocalTime hora, double precio) {
        validarIdLibre(id);

        Pelicula pelicula = peliculaRepo.buscarPorId(idPelicula);
        Funcion funcion = new Funcion(id, pelicula, sala, fecha, hora, precio);
        funcionRepo.guardar(funcion);
        return funcion;
    }


    public List<AsientoFuncion> asientosDisponibles(int idFuncion) {
        // Lanza EntidadNoEncontradaException si la función no existe
        return funcionRepo.buscarPorId(idFuncion).asientosLibres();
    }


    public Funcion duplicarFuncion(int idOriginal, int idNuevo, LocalDate fecha, LocalTime hora) {
        validarIdLibre(idNuevo);
        Funcion original = funcionRepo.buscarPorId(idOriginal);
        Funcion copia = original.clone();   // PATRÓN PROTOTYPE: se copia en vez de construir desde cero
        copia.setId(idNuevo);               // El id se cambia ANTES de guardar
        copia.setFecha(fecha);
        copia.setHora(hora);
        funcionRepo.guardar(copia);
        return copia;
    }


    private void validarIdLibre(int id) {
        for (Funcion f : funcionRepo.listar()) {
            if (f.getId() == id) {
                throw new IllegalArgumentException("Ya existe una función con id " + id);
            }
        }
    }
}