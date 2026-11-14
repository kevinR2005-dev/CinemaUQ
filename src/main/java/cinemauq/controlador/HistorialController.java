package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.servicio.CineFacade;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

/**
 * Historial del cliente: sus compras, boton para cancelar y sus notificaciones.
 * Las reglas de cancelacion las aplica la fachada; aqui solo se muestra el
 * reembolso o e.getMessage().
 */
public class HistorialController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private TableView<Compra> tablaCompras;
    @FXML private TableColumn<Compra, String> colFactura;
    @FXML private TableColumn<Compra, String> colFecha;
    @FXML private TableColumn<Compra, String> colTotal;
    @FXML private TableColumn<Compra, String> colEstado;
    @FXML private ListView<String> listaNotificaciones;
    @FXML private Label lblMensaje;
    @FXML private Label lblResultado;

    public HistorialController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        colFactura.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getNumeroFactura())));
        colFecha.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFecha().format(FORMATO_FECHA)));
        colTotal.setCellValueFactory(c ->
                new SimpleStringProperty(dinero(c.getValue().calcularTotal())));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado().getNombre()));
        recargar();
    }

    /** Vuelve a pedir a la fachada las compras y las notificaciones. */
    private void recargar() {
        Cliente cliente = (Cliente) sesion.getUsuario();
        tablaCompras.setItems(FXCollections.observableArrayList(facade.consultarHistorial(cliente)));
        listaNotificaciones.setItems(
                FXCollections.observableArrayList(facade.consultarNotificaciones(cliente)));
    }

    @FXML
    private void alCancelarCompra() {
        Compra elegida = tablaCompras.getSelectionModel().getSelectedItem();
        if (elegida == null) {
            lblResultado.setText("");
            lblMensaje.setText("Selecciona una compra de la tabla");
            return;
        }
        try {
            double reembolso = facade.cancelarCompra((Cliente) sesion.getUsuario(),
                    elegida.getNumeroFactura());
            lblMensaje.setText("");
            lblResultado.setText("Compra cancelada. Reembolso: " + dinero(reembolso));
            recargar(); // actualiza el estado y la nueva notificacion
        } catch (RuntimeException e) {
            lblResultado.setText("");
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alIrACartelera() {
        app.mostrar("cartelera");
    }

    @FXML
    private void alIrAPuntos() {
        app.mostrar("puntos");
    }

    @FXML
    private void alIrATarjeta() {
        app.mostrar("tarjeta");
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }
}