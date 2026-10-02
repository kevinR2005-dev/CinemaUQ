package cinemauq.modelo;

public class Pelicula {

    private final int id;
    private final String titulo;
    private final int duracionMin;
    private final String genero;

    public Pelicula(int id, String titulo, int duracionMin, String genero) {
        this.id = id;
        this.titulo = titulo;
        this.duracionMin = duracionMin;
        this.genero = genero;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public String getGenero() {
        return genero;
    }
}
