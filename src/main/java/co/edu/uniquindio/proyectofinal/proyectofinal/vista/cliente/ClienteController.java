package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Marco de la app del cliente: menú lateral + encabezado. Cada sección es un .fxml aparte
 * que se carga la primera vez que se abre. El cliente solo ve SUS datos.
 */
public class ClienteController implements ControladorSeccion {

    @FXML private Button btnInicio;
    @FXML private Button btnCuenta;
    @FXML private Button btnMover;
    @FXML private Button btnMovimientos;
    @FXML private Button btnProgramadas;
    @FXML private Button btnRecompensas;
    @FXML private Button btnNotificaciones;
    @FXML private Button btnSeguridad;
    @FXML private Button btnPerfil;
    @FXML private Label lblSaludo;
    @FXML private Label lblPuntos;
    @FXML private Label lblNivel;
    @FXML private StackPane contenido;

    private Contexto ctx;
    private Usuario usuario;
    private final Map<String, Node> vistas = new HashMap<>();
    private final Map<String, ControladorSeccion> controladores = new HashMap<>();
    private ControladorSeccion actual;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.usuario = ctx.getUsuario();
        ctx.setAlCambiar(() -> { refrescar(); ctx.getNavegador().guardar(); });
        ctx.setIrA(this::irA);
        irA("inicio");
    }

    /** nombre de la sección, opcionalmente con una parte: "mover#recargar", "bolsillo#BD000001", "moverbolsillo#cargar:BD000001". */
    public void irA(String destino) {
        String[] p = destino.split("#");
        String seccion = p[0];
        if (!vistas.containsKey(seccion)) {
            FXMLLoader loader = Navegador.cargar("cliente/" + archivo(seccion), ctx);
            vistas.put(seccion, loader.getRoot());
            controladores.put(seccion, loader.getController());
        }
        actual = controladores.get(seccion);
        contenido.getChildren().setAll(vistas.get(seccion));
        for (Button b : new Button[]{btnInicio, btnCuenta, btnMover, btnMovimientos, btnProgramadas, btnRecompensas, btnNotificaciones, btnSeguridad, btnPerfil})
            b.getStyleClass().remove("activo");
        boton(seccion).getStyleClass().add("activo");
        if (p.length > 1) actual.mostrarParte(p[1]);
        refrescar();
    }

    private String archivo(String seccion) {
        return switch (seccion) {
            case "inicio" -> "Inicio.fxml";
            case "cuenta" -> "Cuenta.fxml";
            case "bolsillo" -> "Bolsillo.fxml";
            case "moverbolsillo" -> "MoverBolsillo.fxml";
            case "mover" -> "MoverDinero.fxml";
            case "movimientos" -> "Movimientos.fxml";
            case "programadas" -> "Programadas.fxml";
            case "recompensas" -> "Recompensas.fxml";
            case "notificaciones" -> "Notificaciones.fxml";
            case "perfil" -> "Perfil.fxml";
            case "seguridad" -> "Seguridad.fxml";
            default -> throw new IllegalArgumentException("Sección desconocida: " + seccion);
        };
    }

    private Button boton(String seccion) {
        return switch (seccion) {
            case "cuenta", "bolsillo", "moverbolsillo" -> btnCuenta;
            case "mover" -> btnMover;
            case "movimientos" -> btnMovimientos;
            case "programadas" -> btnProgramadas;
            case "recompensas" -> btnRecompensas;
            case "notificaciones" -> btnNotificaciones;
            case "perfil" -> btnPerfil;
            case "seguridad" -> btnSeguridad;
            default -> btnInicio;
        };
    }

    @Override
    public void refrescar() {
        lblSaludo.setText("Hola, " + usuario.getPrimerNombre());
        lblNivel.setText(usuario.getNivel().name());
        lblNivel.getStyleClass().removeAll("BRONCE", "PLATA", "ORO", "PLATINO");
        lblNivel.getStyleClass().add(usuario.getNivel().name());
        lblPuntos.setText(String.format("%,d pts", usuario.getPuntosDisponibles()));
        int noLeidas = usuario.notificacionesNoLeidas();
        btnNotificaciones.setText(noLeidas > 0 ? "Notificaciones  (" + noLeidas + ")" : "Notificaciones");
        if (actual != null) actual.refrescar();
    }

    @FXML private void irInicio() { irA("inicio"); }
    @FXML private void irCuenta() { irA("cuenta"); }
    @FXML private void irMover() { irA("mover"); }
    @FXML private void irMovimientos() { irA("movimientos"); }
    @FXML private void irProgramadas() { irA("programadas"); }
    @FXML private void irRecompensas() { irA("recompensas"); }
    @FXML private void irNotificaciones() { irA("notificaciones"); }
    @FXML private void irPerfil() { irA("perfil"); }
    @FXML private void irSeguridad() { irA("seguridad"); }
    @FXML private void cerrarSesion() { ctx.getNavegador().cerrarSesion(); }
}
