package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Datos personales, cambio de contraseña y cierre de cuenta. */
public class PerfilController implements ControladorSeccion {

    @FXML private Label lblCedula;
    @FXML private Label lblDesde;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCelular;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField pfActual;
    @FXML private PasswordField pfNueva;
    @FXML private PasswordField pfConfirmar;
    @FXML private PasswordField pfCierre;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
    }

    @Override
    public void refrescar() {
        lblCedula.setText(usuario.getId());
        lblDesde.setText(Ui.fecha(usuario.getFechaRegistro()));
        if (!txtNombre.isFocused()) txtNombre.setText(usuario.getNombre());
        if (!txtCelular.isFocused()) txtCelular.setText(usuario.getTelefono());
        if (!txtCorreo.isFocused()) txtCorreo.setText(usuario.getCorreo());
    }

    @FXML
    private void guardarDatos() {
        Ui.ejecutar(() -> {
            sistema.modificarUsuario(usuario.getId(), txtNombre.getText(), txtCelular.getText(), txtCorreo.getText());
            Ui.info("Datos actualizados");
        }, ctx.getAlCambiar());
    }

    @FXML
    private void cambiarClave() {
        Ui.ejecutar(() -> {
            if (!pfNueva.getText().equals(pfConfirmar.getText())) throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
            sistema.cambiarContrasena(usuario.getId(), pfActual.getText(), pfNueva.getText());
            pfActual.clear();
            pfNueva.clear();
            pfConfirmar.clear();
            Ui.info("Contraseña actualizada");
        }, ctx.getAlCambiar());
    }

    @FXML
    private void cerrarCuenta() {
        if (!Ui.confirmar("¿Seguro que quiere cerrar su cuenta? Esta acción no se puede deshacer.")) return;
        try {
            sistema.eliminarCuenta(usuario.getId(), pfCierre.getText());
            ctx.getNavegador().guardar();
            Ui.info("Su cuenta fue cerrada. Gracias por usar QuindiPay.");
            ctx.getNavegador().mostrarBienvenida();
        } catch (RuntimeException ex) {
            Ui.error(ex.getMessage());
        }
    }
}
