package co.edu.uniquindio.proyectofinal.proyectofinal.model;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Movimiento automático futuro (4.3). Comparable por:
 *  1) fecha y hora de ejecucioon (la mas proxima primero),
 *  2) prioridad del nivel del usuario (mayor nivel primero),
 *  3) id (para desempatar).
 * asi la ColaPrioridad siempre entrega la siguiente que hay que ejecutar.
 */

public class OperacionProgramada implements Comparable<OperacionProgramada>, Serializable {

    private static final long serialVersionUID = 2L;

    private final String id;
    private LocalDateTime fechaEjecucion;
    private final TipoTransaccion tipo;
    private final double valor;
    private final Cuenta origen;
    private final Cuenta destino;
    private final Frecuencia frecuencia;
    private final String descripcion;
    private final int prioridad;     // ordinal del nivel del usuario al programarla
    private boolean avisada;         // ya se mandó la alerta de "próxima a ejecutarse"
    private int ejecuciones;
    private int fallos;

    public OperacionProgramada(String id, LocalDateTime fechaEjecucion, TipoTransaccion tipo, double valor,
                               Cuenta origen, Cuenta destino, Frecuencia frecuencia,
                               String descripcion, int prioridad) {
        this.id = id;
        this.fechaEjecucion = fechaEjecucion;
        this.tipo = tipo;
        this.valor = valor;
        this.origen = origen;
        this.destino = destino;
        this.frecuencia = frecuencia;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.prioridad = prioridad;
    }

    /** Crea la transacción a aplicar (la aplica SistemaFintech). */
    public Transaccion ejecutar(String idTransaccion) {
        Transaccion t = new Transaccion(idTransaccion, fechaEjecucion, tipo, valor, origen, destino);
        t.setProgramada(true);
        t.setDescripcion(descripcion.isBlank() ? "Programada " + id : descripcion + " (" + id + ")");
        return t;
    }

    public void registrarResultado(boolean exitosa) {
        if (exitosa) ejecuciones++; else fallos++;
    }

    /** Siguiente fecha según la frecuencia; null si es única. */
    public LocalDateTime calcularSiguienteFecha() {
        return switch (frecuencia) {
            case DIARIA -> fechaEjecucion.plusDays(1);
            case SEMANAL -> fechaEjecucion.plusWeeks(1);
            case MENSUAL -> fechaEjecucion.plusMonths(1);
            case UNICA -> null;
        };
    }

    public void reprogramar() {
        fechaEjecucion = calcularSiguienteFecha();
        avisada = false;
    }

    public Usuario getUsuario() {
        return origen != null ? origen.getPropietario() : destino.getPropietario();
    }

    @Override
    public int compareTo(OperacionProgramada otra) {
        int c = fechaEjecucion.compareTo(otra.fechaEjecucion);
        if (c != 0) return c;
        c = Integer.compare(otra.prioridad, prioridad); // mayor nivel primero
        return c != 0 ? c : id.compareTo(otra.id);
    }

    public String getId() { return id; }
    public LocalDateTime getFechaEjecucion() { return fechaEjecucion; }
    public TipoTransaccion getTipo() { return tipo; }
    public double getValor() { return valor; }
    public Cuenta getOrigen() { return origen; }
    public Cuenta getDestino() { return destino; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public String getDescripcion() { return descripcion; }
    public int getPrioridad() { return prioridad; }
    public boolean isAvisada() { return avisada; }
    public void setAvisada(boolean avisada) { this.avisada = avisada; }
    public int getEjecuciones() { return ejecuciones; }
    public int getFallos() { return fallos; }

    public String getCodigoOrigen() { return origen == null ? "-" : origen.getCodigo(); }
    public String getCodigoDestino() { return destino == null ? "-" : destino.getCodigo(); }
    


}
