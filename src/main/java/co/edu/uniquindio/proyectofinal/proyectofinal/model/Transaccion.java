package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Movimiento de dinero. Comparable por VALOR (y luego por id para que no haya empates),
 * así se puede guardar en el árbol binario y sacar las de mayor valor.
 */

public class Transaccion implements Comparable<Transaccion>, Serializable {

    private static final long serialVersionUID = 2L;

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
    private String detalle = "";

    public Transaccion(String id, LocalDateTime fecha, TipoTransaccion tipo, double valor, Cuenta origen,
            Cuenta destino) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.valor = valor;
        this.origen = origen;
        this.destino = destino;
        this.usuario = origen != null ? origen.getPropietario() : destino.getPropietario();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public Cuenta getOrigen() {
        return origen;
    }

    public Cuenta getDestino() {
        return destino;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public boolean isProgramada() {
        return programada;
    }

    public void setProgramada(boolean programada) {
        this.programada = programada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle == null ? "" : detalle;
    }

    public String getCodigoOrigen() {
        return origen == null ? "-" : origen.getCodigo();
    }

    public String getCodigoDestino() {
        return destino == null ? "-" : destino.getCodigo();
    }

    /*
     * Es reversible see puede hacer siempre y cuando sea exitosa. No aplica cuando
     * se mandan a bolsillos revertir, por que de igual queda en mi cuenta ese
     * dinero
     */
    public boolean esReversible() {
        return estado == EstadoTransaccion.EXITOSA && tipo != TipoTransaccion.TRANSFERENCIA_PROPIA;

    }

    /*
     * Las operacion de mandar y recibir dinero
     */
    public double valorPara(Usuario u) {
        boolean sale = origen != null && origen.getPropietario().equals(u);
        boolean entra = destino != null && destino.getPropietario().equals(u);
        if (sale && entra)
            return 0; // significa que lo esta moviendo entre su disponible billetera y viceversa.
        if (sale)
            return -(valor + comision);
        return valor;

    }

    /*
     * Si c es diferente de cero, devuelve c, si c es igual a cero, compara los ID
     * de las dos transacciones.
     */
    @Override
    public int compareTo(Transaccion otra) {
        int c = Double.compare(valor, otra.valor);
        return c != 0 ? c : id.compareTo(otra.id);
    }

    /** true si el dinero fue de un usuario a otro. */
    public boolean esEntrePersonas() {
        return origen != null && destino != null && origen.getPropietario() != destino.getPropietario();
    }

    @Override
    public String toString() {
        return id + " " + tipo + " $" + String.format("%,.0f", valor) + " [" + estado + "]";
    }

}
