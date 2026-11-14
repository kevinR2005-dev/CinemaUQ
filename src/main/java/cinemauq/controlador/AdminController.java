package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Funcion;
import cinemauq.servicio.CineFacade;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Administracion de funciones: el administrador cancela una funcion y se
 * muestra cuantas compras se reembolsaron. El permiso lo valida la fachada
 * (CancelacionServicio), no este controlador.
 */
public class AdminController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private ListView<Funcion> listaFunciones;
    @FXML private Label lblMensaje;
    @FXML private Label lblResultado;

    public AdminController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        listaFunciones.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Funcion f, boolean vacio) {
                super.updateItem(f, vacio);
                if (vacio || f == null) {
                    setText(null);
                } else {
                    setText("#" + f.getId() + " - " + f.getPelicula().getTitulo()
                            + " - Sala " + f.getSala().getNumero()
                            + " - " + f.getFecha() + " " + f.getHora());
                }
            }
        });
        recargar();
    }

    /** La cartelera solo trae funciones programadas: las canceladas desaparecen. */
    private void recargar() {
        listaFunciones.setItems(FXCollections.observableArrayList(facade.listarCartelera()));
    }

    @FXML
    private void alCancelarFuncion() {
        Funcion elegida = listaFunciones.getSelectionModel().getSelectedItem();
        if (elegida == null) {
            lblResultado.setText("");
            lblMensaje.setText("Selecciona una funcion de la lista");
            return;
        }
        try {
            int reembolsadas = facade.cancelarFuncion(sesion.getUsuario(), elegida.getId());
            lblMensaje.setText("");
            lblResultado.setText("Funcion cancelada. Compras reembolsadas: " + reembolsadas);
            recargar();
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
    private void alIrAReportes() {
        app.mostrar("reportes");
    }

    @FXML
    private void alIrATarjeta() {
        app.mostrar("tarjeta");
    }
}