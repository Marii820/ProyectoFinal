package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Cuenta
 */
public abstract class Cuenta implements Serializable {

    private static final long serialVersionaUID = 1L;

    private final String codigo;
    private final Usuario propietario;
    private final LocalDateTime fechaCreacion;
    private double saldo;
    private final SimpleLinkedList<Transaccion> historial = new SimpleLinkedList<>();


    protected Cuenta (String codigo, Usuario propietario, LocalDateTime fechaCreacion){
        this.codigo= codigo;
        this.propietario= propietario;
        this.fechaCreacion= fechaCreacion;
    }

    public void recargar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("El valor debe ser mayor que 0");
        saldo += valor;
    }

    public boolean tieneSaldo(double valor) {
        return saldo + 1e-9 >= valor;
    }

    public void retirar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("El valor debe ser mayor que 0");
        if (!tieneSaldo(valor)) throw new IllegalStateException("Saldo insuficiente en " + getNombre());
        saldo -= valor;
    }

}
