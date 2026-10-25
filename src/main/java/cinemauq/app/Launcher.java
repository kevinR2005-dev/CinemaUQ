package cinemauq.app;

/**
 * Clase de arranque separada de Application. Sirve para ejecutar la app
 * desde un jar o desde el IDE sin el error de "faltan componentes JavaFX".
 */
public class Launcher {

    public static void main(String[] args) {
        CinemaUQApp.main(args);
    }
}   