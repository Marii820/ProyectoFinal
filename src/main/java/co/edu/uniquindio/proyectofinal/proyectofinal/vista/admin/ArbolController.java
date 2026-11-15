package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.ArbolBinario;
import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.EntradaRanking;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.NivelUsuario;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** 5.4 Árbol: ranking, búsqueda por rango, clasificación por nivel y recorridos. */
public class ArbolController implements ControladorSeccion {

    @FXML private TextField txtMin;
    @FXML private TextField txtMax;
    @FXML private ComboBox<NivelUsuario> cbNivel;
    @FXML private Label lblFiltro;
    @FXML private TableView<EntradaRanking> tabla;
    @FXML private TableColumn<EntradaRanking, String> colPos;
    @FXML private TableColumn<EntradaRanking, String> colUsuario;
    @FXML private TableColumn<EntradaRanking, Number> colPuntos;
    @FXML private TableColumn<EntradaRanking, String> colNivel;
    @FXML private ComboBox<String> cbRecorrido;
    @FXML private ListView<String> lvRecorrido;
    @FXML private Label lblMedidas;

    private SistemaFintech sistema;
    private String filtro = "TODOS";

    @Override
    public void iniciar(Contexto ctx) {
        this.sistema = ctx.getSistema();
        cbNivel.getItems().setAll(NivelUsuario.values());
        cbRecorrido.getItems().setAll("Inorden (ascendente)", "Preorden", "Postorden", "Amplitud (por niveles)", "Inorden inverso (ranking)");
        cbRecorrido.setValue("Amplitud (por niveles)");
        cbRecorrido.valueProperty().addListener((o, a, n) -> cargarRecorrido());
        Ui.ajustar(tabla, "Sin usuarios en ese filtro");
        Ui.posicion(colPos);
        Ui.texto(colUsuario, e -> e.getUsuario().toString());
        Ui.numero(colPuntos, EntradaRanking::getPuntos, false);
        Ui.texto(colNivel, e -> NivelUsuario.segunPuntos(e.getPuntos()));
    }

    @FXML private void buscarRango() { filtro = "RANGO"; cargarTabla(); }
    @FXML private void porNivel() { filtro = "NIVEL"; cargarTabla(); }
    @FXML private void rankingCompleto() { filtro = "TODOS"; cargarTabla(); }

    private void cargarTabla() {
        try {
            SimpleLinkedList<EntradaRanking> l;
            switch (filtro) {
                case "RANGO" -> {
                    int min = Ui.leerEntero(txtMin, 0), max = Ui.leerEntero(txtMax, Integer.MAX_VALUE);
                    l = invertir(sistema.usuariosPorRangoPuntos(min, max));
                    lblFiltro.setText("Puntos entre " + min + " y " + (max == Integer.MAX_VALUE ? "∞" : max) + ": " + l.getSize() + " usuarios");
                }
                case "NIVEL" -> {
                    if (cbNivel.getValue() == null) throw new IllegalArgumentException("Seleccione un nivel");
                    l = invertir(sistema.usuariosPorNivel(cbNivel.getValue()));
                    lblFiltro.setText("Nivel " + cbNivel.getValue() + ": " + l.getSize() + " usuarios");
                }
                default -> {
                    l = sistema.rankingUsuarios();
                    lblFiltro.setText("Ranking completo (inorden inverso): " + l.getSize() + " usuarios");
                }
            }
            tabla.getItems().setAll(Ui.lista(l));
            tabla.refresh();
        } catch (RuntimeException e) {
            filtro = "TODOS";
            Ui.error(e.getMessage());
        }
    }

    private SimpleLinkedList<EntradaRanking> invertir(SimpleLinkedList<EntradaRanking> l) {
        SimpleLinkedList<EntradaRanking> r = new SimpleLinkedList<>();
        for (EntradaRanking e : l) r.addFirst(e);
        return r;
    }

    private void cargarRecorrido() {
        ArbolBinario<EntradaRanking> a = sistema.getRankingPuntos();
        String sel = cbRecorrido.getValue() == null ? "" : cbRecorrido.getValue();
        SimpleLinkedList<EntradaRanking> r;
        if (sel.startsWith("Preorden")) r = a.recorrerPreorden();
        else if (sel.startsWith("Postorden")) r = a.recorrerPostorden();
        else if (sel.startsWith("Amplitud")) r = a.recorrerAmplitud();
        else if (sel.startsWith("Inorden inverso")) r = a.deMayorAMenor();
        else r = a.recorrerInOrden();
        lvRecorrido.getItems().clear();
        int i = 1;
        for (EntradaRanking e : r) lvRecorrido.getItems().add(i++ + ".  " + e.getUsuario().getNombre() + "  —  " + e.getPuntos() + " pts");
        lblMedidas.setText("Peso (nodos): " + a.getPeso() + "   ·   Altura: " + a.obtenerAltura() + "   ·   Hojas: " + a.contarHojas()
                + (a.isEmpty() ? "" : "   ·   Raíz: " + a.getRaiz().getElemento().getUsuario().getPrimerNombre()));
    }

    @Override
    public void refrescar() {
        cargarTabla();
        cargarRecorrido();
    }
}
