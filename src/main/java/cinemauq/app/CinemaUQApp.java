package cinemauq.app;

import cinemauq.controlador.Sesion;
import cinemauq.modelo.Compra;
import cinemauq.modelo.ConsecutivoFactura;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Pelicula;
import cinemauq.modelo.Usuario;
import cinemauq.repositorio.Repositorio;
import cinemauq.repositorio.RepositorioMemoria;
import cinemauq.servicio.AuthServicio;
import cinemauq.servicio.CarteleraServicio;
import cinemauq.servicio.CineFacade;
import cinemauq.servicio.CompraServicio;
import cinemauq.servicio.PagoServicio;
import cinemauq.servicio.TarjetaServicio;
import cinemauq.servicio.TarjetaServicioProxy;
import cinemauq.servicio.TarjetaServicioReal;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Punto de entrada JavaFX. Arma todo el sistema (repositorios, servicios,
 * fachada, sesion) y cambia de pantalla con mostrar(nombreFxml).
 */
public class CinemaUQApp extends Application {

    private Stage stage;
    private CineFacade facade;
    private Sesion sesion;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        // Repositorios
        Repositorio<Pelicula, Integer> peliculas = new RepositorioMemoria<>(Pelicula::getId);
        Repositorio<Funcion, Integer> funciones = new RepositorioMemoria<>(Funcion::getId);
        Repositorio<Usuario, Integer> usuarios = new RepositorioMemoria<>(Usuario::getId);
        Repositorio<Compra, Integer> compras = new RepositorioMemoria<>(Compra::getNumeroFactura);

        // Unica llamada a getInstancia() de la app grafica
        ConsecutivoFactura consecutivo = ConsecutivoFactura.getInstancia();

        // Datos de prueba
        DatosIniciales datos = new DatosIniciales();
        datos.cargar(peliculas, funciones, usuarios);

        // Servicios
        PagoServicio pagoServicio = new PagoServicio();
        CarteleraServicio cartelera = new CarteleraServicio(funciones, peliculas);
        CompraServicio compraServicio = new CompraServicio(compras, consecutivo, pagoServicio);
        AuthServicio auth = new AuthServicio(usuarios);
        // Proxy de proteccion envolviendo al servicio real
        TarjetaServicio tarjetas = new TarjetaServicioProxy(new TarjetaServicioReal());

        // Fachada y sesion
        facade = new CineFacade(auth, cartelera, compraServicio, tarjetas, datos.getCatalogo());
        sesion = new Sesion();

        stage.setTitle("CinemaUQ");
        mostrar("login");
        stage.show();
    }

    /** Carga /cinemauq/vista/<nombreFxml>.fxml y la muestra en la ventana. */
    public void mostrar(String nombreFxml) {
        try {
            URL fxml = getClass().getResource("/cinemauq/vista/" + nombreFxml + ".fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setControllerFactory(this::crearControlador);
            Parent raiz = loader.load();

            Scene escena = new Scene(raiz, 900, 600);
            URL css = getClass().getResource("/cinemauq/vista/estilos.css");
            if (css != null) { // estilos.css es opcional
                escena.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(escena);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista " + nombreFxml, e);
        }
    }

    /** Construye cada controlador con su constructor (CineFacade, Sesion, CinemaUQApp). */
    private Object crearControlador(Class<?> tipo) {
        try {
            return tipo.getConstructor(CineFacade.class, Sesion.class, CinemaUQApp.class)
                    .newInstance(facade, sesion, this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("No se pudo crear el controlador " + tipo.getName(), e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}