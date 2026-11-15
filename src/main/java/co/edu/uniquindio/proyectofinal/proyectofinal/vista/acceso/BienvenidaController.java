package co.edu.uniquindio.proyectofinal.proyectofinal.vista.acceso;

import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Controlador;
import javafx.fxml.FXML;

/** Primera pantalla: solo Ingresar o Registrarme. */
public class BienvenidaController implements Controlador {

    private Contexto ctx;

    @Override
    public void iniciar(Contexto ctx) { this.ctx = ctx; }

    @FXML
    private void ingresar() { ctx.getNavegador().mostrarIngreso(); }

    @FXML
    private void registrarme() { ctx.getNavegador().mostrarRegistro(); }
}
