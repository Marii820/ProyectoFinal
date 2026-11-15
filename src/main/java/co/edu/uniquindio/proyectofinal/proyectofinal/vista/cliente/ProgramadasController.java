package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Frecuencia;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.OperacionProgramada;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.TipoTransaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Pagos y movimientos automáticos (cola de prioridad). */
public class ProgramadasController implements ControladorSeccion {

    @FXML private ComboBox<TipoTransaccion> cbTipo;
    @FXML private TextField txtDescripcion;
    @FXML private Label lblOrigen;
    @FXML private ComboBox<Cuenta> cbOrigen;
    @FXML private Label lblDestino;
    @FXML private ComboBox<Cuenta> cbDestinoPropio;
    @FXML private TextField txtDestinatario;
    @FXML private TextField txtValor;
    @FXML private ComboBox<Frecuencia> cbFrecuencia;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TableView<OperacionProgramada> tabla;
    @FXML private TableColumn<OperacionProgramada, String> colFecha;
    @FXML private TableColumn<OperacionProgramada, String> colDescripcion;
    @FXML private TableColumn<OperacionProgramada, String> colTipo;
    @FXML private TableColumn<OperacionProgramada, Number> colValor;
    @FXML private TableColumn<OperacionProgramada, String> colDesde;
    @FXML private TableColumn<OperacionProgramada, String> colHacia;
    @FXML private TableColumn<OperacionProgramada, String> colFrecuencia;
    @FXML private TableColumn<OperacionProgramada, String> colResultados;
    @FXML private Label lblAyuda;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();

        // Solo lo que no necesita que el usuario esté presente: ahorro entre bolsillos y pagos a otras personas
        cbTipo.getItems().setAll(TipoTransaccion.TRANSFERENCIA_PROPIA, TipoTransaccion.PAGO_PROGRAMADO);
        cbTipo.setValue(TipoTransaccion.TRANSFERENCIA_PROPIA);
        cbTipo.valueProperty().addListener((o, a, n) -> ajustarCampos());
        cbFrecuencia.getItems().setAll(Frecuencia.values());
        cbFrecuencia.setValue(Frecuencia.MENSUAL);
        dpFecha.setValue(LocalDate.now().plusDays(1));
        ajustarCampos();

        Ui.ajustar(tabla, "No tienes operaciones programadas");
        tabla.setSortPolicy(t -> false); // se respeta el orden de la cola de prioridad
        Ui.texto(colFecha, op -> Ui.fecha(op.getFechaEjecucion()));
        Ui.texto(colDescripcion, op -> op.getDescripcion().isBlank() ? "-" : op.getDescripcion());
        Ui.texto(colTipo, op -> op.getTipo().getEtiqueta());
        Ui.numero(colValor, OperacionProgramada::getValor, true);
        Ui.texto(colDesde, op -> op.getOrigen() == null ? "-" : op.getOrigen().getNombre());
        Ui.texto(colHacia, op -> op.getDestino() == null ? "-" : op.getDestino().getPropietario() == usuario
                ? op.getDestino().getNombre() : SistemaFintech.nombreEnmascarado(op.getDestino().getPropietario()));
        Ui.texto(colFrecuencia, OperacionProgramada::getFrecuencia);
        Ui.texto(colResultados, op -> op.getEjecuciones() + " / " + op.getFallos());

        lblAyuda.setText("Se ejecutan solas cuando llega la fecha. Puedes programar un ahorro (pasar plata de tu cuenta a un bolsillo) "
                + "o un pago a otra persona. Las recargas y los retiros no se programan porque necesitan que tú estés presente. "
                + "Te avisamos un día antes y si alguna se rechaza (por ejemplo, por falta de saldo). "
                + "Cada pago programado exitoso a otra persona te da " + (int) sistema.config("bonoProgramada") + " puntos de bono. "
                + "Si dos operaciones caen a la misma hora, sale primero la del usuario de mayor nivel.");
    }

    /** Muestra los campos según el tipo: a otra persona se escribe cédula/celular/correo; entre cuentas propias, un combo. */
    private void ajustarCampos() {
        TipoTransaccion t = cbTipo.getValue();
        boolean aPersona = t == TipoTransaccion.PAGO_PROGRAMADO;
        lblDestino.setText(aPersona ? "Para:" : "Hacia:");
        cbDestinoPropio.setVisible(!aPersona);
        cbDestinoPropio.setManaged(!aPersona);
        txtDestinatario.setVisible(aPersona);
        txtDestinatario.setManaged(aPersona);
    }

    private LocalDateTime leerFecha() {
        if (dpFecha.getValue() == null) throw new IllegalArgumentException("Seleccione la fecha");
        try {
            return LocalDateTime.of(dpFecha.getValue(), LocalTime.parse(txtHora.getText().trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La hora debe ir en formato HH:mm (ej: 08:30)");
        }
    }

    @Override
    public void refrescar() {
        List<Cuenta> cuentas = Ui.lista(usuario.cuentas());
        Ui.setItems(cbOrigen, cuentas);
        Ui.setItems(cbDestinoPropio, cuentas);
        if (cbOrigen.getValue() == null) cbOrigen.setValue(usuario.getCuentaPrincipal());
        tabla.getItems().setAll(Ui.lista(sistema.programadasDeUsuario(usuario.getId())));
        tabla.refresh();
    }

    @FXML
    private void programar() {
        Ui.ejecutar(() -> {
            TipoTransaccion tipo = cbTipo.getValue();
            String origen = cbOrigen.getValue() == null ? null : cbOrigen.getValue().getCodigo();
            String destino;
            if (txtDestinatario.isVisible()) destino = txtDestinatario.getText();
            else destino = cbDestinoPropio.getValue() == null ? null : cbDestinoPropio.getValue().getCodigo();
            OperacionProgramada op = sistema.programarOperacion(usuario.getId(), tipo, origen, destino, Ui.leerValor(txtValor),
                    leerFecha(), cbFrecuencia.getValue(), txtDescripcion.getText());
            txtValor.clear();
            txtDescripcion.clear();
            Ui.info("Listo. \"" + (op.getDescripcion().isBlank() ? op.getTipo().getEtiqueta() : op.getDescripcion())
                    + "\" se ejecutará el " + Ui.fecha(op.getFechaEjecucion())
                    + (op.getFrecuencia() == Frecuencia.UNICA ? "." : " y se repetirá (" + op.getFrecuencia().toString().toLowerCase() + ")."));
        }, ctx.getAlCambiar());
    }

    @FXML
    private void cancelar() {
        OperacionProgramada op = tabla.getSelectionModel().getSelectedItem();
        if (op == null) { Ui.error("Seleccione una operación en la tabla"); return; }
        if (Ui.confirmar("¿Cancelar esta operación programada?"))
            Ui.ejecutar(() -> sistema.cancelarProgramada(usuario.getId(), op.getId()), ctx.getAlCambiar());
    }
}
