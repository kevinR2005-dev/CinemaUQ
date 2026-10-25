package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Rol;
import cinemauq.servicio.CineFacade;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Muestra la cartelera y deja elegir una funcion. */
public class CarteleraController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private Label lblBienvenida;
    @FXML private TableView<Funcion> tablaFunciones;
    @FXML private TableColumn<Funcion, String> colPelicula;
    @FXML private TableColumn<Funcion, String> colSala;
    @FXML private TableColumn<Funcion, String> colFecha;
    @FXML private TableColumn<Funcion, String> colHora;
    @FXML private TableColumn<Funcion, String> colPrecio;
    @FXML private Button btnElegir;
    @FXML private Label lblMensaje;

    public CarteleraController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        lblBienvenida.setText("Hola, " + sesion.getUsuario().getNombre());

        colPelicula.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getPelicula().getTitulo()));
        colSala.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getSala().getNumero())));
        colFecha.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getFecha())));
        colHora.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getHora())));
        colPrecio.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("$%,.0f", c.getValue().getPrecio())));

        tablaFunciones.setItems(FXCollections.observableArrayList(facade.listarCartelera()));

        // El administrador solo recarga tarjetas, no compra entradas
        if (sesion.getUsuario().getRol() == Rol.ADMINISTRADOR) {
            btnElegir.setDisable(true);
        }
    }

    @FXML
    private void alElegir() {
        Funcion elegida = tablaFunciones.getSelectionModel().getSelectedItem();
        if (elegida == null) {
            lblMensaje.setText("Selecciona una funcion de la tabla");
            return;
        }
        sesion.limpiarCompra(); // empieza una compra nueva
        sesion.setFuncion(elegida);
        app.mostrar("asientos");
    }

    @FXML
    private void alVerTarjeta() {
        app.mostrar("tarjeta");
    }

    @FXML
    private void alCerrarSesion() {
        sesion.limpiarCompra();
        sesion.setUsuario(null);
        app.mostrar("login");
    }
}