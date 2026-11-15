package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.OperacionProgramada;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Detalle de un bolsillo (como en la app del banco): saldo, meta, detalles, movimientos, cargar y sacar. */
public class BolsilloController implements ControladorSeccion {

    @FXML private Label lblNombre;
    @FXML private Label lblSaldo;
    @FXML private ProgressBar barraMeta;
    @FXML private Label lblMeta;
    @FXML private MenuItem miBloquear;
    @FXML private Label lblMetaDetalle;
    @FXML private Label lblTipo;
    @FXML private Label lblCodigo;
    @FXML private Label lblCreado;
    @FXML private Label lblEstado;
    @FXML private VBox listaProgramadas;
    @FXML private TableView<Transaccion> tabla;
    @FXML private TableColumn<Transaccion, String> colFecha;
    @FXML private TableColumn<Transaccion, String> colDetalle;
    @FXML private TableColumn<Transaccion, Number> colValor;
    @FXML private TableColumn<Transaccion, String> colEstado;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;
    private Billetera bolsillo;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        Ui.ajustar(tabla, "Este bolsillo todavía no tiene movimientos");
        Ui.texto(colFecha, t -> Ui.fecha(t.getFecha()));
        Ui.texto(colDetalle, this::detalle);
        Ui.movimiento(colValor, t -> t.getDestino() == bolsillo ? t.getValor() : -(t.getValor() + t.getComision()));
        Ui.texto(colEstado, t -> t.getEstado().name());
    }

    /** Recibe el código del bolsillo a mostrar. */
    @Override
    public void mostrarParte(String codigo) {
        Cuenta c = usuario.buscarCuenta(codigo);
        bolsillo = c instanceof Billetera b ? b : null;
    }

    private String detalle(Transaccion t) {
        if (t.getDestino() == bolsillo) return "Carga desde " + (t.getOrigen() == null ? "recarga" : t.getOrigen().getNombre());
        if (t.esEntrePersonas()) return "Envío a " + SistemaFintech.nombreEnmascarado(t.getDestino().getPropietario());
        return "Salida hacia " + (t.getDestino() == null ? "retiro" : t.getDestino().getNombre());
    }

    @Override
    public void refrescar() {
        if (bolsillo == null || usuario.buscarCuenta(bolsillo.getCodigo()) == null) {
            ctx.irA("cuenta"); // se eliminó o no existe
            return;
        }
        Billetera b = bolsillo;
        lblNombre.setText(b.getNombre());
        lblSaldo.setText(Ui.dinero(b.getSaldo()));
        barraMeta.setVisible(b.tieneMeta());
        barraMeta.setManaged(b.tieneMeta());
        barraMeta.setProgress(b.avanceMeta());
        lblMeta.setText(b.tieneMeta()
                ? String.format("Llevas el %.0f%% de tu meta · te faltan %s", b.avanceMeta() * 100, Ui.dinero(Math.max(0, b.getMeta() - b.getSaldo())))
                : "Sin meta de ahorro (puedes ponerle una en Editar)");
        lblMetaDetalle.setText(b.tieneMeta() ? Ui.dinero(b.getMeta()) : "Sin meta");
        lblTipo.setText(b.getTipo().getEtiqueta());
        lblCodigo.setText(b.getCodigo());
        lblCreado.setText(Ui.fecha(b.getFechaCreacion()));
        lblEstado.setText(b.isActiva() ? "Activo" : "Bloqueado");
        miBloquear.setText(b.isActiva() ? "Bloquear" : "Desbloquear");

        listaProgramadas.getChildren().clear();
        for (OperacionProgramada op : sistema.programadasHacia(b.getCodigo())) {
            Label l = new Label("• " + op.getFrecuencia() + " · " + Ui.dinero(op.getValor()) + " · próxima: " + Ui.fecha(op.getFechaEjecucion()));
            listaProgramadas.getChildren().add(l);
        }
        if (listaProgramadas.getChildren().isEmpty()) {
            Label l = new Label("No tiene ahorro programado.");
            l.getStyleClass().add("ayuda");
            listaProgramadas.getChildren().add(l);
        }
        tabla.getItems().setAll(Ui.lista(b.getHistorial()));
        tabla.refresh();
    }

    @FXML private void volver() { ctx.irA("cuenta"); }

    @FXML private void cargar() { ctx.irA("moverbolsillo#cargar:" + bolsillo.getCodigo()); }

    @FXML private void sacar() { ctx.irA("moverbolsillo#sacar:" + bolsillo.getCodigo()); }

    @FXML private void programarAhorro() { ctx.irA("programadas"); }

    @FXML
    private void renombrar() {
        Ui.pedirTexto("Cambiar nombre", "Nuevo nombre:", bolsillo.getNombre()).ifPresent(n ->
                Ui.ejecutar(() -> sistema.renombrarBilletera(usuario.getId(), bolsillo.getCodigo(), n), ctx.getAlCambiar()));
    }

    @FXML
    private void cambiarMeta() {
        Ui.pedirTexto("Meta de ahorro", "Meta (0 para quitarla):", bolsillo.tieneMeta() ? String.format("%.0f", bolsillo.getMeta()) : "")
                .ifPresent(v -> Ui.ejecutar(() -> {
                    String t = v.trim();
                    sistema.cambiarMetaBilletera(usuario.getId(), bolsillo.getCodigo(), t.isEmpty() || t.equals("0") ? 0 : Ui.leerValor(t));
                }, ctx.getAlCambiar()));
    }

    @FXML
    private void bloquear() {
        Ui.ejecutar(() -> sistema.cambiarEstadoBilletera(usuario.getId(), bolsillo.getCodigo(), !bolsillo.isActiva()), ctx.getAlCambiar());
    }

    @FXML
    private void eliminar() {
        if (bolsillo.getSaldo() > 0) {
            if (!Ui.confirmar("El bolsillo tiene " + Ui.dinero(bolsillo.getSaldo()) + ". ¿Pasar esa plata a tu cuenta y eliminarlo?")) return;
            Ui.ejecutar(() -> {
                sistema.vaciarBilletera(usuario.getId(), bolsillo.getCodigo());
                sistema.eliminarBilletera(usuario.getId(), bolsillo.getCodigo());
            }, ctx.getAlCambiar());
        } else if (Ui.confirmar("¿Eliminar el bolsillo \"" + bolsillo.getNombre() + "\"?")) {
            Ui.ejecutar(() -> sistema.eliminarBilletera(usuario.getId(), bolsillo.getCodigo()), ctx.getAlCambiar());
        }
    }
}
