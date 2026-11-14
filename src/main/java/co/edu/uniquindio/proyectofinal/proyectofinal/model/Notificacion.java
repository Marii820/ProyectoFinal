package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notificacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final LocalDateTime fecha;
    private final TipoNotificacion tipo;
    private final String mensaje;
    private final Usuario usuario;
    private boolean leida;

    public Notificacion(String id, LocalDateTime fecha, TipoNotificacion tipo, String mensaje, Usuario usuario) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public String getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public TipoNotificacion getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public Usuario getUsuario() { return usuario; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    @Override
    public String toString() { return "[" + tipo + "] " + mensaje; }
}
