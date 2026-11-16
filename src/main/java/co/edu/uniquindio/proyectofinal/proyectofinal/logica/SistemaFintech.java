package co.edu.uniquindio.proyectofinal.proyectofinal.logica;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.*;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

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
    /* Se hace asi, poq se necesita esta tabla hash de config */

    private final TablaHash<String, Double> configuracion = new TablaHash<>();

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

    public Usuario registrarUsuario(String id, String nombre, String telefono, String correo, String contrasena) {
        id = limpiar(id);
        correo = limpiar(correo).toLowerCase();
        telefono = limpiar(telefono);
        if (!id.matches("\\d{5,12}")) throw new IllegalArgumentException("La cédula debe tener entre 5 y 12 dígitos");
        if (limpiar(nombre).length() < 3) throw new IllegalArgumentException("Escriba su nombre completo");
        if (!telefono.matches("\\d{10}")) throw new IllegalArgumentException("El celular debe tener 10 dígitos");
        if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[a-z]{2,}")) throw new IllegalArgumentException("El correo no es válido");
        validarContrasena(contrasena);
        if (usuarios.containsKey(id)) throw new IllegalArgumentException("Ya existe una cuenta con esa cédula");
        if (usuariosPorCorreo.containsKey(correo)) throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        if (usuariosPorTelefono.containsKey(telefono)) throw new IllegalArgumentException("Ya existe una cuenta con ese celular");

        Usuario u = new Usuario(id, limpiar(nombre), telefono, correo, contrasena, ahora());
        usuarios.put(id, u);
        usuariosPorCorreo.put(correo, u);
        usuariosPorTelefono.put(telefono, u);
        cuentas.put(u.getCuentaPrincipal().getCodigo(), u.getCuentaPrincipal());
        grafoCuentas.agregarVertice(u.getCuentaPrincipal().getCodigo());
        rankingPuntos.insertar(EntradaRanking.de(u));
        grafoUsuarios.agregarVertice(id);
        notificar(u, TipoNotificacion.BIENVENIDA, "¡Bienvenido(a), " + u.getPrimerNombre()
                + "! Recarga tu Disponible y crea billeteras para separar tu plata. Cada movimiento te da puntos.");
        despacharNotificaciones();
        return u;
    }

    /** Inicia sesión con cédula, correo o celular. */
    public Usuario iniciarSesion(String identificador, String contrasena) {
        Usuario u = buscarPorIdentificador(identificador);
        if (u == null || !u.verificarContrasena(contrasena))
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        procesarProgramadas();
        return u;
    }

    /** Busca por cédula, correo o celular (las tres en tabla hash: O(1)). */
    public Usuario buscarPorIdentificador(String identificador) {
        String idf = limpiar(identificador);
        if (idf.isEmpty()) return null;
        if (idf.contains("@")) return usuariosPorCorreo.get(idf.toLowerCase());
        Usuario u = usuarios.get(idf);
        return u != null ? u : usuariosPorTelefono.get(idf);
    }

    /**
     * Destinatario de un envío: se busca la PERSONA (no una billetera) por cédula, celular o correo.
     * El dinero siempre le llega a su Disponible.
     */
    public Usuario buscarDestinatario(String idUsuarioQueEnvia, String identificador) {
        Usuario d = buscarPorIdentificador(identificador);
        if (d == null) throw new IllegalArgumentException("No encontramos ninguna cuenta con \"" + limpiar(identificador) + "\"");
        if (d.getId().equals(idUsuarioQueEnvia))
            throw new IllegalArgumentException("Ese es usted. Para mover plata entre sus billeteras use \"Entre mis cuentas\"");
        return d;
    }

    /** Nombre para confirmar un envío sin mostrar datos de más (ej: "Laura G."). */
    public static String nombreEnmascarado(Usuario u) {
        String[] partes = u.getNombre().trim().split("\\s+");
        return partes.length > 1 ? partes[0] + " " + partes[1].charAt(0) + "." : partes[0];
    }

    public boolean esAdministrador(String usuario, String clave) {
        return ADMIN_USUARIO.equals(limpiar(usuario)) && ADMIN_CLAVE.equals(clave);
    }

    /** Búsqueda O(1) por cédula. */
    public Usuario buscarUsuario(String id) {
        return id == null ? null : usuarios.get(id.trim());
    }

    private Usuario obtenerUsuario(String id) {
        Usuario u = buscarUsuario(id);
        if (u == null) throw new IllegalArgumentException("No existe el usuario " + id);
        return u;
    }

    public void modificarUsuario(String id, String nombre, String telefono, String correo) {
        Usuario u = obtenerUsuario(id);
        correo = limpiar(correo).toLowerCase();
        telefono = limpiar(telefono);
        if (limpiar(nombre).length() < 3) throw new IllegalArgumentException("Escriba su nombre completo");
        if (!telefono.matches("\\d{10}")) throw new IllegalArgumentException("El celular debe tener 10 dígitos");
        if (!correo.matches("[^@\\s]+@[^@\\s]+\\.[a-z]{2,}")) throw new IllegalArgumentException("El correo no es válido");
        Usuario otro = usuariosPorCorreo.get(correo);
        if (otro != null && otro != u) throw new IllegalArgumentException("Ese correo ya lo usa otra cuenta");
        otro = usuariosPorTelefono.get(telefono);
        if (otro != null && otro != u) throw new IllegalArgumentException("Ese celular ya lo usa otra cuenta");
        usuariosPorCorreo.remove(u.getCorreo());
        usuariosPorTelefono.remove(u.getTelefono());
        u.setNombre(limpiar(nombre));
        u.setTelefono(telefono);
        u.setCorreo(correo);
        usuariosPorCorreo.put(correo, u);
        usuariosPorTelefono.put(telefono, u);
    }

    public void cambiarContrasena(String id, String actual, String nueva) {
        Usuario u = obtenerUsuario(id);
        if (!u.verificarContrasena(actual)) throw new IllegalArgumentException("La contraseña actual no es correcta");
        validarContrasena(nueva);
        u.cambiarContrasena(nueva);
    }

    /** El usuario cierra su cuenta (debe confirmar con su contraseña). */
    public void eliminarCuenta(String id, String contrasena) {
        Usuario u = obtenerUsuario(id);
        if (!u.verificarContrasena(contrasena)) throw new IllegalArgumentException("Contraseña incorrecta");
        eliminarUsuario(id);
    }

    /** Elimina el usuario. Solo si no le queda dinero ni en el Disponible ni en sus billeteras. */
    public void eliminarUsuario(String id) {
        Usuario u = obtenerUsuario(id);
        if (u.saldoTotal() > 0)
            throw new IllegalStateException("La cuenta todavía tiene $" + fmt(u.saldoTotal()) + ". Retire o envíe el dinero primero.");
        for (Cuenta c : u.cuentas()) {
            cuentas.remove(c.getCodigo());
            grafoCuentas.eliminarVertice(c.getCodigo());
        }
        programadas.eliminarSi(op -> op.getUsuario().equals(u)
                || (op.getDestino() != null && op.getDestino().getPropietario().equals(u)));
        rankingPuntos.eliminar(EntradaRanking.de(u));
        grafoUsuarios.eliminarVertice(u.getId());
        usuariosPorCorreo.remove(u.getCorreo());
        usuariosPorTelefono.remove(u.getTelefono());
        usuarios.remove(u.getId());
    }

    public SimpleLinkedList<Usuario> listarUsuarios() {
        SimpleLinkedList<Usuario> l = usuarios.valores();
        l.ordenarLista((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        return l;
    }

    private void validarContrasena(String c) {
        if (c == null || c.length() < 6) throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres");
    }

    // =====================================================================
    // RF2: billeteras (bolsillos) y cuentas
    // =====================================================================

    public Billetera crearBilletera(String idUsuario, String nombre, TipoBilletera tipo) {
        return crearBilletera(idUsuario, nombre, tipo, 0);
    }


    public Billetera crearBilletera(String idUsuario, String nombre, TipoBilletera tipo, double meta) {
        Usuario u = obtenerUsuario(idUsuario);
        if (limpiar(nombre).isEmpty()) throw new IllegalArgumentException("Pongale un nombre a la billetera");
        if (tipo == null) throw new IllegalArgumentException("Seleccione el tipo de billetera");
        int max = (int) config("maxBilleteras");
        if (u.getBilleteras().getSize() >= MAX_BILLETERAS)
            throw new IllegalStateException("Maximo " + MAX_BILLETERAS + " billeteras por usuario");
        String nom = limpiar(nombre);
        if (nom.equalsIgnoreCase("Disponible")) throw new IllegalArgumentException("Ese nombre está reservado para la cuenta principal");
        if (u.getBilleteras().buscar(b -> b.getNombre().equalsIgnoreCase(nom)) != null)
            throw new IllegalArgumentException("Ya tiene una billetera llamada \"" + nom + "\"");
        if (meta < 0) throw new IllegalArgumentException("La meta no puede ser negativa");
        Billetera b = new Billetera(String.format("BD%06d", ++seqBilletera), nom, tipo, u, ahora());
        b.setMeta(meta);
        cuentas.put(b.getCodigo(), b);
        u.agregarBilletera(b);
        grafoCuentas.agregarVertice(b.getCodigo());
        return b;
    }

    /** Búsqueda O(1) por código (cuenta principal o billetera). */
    public Cuenta buscarCuenta(String codigo) {
        return codigo == null ? null : cuentas.get(codigo.trim().toUpperCase());
    }

    /** Cuenta del usuario; si el código viene vacío se usa su Disponible. */
    private Cuenta cuentaPropia(String idUsuario, String codigo) {
        Usuario u = obtenerUsuario(idUsuario);
        if (limpiar(codigo).isEmpty()) return u.getCuentaPrincipal();
        Cuenta c = u.buscarCuenta(codigo);
        if (c == null) throw new IllegalArgumentException("Esa cuenta no es suya");
        return c;
    }

    private Billetera billeteraPropia(String idUsuario, String codigo) {
        Cuenta c = cuentaPropia(idUsuario, codigo);
        if (!(c instanceof Billetera b)) throw new IllegalArgumentException("Seleccione una billetera (el Disponible no se puede modificar)");
        return b;
    }

    public void cambiarEstadoBilletera(String idUsuario, String codigo, boolean activa) {
        billeteraPropia(idUsuario, codigo).setActiva(activa);
    }

    public void renombrarBilletera(String idUsuario, String codigo, String nombre) {
        String nom = limpiar(nombre);
        if (nom.isEmpty()) throw new IllegalArgumentException("El nombre no puede quedar vacío");
        if (nom.equalsIgnoreCase("Disponible")) throw new IllegalArgumentException("Ese nombre está reservado para la cuenta principal");
        billeteraPropia(idUsuario, codigo).setNombre(nom);
    }

    public void cambiarMetaBilletera(String idUsuario, String codigo, double meta) {
        billeteraPropia(idUsuario, codigo).setMeta(meta);
    }

     /** Operaciones programadas que le meten plata a esa cuenta (ej: ahorro programado de un bolsillo). */
    public SimpleLinkedList<OperacionProgramada> programadasHacia(String codigo) {
        return programadas.enOrden().filtrar(op -> op.getDestino() != null && op.getDestino().getCodigo().equals(codigo));
    }

    /** Pasa todo el saldo de la billetera al Disponible. */
    public Transaccion vaciarBilletera(String idUsuario, String codigo) {
        Billetera b = billeteraPropia(idUsuario, codigo);
        if (b.getSaldo() <= 0) throw new IllegalStateException("La billetera ya está en $0");
        return moverEntreCuentas(idUsuario, b.getCodigo(), null, b.getSaldo(), "Vaciar " + b.getNombre());
    }

    public void eliminarBilletera(String idUsuario, String codigo) {
        Usuario u = obtenerUsuario(idUsuario);
        Billetera b = billeteraPropia(idUsuario, codigo);
        if (b.getSaldo() > 0) throw new IllegalStateException("La billetera aún tiene $" + fmt(b.getSaldo()) + ". Pásela al Disponible antes de eliminarla.");
        programadas.eliminarSi(op -> op.getOrigen() == b || op.getDestino() == b);
        u.getBilleteras().eliminarValorNodo(b);
        cuentas.remove(b.getCodigo());
        grafoCuentas.eliminarVertice(b.getCodigo());
    }

    public double consultarSaldo(String codigo) {
        Cuenta c = buscarCuenta(codigo);
        if (c == null) throw new IllegalArgumentException("No existe la cuenta " + codigo);
        return c.getSaldo();
    }


    // =====================================================================
    // 4.2 / RF3: operaciones
    // =====================================================================

    /** Recarga desde un medio externo (PSE, tarjeta...). Por defecto al Disponible. */
    public Transaccion recargar(String idUsuario, String codigoCuenta, double valor, String descripcion) {
        Cuenta c = cuentaPropia(idUsuario, codigoCuenta);
        Transaccion t = new Transaccion(nuevoIdTx(), ahora(), TipoTransaccion.RECARGA, valor, null, c);
        t.setDescripcion(descripcion);
        return aplicarOFallar(t);
    }

    /** Retiro (cajero / corresponsal). Por defecto desde el Disponible. */
    public Transaccion retirar(String idUsuario, String codigoCuenta, double valor, String descripcion) {
        Cuenta c = cuentaPropia(idUsuario, codigoCuenta);
        Transaccion t = new Transaccion(nuevoIdTx(), ahora(), TipoTransaccion.RETIRO, valor, c, null);
        t.setDescripcion(descripcion);
        return aplicarOFallar(t);
    }

    /** Mover plata entre el Disponible y las billeteras del mismo usuario (código vacío = Disponible). */
    public Transaccion moverEntreCuentas(String idUsuario, String origen, String destino, double valor, String descripcion) {
        Cuenta co = cuentaPropia(idUsuario, origen), cd = cuentaPropia(idUsuario, destino);
        if (co == cd) throw new IllegalArgumentException("El origen y el destino no pueden ser la misma cuenta");
        Transaccion t = new Transaccion(nuevoIdTx(), ahora(), TipoTransaccion.TRANSFERENCIA_PROPIA, valor, co, cd);
        t.setDescripcion(descripcion);
        return aplicarOFallar(t);
    }

    /**
     * Envío a otra persona: se identifica por cédula, celular o correo y le llega a su Disponible.
     * Sale del Disponible del que envía (o de la billetera que escoja).
     */
    public Transaccion enviarAPersona(String idUsuario, String codigoOrigen, String destinatario, double valor, String descripcion) {
        Cuenta co = cuentaPropia(idUsuario, codigoOrigen);
        Usuario d = buscarDestinatario(idUsuario, destinatario);
        Transaccion t = new Transaccion(nuevoIdTx(), ahora(), TipoTransaccion.TRANSFERENCIA_EXTERNA, valor, co, d.getCuentaPrincipal());
        t.setDescripcion(descripcion);
        return aplicarOFallar(t);
    }

    /** Comisión que se cobraría (solo retiros y envíos a otras personas; depende del nivel). */
    public double calcularComision(Usuario u, TipoTransaccion tipo, double valor, boolean destinoOtraPersona) {
        boolean cobra = tipo == TipoTransaccion.RETIRO || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA
                || (tipo == TipoTransaccion.PAGO_PROGRAMADO && destinoOtraPersona);
        return cobra ? Math.round(valor * u.getNivel().getComision()) : 0;
    }

    private Transaccion aplicarOFallar(Transaccion t) {
        aplicar(t);
        despacharNotificaciones();
        if (t.getEstado() == EstadoTransaccion.RECHAZADA) throw new IllegalStateException(t.getDetalle());
        return t;
    }

    /**
     * Aplica una transacción: valida, mueve el dinero y actualiza TODO
     * (historiales, tabla hash, árbol por valor, pila, puntos, ranking, grafos, alertas y auditoría).
     * Si algo falla queda RECHAZADA y también se registra.
     */
    private void aplicar(Transaccion t) {
        Usuario actor = t.getUsuario();
        Cuenta bo = t.getOrigen(), bd = t.getDestino();
        boolean otraPersona = t.esEntrePersonas();
        double saldoAntes = bo == null ? 0 : bo.getSaldo();
        try {
            if (Double.isNaN(t.getValor()) || t.getValor() <= 0) throw new IllegalArgumentException("El valor debe ser mayor que $0");
            if (t.getValor() > actor.getNivel().getLimiteTransaccion())
                throw new IllegalStateException("Supera su límite por operación como nivel " + actor.getNivel()
                        + " ($" + fmt(actor.getNivel().getLimiteTransaccion()) + ")");
            if (bo != null && !bo.isActiva()) throw new IllegalStateException("La billetera " + bo.getNombre() + " está bloqueada");
            if (bd != null && !bd.isActiva()) throw new IllegalStateException("La billetera " + bd.getNombre() + " está bloqueada");
            if (bo != null && !cuentas.containsKey(bo.getCodigo())) throw new IllegalStateException("La cuenta de origen ya no existe");
            if (bd != null && !cuentas.containsKey(bd.getCodigo())) throw new IllegalStateException("La cuenta de destino ya no existe");
            double comision = calcularComision(actor, t.getTipo(), t.getValor(), otraPersona);
            if (bo != null && !bo.tieneSaldo(t.getValor() + comision))
                throw new IllegalStateException("Saldo insuficiente en " + bo.getNombre() + ": tiene $" + fmt(bo.getSaldo())
                        + " y necesita $" + fmt(t.getValor() + comision) + (comision > 0 ? " (incluye comisión de $" + fmt(comision) + ")" : ""));
            t.setComision(comision);
            if (bo != null) bo.retirar(t.getValor() + comision);
            if (bd != null) bd.recargar(t.getValor());
            t.setEstado(EstadoTransaccion.EXITOSA);
        } catch (RuntimeException e) {
            t.setEstado(EstadoTransaccion.RECHAZADA);
            t.setDetalle(e.getMessage());
            registrar(t);
            notificar(actor, TipoNotificacion.RECHAZADA, (t.isProgramada() ? "Tu operación programada " : "La operación ")
                    + t.getId() + " fue rechazada: " + e.getMessage());
            return;
        }

        registrar(t);
        transaccionesPorValor.insertar(t);
        actor.getOperacionesReversibles().push(t);

        int puntos = calcularPuntos(t);
        t.setPuntosGenerados(puntos);
        actualizarPuntos(actor, puntos);

        if (bo != null && bd != null) grafoCuentas.agregarArista(bo.getCodigo(), bd.getCodigo(), t.getValor());
        if (otraPersona) {
            grafoUsuarios.agregarArista(actor.getId(), bd.getPropietario().getId(), t.getValor());
            notificar(bd.getPropietario(), TipoNotificacion.DINERO_RECIBIDO, actor.getPrimerNombre() + " te envió $"
                    + fmt(t.getValor()) + " a tu Disponible" + (t.getDescripcion().isBlank() ? "" : " (" + t.getDescripcion() + ")"));
        }
        if (bo != null && saldoAntes >= UMBRAL_SALDO_BAJO && bo.getSaldo() < UMBRAL_SALDO_BAJO) {
            notificar(actor, TipoNotificacion.SALDO_BAJO, "Tu " + (bo.esPrincipal() ? "Disponible" : "billetera " + bo.getNombre())
                    + " quedó con saldo bajo: $" + fmt(bo.getSaldo()));
        }
        if (t.isProgramada()) {
            notificar(actor, TipoNotificacion.PROGRAMADA_EJECUTADA, "Se ejecutó: " + t.getDescripcion()
                    + " por $" + fmt(t.getValor()) + " (+" + puntos + " pts, incluye bono de " + BONO_PROGRAMADA + ")");
        }
        revisarDuplicada(t);
        detectarInusual(t);
    }

    private void registrar(Transaccion t) {
        transacciones.addFirst(t);
        transaccionesPorId.put(t.getId(), t);
        Usuario actor = t.getUsuario();
        actor.registrarTransaccion(t);
        if (t.getOrigen() != null) t.getOrigen().registrarMovimiento(t);
        if (t.getDestino() != null) {
            t.getDestino().registrarMovimiento(t);
            Usuario receptor = t.getDestino().getPropietario();
            if (receptor != actor) receptor.registrarTransaccion(t);
        }
    }

    public Transaccion buscarTransaccion(String id) {
        return id == null ? null : transaccionesPorId.get(id.trim().toUpperCase());
    }

    /** Política 4.4: puntos por cada 100 según el tipo + bono del nivel + bono si fue programada. */
    public int calcularPuntos(Transaccion t) {
        int base = (int) (t.getValor() / (100 * PESOS_POR_UNIDAD)) * t.getTipo().getPuntosPor100();
        int conBono = (int) Math.round(base * (1 + t.getUsuario().getNivel().getBonoPuntos()));
        return t.isProgramada() ? conBono + BONO_PROGRAMADA : conBono;
    }

    /** Cambia los puntos sin dañar el árbol: se saca la entrada vieja, se actualiza y se mete la nueva. */
    private void actualizarPuntos(Usuario u, int delta) {
        rankingPuntos.eliminar(EntradaRanking.de(u));
        NivelUsuario antes = u.getNivel();
        if (delta >= 0) u.agregarPuntos(delta); else u.descontarPuntos(-delta);
        rankingPuntos.insertar(EntradaRanking.de(u));
        if (u.getNivel().ordinal() > antes.ordinal()) {
            notificar(u, TipoNotificacion.ASCENSO_NIVEL, "¡Subiste a nivel " + u.getNivel() + "! Nuevas ventajas: " + u.getNivel().ventajas());
        } else if (u.getNivel().ordinal() < antes.ordinal()) {
            notificar(u, TipoNotificacion.REVERSION, "Por la reversión tu nivel quedó en " + u.getNivel());
        }
    }

    /** Operación duplicada: mismo origen, destino y valor en menos de 2 minutos. */
    private void revisarDuplicada(Transaccion t) {
        for (Transaccion x : t.getUsuario().getHistorial()) {
            if (x == t || x.getEstado() != EstadoTransaccion.EXITOSA || x.getUsuario() != t.getUsuario()) continue;
            long seg = Duration.between(x.getFecha(), t.getFecha()).getSeconds();
            if (seg < 0 || seg > 120) continue;
            if (x.getValor() == t.getValor() && x.getOrigen() == t.getOrigen()
                    && x.getDestino() == t.getDestino()) {
                t.setDetalle("Posible duplicada de " + x.getId());
                notificar(t.getUsuario(), TipoNotificacion.POSIBLE_DUPLICADA, "La operación " + t.getId()
                        + " parece repetida (igual a " + x.getId() + "). Si fue un error, puedes revertirla en Movimientos.");
                return;
            }
        }
    }

    // =====================================================================
    // 4.6 / RF9: reversión con la pila
    // =====================================================================

    /** Deshace la última operación del usuario (tope de la pila). */
    public Transaccion deshacerUltima(String idUsuario) {
        Usuario u = obtenerUsuario(idUsuario);
        Transaccion tope = u.getOperacionesReversibles().peek();
        if (tope == null) throw new IllegalStateException("No tiene operaciones para deshacer");
        return revertirOperacion(idUsuario, tope.getId());
    }

    /**
     * Revierte una operación específica usando la pila: se sacan las de encima a una pila auxiliar,
     * se revierte la buscada y se vuelven a poner las demás en el mismo orden.
     */
    public Transaccion revertirOperacion(String idUsuario, String idTransaccion) {
        Usuario u = obtenerUsuario(idUsuario);
        Pila<Transaccion> pila = u.getOperacionesReversibles();
        Pila<Transaccion> aux = new Pila<>();
        Transaccion objetivo = null;
        while (!pila.isEmpty()) {
            Transaccion t = pila.pop();
            if (t.getId().equals(idTransaccion)) { objetivo = t; break; }
            aux.push(t);
        }
        try {
            if (objetivo == null) throw new IllegalStateException("Esa operación no está en su historial de deshacer (ya se revirtió o no es suya)");
            validarReversion(objetivo);
            aplicarReversion(u, objetivo);
            return objetivo;
        } catch (RuntimeException e) {
            if (objetivo != null) pila.push(objetivo); // no se pudo: vuelve a su lugar
            throw e;
        } finally {
            while (!aux.isEmpty()) pila.push(aux.pop());
            despacharNotificaciones();
        }
    }

    private void validarReversion(Transaccion t) {
        if (!t.esReversible()) throw new IllegalStateException("La operación " + t.getId() + " no se puede revertir (" + t.getEstado() + ")");
        if (Duration.between(t.getFecha(), ahora()).toHours() >= VENTANA_REVERSION_HORAS)
            throw new IllegalStateException("Solo se pueden revertir operaciones de las últimas " + VENTANA_REVERSION_HORAS + " horas");
        if ((t.getOrigen() != null && !cuentas.containsKey(t.getOrigen().getCodigo()))
                || (t.getDestino() != null && !cuentas.containsKey(t.getDestino().getCodigo())))
            throw new IllegalStateException("No se puede revertir: una de las cuentas ya fue eliminada");
        Cuenta bd = t.getDestino();
        if (bd != null && !bd.tieneSaldo(t.getValor()))
            throw new IllegalStateException("No se puede revertir: la cuenta destino ya no tiene los $" + fmt(t.getValor()));
    }

    /** Actualiza saldo, historial (estado) y puntos (8: recalcular puntos al revertir). */
    private void aplicarReversion(Usuario u, Transaccion t) {
        Cuenta bo = t.getOrigen(), bd = t.getDestino();
        if (bd != null) bd.retirar(t.getValor());
        if (bo != null) bo.recargar(t.getValor() + t.getComision());
        t.setEstado(EstadoTransaccion.REVERTIDA);
        t.setDetalle("Revertida el " + ahora().toLocalDate() + (t.getComision() > 0 ? " (se devolvió la comisión)" : ""));
        transaccionesPorValor.eliminar(t);
        actualizarPuntos(u, -t.getPuntosGenerados());
        if (bo != null && bd != null) grafoCuentas.quitarArista(bo.getCodigo(), bd.getCodigo(), t.getValor());
        if (bo != null && bd != null && bo.getPropietario() != bd.getPropietario()) {
            grafoUsuarios.quitarArista(u.getId(), bd.getPropietario().getId(), t.getValor());
            notificar(bd.getPropietario(), TipoNotificacion.REVERSION, "Se revirtió una transferencia de " + u.getPrimerNombre()
                    + " por $" + fmt(t.getValor()) + " (se descontó de tu " + bd.getNombre() + ")");
        }
        notificar(u, TipoNotificacion.REVERSION, "Se revirtió " + t.getTipo().getEtiqueta().toLowerCase() + " " + t.getId()
                + " por $" + fmt(t.getValor()) + ". Se descontaron " + t.getPuntosGenerados() + " puntos.");
    }

    // =====================================================================
    // 4.3 / RF5-RF6: operaciones programadas con la cola de prioridad
    // =====================================================================

    /**
     * Programa una operación futura.
     *  - RECARGA: destino = cuenta propia (vacío = Disponible)
     *  - RETIRO: origen = cuenta propia (vacío = Disponible)
     *  - TRANSFERENCIA_PROPIA: origen y destino = cuentas propias
     *  - TRANSFERENCIA_EXTERNA / PAGO_PROGRAMADO: destino = cédula, celular o correo de la otra persona
     */
    public OperacionProgramada programarOperacion(String idUsuario, TipoTransaccion tipo, String origen, String destino,
                                                  double valor, LocalDateTime fecha, Frecuencia frecuencia, String descripcion) {
        Usuario u = obtenerUsuario(idUsuario);
        if (tipo == null || frecuencia == null) throw new IllegalArgumentException("Seleccione el tipo y la frecuencia");
        if (valor <= 0) throw new IllegalArgumentException("El valor debe ser mayor que $0");
        if (fecha == null || !fecha.isAfter(ahora())) throw new IllegalArgumentException("La fecha y hora deben ser futuras");
        Cuenta co = null, cd = null;
        switch (tipo) {
            case RECARGA -> cd = cuentaPropia(idUsuario, destino);
            case RETIRO -> co = cuentaPropia(idUsuario, origen);
            case TRANSFERENCIA_PROPIA -> { co = cuentaPropia(idUsuario, origen); cd = cuentaPropia(idUsuario, destino); }
            case TRANSFERENCIA_EXTERNA, PAGO_PROGRAMADO -> {
                co = cuentaPropia(idUsuario, origen);
                cd = buscarDestinatario(idUsuario, destino).getCuentaPrincipal();
            }
        }
        if (co != null && co == cd) throw new IllegalArgumentException("El origen y el destino no pueden ser la misma cuenta");
        OperacionProgramada op = new OperacionProgramada(String.format("P%04d", ++seqProgramada), fecha, tipo, valor,
                co, cd, frecuencia, limpiar(descripcion), u.getNivel().ordinal());
        programadas.encolar(op);
        revisarProximas();
        despacharNotificaciones();
        return op;
    }

    public void cancelarProgramada(String idUsuario, String idOperacion) {
        int n = programadas.eliminarSi(op -> op.getId().equals(idOperacion) && op.getUsuario().getId().equals(idUsuario));
        if (n == 0) throw new IllegalArgumentException("No se encontró esa operación programada");
    }

    /** Programadas del usuario, en el orden en que se van a ejecutar. */
    public SimpleLinkedList<OperacionProgramada> programadasDeUsuario(String idUsuario) {
        return programadas.enOrden().filtrar(op -> op.getUsuario().getId().equals(idUsuario));
    }

    public SimpleLinkedList<OperacionProgramada> todasLasProgramadas() {
        return programadas.enOrden();
    }

    /**
     * Ejecuta en orden todas las operaciones cuya fecha ya llegó (las va sacando de la cola de prioridad).
     * Las recurrentes se reprograman y vuelven a entrar a la cola.
     */
    public String procesarProgramadas() {
        LocalDateTime now = ahora();
        int exitosas = 0, rechazadas = 0;
        while (!programadas.isEmpty() && !programadas.peek().getFechaEjecucion().isAfter(now)) {
            OperacionProgramada op = programadas.desencolar();
            Transaccion t = op.ejecutar(nuevoIdTx());
            aplicar(t);
            boolean ok = t.getEstado() == EstadoTransaccion.EXITOSA;
            op.registrarResultado(ok);
            if (ok) exitosas++; else rechazadas++;
            if (op.getFrecuencia() != Frecuencia.UNICA) {
                op.reprogramar();
                programadas.encolar(op);
            }
        }
        revisarProximas();
        despacharNotificaciones();
        return "Programadas ejecutadas: " + exitosas + " exitosas y " + rechazadas + " rechazadas. Quedan " + programadas.getSize() + " en la cola.";
    }

    /** Alerta de las que se ejecutan en las próximas 24 horas. */
    private void revisarProximas() {
        LocalDateTime limite = ahora().plusHours(24);
        for (OperacionProgramada op : programadas) {
            if (!op.isAvisada() && !op.getFechaEjecucion().isAfter(limite)) {
                op.setAvisada(true);
                notificar(op.getUsuario(), TipoNotificacion.PROGRAMADA_PROXIMA, "Mañana o antes se ejecuta "
                        + (op.getDescripcion().isBlank() ? op.getTipo().getEtiqueta().toLowerCase() : op.getDescripcion())
                        + " por $" + fmt(op.getValor()) + " (" + op.getFechaEjecucion().toLocalDate() + " "
                        + op.getFechaEjecucion().toLocalTime().withSecond(0).withNano(0) + ")");
            }
        }
    }

    // =====================================================================
    // 4.4 / 4.5: beneficios y ranking (árbol)
    // =====================================================================

    public void canjearBeneficio(String idUsuario, String idBeneficio) {
        Usuario u = obtenerUsuario(idUsuario);
        Beneficio b = catalogoBeneficios.buscar(x -> x.getId().equals(idBeneficio));
        if (b == null) throw new IllegalArgumentException("Seleccione un beneficio");
        if (u.getNivel().ordinal() < b.getNivelMinimo().ordinal())
            throw new IllegalStateException("Este beneficio es desde nivel " + b.getNivelMinimo());
        if (!b.puedeCanjear(u))
            throw new IllegalStateException("Te faltan " + (b.getCostoPuntos() - u.getPuntosDisponibles()) + " puntos para este beneficio");
        u.gastarPuntos(b.getCostoPuntos());
        u.getBeneficiosCanjeados().addFirst(b);
        notificar(u, TipoNotificacion.CANJE_BENEFICIO, "Canjeaste \"" + b.getNombre() + "\" por " + b.getCostoPuntos() + " puntos");
        despacharNotificaciones();
    }

    /** Para las pruebas: intenta canjear y dice si se pudo. */
    public boolean canjearBeneficioPosible(String idUsuario, String idBeneficio) {
        try { canjearBeneficio(idUsuario, idBeneficio); return true; } catch (RuntimeException e) { return false; }
    }

    /** Ranking de mayor a menor puntos (inorden inverso del árbol). */
    public SimpleLinkedList<EntradaRanking> rankingUsuarios() {
        return rankingPuntos.deMayorAMenor();
    }

    /** Búsqueda por rango de puntos en el árbol (con poda). */
    public SimpleLinkedList<EntradaRanking> usuariosPorRangoPuntos(int min, int max) {
        if (min > max) throw new IllegalArgumentException("El mínimo no puede ser mayor que el máximo");
        return rankingPuntos.buscarRango(EntradaRanking.cotaInferior(min), EntradaRanking.cotaSuperior(max));
    }

    /** Clasificación por nivel = rango de puntos del nivel. */
    public SimpleLinkedList<EntradaRanking> usuariosPorNivel(NivelUsuario nivel) {
        return usuariosPorRangoPuntos(nivel.getPuntosMin(), nivel.getPuntosMax());
    }

    /** Posición del usuario en el ranking (1 = el de más puntos). */
    public int posicionEnRanking(String idUsuario) {
        int pos = 1;
        for (EntradaRanking e : rankingPuntos.deMayorAMenor()) {
            if (e.getIdUsuario().equals(idUsuario)) return pos;
            pos++;
        }
        return -1;
    }

    // =====================================================================
    // 4.9: comportamiento inusual
    // =====================================================================

    /**
     * Revisa la transacción contra el historial del usuario. Cada patrón suma riesgo:
     * 1-2 = MEDIO, 3 o más = ALTO. Si hay riesgo: se marca la transacción, se guarda en auditoría
     * y se manda una alerta de seguridad.
     */


    public Comportamiento detectarInusual(Transaccion t) {
        Usuario u = t.getUsuario();
        LocalDateTime f = t.getFecha();
        boolean saliente = t.getTipo() != TipoTransaccion.RECARGA;
        int riesgo = 0;
        SimpleLinkedList<String> patrones = new SimpleLinkedList<>();
        SimpleLinkedList<String> detalles = new SimpleLinkedList<>();

        int transferencias5min = 0, mismoDestino30min = 0, ultimaHora = 0, ultimos7dias = 0;
        int salientesPrevias = 0, nocturnasPrevias = 0, previas = 0;
        double sumaSalientes = 0;
        TablaHash<String, Boolean> origenes10min = new TablaHash<>();

        for (Transaccion x : u.getHistorial()) {
            if (x.getUsuario() != u || x.getEstado() != EstadoTransaccion.EXITOSA) continue;
            long min = Duration.between(x.getFecha(), f).toMinutes();
            if (min < 0) continue;
            if (x.getTipo().esTransferencia() && min <= 5) transferencias5min++;
            if (min <= 60) ultimaHora++;
            else if (min <= 7 * 24 * 60) ultimos7dias++;
            if (min <= 10 && x.getOrigen() != null && x.getTipo() != TipoTransaccion.TRANSFERENCIA_PROPIA)
                origenes10min.put(x.getOrigen().getCodigo(), true);
            if (t.getDestino() != null && x.getDestino() == t.getDestino()
                    && x.getTipo().esTransferencia() && min <= 30) mismoDestino30min++;
            if (x != t) {
                previas++;
                if (x.getFecha().getHour() < 5) nocturnasPrevias++;
                if (x.getTipo() != TipoTransaccion.RECARGA) { salientesPrevias++; sumaSalientes += x.getValor(); }
            }
        }

        if (t.getTipo().esTransferencia() && transferencias5min >= 3) {
            riesgo += 2; patrones.addLast("RÁFAGA");
            detalles.addLast(transferencias5min + " transferencias en menos de 5 minutos");
        }
        if (saliente && salientesPrevias >= 3 && t.getValor() > 3 * (sumaSalientes / salientesPrevias)) {
            riesgo += 2; patrones.addLast("MONTO_ALTO");
            detalles.addLast("$" + fmt(t.getValor()) + " es más de 3 veces su promedio ($" + fmt(sumaSalientes / salientesPrevias) + ")");
        }
        if (mismoDestino30min >= 3) {
            riesgo += 2; patrones.addLast("DESTINO_REPETIDO");
            detalles.addLast(mismoDestino30min + " envíos a la misma billetera en 30 minutos");
        }
        if (saliente && t.getTipo() != TipoTransaccion.TRANSFERENCIA_PROPIA && origenes10min.getSize() >= 3) {
            riesgo += 2; patrones.addLast("FRAGMENTACIÓN");
            detalles.addLast("salió dinero de " + origenes10min.getSize() + " billeteras distintas en 10 minutos");
        }
        double promedioHora = ultimos7dias / (7.0 * 24);
        if (ultimaHora >= 5 && ultimaHora > 4 * Math.max(promedioHora, 0.25)) {
            riesgo += 1; patrones.addLast("CAMBIO_FRECUENCIA");
            detalles.addLast(ultimaHora + " operaciones en la última hora (su promedio es " + String.format("%.2f", promedioHora) + " por hora)");
        }
        if (f.getHour() < 5 && previas >= 5 && nocturnasPrevias * 10 < previas) {
            riesgo += 1; patrones.addLast("HORARIO_INUSUAL");
            detalles.addLast("operación a las " + f.toLocalTime().withSecond(0).withNano(0) + ", fuera de su horario habitual");
        }

        if (riesgo == 0) return null;
        NivelRiesgo nivel = riesgo >= 3 ? NivelRiesgo.ALTO : NivelRiesgo.MEDIO;
        t.setNivelRiesgo(nivel);
        Comportamiento c = new Comportamiento(String.format("A%04d", ++seqAuditoria), f, t,
                unir(patrones, ", "), nivel, riesgo, unir(detalles, "; "));
        auditoria.addFirst(c);
        notificar(u, TipoNotificacion.ALERTA_SEGURIDAD, "Detectamos actividad inusual (riesgo " + nivel + ") en "
                + t.getId() + ": " + c.getDescripcion() + ". Si no fuiste tú, revierte la operación y cambia tu contraseña.");
        return c;
    }
    
     /** Retiros y envíos a otras personas (lo único que analiza la detección). */
    private static boolean esSalidaReal(Transaccion t) {
        return t.getTipo() == TipoTransaccion.RETIRO || t.getTipo().esHaciaOtraPersona();
    }

    public SimpleLinkedList<Comportamiento> getAuditoria() { return auditoria; }
    /** 4.9: consulta posterior de los eventos detectados de un usuario. */
    public SimpleLinkedList<Comportamiento> eventosDeUsuario(String idUsuario) {
        return auditoria.filtrar(c -> c.getUsuario().getId().equals(idUsuario));
    }



    public SimpleLinkedList<Comportamiento> auditoriaFiltrada(NivelRiesgo riesgo, String idUsuario) {
        return auditoria.filtrar(c -> (riesgo == null || c.getNivelRiesgo() == riesgo)
                && (idUsuario == null || idUsuario.isBlank() || c.getUsuario().getId().equals(idUsuario.trim())));
    }

    // =====================================================================
    // 4.7: notificaciones (cola)
    // =====================================================================

    private void notificar(Usuario u, TipoNotificacion tipo, String mensaje) {
        notificacionesPendientes.encolar(new Notificacion(String.format("N%05d", ++seqNotificacion), ahora(), tipo, mensaje, u));
    }

    /** Saca de la cola (FIFO) las pendientes y se las entrega a cada usuario. */
    public int despacharNotificaciones() {
        int n = 0;
        while (!notificacionesPendientes.isEmpty()) {
            Notificacion x = notificacionesPendientes.desencolar();
            if (usuarios.containsKey(x.getUsuario().getId())) x.getUsuario().recibirNotificacion(x);
            n++;
        }
        notificacionesDespachadas += n;
        return n;
    }

    public long getNotificacionesDespachadas() { return notificacionesDespachadas; }

    // =====================================================================
    // 4.8 / RF11-RF12 / 8: analítica y reportes
    // =====================================================================

    private boolean enRango(Transaccion t, LocalDateTime desde, LocalDateTime hasta) {
        return t.getEstado() == EstadoTransaccion.EXITOSA
                && (desde == null || !t.getFecha().isBefore(desde))
                && (hasta == null || !t.getFecha().isAfter(hasta));
    }

    /** Agrupa con una TablaHash (clave -> ItemReporte) y devuelve ordenado por cantidad (y monto). */
    private SimpleLinkedList<ItemReporte> agrupar(SimpleLinkedList<Transaccion> fuente, LocalDateTime desde, LocalDateTime hasta,
                                                  Function<Transaccion, String[]> claves, int k) {
        TablaHash<String, ItemReporte> grupos = new TablaHash<>();
        for (Transaccion t : fuente) {
            if (!enRango(t, desde, hasta)) continue;
            for (String c : claves.apply(t)) {
                if (c == null) continue;
                ItemReporte it = grupos.get(c);
                if (it == null) { it = new ItemReporte(c); grupos.put(c, it); }
                it.sumar(t.getValor());
            }
        }
        SimpleLinkedList<ItemReporte> l = grupos.valores();
        l.ordenarLista((a, b) -> a.getCantidad() != b.getCantidad() ? Integer.compare(b.getCantidad(), a.getCantidad())
                : Double.compare(b.getMonto(), a.getMonto()));
        return l.primeros(k);
    }

    private String etiquetaCuenta(Cuenta c) {
        return c == null ? null : c.getCodigo() + " · " + c.getNombre() + " (" + c.getPropietario().getPrimerNombre() + ")";
    }

    /** Billeteras (y cuentas Disponible) con mayor uso: cuenta cada movimiento en origen y destino. */
    public SimpleLinkedList<ItemReporte> billeterasMayorUso(LocalDateTime desde, LocalDateTime hasta, int k) {
        return agrupar(transacciones, desde, hasta, t -> new String[]{etiquetaCuenta(t.getOrigen()),
                t.getOrigen() == t.getDestino() ? null : etiquetaCuenta(t.getDestino())}, k);
    }

    /** Usuarios con más transferencias realizadas. */
    public SimpleLinkedList<ItemReporte> usuariosConMasTransferencias(LocalDateTime desde, LocalDateTime hasta, int k) {
        return agrupar(transacciones, desde, hasta,
                t -> new String[]{t.getTipo().esTransferencia() ? t.getUsuario().toString() : null}, k);
    }

    private static String categoria(Cuenta c) {
        return c instanceof Billetera b ? b.getTipo().getEtiqueta() : null; // el Disponible no es una categoría de billetera
    }

    /** Categorías (tipo de billetera) más activas. */
    public SimpleLinkedList<ItemReporte> categoriasMasActivas(LocalDateTime desde, LocalDateTime hasta) {
        return agrupar(transacciones, desde, hasta, t -> {
            String a = categoria(t.getOrigen()), b = categoria(t.getDestino());
            return new String[]{a, b != null && b.equals(a) ? null : b};
        }, Integer.MAX_VALUE);
    }

    /** Frecuencia de transacciones por tipo. */
    public SimpleLinkedList<ItemReporte> frecuenciaPorTipo(LocalDateTime desde, LocalDateTime hasta) {
        return agrupar(transacciones, desde, hasta, t -> new String[]{t.getTipo().getEtiqueta()}, Integer.MAX_VALUE);
    }

    /** Lo mismo pero solo con los movimientos de un usuario (para su resumen). */
    public SimpleLinkedList<ItemReporte> resumenUsuario(String idUsuario, LocalDateTime desde, LocalDateTime hasta) {
        Usuario u = obtenerUsuario(idUsuario);
        return agrupar(u.getHistorial().filtrar(t -> t.getUsuario() == u), desde, hasta,
                t -> new String[]{t.getTipo().getEtiqueta()}, Integer.MAX_VALUE);
    }

    /** Monto total movilizado en un rango de tiempo. */
    public double montoTotalMovilizado(LocalDateTime desde, LocalDateTime hasta) {
        double total = 0;
        for (Transaccion t : transacciones) if (enRango(t, desde, hasta)) total += t.getValor();
        return total;
    }

    public int cantidadTransacciones(LocalDateTime desde, LocalDateTime hasta) {
        return transacciones.contar(t -> enRango(t, desde, hasta));
    }

    /** Usuario con mayor actividad (más operaciones exitosas) en un período. */
    public ItemReporte usuarioMasActivo(LocalDateTime desde, LocalDateTime hasta) {
        SimpleLinkedList<ItemReporte> l = agrupar(transacciones, desde, hasta, t -> new String[]{t.getUsuario().toString()}, 1);
        return l.getFirst();
    }

    /** Las k transacciones de mayor valor, sacadas del árbol ordenado por valor (inorden inverso). */
    public SimpleLinkedList<Transaccion> topTransacciones(int k) {
        return transaccionesPorValor.mayores(k);
    }

    // =====================================================================
    // Datos de ejemplo
    // =====================================================================

    /** Carga usuarios de prueba con movimientos repartidos en los últimos días. */
    public void cargarDatosEjemplo() {
        if (!usuarios.isEmpty()) return;
        long reloj = minutosSimulados;
        minutosSimulados = reloj - 20 * 24 * 60; // se empieza hace 20 días

        Usuario ana = registrarUsuario("1094000001", "Ana Martínez", "3104567890", "ana@correo.com", "123456");
        Usuario laura = registrarUsuario("1094000002", "Laura Gómez", "3157894561", "laura@correo.com", "123456");
        Usuario santi = registrarUsuario("1094000003", "Santiago López", "3001234567", "santiago@correo.com", "123456");
        Usuario vale = registrarUsuario("1094000004", "Valentina Ríos", "3209876543", "valentina@correo.com", "123456");
        Usuario camilo = registrarUsuario("1094000005", "Camilo Pérez", "3112223344", "camilo@correo.com", "123456");

        Billetera anaA = crearBilletera(ana.getId(), "Ahorro viaje", TipoBilletera.AHORRO);
        Billetera anaT = crearBilletera(ana.getId(), "Transporte", TipoBilletera.TRANSPORTE);
        Billetera lauC = crearBilletera(laura.getId(), "Compras", TipoBilletera.COMPRAS);
        Billetera sanI = crearBilletera(santi.getId(), "Inversiones", TipoBilletera.INVERSION);
        Billetera valS = crearBilletera(vale.getId(), "Netflix y Spotify", TipoBilletera.SUSCRIPCIONES);
        crearBilletera(camilo.getId(), "Gastos de la U", TipoBilletera.GASTOS_DIARIOS);

        // R = recarga al Disponible, X = retiro del Disponible, M = mover a una billetera propia, E = enviar a otra persona
        Object[][] ops = {
                {"R", ana, null, 450_000}, {"R", laura, null, 300_000}, {"R", santi, null, 520_000},
                {"R", vale, null, 180_000}, {"R", camilo, null, 90_000},
                {"M", ana, anaA, 120_000}, {"E", ana, laura, 35_000}, {"E", laura, santi, 28_000},
                {"E", santi, ana, 15_000}, {"M", santi, sanI, 200_000}, {"M", vale, valS, 45_000},
                {"E", vale, ana, 22_000}, {"X", ana, null, 40_000}, {"M", ana, anaT, 30_000},
                {"M", laura, lauC, 60_000}, {"E", ana, laura, 18_000}, {"E", camilo, vale, 12_000},
                {"E", vale, camilo, 9_000}, {"X", santi, null, 50_000}, {"E", laura, vale, 14_000},
                {"E", ana, laura, 25_000}, {"R", ana, null, 150_000}, {"E", santi, laura, 33_000},
                {"E", laura, ana, 20_000}, {"E", camilo, ana, 8_000},
        };
        for (Object[] o : ops) {
            minutosSimulados += 11 * 60 + 17; // cada operación ~11 horas después de la anterior
            Usuario u = (Usuario) o[1];
            double v = ((Number) o[3]).doubleValue();
            switch ((String) o[0]) {
                case "R" -> recargar(u.getId(), null, v, "Recarga por PSE");
                case "X" -> retirar(u.getId(), null, v, "Retiro en cajero");
                case "M" -> moverEntreCuentas(u.getId(), null, ((Billetera) o[2]).getCodigo(), v, "");
                default -> enviarAPersona(u.getId(), null, ((Usuario) o[2]).getTelefono(), v, "");
            }
        }

        minutosSimulados = reloj; // vuelve al presente
        LocalDateTime hoy = ahora().withSecond(0).withNano(0);
        programarOperacion(ana.getId(), TipoTransaccion.TRANSFERENCIA_PROPIA, null, anaA.getCodigo(), 50_000,
                hoy.plusHours(5), Frecuencia.SEMANAL, "Ahorro semanal");
        programarOperacion(ana.getId(), TipoTransaccion.PAGO_PROGRAMADO, null, laura.getTelefono(), 30_000,
                hoy.plusDays(3), Frecuencia.MENSUAL, "Arriendo compartido");
        programarOperacion(vale.getId(), TipoTransaccion.TRANSFERENCIA_PROPIA, null, valS.getCodigo(), 38_000,
                hoy.plusDays(1), Frecuencia.MENSUAL, "Suscripciones del mes");
        programarOperacion(santi.getId(), TipoTransaccion.RECARGA, null, null, 100_000,
                hoy.plusDays(2), Frecuencia.UNICA, "Recarga de nómina");
        despacharNotificaciones();
    }

    // =====================================================================
    // Utilidades y getters
    // =====================================================================

    private String nuevoIdTx() { return String.format("T%05d", ++seqTransaccion); }

    private static String limpiar(String s) { return s == null ? "" : s.trim(); }

    private static String unir(SimpleLinkedList<String> l, String sep) {
        StringBuilder sb = new StringBuilder();
        for (String s : l) { if (sb.length() > 0) sb.append(sep); sb.append(s); }
        return sb.toString();
    }

    /* Parametro del sistema (busqueda O(1) en la tabla hash de configuración). */

    public double config(String clave) {
        Double v = configuracion.get(clave);
        if (v == null) throw new IllegalArgumentException("No existe el parámetro " + clave);
        return v;
    }
     public TablaHash<String, Double> getConfiguracion() { return configuracion; }

    /** true si el objeto cargado de disco está completo y es de esta versión. */
    public boolean esValido() {
        return versionDatos == VERSION_DATOS && usuarios != null && usuariosPorCorreo != null && usuariosPorTelefono != null
                && cuentas != null && transaccionesPorId != null && rankingPuntos != null && transaccionesPorValor != null
                && programadas != null && notificacionesPendientes != null && grafoUsuarios != null && grafoCuentas != null
                && transacciones != null && auditoria != null && catalogoBeneficios != null;
    }

    public static String fmt(double v) { return String.format("%,.0f", v); }

    public SimpleLinkedList<Billetera> listarBilleteras() {
        SimpleLinkedList<Billetera> l = new SimpleLinkedList<>();
        for (Cuenta c : cuentas.valores()) if (c instanceof Billetera b) l.addLast(b);
        return l;
    }
    public SimpleLinkedList<Transaccion> getTransacciones() { return transacciones; }
    public SimpleLinkedList<Beneficio> getCatalogoBeneficios() { return catalogoBeneficios; }
    public ArbolBinario<EntradaRanking> getRankingPuntos() { return rankingPuntos; }
    public ArbolBinario<Transaccion> getTransaccionesPorValor() { return transaccionesPorValor; }
    public Grafo getGrafoUsuarios() { return grafoUsuarios; }
    public Grafo getGrafoCuentas() { return grafoCuentas; }
    public TablaHash<String, Usuario> getTablaUsuarios() { return usuarios; }
    public TablaHash<String, Cuenta> getTablaCuentas() { return cuentas; }
    public int cantidadUsuarios() { return usuarios.getSize(); }
    public int cantidadBilleteras() { return listarBilleteras().getSize(); }
    public int cantidadProgramadas() { return programadas.getSize(); }
    public int cantidadNotificacionesPendientes() { return notificacionesPendientes.getSize(); }

    



}
