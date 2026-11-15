package co.edu.uniquindio.proyectofinal.proyectofinal.vista.acceso;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Controlador;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class IngresoController implements Controlador {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;

    private Contexto ctx;

    @Override
    public void iniciar(Contexto ctx) { this.ctx = ctx; }

    @FXML
    private void entrar() {
        SistemaFintech sistema = ctx.getSistema();
        String usuario = txtUsuario.getText(), clave = txtClave.getText();
        if (sistema.esAdministrador(usuario, clave)) {
            ctx.getNavegador().mostrarAdmin();
            return;
        }
        try {
            Usuario u = sistema.iniciarSesion(usuario, clave);
            ctx.getNavegador().guardar();
            ctx.getNavegador().mostrarCliente(u);
        } catch (RuntimeException ex) {
            lblError.setText(ex.getMessage());
            txtClave.clear();
        }
    }

    @FXML
    private void irRegistro() { ctx.getNavegador().mostrarRegistro(); }

    @FXML
    private void volver() { ctx.getNavegador().mostrarBienvenida(); }
}
