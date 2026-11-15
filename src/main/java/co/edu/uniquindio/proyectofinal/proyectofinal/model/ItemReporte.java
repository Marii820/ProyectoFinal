package co.edu.uniquindio.proyectofinal.proyectofinal.model;

/** Fila generica de un reporte: nombre, cantidad de operaciones y monto. */

public class ItemReporte {
    private final String nombre;
    private int cantidad;
    private double monto;

    public String getNombre() {
        return nombre;
    }
    public int getCantidad() {
        return cantidad;
    }
    public double getMonto() {
        return monto;
    }
    public ItemReporte(String nombre){
        this.nombre = nombre;

    }
    public void sumar(double valor){
        cantidad++;
        monto += valor;
    }

}
