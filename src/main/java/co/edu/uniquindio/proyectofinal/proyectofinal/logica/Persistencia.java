package co.edu.uniquindio.proyectofinal.proyectofinal.logica;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda y carga todo el sistema en un archivo (serialización de Java),
 * para que las cuentas y los movimientos no se pierdan al cerrar la aplicación.
 * El archivo queda en la carpeta del usuario: ~/billeteras_fintech.dat
 */

public final class Persistencia {

    public static final Path ARCHIVO = Path.of(System.getProperty("user.home"), "billeteras_fintech.dat");

    private Persistencia() { }

    public static void guardar(SistemaFintech sistema) {
        Path temporal = ARCHIVO.resolveSibling(ARCHIVO.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporal))) {
            out.writeObject(sistema);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar los datos: " + e.getMessage(), e);
        }
        try {
            Files.move(temporal, ARCHIVO, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar los datos: " + e.getMessage(), e);
        }
    }

    /** Carga los datos guardados. Si no hay archivo (o está dañado) crea un sistema con datos de ejemplo. */
    public static SistemaFintech cargar() {
        if (Files.exists(ARCHIVO)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(ARCHIVO))) {
                SistemaFintech s = (SistemaFintech) in.readObject();
                if (s.esValido()) return s;
                System.err.println("El archivo " + ARCHIVO + " es de una versión anterior.");
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                System.err.println("No se pudo leer " + ARCHIVO + " (" + e.getMessage() + ").");
            }
            respaldarArchivoViejo();
        }
        SistemaFintech s = new SistemaFintech();
        s.cargarDatosEjemplo();
        return s;
    }

    /** Renombra el archivo que no se pudo usar (por si acaso) y se empieza con datos de ejemplo. */
    private static void respaldarArchivoViejo() {
        try {
            Path respaldo = ARCHIVO.resolveSibling(ARCHIVO.getFileName() + ".viejo");
            Files.move(ARCHIVO, respaldo, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Se guardó una copia en " + respaldo + " y se empieza con datos de ejemplo.");
        } catch (IOException e) {
            System.err.println("No se pudo respaldar el archivo viejo: " + e.getMessage());
        }
    }

    /** Borra el archivo para empezar de cero. */
    public static void borrar() {
        try {
            Files.deleteIfExists(ARCHIVO);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar el archivo de datos", e);
        }
    }


}
