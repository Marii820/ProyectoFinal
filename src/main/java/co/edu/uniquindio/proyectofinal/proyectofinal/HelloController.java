package co.edu.uniquindio.proyectofinal.proyectofinal;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class HelloController {
    @FXML
    private TextField txtNombre;

    @FXML
    private Label lblResultado;

    @FXML
    protected void onHelloButtonClick(){
        String nombre = txtNombre.getText();

        if (nombre.isBlank()) {
            lblResultado.setText("Ingrese un nombre.");
        } else {
            lblResultado.setText("¡Hola, " + nombre + "!");
        }
}
}
