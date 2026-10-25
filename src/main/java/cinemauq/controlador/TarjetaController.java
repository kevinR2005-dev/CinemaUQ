package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Rol;
import cinemauq.modelo.tarjeta.Movimiento;
import cinemauq.servicio.CineFacade;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;

/**
 * Tarjeta virtual. El cliente ve su saldo y movimientos; el administrador
 * elige un cliente y recarga. La regla "solo el admin recarga" la aplica el
 * Proxy, no este controlador (ocultar el panel es solo estetica).
 */
public class TarjetaController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private Label lblSaldo;
    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, String> colFecha;
    @FXML private TableColumn<Movimiento, String> colTipo;
    @FXML private TableColumn<Movimiento, String> colMonto;
    @FXML private TableColumn<Movimiento, String> colSaldoResultante;
    @FXML private TableColumn<Movimiento, String> colDetalle;
    @FXML private VBox panelAdmin;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private TextField txtMonto;
    @FXML private Button btnRecargar;
    @FXML private Label lblMensaje;

    public TarjetaController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFecha().format(FORMATO_FECHA)));
        colTipo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTipo().name()));
        colMonto.setCellValueFactory(c ->
                new SimpleStringProperty(dinero(c.getValue().getMonto())));
        colSaldoResultante.setCellValueFactory(c ->
                new SimpleStringProperty(dinero(c.getValue().getSaldoResultante())));
        colDetalle.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDetalle()));

        boolean esAdmin = sesion.getUsuario().getRol() == Rol.ADMINISTRADOR;
        panelAdmin.setVisible(esAdmin);
        panelAdmin.setManaged(esAdmin);

        if (esAdmin) {
            cmbClientes.setConverter(new StringConverter<>() {
                @Override
                public String toString(Cliente c) {
                    return c == null ? "" : c.getNombre() + " (" + c.getCorreo() + ")";
                }

                @Override
                public Cliente fromString(String texto) {
                    return null; // el combo no es editable
                }
            });
            cmbClientes.setItems(FXCollections.observableArrayList(facade.listarClientes()));
            cmbClientes.valueProperty().addListener((obs, anterior, nuevo) -> refrescar());
            cmbClientes.getSelectionModel().selectFirst();
        }
        refrescar();
    }

    /** Cliente cuya tarjeta se muestra: el logueado, o el elegido si es admin. */
    private Cliente clienteActual() {
        if (sesion.getUsuario() instanceof Cliente c) {
            return c;
        }
        return cmbClientes.getValue();
    }

    private void refrescar() {
        Cliente cliente = clienteActual();
        if (cliente == null) {
            lblSaldo.setText("Selecciona un cliente");
            tablaMovimientos.getItems().clear();
            return;
        }
        lblSaldo.setText("Saldo: " + dinero(facade.consultarSaldo(cliente)));
        tablaMovimientos.setItems(
                FXCollections.observableArrayList(facade.consultarMovimientos(cliente)));
    }

    @FXML
    private void alRecargar() {
        Cliente destino = clienteActual();
        if (destino == null) {
            mostrarError("Selecciona un cliente");
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarError("Ingresa un monto numerico");
            return;
        }
        try {
            facade.recargar(sesion.getUsuario(), destino, monto);
        } catch (RuntimeException e) {
            // AccesoDenegadoException, monto invalido, etc.
            mostrarError(e.getMessage());
            return;
        }
        txtMonto.clear();
        mostrarOk("Recarga exitosa");
        refrescar();
    }

    @FXML
    private void alVolver() {
        app.mostrar("cartelera");
    }

    private void mostrarError(String texto) {
        lblMensaje.getStyleClass().setAll("label", "mensaje-error");
        lblMensaje.setText(texto);
    }

    private void mostrarOk(String texto) {
        lblMensaje.getStyleClass().setAll("label", "mensaje-ok");
        lblMensaje.setText(texto);
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }
}