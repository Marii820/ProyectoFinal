package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import java.io.Serializable;

public class EntradaRanking implements Comparable<EntradaRanking>, Serializable {
    private static final long serialVersionUID = 1L;
    private final int puntos;
    private final String idUsuario;
    private final Usuario usuario;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
    public int getPuntos() {
        return puntos;
    }
    public String getIdUsuario() {
        return idUsuario;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public EntradaRanking(int puntos, String idUsuario, Usuario usuario) {
        this.puntos = puntos;
        this.idUsuario = idUsuario;
        this.usuario = usuario;
    }
    /* toma la informacion del usuario y crea un objeto EntradaRanking con ella. */
    public static  EntradaRanking de(Usuario u){
        return new EntradaRanking(u.getPuntosAcumulados(), u.getId(), u);
    }
    /* Limites inferiores y sueriores, busquedas por rango  */
    public static EntradaRanking cotaInferior(int puntos){
        return new EntradaRanking(puntos, "", null);
    }
    public static EntradaRanking cotaSuperior (int puntos){
        return new EntradaRanking(puntos, "\uFFFF", null); //El \uFFFF permite establecer un limite superior cuando los puntos coinciden.
    }
    @Override
    public String toString() { return idUsuario + " (" + puntos + " pts)"; }
    @Override
    public int compareTo(EntradaRanking o) {
        int c = Integer.compare(puntos, o.puntos);
        return c != 0 ? c : idUsuario.compareTo(o.idUsuario);
    }

    



}
