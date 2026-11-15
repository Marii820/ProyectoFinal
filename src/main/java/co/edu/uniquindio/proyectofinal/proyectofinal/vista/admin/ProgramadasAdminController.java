package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelUsuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.OperacionProgramada;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Cola de prioridad global + simulación del paso del tiempo (requisito 8: simular la ejecución automática). */
public class ProgramadasAdminController implements ControladorSeccion {

    @FXML private Label lblReloj;
    @FXML private Spinner<Integer> spHoras;
    @FXML private TableView<OperacionProgramada> tabla;
    @FXML private TableColumn<OperacionProgramada, String> colOrden;
    @FXML private TableColumn<OperacionProgramada, String> colId;
    @FXML private TableColumn<OperacionProgramada, String> colFecha;
    @FXML private TableColumn<OperacionProgramada, String> colPrioridad;
    @FXML private TableColumn<OperacionProgramada, String> colUsuario;
    @FXML private TableColumn<OperacionProgramada, String> colTipo;
    @FXML private TableColumn<OperacionProgramada, Number> colValor;
    @FXML private TableColumn<OperacionProgramada, String> colRuta;
    @FXML private TableColumn<OperacionProgramada, String> colFrecuencia;
    @FXML private TableColumn<OperacionProgramada, String> colResultados;

    private Contexto ctx;
    private SistemaFintech sistema;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        spHoras.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24 * 60, 24));
        Ui.ajustar(tabla, "La cola está vacía");
        tabla.setSortPolicy(t -> false);
        Ui.posicion(colOrden);
        Ui.texto(colId, OperacionProgramada::getId);
        Ui.texto(colFecha, op -> Ui.fecha(op.getFechaEjecucion()));
        Ui.texto(colPrioridad, op -> NivelUsuario.values()[op.getPrioridad()].name());
        Ui.texto(colUsuario, op -> op.getUsuario().toString());
        Ui.texto(colTipo, op -> op.getTipo().getEtiqueta());
        Ui.numero(colValor, OperacionProgramada::getValor, true);
        Ui.texto(colRuta, op -> (op.getOrigen() == null ? "-" : op.getOrigen().getNombre())
                + " → " + (op.getDestino() == null ? "-" : op.getDestino().getNombre() + " de " + op.getDestino().getPropietario().getPrimerNombre()));
        Ui.texto(colFrecuencia, OperacionProgramada::getFrecuencia);
        Ui.texto(colResultados, op -> op.getEjecuciones() + " / " + op.getFallos());
    }

    @FXML
    private void avanzar() {
        int horas = spHoras.getValue();
        Ui.ejecutar(() -> Ui.info("Reloj adelantado " + horas + " horas.\n" + sistema.avanzarReloj(horas)), ctx.getAlCambiar());
    }

    @FXML
    private void procesar() {
        Ui.ejecutar(() -> Ui.info(sistema.procesarProgramadas()), ctx.getAlCambiar());
    }

    @Override
    public void refrescar() {
        lblReloj.setText("Hora del sistema: " + Ui.fecha(sistema.ahora()) + "   ·   " + sistema.cantidadProgramadas() + " en la cola");
        tabla.getItems().setAll(Ui.lista(sistema.todasLasProgramadas()));
        tabla.refresh();
    }
}
