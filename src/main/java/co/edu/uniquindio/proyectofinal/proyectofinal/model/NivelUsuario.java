package co.edu.uniquindio.proyectofinal.proyectofinal.model;

public enum NivelUsuario {
    BRONCE(0, 500, 0.010, 0.00, 2_000_000),
    PLATA(501, 1000, 0.005, 0.05, 5_000_000),
    ORO(1001, 5000, 0.0025, 0.10, 10_000_000),
    PLATINO(5001, Integer.MAX_VALUE, 0.0, 0.20, 50_000_000);

    private final int puntosMin;
    private final int puntosMax;
    private final double comision;
    private final double bonoPuntos;
    private final double limiteTransaccion;

    NivelUsuario(int puntosMin, int puntosMax, double comision, double bonoPuntos, double limiteTransaccion) {
        this.puntosMin = puntosMin;
        this.puntosMax = puntosMax;
        this.comision = comision;
        this.bonoPuntos = bonoPuntos;
        this.limiteTransaccion = limiteTransaccion;
    }

    public static NivelUsuario segunPuntos(int puntos) {
        for (NivelUsuario n : values()) {
            if (puntos >= n.puntosMin && puntos <= n.puntosMax) return n;
        }
        return BRONCE;
    }

    public NivelUsuario siguiente() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    public String ventajas() {
        return String.format("Comisión %.2f%% · Bono de puntos +%.0f%% · Límite por operación $%,.0f%s",
                comision * 100, bonoPuntos * 100, limiteTransaccion,
                ordinal() >= ORO.ordinal() ? " · Prioridad en operaciones programadas" : "");
    }

    public int getPuntosMin() { return puntosMin; }
    public int getPuntosMax() { return puntosMax; }
    public double getComision() { return comision; }
    public double getBonoPuntos() { return bonoPuntos; }
    public double getLimiteTransaccion() { return limiteTransaccion; }

}
