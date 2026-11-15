package co.edu.uniquindio.proyectofinal.proyectofinal.vista.cliente;

import co.edu.uniquindio.proyectofinal.proyectofinal.model.Notificacion;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class NotificacionesController implements ControladorSeccion {

    @FXML private Label lblInfo;
    @FXML private ListView<Notificacion> lista;

    private Contexto ctx;
    private Usuario usuario;

    @Override
    public void iniciar(Contexto ctx) {
        this.ctx = ctx;
        this.usuario = ctx.getUsuario();
        lblInfo.setText("Guardamos tus " + Usuario.MAX_NOTIFICACIONES + " notificaciones más recientes.");
        lista.setPlaceholder(new Label("No tienes notificaciones"));
        lista.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notificacion n, boolean vacio) {
                super.updateItem(n, vacio);
                getStyleClass().removeAll("notif-alerta", "notif-nueva");
                if (vacio || n == null) { setText(null); return; }
                setText((n.isLeida() ? "" : "● ") + Ui.fecha(n.getFecha()) + "   " + n.getTipo().getEtiqueta().toUpperCase() + "\n" + n.getMensaje());
                setWrapText(true);
                setPrefWidth(0);
                if (n.getTipo().esAlerta()) getStyleClass().add("notif-alerta");
                if (!n.isLeida()) getStyleClass().add("notif-nueva");
            }
        });
    }

    @Override
    public void refrescar() {
        lista.getItems().setAll(Ui.lista(usuario.getNotificaciones()));
        lista.refresh();
    }

    @FXML
    private void marcarLeidas() {
        usuario.marcarNotificacionesLeidas();
        ctx.cambio();
    }
}
