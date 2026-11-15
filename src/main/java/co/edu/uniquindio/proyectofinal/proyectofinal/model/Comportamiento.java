package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento de comportamiento inusual guardado en el historial de auditoría (4.9).
 */

public class Comportamiento implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final LocalDateTime fecha;
    private final Transaccion transaccion;
    private final String patron;
    private final NivelRiesgo nivelRiesgo;
    private final int puntajeRiesgo;
    private final String descripcion;

    public Comportamiento(String id, LocalDateTime fecha, Transaccion transaccion, String patron, NivelRiesgo nivelRiesgo, int puntajeRiesgo, String descripcion){
        this.id = id;
        this.fecha = fecha;
        this.transaccion =transaccion;
        this.patron = patron;
        this.nivelRiesgo = nivelRiesgo;
        this.puntajeRiesgo = puntajeRiesgo;
        this.descripcion = descripcion;

    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public String getPatron() {
        return patron;
    }

    public Usuario getUsuario() { return transaccion.getUsuario(); }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public int getPuntajeRiesgo() {
        return puntajeRiesgo;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
