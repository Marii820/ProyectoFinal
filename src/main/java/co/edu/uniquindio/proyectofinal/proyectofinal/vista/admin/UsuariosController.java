package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.TablaHash;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Consulta de usuarios (búsqueda O(1) con la tabla hash) y estado de las tablas hash. */
public class UsuariosController implements ControladorSeccion {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Usuario> tabla;
    @FXML private TableColumn<Usuario, String> colCedula;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colCelular;
    @FXML private TableColumn<Usuario, String> colNivel;
    @FXML private TableColumn<Usuario, Number> colPuntos;
    @FXML private TableColumn<Usuario, Number> colDisponible;
    @FXML private TableColumn<Usuario, Number> colEnBilleteras;
    @FXML private TableColumn<Usuario, String> colRegistro;
    @FXML private Label lblDetalle;
    @FXML private TableView<Cuenta> tablaCuentas;
    @FXML private TableColumn<Cuenta, String> colCtaCodigo;
    @FXML private TableColumn<Cuenta, String> colCtaNombre;
    @FXML private TableColumn<Cuenta, String> colCtaTipo;
    @FXML private TableColumn<Cuenta, Number> colCtaSaldo;
    @FXML private TableColumn<Cuenta, String> colCtaEstado;
    @FXML private TableColumn<Cuenta, Number> colCtaMovs;
    @FXML private Label lblHash;

    private Contexto ctx;
    private SistemaFintech sistema;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        Ui.ajustar(tabla, "No hay usuarios");
        Ui.texto(colCedula, Usuario::getId);
        Ui.texto(colNombre, Usuario::getNombre);
        Ui.texto(colCorreo, Usuario::getCorreo);
        Ui.texto(colCelular, Usuario::getTelefono);
        Ui.texto(colNivel, Usuario::getNivel);
        Ui.numero(colPuntos, Usuario::getPuntosAcumulados, false);
        Ui.numero(colDisponible, Usuario::saldoDisponible, true);
        Ui.numero(colEnBilleteras, Usuario::saldoEnBilleteras, true);
        Ui.texto(colRegistro, u -> Ui.fecha(u.getFechaRegistro()));
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, u) -> mostrarDetalle(u));

        Ui.ajustar(tablaCuentas, "Seleccione un usuario");
        Ui.texto(colCtaCodigo, Cuenta::getCodigo);
        Ui.texto(colCtaNombre, Cuenta::getNombre);
        Ui.texto(colCtaTipo, c -> c instanceof Billetera b ? b.getTipo().getEtiqueta() : "Cuenta principal");
        Ui.numero(colCtaSaldo, Cuenta::getSaldo, true);
        Ui.texto(colCtaEstado, c -> c.isActiva() ? "Activa" : "Bloqueada");
        Ui.numero(colCtaMovs, c -> c.getHistorial().getSize(), false);
    }

    private void mostrarDetalle(Usuario u) {
        if (u == null) {
            lblDetalle.setText("Cuentas del usuario");
            tablaCuentas.getItems().clear();
            return;
        }
        lblDetalle.setText("Cuentas de " + u.getNombre() + " · " + u.getHistorial().getSize() + " movimientos · "
                + u.getOperacionesReversibles().getSize() + " en su pila de deshacer");
        tablaCuentas.getItems().setAll(Ui.lista(u.cuentas()));
    }

    @Override
    public void refrescar() {
        Usuario sel = tabla.getSelectionModel().getSelectedItem();
        tabla.getItems().setAll(Ui.lista(sistema.listarUsuarios()));
        if (sel != null && tabla.getItems().contains(sel)) tabla.getSelectionModel().select(sel);
        mostrarDetalle(tabla.getSelectionModel().getSelectedItem());
        TablaHash<String, Usuario> tu = sistema.getTablaUsuarios();
        TablaHash<String, Cuenta> tc = sistema.getTablaCuentas();
        lblHash.setText(String.format("Usuarios: %d elementos en %d cubetas (factor de carga %.2f, %d colisiones)   ·   "
                        + "Cuentas: %d elementos en %d cubetas (factor de carga %.2f, %d colisiones). Se hace rehash al pasar de 0,75.",
                tu.getSize(), tu.getCapacidad(), tu.factorCarga(), tu.getColisiones(),
                tc.getSize(), tc.getCapacidad(), tc.factorCarga(), tc.getColisiones())
                + "\nConfiguración del sistema (tabla hash, consulta O(1)): " + configuracion());
    }

    private String configuracion() {
        StringBuilder sb = new StringBuilder();
        for (String clave : sistema.getConfiguracion().claves()) {
            if (sb.length() > 0) sb.append("  ·  ");
            sb.append(clave).append(" = ").append(String.format("%,.0f", sistema.config(clave)));
        }
        return sb.toString();
    }

    @FXML
    private void buscar() {
        Usuario u = sistema.buscarPorIdentificador(txtBuscar.getText());
        if (u == null) { Ui.error("No existe un usuario con \"" + txtBuscar.getText() + "\""); return; }
        tabla.getSelectionModel().select(u);
        tabla.scrollTo(u);
    }

    @FXML
    private void eliminar() {
        Usuario u = tabla.getSelectionModel().getSelectedItem();
        if (u == null) { Ui.error("Seleccione un usuario"); return; }
        if (Ui.confirmar("¿Eliminar a " + u + "? Solo es posible si su saldo total es $0."))
            Ui.ejecutar(() -> sistema.eliminarUsuario(u.getId()), ctx.getAlCambiar());
    }
}
