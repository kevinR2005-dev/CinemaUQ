package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.servicio.CineFacade;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/** Confiteria: el usuario agrega o quita productos y combos de la sesion. */
public class ConfiteriaController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private ListView<ItemConfiteria> listaCatalogo;
    @FXML private ListView<ItemConfiteria> listaSeleccion;
    @FXML private Label lblTotalConfiteria;
    @FXML private Label lblMensaje;

    public ConfiteriaController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        listaCatalogo.setCellFactory(l -> crearCelda());
        listaSeleccion.setCellFactory(l -> crearCelda());
        listaCatalogo.setItems(FXCollections.observableArrayList(facade.listarConfiteria()));
        refrescarSeleccion();
    }

    /** Celda que muestra "nombre (combo) - $precio". */
    private ListCell<ItemConfiteria> crearCelda() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ItemConfiteria item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio || item == null) {
                    setText(null);
                } else {
                    String tipo = (item instanceof Combo) ? " (combo)" : "";
                    setText(item.getNombre() + tipo + " - " + dinero(item.getPrecio()));
                }
            }
        };
    }

    private void refrescarSeleccion() {
        listaSeleccion.setItems(FXCollections.observableArrayList(sesion.getConfiteria()));
        double total = 0;
        for (ItemConfiteria item : sesion.getConfiteria()) {
            total += item.getPrecio();
        }
        lblTotalConfiteria.setText("Total confiteria: " + dinero(total));
    }

    @FXML
    private void alAgregar() {
        ItemConfiteria item = listaCatalogo.getSelectionModel().getSelectedItem();
        if (item == null) {
            lblMensaje.setText("Selecciona un producto o combo del catalogo");
            return;
        }
        lblMensaje.setText("");
        sesion.agregarItem(item);
        refrescarSeleccion();
    }

    @FXML
    private void alQuitar() {
        ItemConfiteria item = listaSeleccion.getSelectionModel().getSelectedItem();
        if (item == null) {
            lblMensaje.setText("Selecciona un item de tu seleccion para quitarlo");
            return;
        }
        lblMensaje.setText("");
        sesion.quitarItem(item);
        refrescarSeleccion();
    }

    @FXML
    private void alContinuar() {
        app.mostrar("compra"); // la confiteria es opcional
    }

    @FXML
    private void alVolver() {
        app.mostrar("asientos");
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }
}