package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/*
 Cola (FIFO) enlazada
usad aen notificaciones pendientes de despacho (se entregan en el orden en que se generaron) y el recorrido BFS del grafo y del árbol (amplitud).
 */
public class Cola<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private int size;
    private NodeSimple<T> firstNode;
    private NodeSimple<T> lastNode;

    public Cola() {
        this.size = 0;
        firstNode = null;
        lastNode = null;
    }

    public int getSize() { return size; }

    public boolean isEmpty() { return firstNode == null; }

    public void encolar(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);
        if (isEmpty()) {
            firstNode = nuevo;
        } else {
            lastNode.setNext(nuevo);
        }
        lastNode = nuevo;
        size++;
    }

    public T desencolar() {
        if (isEmpty()) throw new IllegalStateException("La cola está vacía");
        T data = firstNode.getData();
        firstNode = firstNode.getNext();
        if (firstNode == null) lastNode = null;
        size--;
        return data;
    }

    /** Primero de la cola sin sacarlo (null si está vacía). */
    public T peek() {
        return isEmpty() ? null : firstNode.getData();
    }

    public void invertirColaRecursivo() {
        if (isEmpty()) return;
        T elemento = desencolar();
        invertirColaRecursivo();
        encolar(elemento);
    }

    public Cola<T> clonar() {
        Cola<T> copia = new Cola<>();
        for (NodeSimple<T> actual = firstNode; actual != null; actual = actual.getNext()) copia.encolar(actual.getData());
        return copia;
    }

    public boolean sonIdenticas(Cola<T> otra) {
        if (otra == null || this.size != otra.size) return false;
        NodeSimple<T> a = this.firstNode;
        NodeSimple<T> b = otra.firstNode;
        while (a != null) {
            if (!Objects.equals(a.getData(), b.getData())) return false;
            a = a.getNext();
            b = b.getNext();
        }
        return true;
    }

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
}


