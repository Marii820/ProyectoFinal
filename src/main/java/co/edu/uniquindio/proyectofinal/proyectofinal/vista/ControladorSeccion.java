package co.edu.uniquindio.proyectofinal.proyectofinal.vista;

/** Controlador de una sección del menú lateral: además sabe volver a cargar sus datos. */
public interface ControladorSeccion extends Controlador {

    void refrescar();

    /** Para secciones con pestañas (ej: "recargar"). Por defecto no hace nada. */
    default void mostrarParte(String parte) { }
}
