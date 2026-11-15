package co.edu.uniquindio.proyectofinal.proyectofinal.model;

/** Una fila de la comparacion de rendimiento: la misma operacion con dos estructuras. */

public class ResultadoRendimiento {
    private final String operacion; 
    private final int n ;
    private final String estructuraA;
    private final String complejidadA;
    private final double msA;
    private final String estructuraB;
    private final String complejidadB;
    private final double msB;

    /* Cuantas veces fue mas rapida A que B  */

    public double getMejora(){ return  msA <= 0 ? 0: msB/msA;}
    public String getOperacion() {
        return operacion;
    }
    public int getN() {
        return n;
    }
    public String getEstructuraA() {
        return estructuraA;
    }

    public String getComplejidadA() {
        return complejidadA;
    }

    public double getMsA() {
        return msA;
    }
    public String getEstructuraB() {
        return estructuraB;
    }
    public String getComplejidadB() {
        return complejidadB;
    }
    public double getMsB() {
        return msB;
    }

    public ResultadoRendimiento(String operacion, int n, String estructuraA,String complejidadA,  double msA, String estructuraB,
            String complejidadB, double msB) {
        this.operacion = operacion;
        this.n = n;
        this.estructuraA = estructuraA;
        this.complejidadA= complejidadA;
        this.msA = msA;
        this.estructuraB = estructuraB;
        this.complejidadB = complejidadB;
        this.msB = msB;
    }


}
