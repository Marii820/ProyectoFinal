package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

 // Usado en operaciones programadas (sale primero la de fecha/hora más próxima;
 // si  dos tienen la misma fecha, sale primero la del usuario de mayor nivel).

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class ColaPrioridad<T extends Comparable<T>> implements Iterable<T>, Serializable {

    private static final long serialVersionUID= 1L;

    private Object[] heap;
    private int size;

    public ColaPrioridad() {
        heap = new Object[16];
        size = 0;
    }

    public int getSize() { return size; }

    public boolean isEmpty() { return size == 0; }

    private T elemento(int i) { return (T) heap[i]; }

    public void encolar(T dato) {
        if (dato == null) throw new IllegalArgumentException("No se permiten nulos");
        if (size == heap.length) {
            Object[] nuevo = new Object[heap.length * 2];
            System.arraycopy(heap, 0, nuevo, 0, size);
            heap = nuevo;
        }
        heap[size] = dato;
        subir(size);
        size++;
    }

    public T desencolar() {
        if (isEmpty()) throw new IllegalStateException("La cola de prioridad está vacía");
        T raiz = elemento(0);
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) bajar(0);
        return raiz;
    }

    public T peek() {
        return isEmpty() ? null : elemento(0);
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (elemento(i).compareTo(elemento(padre)) >= 0) break;
            intercambiar(i, padre);
            i = padre;
        }
    }

    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1, der = 2 * i + 2, menor = i;
            if (izq < size && elemento(izq).compareTo(elemento(menor)) < 0) menor = izq;
            if (der < size && elemento(der).compareTo(elemento(menor)) < 0) menor = der;
            if (menor == i) return;
            intercambiar(i, menor);
            i = menor;
        }
    }

    private void intercambiar(int a, int b) {
        Object t = heap[a];
        heap[a] = heap[b];
        heap[b] = t;
    }

    public int eliminarSi(Predicate<T> condicion) {
        SimpleLinkedList<T> quedan = new SimpleLinkedList<>();
        int eliminados = 0;
        for (int i = 0; i < size; i++) {
            if (condicion.test(elemento(i))) eliminados++;
            else quedan.addLast(elemento(i));
        }
        heap = new Object[Math.max(16, heap.length)];
        size = 0;
        for (T t : quedan) encolar(t);
        return eliminados;
    }

    //Copia ordenada de menor a mayor (sin modificar la cola original).
    public SimpleLinkedList<T> enOrden() {
        ColaPrioridad<T> copia = new ColaPrioridad<>();
        for (int i = 0; i < size; i++) copia.encolar(elemento(i));
        SimpleLinkedList<T> r = new SimpleLinkedList<>();
        while (!copia.isEmpty()) r.addLast(copia.desencolar());
        return r;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() { return i < size; }

            @Override
            public T next() {
                if (i >= size) throw new NoSuchElementException();
                return elemento(i++);
            }
        };
    }





}
