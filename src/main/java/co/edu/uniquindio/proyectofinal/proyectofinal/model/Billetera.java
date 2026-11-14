package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.time.LocalDateTime;

public class Billetera extends Cuenta {

    private static final long serialVersionUID = 2L;

    private String nombre;
    private final TipoBilletera tipo;
    private boolean activa = true;

    public Billetera(String codigo, String nombre, TipoBilletera tipo, Usuario propietario, LocalDateTime fechaCreacion) {
        super(codigo, propietario, fechaCreacion);
        this.nombre = nombre;
        this.tipo = tipo;
    }

    @Override
    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoBilletera getTipo() { return tipo; }

    @Override
    public boolean isActiva() { return activa; }

    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public boolean esPrincipal() { return false; }
}
