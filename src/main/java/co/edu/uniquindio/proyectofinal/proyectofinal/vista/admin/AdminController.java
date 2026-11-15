package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Navegador;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Panel interno del administrador (se entra con "admin" desde Ingresar).
 * Aquí van los módulos que necesitan ver a TODOS los usuarios: reportes 4.8, auditoría 4.9,
 * árbol, grafo, cola global de programadas y comparación de rendimiento.
 */
public class AdminController implements ControladorSeccion {

    @FXML private Button btnReportes;
    @FXML private Button btnUsuarios;
    @FXML private Button btnArbol;
    @FXML private Button btnGrafo;
    @FXML private Button btnSeguridad;
    @FXML private Button btnProgramadas;
    @FXML private Button btnRendimiento;
    @FXML private Label lblEstado;
    @FXML private StackPane contenido;

    private Contexto ctx;
    private final Map<String, Node> vistas = new HashMap<>();
    private final Map<String, ControladorSeccion> controladores = new HashMap<>();
    private ControladorSeccion actual;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        ctx.setAlCambiar(() -> { refrescar(); ctx.getNavegador().guardar(); });
        ctx.setIrA(this::irA);
        irA("Reportes");
    }

    private void irA(String nombre) {
        if (!vistas.containsKey(nombre)) {
            FXMLLoader loader = Navegador.cargar("admin/" + nombre + ".fxml", ctx);
            vistas.put(nombre, loader.getRoot());
            controladores.put(nombre, loader.getController());
        }
        actual = controladores.get(nombre);
        contenido.getChildren().setAll(vistas.get(nombre));
        Button[] botones = {btnReportes, btnUsuarios, btnArbol, btnGrafo, btnSeguridad, btnProgramadas, btnRendimiento};
        String[] nombres = {"Reportes", "Usuarios", "Arbol", "Grafo", "Seguridad", "ProgramadasAdmin", "Rendimiento"};
        for (int i = 0; i < botones.length; i++) {
            botones[i].getStyleClass().remove("activo");
            if (nombres[i].equals(nombre)) botones[i].getStyleClass().add("activo");
        }
        refrescar();
    }

    @Override
    public void refrescar() {
        SistemaFintech s = ctx.getSistema();
        lblEstado.setText(s.cantidadUsuarios() + " usuarios · " + s.cantidadBilleteras() + " billeteras · "
                + s.getTransacciones().getSize() + " transacciones · reloj: " + Ui.fecha(s.ahora()));
        if (actual != null) actual.refrescar();
    }

    @FXML private void irReportes() { irA("Reportes"); }
    @FXML private void irUsuarios() { irA("Usuarios"); }
    @FXML private void irArbol() { irA("Arbol"); }
    @FXML private void irGrafo() { irA("Grafo"); }
    @FXML private void irSeguridad() { irA("Seguridad"); }
    @FXML private void irProgramadas() { irA("ProgramadasAdmin"); }
    @FXML private void irRendimiento() { irA("Rendimiento"); }
    @FXML private void cerrarSesion() { ctx.getNavegador().cerrarSesion(); }
}
