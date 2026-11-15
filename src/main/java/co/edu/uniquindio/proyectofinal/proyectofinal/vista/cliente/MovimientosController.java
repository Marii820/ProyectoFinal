package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.EstadoTransaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.ItemReporte;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Historial (listas), deshacer/revertir (pila) y resumen por período. */
public class MovimientosController implements ControladorSeccion {

    private static final String TODO = "Todos mis movimientos";

    @FXML private ComboBox<Object> cbCuenta;
    @FXML private TableView<Transaccion> tabla;
    @FXML private TableColumn<Transaccion, String> colFecha;
    @FXML private TableColumn<Transaccion, String> colTipo;
    @FXML private TableColumn<Transaccion, String> colDetalle;
    @FXML private TableColumn<Transaccion, Number> colValor;
    @FXML private TableColumn<Transaccion, Number> colComision;
    @FXML private TableColumn<Transaccion, String> colEstado;
    @FXML private TableColumn<Transaccion, Number> colPuntos;
    @FXML private TableColumn<Transaccion, String> colRevertible;
    @FXML private TableColumn<Transaccion, String> colId;
    @FXML private Label lblTope;
    @FXML private Label lblAyudaReversion;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<ItemReporte> tablaResumen;
    @FXML private TableColumn<ItemReporte, String> colResTipo;
    @FXML private TableColumn<ItemReporte, Number> colResCantidad;
    @FXML private TableColumn<ItemReporte, Number> colResMonto;
    @FXML private Label lblTotales;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();

        Ui.ajustar(tabla, "Sin movimientos");
        Ui.texto(colFecha, t -> Ui.fecha(t.getFecha()));
        Ui.texto(colTipo, t -> t.getTipo().getEtiqueta());
        Ui.texto(colDetalle, this::detalle);
        Ui.movimiento(colValor, t -> t.valorPara(usuario));
        Ui.numero(colComision, t -> t.getUsuario() == usuario ? t.getComision() : 0, true);
        Ui.texto(colEstado, t -> t.getEstado().name());
        Ui.numero(colPuntos, t -> t.getUsuario() == usuario ? t.getPuntosGenerados() : 0, false);
        Ui.texto(colId, Transaccion::getId);
        Ui.texto(colRevertible, this::revertible);
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Transaccion t, boolean vacio) {
                super.updateItem(t, vacio);
                getStyleClass().removeAll("fila-rechazada", "fila-revertida", "fila-riesgo");
                if (vacio || t == null) return;
                if (t.getEstado() == EstadoTransaccion.RECHAZADA) getStyleClass().add("fila-rechazada");
                else if (t.getEstado() == EstadoTransaccion.REVERTIDA) getStyleClass().add("fila-revertida");
                else if (t.getNivelRiesgo() != NivelRiesgo.BAJO) getStyleClass().add("fila-riesgo");
            }
        });

        Ui.ajustar(tablaResumen, "Sin movimientos en ese período");
        Ui.texto(colResTipo, ItemReporte::getNombre);
        Ui.numero(colResCantidad, ItemReporte::getCantidad, false);
        Ui.numero(colResMonto, ItemReporte::getMonto, true);

        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        cbCuenta.valueProperty().addListener((o, a, n) -> cargarTabla());
        lblAyudaReversion.setText("Puedes revertir envíos a otras personas, recargas y retiros de las últimas "
                + (int) sistema.config("ventanaReversionHoras") + " horas (por ejemplo, una transferencia por error, una recarga mal aplicada "
                + "o una operación duplicada). Se devuelve el dinero y la comisión, y se descuentan los puntos que te dio. "
                + "Lo que pasas entre tus bolsillos no se revierte: simplemente pasa la plata de vuelta.");
    }

    private String detalle(Transaccion t) {
        StringBuilder sb = new StringBuilder();
        if (t.esEntrePersonas()) {
            sb.append(t.getOrigen().getPropietario() == usuario
                    ? "Para " + SistemaFintech.nombreEnmascarado(t.getDestino().getPropietario()) + " (desde " + t.getOrigen().getNombre() + ")"
                    : "De " + SistemaFintech.nombreEnmascarado(t.getOrigen().getPropietario()));
        } else if (t.getOrigen() != null && t.getDestino() != null) {
            sb.append(t.getOrigen().getNombre()).append(" → ").append(t.getDestino().getNombre());
        } else if (t.getOrigen() != null) {
            sb.append("Desde ").append(t.getOrigen().getNombre());
        } else if (t.getDestino() != null) {
            sb.append("A ").append(t.getDestino().getNombre());
        }
        if (!t.getDescripcion().isBlank()) sb.append(" · ").append(t.getDescripcion());
        if (t.getEstado() == EstadoTransaccion.RECHAZADA || t.getEstado() == EstadoTransaccion.REVERTIDA || t.getDetalle().startsWith("Posible"))
            sb.append(" · ").append(t.getDetalle());
        return sb.toString();
    }

    private String revertible(Transaccion t) {
        if (t.getUsuario() != usuario) return "-";
        if (t.getTipo() == co.edu.uniquindio.proyectofinal.proyectofinal.model.TipoTransaccion.TRANSFERENCIA_PROPIA) return "No (entre bolsillos)";
        if (!t.esReversible()) return "No";
        var limite = t.getFecha().plusHours((long) sistema.config("ventanaReversionHoras"));
        return sistema.ahora().isBefore(limite) ? "Sí, hasta " + Ui.fechaCorta(limite) : "No (ya pasó el plazo)";
    }

    private void cargarTabla() {
        Object sel = cbCuenta.getValue();
        SimpleLinkedList<Transaccion> fuente = sel instanceof Cuenta c ? c.getHistorial() : usuario.getHistorial();
        tabla.getItems().setAll(Ui.lista(fuente));
        tabla.refresh();
        Transaccion tope = usuario.getOperacionesReversibles().peek();
        lblTope.setText(tope == null ? "Nada para deshacer"
                : "El último que se puede deshacer: " + tope.getTipo().getEtiqueta() + " de " + Ui.dinero(tope.getValor())
                + " (" + Ui.fecha(tope.getFecha()) + ")   ·   " + usuario.getOperacionesReversibles().getSize() + " en tu pila de deshacer");
    }

    @Override
    public void refrescar() {
        List<Object> opciones = new ArrayList<>();
        opciones.add(TODO);
        opciones.addAll(Ui.lista(usuario.cuentas()));
        Ui.setItems(cbCuenta, opciones);
        if (cbCuenta.getValue() == null) cbCuenta.setValue(TODO);
        cargarTabla();
        verResumen();
    }

    @FXML
    private void deshacer() {
        Transaccion tope = usuario.getOperacionesReversibles().peek();
        if (tope == null) { Ui.error("No tienes operaciones para deshacer"); return; }
        if (Ui.confirmar("¿Deshacer " + tope.getTipo().getEtiqueta().toLowerCase() + " de " + Ui.dinero(tope.getValor())
                + " (" + tope.getId() + ")?\nSe devuelve el dinero y se descuentan " + tope.getPuntosGenerados() + " puntos."))
            Ui.ejecutar(() -> { sistema.deshacerUltima(usuario.getId()); Ui.info("Operación revertida"); }, ctx.getAlCambiar());
    }

    @FXML
    private void revertirSeleccionada() {
        Transaccion t = tabla.getSelectionModel().getSelectedItem();
        if (t == null) { Ui.error("Seleccione un movimiento en la tabla"); return; }
        if (t.getUsuario() != usuario) { Ui.error("Solo puede revertir operaciones que usted hizo"); return; }
        if (!t.esReversible()) {
            Ui.error(t.getTipo() == co.edu.uniquindio.proyectofinal.proyectofinal.model.TipoTransaccion.TRANSFERENCIA_PROPIA
                    ? "Los movimientos entre tus bolsillos no se revierten: simplemente pasa la plata de vuelta."
                    : "Esta operación no se puede revertir (" + t.getEstado().name().toLowerCase() + ").");
            return;
        }
        if (Ui.confirmar("¿Revertir " + t.getTipo().getEtiqueta().toLowerCase() + " de " + Ui.dinero(t.getValor()) + " (" + t.getId() + ")?"))
            Ui.ejecutar(() -> { sistema.revertirOperacion(usuario.getId(), t.getId()); Ui.info("Operación revertida"); }, ctx.getAlCambiar());
    }

    @FXML
    private void verResumen() {
        if (dpDesde.getValue() == null || dpHasta.getValue() == null) return;
        LocalDateTime desde = dpDesde.getValue().atStartOfDay();
        LocalDateTime hasta = dpHasta.getValue().atTime(23, 59, 59);
        tablaResumen.getItems().setAll(Ui.lista(sistema.resumenUsuario(usuario.getId(), desde, hasta)));
        double entro = 0, salio = 0;
        for (Transaccion t : usuario.getHistorial()) {
            if (t.getEstado() != EstadoTransaccion.EXITOSA || t.getFecha().isBefore(desde) || t.getFecha().isAfter(hasta)) continue;
            double v = t.valorPara(usuario);
            if (v > 0) entro += v; else salio -= v;
        }
        lblTotales.setText("Entró: " + Ui.dinero(entro) + "     Salió: " + Ui.dinero(salio) + "     Neto: " + Ui.dineroConSigno(entro - salio));
    }
}
