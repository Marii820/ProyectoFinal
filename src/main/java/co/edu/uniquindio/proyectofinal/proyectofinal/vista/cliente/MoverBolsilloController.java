package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Cargar un bolsillo (desde tu cuenta o desde otro bolsillo) o sacarle plata.
 * Por debajo es una TRANSFERENCIA_PROPIA: queda en el historial, da puntos y se puede revertir.
 */
public class MoverBolsilloController implements ControladorSeccion {

    @FXML private Label lblTitulo;
    @FXML private VBox cajaOrigen;
    @FXML private Button btnDesdeCuenta;
    @FXML private Button btnDesdeBolsillo;
    @FXML private ComboBox<Cuenta> cbDesde;
    @FXML private Label lblSaldoDesde;
    @FXML private ComboBox<Cuenta> cbHacia;
    @FXML private Label lblSaldoHacia;
    @FXML private Label lblPregunta;
    @FXML private RadioButton rbTodo;
    @FXML private RadioButton rbOtro;
    @FXML private TextField txtValor;
    @FXML private Button btnConfirmar;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;
    private boolean modoCargar = true;
    private boolean desdeBolsillo = false;
    private String codigoInicial;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        ToggleGroup grupo = new ToggleGroup();
        rbTodo.setToggleGroup(grupo);
        rbOtro.setToggleGroup(grupo);
        rbOtro.setSelected(true);
        rbTodo.selectedProperty().addListener((o, a, n) -> txtValor.setDisable(n));
        txtValor.textProperty().addListener((o, a, n) -> { if (n != null && !n.isBlank()) rbOtro.setSelected(true); });
        cbDesde.valueProperty().addListener((o, a, n) -> actualizarSaldos());
        cbHacia.valueProperty().addListener((o, a, n) -> actualizarSaldos());
    }

    /** "cargar:BD000001" o "sacar:BD000001". */
    @Override
    public void mostrarParte(String parte) {
        String[] p = parte.split(":");
        modoCargar = !p[0].equals("sacar");
        codigoInicial = p.length > 1 ? p[1] : null;
        desdeBolsillo = false;
        cbDesde.setValue(null);
        cbHacia.setValue(null);
        txtValor.clear();
        rbOtro.setSelected(true);
    }

    @Override
    public void refrescar() {
        List<Cuenta> bolsillos = new ArrayList<>(Ui.lista(usuario.getBilleteras()));
        Cuenta inicial = codigoInicial == null ? null : usuario.buscarCuenta(codigoInicial);

        cajaOrigen.setVisible(modoCargar);
        cajaOrigen.setManaged(modoCargar);
        if (modoCargar) {
            lblTitulo.setText("Cargar bolsillo");
            lblPregunta.setText("¿Cuánto vas a cargar?");
            btnConfirmar.setText("Cargar");
            btnDesdeCuenta.getStyleClass().remove("elegida");
            btnDesdeBolsillo.getStyleClass().remove("elegida");
            (desdeBolsillo ? btnDesdeBolsillo : btnDesdeCuenta).getStyleClass().add("elegida");
            if (desdeBolsillo) {
                Ui.setItems(cbDesde, bolsillos);
                cbDesde.setDisable(false);
                if (cbDesde.getValue() == null) for (Cuenta c : bolsillos) if (c != inicial) { cbDesde.setValue(c); break; }
            } else {
                Ui.setItems(cbDesde, List.of(usuario.getCuentaPrincipal()));
                cbDesde.setValue(usuario.getCuentaPrincipal());
                cbDesde.setDisable(true);
            }
            Ui.setItems(cbHacia, bolsillos);
            cbHacia.setDisable(false);
            if (cbHacia.getValue() == null && inicial instanceof Billetera) cbHacia.setValue(inicial);
        } else {
            lblTitulo.setText("Sacar de un bolsillo");
            lblPregunta.setText("¿Cuánto vas a sacar?");
            btnConfirmar.setText("Pasar a mi cuenta");
            Ui.setItems(cbDesde, bolsillos);
            cbDesde.setDisable(false);
            if (cbDesde.getValue() == null && inicial instanceof Billetera) cbDesde.setValue(inicial);
            Ui.setItems(cbHacia, List.of(usuario.getCuentaPrincipal()));
            cbHacia.setValue(usuario.getCuentaPrincipal());
            cbHacia.setDisable(true);
        }
        rbTodo.setText(modoCargar && !desdeBolsillo ? "Todo el saldo de la cuenta" : "Todo el saldo del bolsillo");
        actualizarSaldos();
    }

    private void actualizarSaldos() {
        lblSaldoDesde.setText(cbDesde.getValue() == null ? "" : Ui.dinero(cbDesde.getValue().getSaldo()));
        lblSaldoHacia.setText(cbHacia.getValue() == null ? "" : Ui.dinero(cbHacia.getValue().getSaldo()));
    }

    @FXML
    private void elegirDesdeCuenta() {
        desdeBolsillo = false;
        cbDesde.setValue(null);
        refrescar();
    }

    @FXML
    private void elegirDesdeBolsillo() {
        if (usuario.getBilleteras().getSize() < 2) {
            Ui.error("Necesitas al menos dos bolsillos para pasar plata de uno a otro");
            return;
        }
        desdeBolsillo = true;
        cbDesde.setValue(null);
        refrescar();
    }

    @FXML
    private void confirmar() {
        Ui.ejecutar(() -> {
            Cuenta desde = cbDesde.getValue(), hacia = cbHacia.getValue();
            if (desde == null || hacia == null) throw new IllegalArgumentException("Seleccione desde dónde y hacia dónde");
            if (desde == hacia) throw new IllegalArgumentException("El origen y el destino no pueden ser el mismo");
            double valor = rbTodo.isSelected() ? desde.getSaldo() : Ui.leerValor(txtValor);
            if (valor <= 0) throw new IllegalArgumentException("No hay saldo para mover");
            Transaccion t = sistema.moverEntreCuentas(usuario.getId(), desde.getCodigo(), hacia.getCodigo(), valor,
                    modoCargar ? "Carga de bolsillo" : "Saca de bolsillo");
            txtValor.clear();
            Ui.info("✓ Listo: " + Ui.dinero(t.getValor()) + " de " + desde.getNombre() + " a " + hacia.getNombre()
                    + "\n\n" + desde.getNombre() + " quedó con " + Ui.dinero(desde.getSaldo())
                    + "\n" + hacia.getNombre() + " quedó con " + Ui.dinero(hacia.getSaldo()));
            Cuenta bolsillo = modoCargar ? hacia : desde;
            codigoInicial = bolsillo.getCodigo();
            ctx.irA("bolsillo#" + bolsillo.getCodigo());
        }, () -> { });
        ctx.getNavegador().guardar();
    }

    @FXML
    private void volver() {
        if (codigoInicial != null && usuario.buscarCuenta(codigoInicial) instanceof Billetera) ctx.irA("bolsillo#" + codigoInicial);
        else ctx.irA("cuenta");
    }
}
