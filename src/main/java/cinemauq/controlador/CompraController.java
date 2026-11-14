package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.modelo.Cliente;
import cinemauq.modelo.Compra;
import cinemauq.modelo.Funcion;
import cinemauq.modelo.confiteria.Combo;
import cinemauq.modelo.confiteria.ItemConfiteria;
import cinemauq.modelo.confiteria.Producto;
import cinemauq.modelo.precio.CalculadorPrecio;
import cinemauq.modelo.precio.CargoServicio;
import cinemauq.modelo.precio.DescuentoPromocion;
import cinemauq.modelo.precio.DescuentoPuntos;
import cinemauq.modelo.precio.PrecioBase;
import cinemauq.modelo.puntos.TipoBeneficio;
import cinemauq.servicio.CineFacade;
import cinemauq.servicio.CompraServicio;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

/**
 * Resumen y pago. El resumen es solo una vista previa; el total real lo
 * calcula CompraServicio. Aqui no se validan reglas: se llama a la fachada
 * y se muestra el resultado o e.getMessage().
 */
public class CompraController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private Label lblFuncion;
    @FXML private Label lblAsientos;
    @FXML private Label lblConfiteria;
    @FXML private CheckBox chkPromocion;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblCargo;
    @FXML private Label lblTotal;
    @FXML private Button btnPagar;
    @FXML private Button btnVolver;
    @FXML private Button btnInicio;
    @FXML private Label lblMensaje;
    @FXML private Label lblFactura;
    @FXML private ComboBox<TipoBeneficio> cmbCanje;
    @FXML private ComboBox<ItemConfiteria> cmbItemCanje;
    @FXML private Button btnCanjear;
    @FXML private Label lblCanjes;
    @FXML private Label lblValorCanjeado;

    public CompraController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void initialize() {
        Funcion f = sesion.getFuncion();
        lblFuncion.setText("Funcion: " + f.getPelicula().getTitulo() + " - Sala "
                + f.getSala().getNumero() + " - " + f.getFecha() + " " + f.getHora());
        lblAsientos.setText("Asientos (" + sesion.getAsientos().size() + "): "
                + String.join(", ", sesion.getAsientos()));

        if (sesion.getConfiteria().isEmpty()) {
            lblConfiteria.setText("Confiteria: ninguna");
        } else {
            StringBuilder sb = new StringBuilder("Confiteria: ");
            for (ItemConfiteria item : sesion.getConfiteria()) {
                sb.append(item.getNombre()).append(", ");
            }
            sb.setLength(sb.length() - 2); // quita la ultima coma
            lblConfiteria.setText(sb.toString());
        }

        // Seccion "Canjear puntos"
        cmbCanje.getItems().setAll(TipoBeneficio.values());
        cmbItemCanje.setConverter(new StringConverter<>() {
            @Override
            public String toString(ItemConfiteria item) {
                return item == null ? "" : item.getNombre();
            }

            @Override
            public ItemConfiteria fromString(String texto) {
                return null;
            }
        });
        cmbItemCanje.setDisable(true); // solo se usa con PRODUCTO o COMBO

        btnInicio.setVisible(false); // solo se muestra despues de pagar
        btnInicio.setManaged(false);
        actualizarResumen();
    }

    /** Arma un Builder nuevo con lo que hay en la sesion. */
    private Compra.Builder construirBuilder() {
        Compra.Builder b = new Compra.Builder()
                .conCliente((Cliente) sesion.getUsuario())
                .conFuncion(sesion.getFuncion());
        for (String asiento : sesion.getAsientos()) {
            b.conAsiento(asiento);
        }
        for (ItemConfiteria item : sesion.getConfiteria()) {
            b.conConfiteria(item);
        }
        for (int i = 0; i < sesion.getCanjes().size(); i++) {
            b.conCanje(sesion.getCanjes().get(i), sesion.getValoresCanje().get(i));
        }
        return b;
    }

    /** Vista previa con la misma cadena Decorator que usa CompraServicio. */
    private void actualizarResumen() {
        double subtotal = construirBuilder().calcularSubtotal();

        CalculadorPrecio calculador = new PrecioBase(subtotal);
        double descuento = 0;
        if (chkPromocion.isSelected()) {
            calculador = new DescuentoPromocion(calculador, CompraServicio.PROMOCION_PORCENTAJE);
            descuento = subtotal - calculador.calcular();
        }
        double valorCanjeado = 0;
        for (double v : sesion.getValoresCanje()) {
            valorCanjeado += v;
        }
        if (valorCanjeado > 0) {
            calculador = new DescuentoPuntos(calculador, valorCanjeado);
        }
        calculador = new CargoServicio(calculador, CompraServicio.CARGO_SERVICIO);

        lblSubtotal.setText("Subtotal: " + dinero(subtotal));
        lblDescuento.setText(String.format("Descuento (%.0f%%): -%s",
                chkPromocion.isSelected() ? CompraServicio.PROMOCION_PORCENTAJE : 0.0,
                dinero(descuento)));
        lblCanjes.setText("Canjes: " + (sesion.getCanjes().isEmpty() ? "ninguno" : sesion.getCanjes()));
        lblValorCanjeado.setText("Valor canjeado: -" + dinero(valorCanjeado));
        lblCargo.setText("Cargo de servicio: +" + dinero(CompraServicio.CARGO_SERVICIO));
        lblTotal.setText("Total: " + dinero(calculador.calcular()));
    }

    @FXML
    private void alCambiarPromocion() {
        actualizarResumen();
    }

    /** Al cambiar el tipo, el combo de items muestra solo los de ese tipo. */
    @FXML
    private void alCambiarTipoCanje() {
        TipoBeneficio tipo = cmbCanje.getValue();
        cmbItemCanje.getItems().clear();
        for (ItemConfiteria item : sesion.getConfiteria()) {
            if ((tipo == TipoBeneficio.PRODUCTO && item instanceof Producto)
                    || (tipo == TipoBeneficio.COMBO && item instanceof Combo)) {
                cmbItemCanje.getItems().add(item);
            }
        }
        cmbItemCanje.setDisable(tipo == null || tipo == TipoBeneficio.ENTRADA);
    }

    /** Agrega el canje a la sesion; la fachada verifica los puntos al pagar. */
    @FXML
    private void alCanjear() {
        TipoBeneficio tipo = cmbCanje.getValue();
        if (tipo == null) {
            lblMensaje.setText("Elige que quieres canjear");
            return;
        }
        double valor;
        if (tipo == TipoBeneficio.ENTRADA) {
            valor = sesion.getFuncion().getPrecio();
        } else {
            ItemConfiteria item = cmbItemCanje.getValue();
            if (item == null) {
                lblMensaje.setText("Elige el item de confiteria a canjear");
                return;
            }
            valor = item.getPrecio();
        }
        lblMensaje.setText("");
        sesion.agregarCanje(tipo, valor);
        actualizarResumen();
    }

    @FXML
    private void alPagar() {
        double descuento = chkPromocion.isSelected() ? CompraServicio.PROMOCION_PORCENTAJE : 0;
        try {
            Compra compra = facade.comprar(construirBuilder(), descuento);

            lblMensaje.setText("");
            lblFactura.setText("Compra exitosa\n"
                    + "Factura No. " + compra.getNumeroFactura() + "\n"
                    + "Pelicula: " + compra.getFuncion().getPelicula().getTitulo() + "\n"
                    + "Asientos: " + String.join(", ", compra.getAsientos()) + "\n"
                    + "Valor canjeado: " + dinero(compra.getValorCanjeado()) + "\n"
                    + "Total pagado: " + dinero(compra.calcularTotal()));

            sesion.limpiarCompra();
            btnPagar.setDisable(true);
            btnVolver.setDisable(true);
            chkPromocion.setDisable(true);
            cmbCanje.setDisable(true);
            cmbItemCanje.setDisable(true);
            btnCanjear.setDisable(true);
            btnInicio.setVisible(true);
            btnInicio.setManaged(true);
        } catch (RuntimeException e) {
            lblFactura.setText("");
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void alVolver() {
        app.mostrar("confiteria");
    }

    @FXML
    private void alIrACartelera() {
        app.mostrar("cartelera");
    }

    private static String dinero(double valor) {
        return String.format("$%,.0f", valor);
    }
}