package cinemauq.controlador;

import cinemauq.app.CinemaUQApp;
import cinemauq.excepcion.CredencialesInvalidasException;
import cinemauq.modelo.Usuario;
import cinemauq.servicio.CineFacade;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Pantalla de login. No valida nada: delega en la fachada y muestra el mensaje. */
public class LoginController {

    private final CineFacade facade;
    private final Sesion sesion;
    private final CinemaUQApp app;

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    public LoginController(CineFacade facade, Sesion sesion, CinemaUQApp app) {
        this.facade = facade;
        this.sesion = sesion;
        this.app = app;
    }

    @FXML
    private void alIngresar() {
        Usuario usuario;
        try {
            usuario = facade.iniciarSesion(txtCorreo.getText().trim(), txtContrasena.getText());
        } catch (CredencialesInvalidasException e) {
            lblMensaje.setText(e.getMessage());
            return;
        }
        sesion.setUsuario(usuario);
        app.mostrar("cartelera");
    }
}