package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;

public class TablaHash<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double FACTOR_CARGA_MAX = 0.75;

    private static class NodoHash<K, V> implements Serializable {
        private static final long serialVersionUID = 1L;
        final K clave;
        V valor;
        NodoHash<K, V> siguiente;

        NodoHash(K clave, V valor, NodoHash<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private NodoHash<K, V>[] cubetas;
    private int size;
    private int colisiones;

    public TablaHash() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        cubetas = (NodoHash<K, V>[]) new NodoHash[Math.max(4, capacidad)];
    }

    private int indice(K clave, int capacidad) {
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    /** Inserta o reemplaza. */
    public void put(K clave, V valor) {
        if (clave == null) throw new IllegalArgumentException("La clave no puede ser nula");
        int i = indice(clave, cubetas.length);
        for (NodoHash<K, V> n = cubetas[i]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                n.valor = valor;
                return;
            }
        }
        if (cubetas[i] != null) colisiones++;
        cubetas[i] = new NodoHash<>(clave, valor, cubetas[i]);
        size++;
        if ((double) size / cubetas.length > FACTOR_CARGA_MAX) rehash();
    }

    public V get(K clave) {
        if (clave == null) return null;
        for (NodoHash<K, V> n = cubetas[indice(clave, cubetas.length)]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) return n.valor;
        }
        return null;
    }

    public V getOrDefault(K clave, V porDefecto) {
        V v = get(clave);
        return v == null ? porDefecto : v;
    }

    public boolean containsKey(K clave) {
        return get(clave) != null;
    }

    public V remove(K clave) {
        if (clave == null) return null;
        int i = indice(clave, cubetas.length);
        NodoHash<K, V> anterior = null;
        for (NodoHash<K, V> n = cubetas[i]; n != null; anterior = n, n = n.siguiente) {
            if (n.clave.equals(clave)) {
                if (anterior == null) cubetas[i] = n.siguiente;
                else anterior.siguiente = n.siguiente;
                size--;
                return n.valor;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void rehash() {
        NodoHash<K, V>[] viejas = cubetas;
        cubetas = (NodoHash<K, V>[]) new NodoHash[viejas.length * 2];
        colisiones = 0;
        for (NodoHash<K, V> cabeza : viejas) {
            for (NodoHash<K, V> n = cabeza; n != null; ) {
                NodoHash<K, V> sig = n.siguiente;
                int i = indice(n.clave, cubetas.length);
                if (cubetas[i] != null) colisiones++;
                n.siguiente = cubetas[i];
                cubetas[i] = n;
                n = sig;
            }
        }
    }

    public SimpleLinkedList<K> claves() {
        SimpleLinkedList<K> r = new SimpleLinkedList<>();
        for (NodoHash<K, V> cabeza : cubetas)
            for (NodoHash<K, V> n = cabeza; n != null; n = n.siguiente) r.addLast(n.clave);
        return r;
    }

    public SimpleLinkedList<V> valores() {
        SimpleLinkedList<V> r = new SimpleLinkedList<>();
        for (NodoHash<K, V> cabeza : cubetas)
            for (NodoHash<K, V> n = cabeza; n != null; n = n.siguiente) r.addLast(n.valor);
        return r;
    }

    public int getSize() { return size; }

    public boolean isEmpty() { return size == 0; }

    public int getCapacidad() { return cubetas.length; }

    public int getColisiones() { return colisiones; }

    public double factorCarga() { return (double) size / cubetas.length; }
}
