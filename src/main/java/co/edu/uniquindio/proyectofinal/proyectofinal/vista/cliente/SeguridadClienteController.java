package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Comportamiento;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/** 4.9: el cliente consulta después los eventos inusuales detectados en su cuenta. */
public class SeguridadClienteController implements ControladorSeccion {

    @FXML private Label lblTotal;
    @FXML private Label lblAltos;
    @FXML private TableView<Comportamiento> tabla;
    @FXML private TableColumn<Comportamiento, String> colFecha;
    @FXML private TableColumn<Comportamiento, String> colOperacion;
    @FXML private TableColumn<Comportamiento, String> colRiesgo;
    @FXML private TableColumn<Comportamiento, String> colPatron;
    @FXML private TableColumn<Comportamiento, String> colDescripcion;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        Ui.ajustar(tabla, "No hemos detectado nada raro en tu cuenta ✓");
        Ui.texto(colFecha, c -> Ui.fecha(c.getFecha()));
        Ui.texto(colOperacion, c -> c.getTransaccion().getTipo().getEtiqueta() + " de " + Ui.dinero(c.getTransaccion().getValor())
                + " (" + c.getTransaccion().getId() + ", " + c.getTransaccion().getEstado().name().toLowerCase() + ")");
        Ui.texto(colRiesgo, c -> c.getNivelRiesgo().name());
        Ui.texto(colPatron, Comportamiento::getPatron);
        Ui.texto(colDescripcion, Comportamiento::getDescripcion);
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Comportamiento c, boolean vacio) {
                super.updateItem(c, vacio);
                getStyleClass().removeAll("fila-rechazada", "fila-riesgo");
                if (vacio || c == null) return;
                getStyleClass().add(c.getNivelRiesgo() == NivelRiesgo.ALTO ? "fila-rechazada" : "fila-riesgo");
            }
        });
    }

    @Override
    public void refrescar() {
        SimpleLinkedList<Comportamiento> eventos = sistema.eventosDeUsuario(usuario.getId());
        tabla.getItems().setAll(Ui.lista(eventos));
        lblTotal.setText(String.valueOf(eventos.getSize()));
        lblAltos.setText(String.valueOf(eventos.contar(c -> c.getNivelRiesgo() == NivelRiesgo.ALTO)));
    }

    @FXML
    private void noFuiYo() {
        Comportamiento c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) { Ui.error("Seleccione el evento en la tabla"); return; }
        Transaccion t = c.getTransaccion();
        if (!t.esReversible()) { Ui.error("Esa operación ya no se puede revertir (" + t.getEstado().name().toLowerCase() + ")"); return; }
        if (Ui.confirmar("¿Revertir " + t.getTipo().getEtiqueta().toLowerCase() + " de " + Ui.dinero(t.getValor()) + " (" + t.getId() + ")?"))
            Ui.ejecutar(() -> {
                sistema.revertirOperacion(usuario.getId(), t.getId());
                Ui.info("Operación revertida. Te recomendamos cambiar tu contraseña en Mi perfil.");
            }, ctx.getAlCambiar());
    }
}
