package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;
import java.io.Serializable;

/*
arbol binario de busqueda ABB generico.
 - ranking de usuarios por puntos (EntradaRanking), busqueda por rango de puntos y por nivel,
  - transacciones ordenadas por valor para sacar las de mayor valor.
 */ 

public class ArbolBinario<T extends Comparable<T>> implements Serializable  {
     private static final long serialVersionUID = 1L;

    private int peso;
    private NodoArbol<T> raiz;

    public ArbolBinario() {
        peso = 0;
        raiz = null;
    }

    public int getPeso() { return peso; }

    public NodoArbol<T> getRaiz() { return raiz; }

    public boolean isEmpty() { return raiz == null; }

    /** Inserta el elemento. Si ya existe lanza excepcion. O(altura) */
    public void insertar(T elemento) {
        raiz = insertar(raiz, null, elemento);
    }

    private NodoArbol<T> insertar(NodoArbol<T> raizAux, NodoArbol<T> padre, T elemento) {
        if (raizAux == null) {
            peso++;
            return new NodoArbol<>(elemento, padre);
        }
        int comp = raizAux.getElemento().compareTo(elemento);
        if (comp == 0) throw new IllegalArgumentException("Error: ya existe ese elemento");
        if (comp > 0) raizAux.setIzquierda(insertar(raizAux.getIzquierda(), raizAux, elemento));
        else raizAux.setDerecho(insertar(raizAux.getDerecho(), raizAux, elemento));
        return raizAux;
    }
    /* Buscar */
     public boolean existe(T elemento) {
        return buscar(elemento) != null;
    }

    /** Devuelve el elemento guardado que es "igual" (compareTo == 0) o null. O(altura) */
    public T buscar(T elemento) {
        NodoArbol<T> aux = raiz;
        while (aux != null) {
            int comp = aux.getElemento().compareTo(elemento);
            if (comp == 0) return aux.getElemento();
            aux = comp > 0 ? aux.getIzquierda() : aux.getDerecho();
        }
        return null;
    }
    private NodoArbol<T> obtenerMenor(NodoArbol<T> actual) {
        if (actual == null) return null;
        while (actual.getIzquierda() != null) actual = actual.getIzquierda();
        return actual;
    }

    public T menor() {
        NodoArbol<T> n = obtenerMenor(raiz);
        return n == null ? null : n.getElemento();
    }

    public T mayor() {
        NodoArbol<T> n = raiz;
        if (n == null) return null;
        while (n.getDerecho() != null) n = n.getDerecho();
        return n.getElemento();
    }




}
