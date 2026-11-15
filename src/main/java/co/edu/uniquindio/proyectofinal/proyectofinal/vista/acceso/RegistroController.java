package co.edu.uniquindio.proyectofinal.proyectofinal.vista.acceso;

import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Controlador;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistroController implements Controlador {

    @FXML private TextField txtCedula;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCelular;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtClave;
    @FXML private PasswordField txtClave2;
    @FXML private Label lblError;

    private Contexto ctx;

    @Override
    public void iniciar(Contexto ctx) { this.ctx = ctx; }

    @FXML
    private void crear() {
        try {
            if (!txtClave.getText().equals(txtClave2.getText())) throw new IllegalArgumentException("Las contraseñas no coinciden");
            Usuario u = ctx.getSistema().registrarUsuario(txtCedula.getText(), txtNombre.getText(),
                    txtCelular.getText(), txtCorreo.getText(), txtClave.getText());
            ctx.getNavegador().guardar();
            Ui.info("¡Listo, " + u.getPrimerNombre() + "! Tu cuenta quedó creada.\n\n"
                    + "Recarga tu Disponible y, si quieres, crea billeteras para separar tu plata.");
            ctx.getNavegador().mostrarCliente(u);
        } catch (RuntimeException ex) {
            lblError.setText(ex.getMessage());
        }
    }

    @FXML
    private void irIngreso() { ctx.getNavegador().mostrarIngreso(); }

    @FXML
    private void volver() { ctx.getNavegador().mostrarBienvenida(); }
}
