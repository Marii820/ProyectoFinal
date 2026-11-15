package co.edu.uniquindio.proyectofinal.proyectofinal.vista;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.Persistencia;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * Carga las pantallas (.fxml), les pasa el contexto a su controlador, guarda los datos
 * y cada 20 segundos ejecuta solas las operaciones programadas que ya se vencieron.
 */
public class Navegador {

    private final Stage stage;
    private final SistemaFintech sistema;
    private final Scene scene;
    private Runnable refrescarActual = () -> { };

    public Navegador(Stage stage, SistemaFintech sistema, Scene scene) {
        this.stage = stage;
        this.sistema = sistema;
        this.scene = scene;

        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(20), e -> {
            int antes = sistema.getTransacciones().getSize();
            sistema.procesarProgramadas();
            if (sistema.getTransacciones().getSize() != antes) {
                refrescarActual.run();
                guardar();
            }
        }));
        reloj.setCycleCount(Animation.INDEFINITE);
        reloj.play();
    }

    /** Carga un .fxml de /fxml/, le pasa el contexto al controlador y devuelve el loader. */
    public static FXMLLoader cargar(String ruta, Contexto ctx) {
        URL url = Navegador.class.getResource("/fxml/" + ruta);
        if (url == null) throw new IllegalStateException("No se encontró /fxml/" + ruta);
        FXMLLoader loader = new FXMLLoader(url);
        try {
            loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Error cargando " + ruta + ": " + e.getMessage(), e);
        }
        if (loader.getController() instanceof Controlador c) c.iniciar(ctx);
        return loader;
    }

    private void mostrar(String ruta, String titulo, Usuario usuario) {
        Contexto ctx = new Contexto(sistema, this, usuario);
        FXMLLoader loader = cargar(ruta, ctx);
        scene.setRoot(loader.<Parent>getRoot());
        stage.setTitle(titulo);
        Object c = loader.getController();
        refrescarActual = c instanceof ControladorSeccion s ? s::refrescar : () -> { };
    }

    public void mostrarBienvenida() { mostrar("Bienvenida.fxml", "QuindiPay · Billetera digital", null); }

    public void mostrarIngreso() { mostrar("Ingreso.fxml", "QuindiPay · Ingresar", null); }

    public void mostrarRegistro() { mostrar("Registro.fxml", "QuindiPay · Crear cuenta", null); }

    public void mostrarCliente(Usuario u) { mostrar("cliente/Cliente.fxml", "QuindiPay · " + u.getNombre(), u); }

    public void mostrarAdmin() { mostrar("admin/Admin.fxml", "QuindiPay · Administración", null); }

    public void cerrarSesion() {
        guardar();
        mostrarBienvenida();
    }

    public void guardar() {
        try {
            Persistencia.guardar(sistema);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
