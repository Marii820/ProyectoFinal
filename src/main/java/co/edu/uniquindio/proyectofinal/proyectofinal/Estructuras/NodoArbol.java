package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;

public class NodoArbol<T extends Comparable<T>> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T elemento;
    private NodoArbol<T> izquierda;
    private NodoArbol<T> derecho;
    private NodoArbol<T> padre;

    public NodoArbol(T elemento, NodoArbol<T> padre) {
        this.elemento = elemento;
        this.padre = padre;
    }

    public T getElemento() {
        return elemento;
    }

    public void setElemento(T elemento) {
        this.elemento = elemento;
    }

    public NodoArbol<T> getIzquierda() {
        return izquierda;
    }

    public void setIzquierda(NodoArbol<T> izquierda) {
        this.izquierda = izquierda;
    }

    public NodoArbol<T> getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoArbol<T> derecho) {
        this.derecho = derecho;
    }

    public NodoArbol<T> getPadre() {
        return padre;
    }

    public void setPadre(NodoArbol<T> padre) {
        this.padre = padre;
    }

    public boolean esHoja() {
        return izquierda == null && derecho == null;
    }
}
