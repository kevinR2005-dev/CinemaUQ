package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.reporte.TipoReporte;
import cinemauq.servicio.CineFacade;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reportes y estadisticas (solo administrador). El texto, las series de los
 * graficos y las estadisticas salen de la fachada: este controlador no calcula nada.
 */
public class ReportesController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private ComboBox<TipoReporte> cmbTipo;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<String> cmbFormato;
    @FXML private TextArea areaReporte;
    @FXML private Label lblMensaje;
    @FXML private Label lblResultado;
    @FXML private BarChart<String, Number> graficoBarras;
    @FXML private PieChart graficoPastel;
    @FXML private LineChart<String, Number> graficoLineas;
    @FXML private Label lblVentasTotales;
    @FXML private Label lblPeliculaTop;
    @FXML private Label lblProductoTop;
    @FXML private Label lblReembolsos;

    public ReportesController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        cmbTipo.getItems().setAll(TipoReporte.values());
        cmbFormato.getItems().setAll("TXT", "CSV");
        cmbFormato.setValue("TXT");
        // Rango por defecto: los ultimos dos meses (donde estan los datos de prueba)
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusMonths(2));
        graficoBarras.setAnimated(false);
        graficoLineas.setAnimated(false);
        graficoPastel.setAnimated(false);
    }

    @FXML
    private void alEmitir() {
        if (!hayDatosCompletos()) {
            return;
        }
        TipoReporte tipo = cmbTipo.getValue();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        try {
            areaReporte.setText(facade.emitirReporte(tipo, desde, hasta, cmbFormato.getValue()));
            dibujarGraficos(facade.serieReporte(tipo, desde, hasta));
            mostrarEstadisticas(desde, hasta);
            lblMensaje.setText("");
            lblResultado.setText("Reporte emitido");
        } catch (RuntimeException e) {
            lblResultado.setText("");
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alExportar() {
        if (!hayDatosCompletos()) {
            return;
        }
        TipoReporte tipo = cmbTipo.getValue();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        // Archivo .txt dentro de la carpeta "reportes" (el exportador crea la carpeta)
        Path ruta = Path.of("reportes", tipo.name().toLowerCase() + "_" + desde + "_" + hasta + ".txt");
        try {
            facade.exportarReporte(tipo, desde, hasta, cmbFormato.getValue(), ruta);
            lblMensaje.setText("");
            lblResultado.setText("Reporte guardado en " + ruta.toAbsolutePath());
        } catch (RuntimeException e) {
            lblResultado.setText("");
            lblMensaje.setText(e.getMessage());
        }
    }

    /** Pinta la misma serie en los tres graficos. */
    private void dibujarGraficos(Map<String, Double> serie) {
        XYChart.Series<String, Number> barras = new XYChart.Series<>();
        XYChart.Series<String, Number> lineas = new XYChart.Series<>();
        graficoPastel.getData().clear();
        for (Map.Entry<String, Double> par : serie.entrySet()) {
            barras.getData().add(new XYChart.Data<>(par.getKey(), par.getValue()));
            lineas.getData().add(new XYChart.Data<>(par.getKey(), par.getValue()));
            graficoPastel.getData().add(new PieChart.Data(par.getKey(), par.getValue()));
        }
        graficoBarras.getData().setAll(barras);
        graficoLineas.getData().setAll(lineas);
    }

    /** Muestra lo que devuelve la fachada. */
    private void mostrarEstadisticas(LocalDate desde, LocalDate hasta) {
        lblVentasTotales.setText("Ventas totales: "
                + dinero(facade.ventasTotales(desde, hasta)));
        lblPeliculaTop.setText("Pelicula mas vendida: "
                + primero(facade.peliculasMasVendidas(1)));
        lblProductoTop.setText("Producto mas vendido: "
                + primero(facade.productosMasVendidos(1)));
        lblReembolsos.setText("Total reembolsos: "
                + dinero(facade.totalReembolsos(desde, hasta)));
    }

    /** Solo revisa que el usuario haya elegido todo; las reglas las valida el servicio. */
    private boolean hayDatosCompletos() {
        if (cmbTipo.getValue() == null || dpDesde.getValue() == null
                || dpHasta.getValue() == null || cmbFormato.getValue() == null) {
            lblResultado.setText("");
            lblMensaje.setText("Elige el tipo, las dos fechas y el formato");
            return false;
        }
        return true;
    }

    private static String primero(List<String> lista) {
        return lista.isEmpty() ? "sin datos" : lista.get(0);
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }

    @FXML
    private void alIrACartelera() {
        app.mostrar("cartelera");
    }

    @FXML
    private void alIrAAdmin() {
        app.mostrar("admin");
    }
}