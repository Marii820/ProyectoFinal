package co.edu.uniquindio.proyectofinal.proyectofinal.logica;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.*;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.*;

import java.time.LocalDateTime;

public class SistemaFintech {

    private static final long serialVersionUID = 2L;
    public static final int VERSION_DATOS = 2;
    private final int versionDatos = VERSION_DATOS;

    public static final String ADMIN_USUARIO = "admin";
    public static final String ADMIN_CLAVE = "admin123";
    public static final double UMBRAL_SALDO_BAJO = 20_000;
    public static final int BONO_PROGRAMADA = 20;
    public static final int VENTANA_REVERSION_HORAS = 24;
    public static final int MAX_BILLETERAS = 8;

    public static final double PESOS_POR_UNIDAD = 10;

    private final TablaHash<String, Usuario> usuarios = new TablaHash<>();
    private final TablaHash<String, Usuario> usuariosPorCorreo = new TablaHash<>();
    private final TablaHash<String, Usuario> usuariosPorTelefono = new TablaHash<>();
    private final TablaHash<String, Cuenta> cuentas = new TablaHash<>();   // cuentas principales y billeteras por código
    private final TablaHash<String, Transaccion> transaccionesPorId = new TablaHash<>();
    private final ArbolBinario<EntradaRanking> rankingPuntos = new ArbolBinario<>();
    private final ArbolBinario<Transaccion> transaccionesPorValor = new ArbolBinario<>();
    private final ColaPrioridad<OperacionProgramada> programadas = new ColaPrioridad<>();
    private final Cola<Notificacion> notificacionesPendientes = new Cola<>();
    private final Grafo grafoUsuarios = new Grafo();
    private final Grafo grafoCuentas = new Grafo();
    private final SimpleLinkedList<Transaccion> transacciones = new SimpleLinkedList<>();
    private final SimpleLinkedList<Comportamiento> auditoria = new SimpleLinkedList<>();
    private final SimpleLinkedList<Beneficio> catalogoBeneficios = new SimpleLinkedList<>();

    private int seqTransaccion, seqBilletera, seqProgramada, seqNotificacion, seqAuditoria;
    private long notificacionesDespachadas;
    private long minutosSimulados; // reloj simulado (para probar las programadas sin esperar)

    public SistemaFintech() {
        catalogoBeneficios.addLast(new Beneficio("B1", "Café gratis", "Un café en tiendas aliadas", 150, NivelUsuario.BRONCE));
        catalogoBeneficios.addLast(new Beneficio("B2", "Cine 2x1", "Dos boletas por el precio de una", 400, NivelUsuario.BRONCE));
        catalogoBeneficios.addLast(new Beneficio("B3", "Bono de $20.000", "Bono para compras en comercios aliados", 800, NivelUsuario.PLATA));
        catalogoBeneficios.addLast(new Beneficio("B4", "Domicilios gratis", "Un mes de domicilios sin costo", 1500, NivelUsuario.ORO));
        catalogoBeneficios.addLast(new Beneficio("B5", "Asesoría financiera", "Sesión con un asesor de inversiones", 4000, NivelUsuario.PLATINO));
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now().plusMinutes(minutosSimulados);
    }

    public String avanzarReloj(long horas) {
        if (horas <= 0) throw new IllegalArgumentException("Las horas deben ser mayores que 0");
        minutosSimulados += horas * 60;
        return procesarProgramadas();
    }


}
