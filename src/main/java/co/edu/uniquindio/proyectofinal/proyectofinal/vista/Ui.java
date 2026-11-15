package co.edu.uniquindio.proyectofinal.proyectofinal.vista;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/** Utilidades de la interfaz (alertas, formatos y columnas de tablas) para no repetir código. */
public final class Ui {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    private Ui() { }

    // ---------------- acciones y mensajes ----------------

    /** Ejecuta la acción; si falla muestra el mensaje. Al final llama a "despues" (refrescar y guardar). */
    public static void ejecutar(Runnable accion, Runnable despues) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            error(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        } finally {
            despues.run();
        }
    }

    public static void error(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText("No se pudo completar");
        a.setTitle("QuindiPay");
        a.showAndWait();
    }

    public static void info(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("QuindiPay");
        a.showAndWait();
    }

    /** Muestra un mensaje con un botón extra (ej: "Deshacer"). Devuelve true si se presionó ese botón. */
    public static boolean infoConAccion(String mensaje, String textoAccion) {
        ButtonType accion = new ButtonType(textoAccion);
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK, accion);
        a.setHeaderText(null);
        a.setTitle("QuindiPay");
        return a.showAndWait().filter(b -> b == accion).isPresent();
    }

    public static boolean confirmar(String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        a.setTitle("Confirmar");
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    /** Pide un texto en una ventanita. */
    public static Optional<String> pedirTexto(String titulo, String etiqueta, String valorInicial) {
        TextInputDialog d = new TextInputDialog(valorInicial);
        d.setTitle(titulo);
        d.setHeaderText(null);
        d.setContentText(etiqueta);
        return d.showAndWait();
    }

    // ---------------- lectura y formato ----------------

    /** Lee un valor en pesos (acepta "50000", "50.000", "$50,000"). */
    public static double leerValor(String texto) {
        String t = texto == null ? "" : texto.trim().replace("$", "").replace(".", "").replace(",", "").replace(" ", "");
        if (t.isEmpty()) throw new IllegalArgumentException("Escriba el valor");
        try {
            double v = Double.parseDouble(t);
            if (v <= 0) throw new IllegalArgumentException("El valor debe ser mayor que $0");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + texto + "\" no es un valor válido");
        }
    }

    public static double leerValor(TextField campo) { return leerValor(campo.getText()); }

    public static int leerEntero(TextField campo, int porDefecto) {
        String t = campo.getText() == null ? "" : campo.getText().trim().replace(".", "").replace(",", "");
        if (t.isEmpty()) return porDefecto;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + campo.getText() + "\" no es un número entero");
        }
    }

    public static String dinero(double v) { return "$" + String.format("%,.0f", v); }

    public static String dineroConSigno(double v) {
        return v > 0 ? "+ " + dinero(v) : v < 0 ? "- " + dinero(-v) : dinero(0);
    }

    public static String fecha(LocalDateTime f) { return f == null ? "" : f.format(FECHA); }

    public static String fechaCorta(LocalDateTime f) { return f == null ? "" : f.format(FECHA_CORTA); }

    public static <T> List<T> lista(Iterable<T> it) {
        List<T> l = new ArrayList<>();
        for (T t : it) l.add(t);
        return l;
    }

    /** Cambia los ítems del combo sin perder la selección. */
    public static <T> void setItems(ComboBox<T> combo, List<T> items) {
        T sel = combo.getValue();
        combo.getItems().setAll(items);
        if (sel != null && items.contains(sel)) combo.setValue(sel);
        else combo.setValue(null);
    }

    // ---------------- columnas de tablas (las columnas se crean en el .fxml) ----------------

    public static void ajustar(TableView<?> tabla, String textoVacio) {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label(textoVacio));
    }

    public static <T> void texto(TableColumn<T, String> col, Function<T, ?> valor) {
        col.setCellValueFactory(d -> {
            Object v = d.getValue() == null ? null : valor.apply(d.getValue());
            return new SimpleStringProperty(v == null ? "" : v.toString());
        });
    }

    /** Columna numérica (ordena como número). */
    public static <T> void numero(TableColumn<T, Number> col, ToDoubleFunction<T> valor, boolean esDinero) {
        col.setCellValueFactory(d -> new SimpleDoubleProperty(valor.applyAsDouble(d.getValue())));
        col.setCellFactory(c -> new TableCell<T, Number>() {
            @Override
            protected void updateItem(Number v, boolean vacio) {
                super.updateItem(v, vacio);
                setText(vacio || v == null ? null : (esDinero ? "$" : "") + String.format("%,.0f", v.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
    }

    /** Dinero con signo (+ entra en verde / - sale en rojo). */
    public static <T> void movimiento(TableColumn<T, Number> col, ToDoubleFunction<T> valor) {
        col.setCellValueFactory(d -> new SimpleDoubleProperty(valor.applyAsDouble(d.getValue())));
        col.setCellFactory(c -> new TableCell<T, Number>() {
            @Override
            protected void updateItem(Number v, boolean vacio) {
                super.updateItem(v, vacio);
                getStyleClass().removeAll("monto-entra", "monto-sale");
                if (vacio || v == null) { setText(null); return; }
                setText(dineroConSigno(v.doubleValue()));
                setAlignment(Pos.CENTER_RIGHT);
                if (v.doubleValue() > 0) getStyleClass().add("monto-entra");
                else if (v.doubleValue() < 0) getStyleClass().add("monto-sale");
            }
        });
    }

    /** Columna con el número de fila (1, 2, 3...). */
    public static <T> void posicion(TableColumn<T, String> col) {
        col.setSortable(false);
        col.setCellFactory(c -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String v, boolean vacio) {
                super.updateItem(v, vacio);
                setText(vacio || getTableRow() == null || getTableRow().getItem() == null ? null : String.valueOf(getIndex() + 1));
            }
        });
    }
}
