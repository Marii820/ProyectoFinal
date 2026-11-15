package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Billetera;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.EstadoTransaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Notificacion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.OperacionProgramada;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Transaccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Navegador;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Inicio: Disponible (cuenta principal) separado de las billeteras, y lo más reciente. */
public class InicioController implements ControladorSeccion {

    @FXML private Label lblDisponible;
    @FXML private Label lblResumen;
    @FXML private FlowPane tarjetas;
    @FXML private Label lblSinBilleteras;
    @FXML private VBox ultimos;
    @FXML private VBox proximas;
    @FXML private VBox alertas;

    private Contexto ctx;
    private SistemaFintech sistema;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.sistema = ctx.getSistema();
        this.usuario = ctx.getUsuario();
    }

    @Override
    public void refrescar() {
        lblDisponible.setText(Ui.dinero(usuario.saldoDisponible()));
        lblResumen.setText("En bolsillos: " + Ui.dinero(usuario.saldoEnBilleteras()) + "   ·   Total: " + Ui.dinero(usuario.saldoTotal())
                + "   ·   Nivel " + usuario.getNivel() + " · " + String.format("%,d", usuario.getPuntosDisponibles()) + " puntos");

        tarjetas.getChildren().clear();
        for (Billetera b : usuario.getBilleteras()) {
            FXMLLoader loader = Navegador.cargar("cliente/TarjetaBilletera.fxml", ctx);
            TarjetaBilleteraController c = loader.getController();
            c.mostrar(b);
            tarjetas.getChildren().add(loader.getRoot());
        }
        lblSinBilleteras.setVisible(usuario.getBilleteras().isEmpty());
        lblSinBilleteras.setManaged(usuario.getBilleteras().isEmpty());

        ultimos.getChildren().clear();
        int n = 0;
        for (Transaccion t : usuario.getHistorial()) {
            if (n++ == 6) break;
            double v = t.valorPara(usuario);
            String texto = Ui.fechaCorta(t.getFecha()) + "   " + describir(t) + "   " + (v == 0 ? Ui.dinero(t.getValor()) : Ui.dineroConSigno(v))
                    + (t.getEstado() == EstadoTransaccion.EXITOSA ? "" : "  (" + t.getEstado().name().toLowerCase() + ")");
            Label l = new Label(texto);
            l.setWrapText(true);
            l.getStyleClass().add(v > 0 ? "monto-entra" : v < 0 ? "monto-sale" : "ayuda");
            ultimos.getChildren().add(l);
        }
        if (ultimos.getChildren().isEmpty()) ultimos.getChildren().add(ayuda("Aún no tienes movimientos. Empieza recargando tu Disponible."));

        proximas.getChildren().clear();
        n = 0;
        for (OperacionProgramada op : sistema.programadasDeUsuario(usuario.getId())) {
            if (n++ == 3) break;
            proximas.getChildren().add(new Label(Ui.fechaCorta(op.getFechaEjecucion()) + "   "
                    + (op.getDescripcion().isBlank() ? op.getTipo().getEtiqueta() : op.getDescripcion()) + "   " + Ui.dinero(op.getValor())));
        }
        if (proximas.getChildren().isEmpty()) proximas.getChildren().add(ayuda("No tienes operaciones programadas."));

        alertas.getChildren().clear();
        n = 0;
        for (Notificacion x : usuario.getNotificaciones()) {
            if (x.isLeida()) continue;
            if (n++ == 3) break;
            Label l = ayuda("• " + x.getMensaje());
            if (x.getTipo().esAlerta()) l.getStyleClass().add("texto-error");
            alertas.getChildren().add(l);
        }
        if (alertas.getChildren().isEmpty()) alertas.getChildren().add(ayuda("Todo en orden ✓"));
    }

    /** Texto corto de un movimiento desde el punto de vista del usuario. */
    private String describir(Transaccion t) {
        if (t.esEntrePersonas()) {
            return t.getOrigen().getPropietario() == usuario
                    ? "Enviaste a " + SistemaFintech.nombreEnmascarado(t.getDestino().getPropietario())
                    : "Recibiste de " + SistemaFintech.nombreEnmascarado(t.getOrigen().getPropietario());
        }
        if (t.getOrigen() != null && t.getDestino() != null) return t.getOrigen().getNombre() + " → " + t.getDestino().getNombre();
        return t.getTipo().getEtiqueta();
    }

    private Label ayuda(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("ayuda");
        l.setWrapText(true);
        return l;
    }

    @FXML private void irRecargar() { ctx.irA("mover#recargar"); }
    @FXML private void irEnviar() { ctx.irA("mover#enviar"); }
    @FXML private void irRetirar() { ctx.irA("mover#retirar"); }
    @FXML private void irProgramar() { ctx.irA("programadas"); }
    @FXML private void irCuenta() { ctx.irA("cuenta"); }
}
