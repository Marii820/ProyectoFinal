package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Cuenta
 * Lugar donde hay dinero de un usuario. Hay dos tipos:
 *  - CuentaPrincipal: el "Disponible" (una por usuario). Ahí llegan las recargas y las transferencias de otras personas.
 *  - Billetera: bolsillos que el usuario crea para separar su plata (ahorro, transporte, compras...).
 * Las transacciones mueven dinero entre cuentas.

 */
public abstract class Cuenta implements Serializable {

    private static final long serialVersionaUID = 1L;

    private final String codigo;
    private final Usuario propietario;
    private final LocalDateTime fechaCreacion;
    private double saldo;
    private final SimpleLinkedList<Transaccion> historial = new SimpleLinkedList<>();

    protected Cuenta(String codigo, Usuario propietario, LocalDateTime fechaCreacion) {
        this.codigo = codigo;
        this.propietario = propietario;
        this.fechaCreacion = fechaCreacion;
    }

    public abstract String getNombre();

    public abstract boolean isActiva();

    public abstract boolean esPrincipal();

    public String getCodigo() {
        return codigo;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public double getSaldo() {
        return saldo;
    }

    public SimpleLinkedList<Transaccion> getHistorial() {
        return historial;
    }

    @Override
    public String toString() {
        return getNombre();
    }

    public void recargar(double valor) {
        if (valor <= 0)
            throw new IllegalArgumentException("El valor debe ser mayor que 0");
        saldo += valor;
    }

    public boolean tieneSaldo(double valor) {
        return saldo + 1e-9 >= valor;
    }

    public void retirar(double valor) {
        if (valor <= 0)
            throw new IllegalArgumentException("El valor debe ser mayor que 0");
        if (!tieneSaldo(valor))
            throw new IllegalStateException("Saldo insuficiente en " + getNombre());
        saldo -= valor;
    }

    /* Lo mas reciente primero */
    public void registrarMovimiento(Transaccion t){
        historial.addFirst(t);
    }

}
