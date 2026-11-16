package co.edu.uniquindio.proyectofinal.proyectofinal;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.Persistencia;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Navegador;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SistemaFintech sistema = Persistencia.cargar();
        Scene scene = new Scene(new StackPane(), 1280, 800);
        URL css = getClass().getResource("/estilos.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        Navegador nav = new Navegador(stage, sistema, scene);
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setOnCloseRequest(e -> nav.guardar());
        nav.mostrarBienvenida();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
