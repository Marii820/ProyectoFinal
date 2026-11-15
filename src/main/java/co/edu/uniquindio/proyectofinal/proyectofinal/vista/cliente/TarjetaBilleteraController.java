package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Controlador;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/** Tarjeta de un bolsillo (TarjetaBilletera.fxml). Al darle clic abre el detalle del bolsillo. */
public class TarjetaBilleteraController implements Controlador {

    @FXML private VBox raiz;
    @FXML private Label lblNombre;
    @FXML private Label lblTipo;
    @FXML private Label lblSaldo;
    @FXML private ProgressBar barraMeta;
    @FXML private Label lblMeta;

    private Contexto ctx;
    private Billetera billetera;

    @Override
    public void iniciar(Contexto ctx) { this.ctx = ctx; }

    public void mostrar(Billetera b) {
        this.billetera = b;
        lblNombre.setText(b.getNombre());
        lblTipo.setText(b.getTipo().getEtiqueta() + (b.isActiva() ? "" : " · BLOQUEADO"));
        lblSaldo.setText(Ui.dinero(b.getSaldo()));
        barraMeta.setVisible(b.tieneMeta());
        barraMeta.setManaged(b.tieneMeta());
        barraMeta.setProgress(b.avanceMeta());
        lblMeta.setText(b.tieneMeta()
                ? String.format("%.0f%% de la meta (%s)", b.avanceMeta() * 100, Ui.dinero(b.getMeta()))
                : "Sin meta de ahorro");
        raiz.getStyleClass().removeIf(c -> c.startsWith("tipo-") || c.equals("bloqueada"));
        raiz.getStyleClass().add("tipo-" + b.getTipo().name());
        if (!b.isActiva()) raiz.getStyleClass().add("bloqueada");
    }

    @FXML
    private void abrir() { ctx.irA("bolsillo#" + billetera.getCodigo()); }
}
