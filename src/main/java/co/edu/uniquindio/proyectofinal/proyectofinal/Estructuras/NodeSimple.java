package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;

public class NodeSimple<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T data;
    private NodeSimple<T> next;

    public NodeSimple(T data) {
        this.data = data;
    }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public NodeSimple<T> getNext() { return next; }
    public void setNext(NodeSimple<T> next) { this.next = next; }

}
