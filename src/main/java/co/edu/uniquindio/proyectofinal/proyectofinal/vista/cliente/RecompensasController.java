package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Beneficio;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelUsuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Puntos, nivel, ventajas y canje de beneficios. */
public class RecompensasController implements ControladorSeccion {

    @FXML private Label lblNivel;
    @FXML private Label lblPuntos;
    @FXML private ProgressBar progreso;
    @FXML private Label lblProgreso;
    @FXML private Label lblVentajas;
    @FXML private Label lblRanking;
    @FXML private TableView<Beneficio> tabla;
    @FXML private TableColumn<Beneficio, String> colNombre;
    @FXML private TableColumn<Beneficio, String> colDetalle;
    @FXML private TableColumn<Beneficio, Number> colPuntos;
    @FXML private TableColumn<Beneficio, String> colNivel;
    @FXML private TableColumn<Beneficio, String> colDisponible;
    @FXML private ListView<Beneficio> lvCanjeados;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        Ui.ajustar(tabla, "Sin beneficios");
        Ui.texto(colNombre, Beneficio::getNombre);
        Ui.texto(colDetalle, Beneficio::getDescripcion);
        Ui.numero(colPuntos, Beneficio::getCostoPuntos, false);
        Ui.texto(colNivel, Beneficio::getNivelMinimo);
        Ui.texto(colDisponible, b -> b.puedeCanjear(usuario) ? "Sí" : "No");
        lvCanjeados.setPlaceholder(new Label("Aún no has canjeado beneficios"));
    }

    @Override
    public void refrescar() {
        NivelUsuario n = usuario.getNivel();
        lblNivel.setText("Nivel " + n);
        lblNivel.getStyleClass().removeAll("BRONCE", "PLATA", "ORO", "PLATINO");
        lblNivel.getStyleClass().add(n.name());
        lblPuntos.setText(String.format("%,d puntos disponibles  ·  %,d acumulados", usuario.getPuntosDisponibles(), usuario.getPuntosAcumulados()));
        NivelUsuario sig = n.siguiente();
        if (sig == null) {
            progreso.setProgress(1);
            lblProgreso.setText("¡Estás en el nivel más alto!");
        } else {
            double rango = sig.getPuntosMin() - n.getPuntosMin();
            progreso.setProgress(Math.max(0, Math.min(1, (usuario.getPuntosAcumulados() - n.getPuntosMin()) / rango)));
            lblProgreso.setText("Te faltan " + String.format("%,d", usuario.puntosParaSiguienteNivel()) + " puntos para " + sig);
        }
        lblVentajas.setText(n.ventajas());
        lblRanking.setText("Puesto #" + sistema.posicionEnRanking(usuario.getId()) + " de " + sistema.cantidadUsuarios() + " usuarios en puntos");
        tabla.getItems().setAll(Ui.lista(sistema.getCatalogoBeneficios()));
        tabla.refresh();
        lvCanjeados.getItems().setAll(Ui.lista(usuario.getBeneficiosCanjeados()));
    }

    @FXML
    private void canjear() {
        Beneficio b = tabla.getSelectionModel().getSelectedItem();
        if (b == null) { Ui.error("Seleccione un beneficio"); return; }
        if (Ui.confirmar("¿Canjear \"" + b.getNombre() + "\" por " + b.getCostoPuntos() + " puntos?"))
            Ui.ejecutar(() -> {
                sistema.canjearBeneficio(usuario.getId(), b.getId());
                Ui.info("¡Canje exitoso! Disfruta: " + b.getNombre());
            }, ctx.getAlCambiar());
    }
}
