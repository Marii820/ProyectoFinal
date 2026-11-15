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

    public SimpleLinkedList<T> recorrerInOrden() {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        recorrerInOrden(raiz, r);
        return r;
    }

    private void recorrerInOrden(NodoArbol<T> n, SimpleLinkedList<T> r) {
        if (n == null) return;
        recorrerInOrden(n.getIzquierda(), r);
        r.addLast(n.getElemento());
        recorrerInOrden(n.getDerecho(), r);
    }

    public SimpleLinkedList<T> recorrerPreorden() {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        recorrerPreorden(raiz, r);
        return r;
    }

    private void recorrerPreorden(NodoArbol<T> n, SimpleLinkedList<T> r) {
        if (n == null) return;
        r.addLast(n.getElemento());
        recorrerPreorden(n.getIzquierda(), r);
        recorrerPreorden(n.getDerecho(), r);
    }

    public SimpleLinkedList<T> recorrerPostorden() {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        recorrerPostorden(raiz, r);
        return r;
    }

    private void recorrerPostorden(NodoArbol<T> n, SimpleLinkedList<T> r) {
        if (n == null) return;
        recorrerPostorden(n.getIzquierda(), r);
        recorrerPostorden(n.getDerecho(), r);
        r.addLast(n.getElemento());
    }

    /** Recorrido por niveles (amplitud) usando la Cola. */
    public SimpleLinkedList<T> recorrerAmplitud() {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        if (raiz == null) return r;
        Cola<NodoArbol<T>> cola = new Cola<>();
        cola.encolar(raiz);
        while (!cola.isEmpty()) {
            NodoArbol<T> n = cola.desencolar();
            r.addLast(n.getElemento());
            if (n.getIzquierda() != null) cola.encolar(n.getIzquierda());
            if (n.getDerecho() != null) cola.encolar(n.getDerecho());
        }
        return r;
    }

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

    public boolean eliminar(T elemento) {
        int antes = peso;
        raiz = eliminar(raiz, elemento);
        if (raiz != null) raiz.setPadre(null);
        return peso < antes;
    }

    private NodoArbol<T> eliminar(NodoArbol<T> raizAux, T elemento) {
        if (raizAux == null) return null;
        int comp = raizAux.getElemento().compareTo(elemento);
        if (comp > 0) {
            raizAux.setIzquierda(eliminar(raizAux.getIzquierda(), elemento));
            if (raizAux.getIzquierda() != null) raizAux.getIzquierda().setPadre(raizAux);
        } else if (comp < 0) {
            raizAux.setDerecho(eliminar(raizAux.getDerecho(), elemento));
            if (raizAux.getDerecho() != null) raizAux.getDerecho().setPadre(raizAux);
        } else {
            // caso 1 y 2: hoja o un solo hijo
            if (raizAux.getIzquierda() == null) { peso--; return raizAux.getDerecho(); }
            if (raizAux.getDerecho() == null) { peso--; return raizAux.getIzquierda(); }
            // caso 3: dos hijos -> se reemplaza por el menor del subárbol derecho (sucesor inorden)
            NodoArbol<T> sucesor = obtenerMenor(raizAux.getDerecho());
            raizAux.setElemento(sucesor.getElemento());
            raizAux.setDerecho(eliminar(raizAux.getDerecho(), sucesor.getElemento()));
            if (raizAux.getDerecho() != null) raizAux.getDerecho().setPadre(raizAux);
        }
        return raizAux;
    }

    public SimpleLinkedList<T> deMayorAMenor() {
        return mayores(Integer.MAX_VALUE);
    }

    private void mayores(NodoArbol<T> n, int k, SimpleLinkedList<T> r) {
        if (n == null || r.getSize() >= k) return;
        mayores(n.getDerecho(), k, r);
        if (r.getSize() < k) r.addLast(n.getElemento());
        mayores(n.getIzquierda(), k, r);
    }

    public SimpleLinkedList<T> mayores(int k) {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        mayores(raiz, k, r);
        return r;
    }

    public SimpleLinkedList<T> buscarRango(T min, T max) {
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        buscarRango(raiz, min, max, r);
        return r;
    }

    private void buscarRango(NodoArbol<T> n, T min, T max, SimpleLinkedList<T> r) {
        if (n == null) return;
        T e = n.getElemento();
        if (e.compareTo(min) > 0) buscarRango(n.getIzquierda(), min, max, r);
        if (e.compareTo(min) >= 0 && e.compareTo(max) <= 0) r.addLast(e);
        if (e.compareTo(max) < 0) buscarRango(n.getDerecho(), min, max, r);
    }

    public int obtenerPeso() {
        return obtenerPeso(raiz);
    }

    private int obtenerPeso(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + obtenerPeso(nodo.getIzquierda()) + obtenerPeso(nodo.getDerecho());
    }

    public int obtenerAltura() {
        return obtenerAltura(raiz);
    }

    private int obtenerAltura(NodoArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + Math.max(obtenerAltura(nodo.getIzquierda()), obtenerAltura(nodo.getDerecho()));
    }

    public int contarHojas() {
        return contarHojas(raiz);
    }

    private int contarHojas(NodoArbol<T> n) {
        if (n == null) return 0;
        if (n.esHoja()) return 1;
        return contarHojas(n.getIzquierda()) + contarHojas(n.getDerecho());
    }




}
