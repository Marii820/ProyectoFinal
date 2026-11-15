package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Comportamiento;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** 4.9 Historial de auditoría de comportamiento inusual. */
public class SeguridadController implements ControladorSeccion {

    @FXML private ComboBox<NivelRiesgo> cbRiesgo;
    @FXML private TextField txtCedula;
    @FXML private Label lblResumen;
    @FXML private TableView<Comportamiento> tabla;
    @FXML private TableColumn<Comportamiento, String> colId;
    @FXML private TableColumn<Comportamiento, String> colFecha;
    @FXML private TableColumn<Comportamiento, String> colUsuario;
    @FXML private TableColumn<Comportamiento, String> colTransaccion;
    @FXML private TableColumn<Comportamiento, String> colRiesgo;
    @FXML private TableColumn<Comportamiento, String> colPatron;
    @FXML private TableColumn<Comportamiento, String> colDescripcion;

    private SistemaFintech sistema;

    @Override
    public void iniciar(Contexto ctx) {
        this.sistema = ctx.getSistema();
        cbRiesgo.getItems().setAll(NivelRiesgo.MEDIO, NivelRiesgo.ALTO);
        Ui.ajustar(tabla, "No se ha detectado comportamiento inusual");
        Ui.texto(colId, Comportamiento::getId);
        Ui.texto(colFecha, c -> Ui.fecha(c.getFecha()));
        Ui.texto(colUsuario, c -> c.getUsuario().toString());
        Ui.texto(colTransaccion, c -> c.getTransaccion().getId() + " · " + c.getTransaccion().getTipo().getEtiqueta()
                + " " + Ui.dinero(c.getTransaccion().getValor()) + " [" + c.getTransaccion().getEstado() + "]");
        Ui.texto(colRiesgo, c -> c.getNivelRiesgo() + " (" + c.getPuntajeRiesgo() + ")");
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

    @FXML private void filtrar() { refrescar(); }

    @FXML
    private void verTodo() {
        cbRiesgo.setValue(null);
        txtCedula.clear();
        refrescar();
    }

    @Override
    public void refrescar() {
        SimpleLinkedList<Comportamiento> l = sistema.auditoriaFiltrada(cbRiesgo.getValue(), txtCedula.getText());
        tabla.getItems().setAll(Ui.lista(l));
        lblResumen.setText("   " + l.getSize() + " eventos  (total: " + sistema.getAuditoria().getSize() + ")");
    }
}
