package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;

public class Beneficio implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nombre;
    private final String descripcion;
    private final int costoPuntos;
    private final NivelUsuario nivelMinimo;

    public Beneficio(String id, String nombre, String descripcion, int costoPuntos, NivelUsuario nivelMinimo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
        this.nivelMinimo = nivelMinimo;
    }

    public boolean puedeCanjear(Usuario u) {
        return u.getPuntosDisponibles() >= costoPuntos && u.getNivel().ordinal() >= nivelMinimo.ordinal();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCostoPuntos() { return costoPuntos; }
    public NivelUsuario getNivelMinimo() { return nivelMinimo; }

    @Override
    public String toString() { return nombre; }
}
