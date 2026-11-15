package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.Grafo;
import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Cuenta;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/** 5.6 Grafos: transferencias entre usuarios y relaciones entre cuentas. */
public class GrafoController implements ControladorSeccion {

    @FXML private Label lblResumen;
    @FXML private TableView<Grafo.Arista> tRutas;
    @FXML private TableColumn<Grafo.Arista, String> colRutaOrigen;
    @FXML private TableColumn<Grafo.Arista, String> colRutaDestino;
    @FXML private TableColumn<Grafo.Arista, Number> colRutaVeces;
    @FXML private TableColumn<Grafo.Arista, Number> colRutaMonto;
    @FXML private ListView<String> lvCiclos;
    @FXML private ComboBox<Usuario> cbDesde;
    @FXML private ComboBox<Usuario> cbHasta;
    @FXML private Label lblRuta;
    @FXML private Label lblAlcance;
    @FXML private TableView<Grafo.Arista> tCuentas;
    @FXML private TableColumn<Grafo.Arista, String> colCtaOrigen;
    @FXML private TableColumn<Grafo.Arista, String> colCtaDestino;
    @FXML private TableColumn<Grafo.Arista, Number> colCtaVeces;
    @FXML private TableColumn<Grafo.Arista, Number> colCtaMonto;

    private SistemaFintech sistema;

    @Override
    public void iniciar(Contexto ctx) {
        this.sistema = ctx.getSistema();
        Ui.ajustar(tRutas, "Todavía no hay transferencias entre usuarios");
        Ui.texto(colRutaOrigen, a -> etiqueta(a.getOrigen()));
        Ui.texto(colRutaDestino, a -> etiqueta(a.getDestino()));
        Ui.numero(colRutaVeces, Grafo.Arista::getVeces, false);
        Ui.numero(colRutaMonto, Grafo.Arista::getMonto, true);
        Ui.ajustar(tCuentas, "Todavía no hay movimientos entre cuentas");
        Ui.texto(colCtaOrigen, a -> etiqueta(a.getOrigen()));
        Ui.texto(colCtaDestino, a -> etiqueta(a.getDestino()));
        Ui.numero(colCtaVeces, Grafo.Arista::getVeces, false);
        Ui.numero(colCtaMonto, Grafo.Arista::getMonto, true);
        lvCiclos.setPlaceholder(new Label("No hay ciclos de dinero"));
    }

    /** Traduce un vértice (cédula o código de cuenta) a un nombre legible. */
    private String etiqueta(String vertice) {
        Usuario u = sistema.buscarUsuario(vertice);
        if (u != null) return u.getNombre() + " (" + u.getId() + ")";
        Cuenta c = sistema.buscarCuenta(vertice);
        return c == null ? vertice : c.getNombre() + " de " + c.getPropietario().getPrimerNombre() + " (" + c.getCodigo() + ")";
    }

    private String nombres(Iterable<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (sb.length() > 0) sb.append(" → ");
            Usuario u = sistema.buscarUsuario(id);
            sb.append(u == null ? id : u.getPrimerNombre());
        }
        return sb.toString();
    }

    @FXML
    private void buscarRuta() {
        if (cbDesde.getValue() == null || cbHasta.getValue() == null) { Ui.error("Seleccione los dos usuarios"); return; }
        Usuario a = cbDesde.getValue(), b = cbHasta.getValue();
        SimpleLinkedList<String> ruta = sistema.getGrafoUsuarios().buscarRuta(a.getId(), b.getId());
        lblRuta.setText(ruta.isEmpty() ? "No hay ruta de dinero de " + a.getPrimerNombre() + " a " + b.getPrimerNombre()
                : "Ruta más corta (" + (ruta.getSize() - 1) + " saltos): " + nombres(ruta));
        SimpleLinkedList<String> alc = sistema.getGrafoUsuarios().alcanzables(a.getId());
        lblAlcance.setText("El dinero de " + a.getPrimerNombre() + " puede llegar (directa o indirectamente) a "
                + alc.getSize() + " usuarios" + (alc.isEmpty() ? "." : ": " + nombres(alc)));
    }

    @Override
    public void refrescar() {
        Grafo g = sistema.getGrafoUsuarios();
        lblResumen.setText(g.cantidadVertices() + " usuarios (vértices) · " + g.cantidadAristas() + " relaciones (aristas)");
        tRutas.getItems().setAll(Ui.lista(g.rutasFrecuentes(15)));
        lvCiclos.getItems().clear();
        for (String c : g.detectarCiclos(6)) lvCiclos.getItems().add(nombres(List.of(c.split(" -> "))));
        tCuentas.getItems().setAll(Ui.lista(sistema.getGrafoCuentas().rutasFrecuentes(15)));
        List<Usuario> us = Ui.lista(sistema.listarUsuarios());
        Ui.setItems(cbDesde, us);
        Ui.setItems(cbHasta, us);
    }
}
