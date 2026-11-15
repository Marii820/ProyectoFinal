package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.TipoTransaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.util.List;

/** Enviar a otra persona, recargar y retirar (lo de los bolsillos está en Mi cuenta). */
public class MoverDineroController implements ControladorSeccion {

    @FXML private Label lblDisponible;
    @FXML private TabPane pestanas;
    @FXML private Tab tabEnviar;
    @FXML private Tab tabRecargar;
    @FXML private Tab tabRetirar;
    // enviar
    @FXML private TextField txtDestinatario;
    @FXML private Label lblDestinatario;
    @FXML private ComboBox<Cuenta> cbEnvOrigen;
    @FXML private TextField txtEnvValor;
    @FXML private Label lblEnvComision;
    @FXML private TextField txtEnvDesc;
    // recargar
    @FXML private ComboBox<String> cbMedio;
    @FXML private TextField txtRecValor;
    // retirar
    @FXML private TextField txtRetValor;
    @FXML private Label lblRetComision;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;
    private Usuario destinatario; // persona verificada para el envío

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
        cbMedio.getItems().setAll("PSE", "Tarjeta débito");
        cbMedio.setValue("PSE");
        txtDestinatario.textProperty().addListener((o, a, n) -> { destinatario = null; lblDestinatario.setText(""); });
        txtEnvValor.textProperty().addListener((o, a, n) -> comision(TipoTransaccion.TRANSFERENCIA_EXTERNA, txtEnvValor, lblEnvComision, true));
        txtRetValor.textProperty().addListener((o, a, n) -> comision(TipoTransaccion.RETIRO, txtRetValor, lblRetComision, false));
    }

    @Override
    public void mostrarParte(String parte) {
        switch (parte) {
            case "enviar" -> pestanas.getSelectionModel().select(tabEnviar);
            case "recargar" -> pestanas.getSelectionModel().select(tabRecargar);
            case "retirar" -> pestanas.getSelectionModel().select(tabRetirar);
            default -> { }
        }
    }

    @Override
    public void refrescar() {
        lblDisponible.setText("Saldo disponible: " + Ui.dinero(usuario.saldoDisponible()) + "    ·    En bolsillos: " + Ui.dinero(usuario.saldoEnBilleteras()));
        List<Cuenta> cuentas = Ui.lista(usuario.cuentas());
        Ui.setItems(cbEnvOrigen, cuentas);
        if (cbEnvOrigen.getValue() == null) cbEnvOrigen.setValue(usuario.getCuentaPrincipal());
    }

    private String codigo(ComboBox<Cuenta> cb) {
        return cb.getValue() == null ? null : cb.getValue().getCodigo();
    }

    @FXML
    private void verificarDestinatario() {
        try {
            destinatario = sistema.buscarDestinatario(usuario.getId(), txtDestinatario.getText());
            lblDestinatario.setText("✓ " + SistemaFintech.nombreEnmascarado(destinatario));
            lblDestinatario.getStyleClass().setAll("label", "texto-ok");
        } catch (RuntimeException ex) {
            destinatario = null;
            lblDestinatario.setText(ex.getMessage());
            lblDestinatario.getStyleClass().setAll("label", "texto-error");
        }
    }

    @FXML
    private void enviar() {
        double valor;
        try {
            if (destinatario == null) verificarDestinatario();
            if (destinatario == null) throw new IllegalArgumentException("Escriba la cédula, el celular o el correo de quien recibe y presione Buscar");
            valor = Ui.leerValor(txtEnvValor);
            double comision = sistema.calcularComision(usuario, TipoTransaccion.TRANSFERENCIA_EXTERNA, valor, true);
            if (!Ui.confirmar("¿Enviar " + Ui.dinero(valor) + " a " + SistemaFintech.nombreEnmascarado(destinatario) + "?\n"
                    + "Desde: " + cbEnvOrigen.getValue() + "\nComisión: " + Ui.dinero(comision) + "\nTotal: " + Ui.dinero(valor + comision)))
                return;
        } catch (RuntimeException ex) {
            Ui.error(ex.getMessage());
            return;
        }
        String para = destinatario.getId();
        Ui.ejecutar(() -> {
            Transaccion t = sistema.enviarAPersona(usuario.getId(), codigo(cbEnvOrigen), para, valor, txtEnvDesc.getText());
            txtEnvValor.clear();
            txtEnvDesc.clear();
            comprobante(t);
        }, ctx.getAlCambiar());
    }

    @FXML
    private void recargar() {
        Ui.ejecutar(() -> {
            Transaccion t = sistema.recargar(usuario.getId(), null, Ui.leerValor(txtRecValor), "Recarga por " + cbMedio.getValue());
            txtRecValor.clear();
            comprobante(t);
        }, ctx.getAlCambiar());
    }

    @FXML
    private void retirar() {
        Ui.ejecutar(() -> {
            Transaccion t = sistema.retirar(usuario.getId(), null, Ui.leerValor(txtRetValor), "Retiro");
            txtRetValor.clear();
            comprobante(t);
        }, ctx.getAlCambiar());
    }

    private void comision(TipoTransaccion tipo, TextField campo, Label lbl, boolean otraPersona) {
        try {
            double v = Ui.leerValor(campo);
            double c = sistema.calcularComision(usuario, tipo, v, otraPersona);
            lbl.setText("Comisión (" + usuario.getNivel() + "): " + Ui.dinero(c) + "   ·   Total: " + Ui.dinero(v + c));
        } catch (RuntimeException e) {
            lbl.setText("");
        }
    }

    private void comprobante(Transaccion t) {
        StringBuilder sb = new StringBuilder("✓ ").append(t.getTipo().getEtiqueta()).append(" exitosa\n\n")
                .append("Comprobante: ").append(t.getId()).append('\n')
                .append("Fecha: ").append(Ui.fecha(t.getFecha())).append('\n')
                .append("Valor: ").append(Ui.dinero(t.getValor())).append('\n');
        if (t.getComision() > 0) sb.append("Comisión: ").append(Ui.dinero(t.getComision())).append('\n');
        if (t.getOrigen() != null) sb.append("Desde: ").append(t.getOrigen().getNombre())
                .append(" (queda ").append(Ui.dinero(t.getOrigen().getSaldo())).append(")\n");
        if (t.getDestino() != null) sb.append("Hacia: ").append(t.esEntrePersonas()
                ? SistemaFintech.nombreEnmascarado(t.getDestino().getPropietario()) : t.getDestino().getNombre()).append('\n');
        sb.append("Puntos ganados: +").append(t.getPuntosGenerados());
        sb.append("\n\n¿Te equivocaste? Puedes deshacerla ahora o desde Movimientos durante las próximas ")
                .append((int) sistema.config("ventanaReversionHoras")).append(" horas.");
        if (t.getNivelRiesgo() != NivelRiesgo.BAJO)
            sb.append("\n\n⚠ Detectamos actividad inusual (riesgo ").append(t.getNivelRiesgo())
                    .append("). Si no fuiste tú, revierte la operación en Movimientos.");
        if (!t.getDetalle().isBlank()) sb.append("\n\nNota: ").append(t.getDetalle());
        if (Ui.infoConAccion(sb.toString(), "Deshacer esta operación")
                && Ui.confirmar("¿Deshacer " + t.getTipo().getEtiqueta().toLowerCase() + " de " + Ui.dinero(t.getValor()) + "?")) {
            Ui.ejecutar(() -> {
                sistema.revertirOperacion(usuario.getId(), t.getId());
                Ui.info("Listo, la operación " + t.getId() + " se revirtió.");
            }, ctx.getAlCambiar());
        }
    }
}
