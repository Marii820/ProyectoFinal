package co.edu.uniquindio.proyectofinal.proyectofinal.vista;

import co.edu.uniquindio.proyectofinal.proyectofinal.logica.SistemaFintech;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.Usuario;

import java.util.function.Consumer;

/**
 * Lo que necesita cada controlador: el sistema, el navegador, el usuario que inició sesión
 * (null en el panel de administración), qué hacer después de un cambio y cómo ir a otra sección.
 */
public class Contexto {

    private final SistemaFintech sistema;
    private final Navegador navegador;
    private final Usuario usuario;
    private Runnable alCambiar = () -> { };
    private Consumer<String> irA = s -> { };

    public Contexto(SistemaFintech sistema, Navegador navegador, Usuario usuario) {
        this.sistema = sistema;
        this.navegador = navegador;
        this.usuario = usuario;
    }

    public SistemaFintech getSistema() { return sistema; }
    public Navegador getNavegador() { return navegador; }
    public Usuario getUsuario() { return usuario; }

    /** Refresca la pantalla actual y guarda los datos. */
    public void cambio() { alCambiar.run(); }

    public Runnable getAlCambiar() { return alCambiar; }
    public void setAlCambiar(Runnable alCambiar) { this.alCambiar = alCambiar; }

    /** Ir a otra sección del menú (ej: "mover#recargar"). */
    public void irA(String seccion) { irA.accept(seccion); }
    public void setIrA(Consumer<String> irA) { this.irA = irA; }
}
