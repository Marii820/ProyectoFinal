package co.edu.uniquindio.proyectofinal.proyectofinal.model;



public enum TipoNotificacion {

    BIENVENIDA("Bienvenida"),
    SALDO_BAJO("Saldo bajo"),
    PROGRAMADA_PROXIMA("Programada próxima"),
    PROGRAMADA_EJECUTADA("Programada ejecutada"),
    RECHAZADA("Operación rechazada"),
    REVERSION("Reversión"),
    POSIBLE_DUPLICADA("Posible duplicada"),
    ASCENSO_NIVEL("Ascenso de nivel"),
    CANJE_BENEFICIO("Canje de beneficio"),
    DINERO_RECIBIDO("Dinero recibido"),
    ALERTA_SEGURIDAD("Alerta de seguridad");

    private final String etiqueta;

    TipoNotificacion(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }

    public boolean esAlerta() {
        return this == SALDO_BAJO || this == RECHAZADA || this == ALERTA_SEGURIDAD || this == POSIBLE_DUPLICADA;
    }

    @Override
    public String toString() { return etiqueta; }
}
