package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.puntos.LotePuntos;
import cinemauq.servicio.CineFacade;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Muestra los puntos vigentes del cliente y sus lotes. */
public class PuntosController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private Label lblPuntosVigentes;
    @FXML private TableView<LotePuntos> tablaLotes;
    @FXML private TableColumn<LotePuntos, String> colObtenidos;
    @FXML private TableColumn<LotePuntos, String> colRestantes;
    @FXML private TableColumn<LotePuntos, String> colObtencion;
    @FXML private TableColumn<LotePuntos, String> colVencimiento;
    @FXML private Label lblMensaje;

    public PuntosController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        Cliente cliente = (Cliente) sesion.getUsuario();

        colObtenidos.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPuntosObtenidos())));
        colRestantes.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getPuntosRestantes())));
        colObtencion.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getFechaObtencion())));
        colVencimiento.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getFechaVencimiento())));

        try {
            lblPuntosVigentes.setText("Puntos vigentes: " + facade.consultarPuntos(cliente));
            tablaLotes.setItems(FXCollections.observableArrayList(facade.consultarLotes(cliente)));
        } catch (RuntimeException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alIrACartelera() {
        app.mostrar("cartelera");
    }

    @FXML
    private void alIrAHistorial() {
        app.mostrar("historial");
    }

    @FXML
    private void alIrATarjeta() {
        app.mostrar("tarjeta");
    }
}