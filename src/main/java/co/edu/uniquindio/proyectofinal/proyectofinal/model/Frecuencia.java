package co.edu.uniquindio.proyectofinal.proyectofinal.model;

public enum Frecuencia {
    UNICA("Una sola vez"),
    DIARIA("Diaria"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual");

    private final String etiqueta;

    Frecuencia(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

}
