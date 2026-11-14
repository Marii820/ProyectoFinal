package co.edu.uniquindio.proyectofinal.proyectofinal.model;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.Pila;
import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.SimpleLinkedList;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Usuario implements Serializable {

    private static final long serialVersionUID = 2L;
    public static final int MAX_NOTIFICACIONES = 30;

    private final String id;
    private String nombre;
    private String telefono;
    private String correo;
    private String contrasenaHash;  // nunca se guarda la contraseña en texto plano
    private final LocalDateTime fechaRegistro;
    private int puntosAcumulados;       // histórico: define el nivel
    private int puntosDisponibles;      // lo que se puede gastar en beneficios
    private NivelUsuario nivel = NivelUsuario.BRONCE;

    private final CuentaPrincipal cuentaPrincipal;  // el "Disponible"
    private final SimpleLinkedList<Billetera> billeteras = new SimpleLinkedList<>();
    private final SimpleLinkedList<Transaccion> historial = new SimpleLinkedList<>();
    private final Pila<Transaccion> operacionesReversibles = new Pila<>();
    private final SimpleLinkedList<Beneficio> beneficiosCanjeados = new SimpleLinkedList<>();
    private final SimpleLinkedList<Notificacion> notificaciones = new SimpleLinkedList<>();


    public Usuario(String id, String nombre, String telefono, String correo, String contrasena, LocalDateTime fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasenaHash = hash(contrasena);
        this.fechaRegistro = fechaRegistro;
        this.cuentaPrincipal = new CuentaPrincipal(this, fechaRegistro);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public int getPuntosAcumulados() { return puntosAcumulados; }
    public int getPuntosDisponibles() { return puntosDisponibles; }
    public NivelUsuario getNivel() { return nivel; }
    public CuentaPrincipal getCuentaPrincipal() { return cuentaPrincipal; }
    public SimpleLinkedList<Billetera> getBilleteras() { return billeteras; }
    public SimpleLinkedList<Transaccion> getHistorial() { return historial; }
    public Pila<Transaccion> getOperacionesReversibles() { return operacionesReversibles; }
    public SimpleLinkedList<Beneficio> getBeneficiosCanjeados() { return beneficiosCanjeados; }
    public SimpleLinkedList<Notificacion> getNotificaciones() { return notificaciones; }

    public String getPrimerNombre() {
        String n = nombre == null ? "" : nombre.trim();
        int i = n.indexOf(' ');
        return i > 0 ? n.substring(0, i) : n;
    }


    private String hash(String contrasena) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest((id + ":" + contrasena).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }




    @Override
    public String toString() { return nombre + " (" + id + ")"; }

    @Override
    public boolean equals(Object o) { return o instanceof Usuario u && u.id.equals(id); }

    @Override
    public int hashCode() { return id.hashCode(); }


}
