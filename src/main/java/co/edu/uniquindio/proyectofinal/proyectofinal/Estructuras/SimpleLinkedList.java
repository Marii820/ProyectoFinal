package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class SimpleLinkedList<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private int size;
    private NodeSimple<T> firstNode;
    private NodeSimple<T> lastNode;

    public SimpleLinkedList() {
        this.size = 0;
        firstNode = null;
        lastNode = null;
    }

    public int getSize() { return size; }

    public boolean isEmpty() { return firstNode == null; }

    public boolean estaVacia() { return isEmpty(); }

    public void addFirst(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);
        if (isEmpty()) {
            firstNode = nuevo;
            lastNode = nuevo;
        } else {
            nuevo.setNext(firstNode);
            firstNode = nuevo;
        }
        size++;
    }

    public void addLast(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);
        if (isEmpty()) {
            firstNode = nuevo;
            lastNode = nuevo;
        } else {
            lastNode.setNext(nuevo);
            lastNode = nuevo;
        }
        size++;
    }

    public void agregarPosicion(T data, int posicion) {
        if (posicion < 0 || posicion > size) throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        if (posicion == 0) { addFirst(data); return; }
        if (posicion == size) { addLast(data); return; }
        NodeSimple<T> aux = firstNode;
        for (int i = 0; i < posicion - 1; i++) aux = aux.getNext();
        NodeSimple<T> nuevo = new NodeSimple<>(data);
        nuevo.setNext(aux.getNext());
        aux.setNext(nuevo);
        size++;
    }

    public boolean indiceValido(int indice) {
        return indice >= 0 && indice < size;
    }

    public T obtenerValorNodo(int posicion) {
        if (!indiceValido(posicion)) throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        NodeSimple<T> aux = firstNode;
        for (int i = 0; i < posicion; i++) aux = aux.getNext();
        return aux.getData();
    }

    public T getFirst() { return isEmpty() ? null : firstNode.getData(); }

    public T getLast() { return isEmpty() ? null : lastNode.getData(); }

    public int obtenerPosicionNodo(T data) {
        int posicion = 0;
        NodeSimple<T> aux = firstNode;
        while (aux != null) {
            if (aux.getData().equals(data)) return posicion;
            aux = aux.getNext();
            posicion++;
        }
        return -1;
    }

    public boolean contains(T data) {
        return obtenerPosicionNodo(data) != -1;
    }


    public void modificarNodo(int posicion, T nuevoDato) {
        if (!indiceValido(posicion)) throw new IndexOutOfBoundsException("Posición inválida: " + posicion);
        NodeSimple<T> aux = firstNode;
        for (int i = 0; i < posicion; i++) aux = aux.getNext();
        aux.setData(nuevoDato);
    }

    public T eliminarPrimero() {
        if (isEmpty()) throw new IllegalStateException("La lista está vacía");
        T data = firstNode.getData();
        firstNode = firstNode.getNext();
        if (firstNode == null) lastNode = null;
        size--;
        return data;
    }

    public T eliminarUltimo() {
        if (isEmpty()) throw new IllegalStateException("La lista está vacía");
        T data = lastNode.getData();
        if (size == 1) {
            firstNode = null;
            lastNode = null;
        } else {
            NodeSimple<T> aux = firstNode;
            while (aux.getNext() != lastNode) aux = aux.getNext();
            aux.setNext(null);
            lastNode = aux;
        }
        size--;
        return data;
    }

    public boolean eliminarValorNodo(T data) {
        if (isEmpty()) return false;
        if (firstNode.getData().equals(data)) {
            eliminarPrimero();
            return true;
        }
        NodeSimple<T> aux = firstNode;
        while (aux.getNext() != null) {
            if (aux.getNext().getData().equals(data)) {
                if (aux.getNext() == lastNode) lastNode = aux;
                aux.setNext(aux.getNext().getNext());
                size--;
                return true;
            }
            aux = aux.getNext();
        }
        return false;
    }

    public int eliminarSi(Predicate<T> condicion) { // Predicate: recibe una condición que se evalúa sobre cada elemento de tipo T
        int eliminados = 0;                         //Modifica la lista original
        while (!isEmpty() && condicion.test(firstNode.getData())) {
            eliminarPrimero();
            eliminados++;
        }
        NodeSimple<T> aux = firstNode;
        while (aux != null && aux.getNext() != null) {
            if (condicion.test(aux.getNext().getData())) {
                if (aux.getNext() == lastNode) lastNode = aux;
                aux.setNext(aux.getNext().getNext());
                size--;
                eliminados++;
            } else {
                aux = aux.getNext();
            }
        }
        return eliminados;
    }

    public T buscar(Predicate<T> condicion) { // Predicate: recibe una condición que se evalúa sobre cada elemento de tipo T
        for (NodeSimple<T> aux = firstNode; aux != null; aux = aux.getNext()) {
            if (condicion.test(aux.getData())) return aux.getData(); //No continúa buscando después de hallar el primer elemento
                                                                     // que cumpla la condición
        }
        return null;
    }

    public SimpleLinkedList<T> filtrar(Predicate<T> condicion) { // Predicate: recibe una condición que se evalúa sobre cada elemento de tipo T
        SimpleLinkedList<T> r = new SimpleLinkedList<>();        // Devuelve una lista nueva con todos los elementos que cumplan una condición
        for (NodeSimple<T> aux = firstNode; aux != null; aux = aux.getNext()) {
            if (condicion.test(aux.getData())) r.addLast(aux.getData());
        }
        return r;
    }

    public int contar(Predicate<T> condicion) {
        int c = 0;
        for (NodeSimple<T> aux = firstNode; aux != null; aux = aux.getNext()) {
            if (condicion.test(aux.getData())) c++;
        }
        return c;
    }

    /**
    *  Ordenamiento burbuja intercambiando datos
    *  O(n^2). Se usa solo en reportes pequeños.*/

    public void ordenarLista(Comparator<? super T> comparador) {
        if (size < 2) return;
        boolean huboCambio = true;
        while (huboCambio) {
            huboCambio = false;
            NodeSimple<T> actual = firstNode;
            while (actual.getNext() != null) {
                NodeSimple<T> siguiente = actual.getNext();
                if (comparador.compare(actual.getData(), siguiente.getData()) > 0) {
                    T temporal = actual.getData();
                    actual.setData(siguiente.getData());
                    siguiente.setData(temporal);
                    huboCambio = true;
                }
                actual = actual.getNext();
            }
        }
    }


    public SimpleLinkedList<T> primeros(int n) { //Copia los primeros elementos
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        for (NodeSimple<T> aux = firstNode; aux != null && r.getSize() < n; aux = aux.getNext()) r.addLast(aux.getData());
        return r;
    }

    public SimpleLinkedList<T> clonar() {
        SimpleLinkedList<T> copia = new SimpleLinkedList<>();
        for (NodeSimple<T> aux = firstNode; aux != null; aux = aux.getNext()) copia.addLast(aux.getData());
        return copia;
    }

    public void borrarLista() {
        firstNode = null;
        lastNode = null;
        size = 0;
    }

    public void clear() { borrarLista(); }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private NodeSimple<T> actual = firstNode;

            @Override
            public boolean hasNext() { return actual != null; }

            @Override
            public T next() {
                if (actual == null) throw new NoSuchElementException();
                T d = actual.getData();
                actual = actual.getNext();
                return d;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NodeSimple<T> aux = firstNode; aux != null; aux = aux.getNext()) sb.append(aux.getData()).append(" -> ");
        return sb.append("null").toString();
    }

}
