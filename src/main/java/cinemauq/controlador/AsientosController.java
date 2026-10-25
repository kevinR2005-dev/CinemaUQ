package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.AsientoFuncion;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.Sala;
import cinemauq.servicio.CineFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapa grafico de asientos. "Seleccionado" es solo estado visual de este
 * controlador; el modelo no cambia hasta que se paga.
 */
public class AsientosController {

    private static final int MAX_ASIENTOS = 8;

    private static final String ESTILO_LIBRE =
            "-fx-background-color: #4caf50; -fx-text-fill: white;";
    private static final String ESTILO_SELECCIONADO =
            "-fx-background-color: #ff9800; -fx-text-fill: white;";
    private static final String ESTILO_OCUPADO =
            "-fx-background-color: #9e9e9e; -fx-text-fill: white;";

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    // codigos elegidos, en el orden en que se hizo clic
    private final List<String> seleccionados = new ArrayList<>();

    @FXML private Label lblFuncion;
    @FXML private GridPane gridAsientos;
    @FXML private Label lblSeleccionados;
    @FXML private Label lblMensaje;

    public AsientosController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        Funcion funcion = sesion.getFuncion();
        Sala sala = funcion.getSala();

        lblFuncion.setText(funcion.getPelicula().getTitulo() + " - Sala " + sala.getNumero()
                + " - " + funcion.getFecha() + " " + funcion.getHora());

        // codigos libres segun la fachada
        Set<String> libres = new HashSet<>();
        for (AsientoFuncion a : facade.asientosDisponibles(funcion.getId())) {
            if (a.estaLibre()) {
                libres.add(a.getCodigo());
            }
        }

        // si el usuario vuelve desde confiteria, recupera lo que ya habia elegido
        List<String> previos = sesion.getAsientos();

        // encabezados de columnas (numeros)
        for (int c = 0; c < sala.getColumnas(); c++) {
            gridAsientos.add(new Label(String.valueOf(c + 1)), c + 1, 0);
        }

        for (int f = 0; f < sala.getFilas(); f++) {
            char letra = (char) ('A' + f);
            gridAsientos.add(new Label(String.valueOf(letra)), 0, f + 1); // letra de la fila

            for (int c = 0; c < sala.getColumnas(); c++) {
                String codigo = letra + String.valueOf(c + 1);
                ToggleButton boton = new ToggleButton(codigo);
                boton.setPrefWidth(48);

                if (libres.contains(codigo)) {
                    boton.setStyle(ESTILO_LIBRE);
                    boton.setOnAction(e -> alClicAsiento(boton, codigo));
                    if (previos.contains(codigo)) {
                        boton.setSelected(true);
                        boton.setStyle(ESTILO_SELECCIONADO);
                        seleccionados.add(codigo);
                    }
                } else {
                    boton.setStyle(ESTILO_OCUPADO);
                    boton.setDisable(true);
                }
                gridAsientos.add(boton, c + 1, f + 1);
            }
        }
        actualizarResumen();
    }

    private void alClicAsiento(ToggleButton boton, String codigo) {
        lblMensaje.setText("");
        if (boton.isSelected()) {
            if (seleccionados.size() >= MAX_ASIENTOS) {
                boton.setSelected(false);
                lblMensaje.setText("Maximo " + MAX_ASIENTOS + " asientos por compra");
                return;
            }
            seleccionados.add(codigo);
            boton.setStyle(ESTILO_SELECCIONADO);
        } else {
            seleccionados.remove(codigo);
            boton.setStyle(ESTILO_LIBRE);
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        if (seleccionados.isEmpty()) {
            lblSeleccionados.setText("Ningun asiento seleccionado");
        } else {
            lblSeleccionados.setText("Seleccionados (" + seleccionados.size() + "): "
                    + String.join(", ", seleccionados));
        }
    }

    @FXML
    private void alContinuar() {
        if (seleccionados.isEmpty()) {
            lblMensaje.setText("Selecciona al menos un asiento");
            return;
        }
        sesion.setAsientos(seleccionados);
        app.mostrar("confiteria");
    }

    @FXML
    private void alVolver() {
        app.mostrar("cartelera");
    }
}