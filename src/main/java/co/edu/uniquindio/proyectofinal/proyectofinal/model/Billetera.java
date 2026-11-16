package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.time.LocalDateTime;

public class Billetera extends Cuenta {

    private static final long serialVersionUID = 2L;

    private String nombre;
    private final TipoBilletera tipo;
    private boolean activa = true;
    private double meta;
    //meta de ahorro de la billetera

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

    public double getMeta() { return meta; }

    public void setMeta(double meta) {
        if (meta < 0) throw new IllegalArgumentException("La meta no puede ser negativa");
        this.meta = meta;
    }
    public boolean tieneMeta() { return meta > 0; }

    /* Avance hacia la meta entre 0 y 1 (0 si no tiene meta). */
    public double avanceMeta() { return meta <= 0 ? 0 : Math.min(1, getSaldo() / meta); }


    @Override
    public boolean esPrincipal() { return false; }


}
