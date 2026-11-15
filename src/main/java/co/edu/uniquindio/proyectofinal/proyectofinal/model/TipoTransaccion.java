package co.edu.uniquindio.proyectofinal.proyectofinal.model;

/**
 * TipoTransaccion
 */
public enum TipoTransaccion {
    RECARGA("Recarga", 1),
    RETIRO("Retiro", 2),
    TRANSFERENCIA_PROPIA("Entre mis bolsillos", 0),
    TRANSFERENCIA_EXTERNA("Envío a otra persona", 3),
    PAGO_PROGRAMADO("Pago programado", 3);

    private final String etiqueta;
    private final int puntosPor100;

    TipoTransaccion(String etiqueta, int puntosPor100) {
        this.etiqueta = etiqueta;
        this.puntosPor100 = puntosPor100;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getPuntosPor100() {
        return puntosPor100;
    }

    /* Plata enviada a other person */
    public boolean esHaciaOtraPersona() {
        return this == TRANSFERENCIA_EXTERNA || this == PAGO_PROGRAMADO;
    }

    /*
     * Claramete solo se programa para enviar dinero a otra persona o un ahorro en
     * sus bolsillos y ambas
     */
    public boolean esProgramable() {
        return this == TRANSFERENCIA_PROPIA || this == PAGO_PROGRAMADO;
    }

    public boolean esTransferencia() {
        return this == TRANSFERENCIA_PROPIA || this == TRANSFERENCIA_EXTERNA || this == PAGO_PROGRAMADO;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

}
