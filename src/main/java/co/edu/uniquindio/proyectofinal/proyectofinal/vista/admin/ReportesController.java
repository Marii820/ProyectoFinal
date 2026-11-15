package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.Grafo;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.ItemReporte;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 4.8 Analítica: billeteras con mayor uso, usuarios con más transferencias, categorías, frecuencia por tipo, monto total, top. */
public class ReportesController implements ControladorSeccion {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblMonto;
    @FXML private Label lblCantidad;
    @FXML private Label lblActivo;
    @FXML private TableView<ItemReporte> tCuentas;
    @FXML private TableView<ItemReporte> tUsuarios;
    @FXML private TableView<ItemReporte> tCategorias;
    @FXML private TableView<ItemReporte> tTipos;
    @FXML private TableView<Grafo.Arista> tPatrones;
    @FXML private ListView<String> lvCiclos;
    @FXML private Spinner<Integer> spTop;
    @FXML private TableView<Transaccion> tTop;
    @FXML private TableColumn<Transaccion, String> colTopId;
    @FXML private TableColumn<Transaccion, String> colTopFecha;
    @FXML private TableColumn<Transaccion, String> colTopTipo;
    @FXML private TableColumn<Transaccion, Number> colTopValor;
    @FXML private TableColumn<Transaccion, String> colTopUsuario;
    @FXML private TableColumn<Transaccion, String> colTopRuta;

    private SistemaFintech sistema;

    @Override
    public void iniciar(Contexto ctx) {
        this.sistema = ctx.getSistema();
        dpDesde.setValue(LocalDate.now().minusDays(30));
        dpHasta.setValue(LocalDate.now());
        columnasReporte(tCuentas, "Cuenta");
        columnasReporte(tUsuarios, "Usuario");
        columnasReporte(tCategorias, "Categoría");
        columnasReporte(tTipos, "Tipo de transacción");

        TableColumn<Grafo.Arista, String> envia = new TableColumn<>("Quién envía");
        TableColumn<Grafo.Arista, String> recibe = new TableColumn<>("Quién recibe");
        TableColumn<Grafo.Arista, Number> veces = new TableColumn<>("Veces");
        TableColumn<Grafo.Arista, Number> monto = new TableColumn<>("Monto");
        Ui.texto(envia, a -> nombre(a.getOrigen()));
        Ui.texto(recibe, a -> nombre(a.getDestino()));
        Ui.numero(veces, Grafo.Arista::getVeces, false);
        Ui.numero(monto, Grafo.Arista::getMonto, true);
        tPatrones.getColumns().setAll(List.of(envia, recibe, veces, monto));
        Ui.ajustar(tPatrones, "Sin transferencias entre usuarios");
        lvCiclos.setPlaceholder(new Label("Sin ciclos"));

        spTop.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10));
        spTop.valueProperty().addListener((o, a, n) -> cargarTop());
        Ui.ajustar(tTop, "Sin transacciones");
        Ui.texto(colTopId, Transaccion::getId);
        Ui.texto(colTopFecha, t -> Ui.fecha(t.getFecha()));
        Ui.texto(colTopTipo, t -> t.getTipo().getEtiqueta());
        Ui.numero(colTopValor, Transaccion::getValor, true);
        Ui.texto(colTopUsuario, t -> t.getUsuario().toString());
        Ui.texto(colTopRuta, t -> t.getCodigoOrigen() + " → " + t.getCodigoDestino());
    }

    /** Las 4 tablas de reporte tienen las mismas 3 columnas: se crean aquí. */
    private void columnasReporte(TableView<ItemReporte> tabla, String titulo) {
        TableColumn<ItemReporte, String> nombre = new TableColumn<>(titulo);
        TableColumn<ItemReporte, Number> cantidad = new TableColumn<>("Operaciones");
        TableColumn<ItemReporte, Number> monto = new TableColumn<>("Monto");
        nombre.setPrefWidth(260);
        cantidad.setPrefWidth(90);
        monto.setPrefWidth(120);
        Ui.texto(nombre, ItemReporte::getNombre);
        Ui.numero(cantidad, ItemReporte::getCantidad, false);
        Ui.numero(monto, ItemReporte::getMonto, true);
        tabla.getColumns().setAll(List.of(nombre, cantidad, monto));
        Ui.ajustar(tabla, "Sin datos en el período");
    }

    private String nombre(String cedula) {
        Usuario u = sistema.buscarUsuario(cedula);
        return u == null ? cedula : u.getNombre();
    }

    private void cargarTop() {
        tTop.getItems().setAll(Ui.lista(sistema.topTransacciones(spTop.getValue())));
    }

    @FXML
    private void generar() { refrescar(); }

    @Override
    public void refrescar() {
        LocalDateTime d = dpDesde.getValue() == null ? null : dpDesde.getValue().atStartOfDay();
        LocalDateTime h = dpHasta.getValue() == null ? null : dpHasta.getValue().atTime(23, 59, 59);
        lblMonto.setText(Ui.dinero(sistema.montoTotalMovilizado(d, h)));
        lblCantidad.setText(String.valueOf(sistema.cantidadTransacciones(d, h)));
        ItemReporte activo = sistema.usuarioMasActivo(d, h);
        lblActivo.setText(activo == null ? "-" : activo.getNombre() + " · " + activo.getCantidad() + " op.");
        tCuentas.getItems().setAll(Ui.lista(sistema.billeterasMayorUso(d, h, 10)));
        tUsuarios.getItems().setAll(Ui.lista(sistema.usuariosConMasTransferencias(d, h, 10)));
        tCategorias.getItems().setAll(Ui.lista(sistema.categoriasMasActivas(d, h)));
        tTipos.getItems().setAll(Ui.lista(sistema.frecuenciaPorTipo(d, h)));
        tPatrones.getItems().setAll(Ui.lista(sistema.getGrafoUsuarios().rutasFrecuentes(10)));
        lvCiclos.getItems().clear();
        for (String c : sistema.getGrafoUsuarios().detectarCiclos(5)) {
            StringBuilder sb = new StringBuilder();
            for (String id : c.split(" -> ")) {
                if (sb.length() > 0) sb.append(" → ");
                Usuario u = sistema.buscarUsuario(id);
                sb.append(u == null ? id : u.getPrimerNombre());
            }
            lvCiclos.getItems().add(sb.toString());
        }
        cargarTop();
    }
}
