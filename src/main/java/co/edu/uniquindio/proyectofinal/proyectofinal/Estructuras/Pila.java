package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Pila<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID= 1L;

    private int size;
    private NodeSimple<T> top;

    public Pila(){
        this.size= 0;
        this.top= null;
    }

    public boolean isEmpty() { return top == null; }

    public int getSize() { return size; }

    public void push(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);
        if (!isEmpty()) nuevo.setNext(top);
        top = nuevo;
        size++;
    }

    public T pop() {
        if (isEmpty()) throw new IllegalStateException("La pila está vacía");
        T data = top.getData();
        top = top.getNext();
        size--;
        return data;
    }

    public T peek() {
        return isEmpty() ? null : top.getData();
    }

    public Pila<T> clonar() {
        Pila<T> aux = new Pila<>();
        Pila<T> copia = new Pila<>();
        for (NodeSimple<T> actual = top; actual != null; actual = actual.getNext()) aux.push(actual.getData());
        while (!aux.isEmpty()) copia.push(aux.pop());
        return copia;
    }

    public void addAll(Pila<T> otra) {
        if (otra == null || otra.isEmpty()) return;
        Pila<T> aux = new Pila<>();
        for (NodeSimple<T> actual = otra.top; actual != null; actual = actual.getNext()) aux.push(actual.getData());
        while (!aux.isEmpty()) push(aux.pop());
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private NodeSimple<T> actual = top;

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
