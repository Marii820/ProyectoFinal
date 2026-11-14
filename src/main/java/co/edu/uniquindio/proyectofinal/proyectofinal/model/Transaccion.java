package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaccion implements Comparable<Transaccion>, Serializable {

    private  static final long serialVersionUID = 2L;

    private final String id;
    private final LocalDateTime fecha;
    private final TipoTransaccion tipo;
    private final double valor;
    private final Cuenta origen;
    private final Cuenta destino;
    private final Usuario usuario;
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;
    private double comision;
    private int puntosGenerados;
    private NivelRiesgo nivelRiesgo = NivelRiesgo.BAJO;
    private boolean programada;
    private String descripcion = "";
    private String detalle;




}
