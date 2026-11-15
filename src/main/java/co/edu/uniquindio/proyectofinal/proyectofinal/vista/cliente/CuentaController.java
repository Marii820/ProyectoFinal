package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.TipoBilletera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Navegador;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

/** "Mi cuenta": saldo disponible y bolsillos (aquí se crean). No muestra transferencias. */
public class CuentaController implements ControladorSeccion {

    @FXML private Label lblNumero;
    @FXML private Label lblSaldo;
    @FXML private Label lblEnBolsillos;
    @FXML private FlowPane bolsillos;
    @FXML private Label lblSinBolsillos;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<TipoBilletera> cbTipo;
    @FXML private TextField txtMeta;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        cbTipo.getItems().setAll(TipoBilletera.values());
    }

    @Override
    public void refrescar() {
        String codigo = usuario.getCuentaPrincipal().getCodigo();
        lblNumero.setText("Cuenta *" + codigo.substring(Math.max(0, codigo.length() - 4)));
        lblSaldo.setText(Ui.dinero(usuario.saldoDisponible()));
        lblEnBolsillos.setText("En bolsillos: " + Ui.dinero(usuario.saldoEnBilleteras()) + "   ·   Total: " + Ui.dinero(usuario.saldoTotal()));
        bolsillos.getChildren().clear();
        for (Billetera b : usuario.getBilleteras()) {
            FXMLLoader loader = Navegador.cargar("cliente/TarjetaBilletera.fxml", ctx);
            TarjetaBilleteraController c = loader.getController();
            c.mostrar(b);
            bolsillos.getChildren().add(loader.getRoot());
        }
        boolean vacio = usuario.getBilleteras().isEmpty();
        lblSinBolsillos.setVisible(vacio);
        lblSinBolsillos.setManaged(vacio);
    }

    @FXML
    private void crear() {
        Ui.ejecutar(() -> {
            String m = txtMeta.getText() == null ? "" : txtMeta.getText().trim();
            double meta = m.isEmpty() ? 0 : Ui.leerValor(m);
            Billetera b = sistema.crearBilletera(usuario.getId(), txtNombre.getText(), cbTipo.getValue(), meta);
            txtNombre.clear();
            txtMeta.clear();
            cbTipo.setValue(null);
            Ui.info("Bolsillo \"" + b.getNombre() + "\" creado. Dale clic para cargarle plata.");
        }, ctx.getAlCambiar());
    }

    @FXML private void volver() { ctx.irA("inicio"); }
    @FXML private void irTransferir() { ctx.irA("mover#enviar"); }
    @FXML private void irRecargar() { ctx.irA("mover#recargar"); }

    @FXML
    private void irCargar() {
        if (usuario.getBilleteras().isEmpty()) { Ui.error("Primero crea un bolsillo aquí abajo"); return; }
        ctx.irA("moverbolsillo#cargar:" + usuario.getBilleteras().getFirst().getCodigo());
    }
}
