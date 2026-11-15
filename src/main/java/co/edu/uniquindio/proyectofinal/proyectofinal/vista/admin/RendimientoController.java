package co.edu.uniquindio.proyectofinal.proyectofinal.vista.admin;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.ComparadorRendimiento;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.ResultadoRendimiento;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Contexto;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.ControladorSeccion;
import co.edu.uniquindio.proyectofinal.proyectofinal.vista.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Requisito 8: comparar el rendimiento de distintas estructuras en operaciones similares. */
public class RendimientoController implements ControladorSeccion {

    @FXML private ComboBox<Integer> cbN;
    @FXML private TableView<ResultadoRendimiento> tabla;
    @FXML private TableColumn<ResultadoRendimiento, String> colOperacion;
    @FXML private TableColumn<ResultadoRendimiento, String> colEstrA;
    @FXML private TableColumn<ResultadoRendimiento, String> colCompA;
    @FXML private TableColumn<ResultadoRendimiento, String> colMsA;
    @FXML private TableColumn<ResultadoRendimiento, String> colEstrB;
    @FXML private TableColumn<ResultadoRendimiento, String> colCompB;
    @FXML private TableColumn<ResultadoRendimiento, String> colMsB;
    @FXML private TableColumn<ResultadoRendimiento, String> colMejora;

    @Override
    public void iniciar(Contexto ctx) {
        cbN.getItems().setAll(1000, 5000, 10000, 20000);
        cbN.setValue(5000);
        Ui.ajustar(tabla, "Elija la cantidad de datos y ejecute la comparación");
        Ui.texto(colOperacion, ResultadoRendimiento::getOperacion);
        Ui.texto(colEstrA, ResultadoRendimiento::getEstructuraA);
        Ui.texto(colCompA, ResultadoRendimiento::getComplejidadA);
        Ui.texto(colMsA, r -> String.format("%.3f", r.getMsA()));
        Ui.texto(colEstrB, ResultadoRendimiento::getEstructuraB);
        Ui.texto(colCompB, ResultadoRendimiento::getComplejidadB);
        Ui.texto(colMsB, r -> String.format("%.3f", r.getMsB()));
        Ui.texto(colMejora, r -> String.format("%.1f veces", r.getMejora()));
    }

    @FXML
    private void ejecutar() {
        try {
            tabla.getItems().setAll(Ui.lista(ComparadorRendimiento.comparar(cbN.getValue())));
        } catch (RuntimeException ex) {
            Ui.error(ex.getMessage());
        }
    }

    @Override
    public void refrescar() { }
}
