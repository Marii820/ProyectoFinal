package co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras;

import java.io.Serializable;

public class Grafo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Arista implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String origen;
        private final String destino;
        private int veces;
        private double monto;

        public Arista(String origen, String destino) {
            this.origen = origen;
            this.destino = destino;
        }

        public String getOrigen() { return origen; }
        public String getDestino() { return destino; }
        public int getVeces() { return veces; }
        public double getMonto() { return monto; }

        @Override
        public String toString() {
            return origen + " -> " + destino + "  (" + veces + " veces, $" + String.format("%,.0f", monto) + ")";
        }
    }

    private final TablaHash<String, SimpleLinkedList<Arista>> adyacencia = new TablaHash<>();
    private int cantidadAristas;

    public void agregarVertice(String v) {
        if (!adyacencia.containsKey(v)) adyacencia.put(v, new SimpleLinkedList<>());
    }

    public boolean existeVertice(String v) { return adyacencia.containsKey(v); }

    public void eliminarVertice(String v) {
        SimpleLinkedList<Arista> salen = adyacencia.remove(v);
        if (salen != null) cantidadAristas -= salen.getSize();
        for (SimpleLinkedList<Arista> lista : adyacencia.valores()) {
            cantidadAristas -= lista.eliminarSi(a -> a.destino.equals(v));
        }
    }

    private Arista buscarArista(String origen, String destino) {
        SimpleLinkedList<Arista> lista = adyacencia.get(origen);
        return lista == null ? null : lista.buscar(a -> a.destino.equals(destino));
    }

    //Registra un movimiento origen -> destino (crea la arista o le suma peso)

    public void agregarArista(String origen, String destino, double monto) {
        agregarVertice(origen);
        agregarVertice(destino);
        Arista a = buscarArista(origen, destino);
        if (a == null) {
            a = new Arista(origen, destino);
            adyacencia.get(origen).addLast(a);
            cantidadAristas++;
        }
        a.veces++;
        a.monto += monto;
    }

    public void quitarArista(String origen, String destino, double monto) {
        Arista a = buscarArista(origen, destino);
        if (a == null) return;
        a.veces--;
        a.monto -= monto;
        if (a.veces <= 0) {
            adyacencia.get(origen).eliminarValorNodo(a);
            cantidadAristas--;
        }
    }

    public SimpleLinkedList<Arista> aristasDe(String origen) {
        SimpleLinkedList<Arista> l = adyacencia.get(origen);
        return l == null ? new SimpleLinkedList<>() : l;
    }

    public SimpleLinkedList<Arista> todasLasAristas() {
        SimpleLinkedList<Arista> r = new SimpleLinkedList<>();
        for (SimpleLinkedList<Arista> l : adyacencia.valores()) for (Arista a : l) r.addLast(a);
        return r;
    }

    public SimpleLinkedList<Arista> rutasFrecuentes(int k) {
        SimpleLinkedList<Arista> todas = todasLasAristas();
        todas.ordenarLista((a, b) -> a.veces != b.veces ? Integer.compare(b.veces, a.veces) : Double.compare(b.monto, a.monto));
        return todas.primeros(k);
    }

    public int gradoSalida(String v) { return aristasDe(v).getSize(); }

    public int gradoEntrada(String v) {
        int g = 0;
        for (SimpleLinkedList<Arista> l : adyacencia.valores()) g += l.contar(a -> a.destino.equals(v));
        return g;
    }

    // Ciclos simples de longitud <= maxLongitud usando búsqueda en profundidad (DFS).
    //Cada ciclo se reporta una sola vez: se arma una forma canónica rotándolo
    //para que empiece en el vértice "menor" y se guarda en una TablaHash de vistos.

    public SimpleLinkedList<String> detectarCiclos(int maxLongitud) {
        SimpleLinkedList<String> ciclos = new SimpleLinkedList<>();
        TablaHash<String, Boolean> vistos = new TablaHash<>();
        for (String inicio : adyacencia.claves()) {
            SimpleLinkedList<String> camino = new SimpleLinkedList<>();
            camino.addLast(inicio);
            dfsCiclos(inicio, inicio, camino, maxLongitud, ciclos, vistos);
        }
        return ciclos;
    }

    private void dfsCiclos(String inicio, String actual, SimpleLinkedList<String> camino, int max,
                           SimpleLinkedList<String> ciclos, TablaHash<String, Boolean> vistos) {
        for (Arista a : aristasDe(actual)) {
            String v = a.destino;
            if (v.equals(inicio) && camino.getSize() > 1) {
                String clave = canonico(camino);
                if (!vistos.containsKey(clave)) {
                    vistos.put(clave, true);
                    ciclos.addLast(clave);
                }
            } else if (!camino.contains(v) && camino.getSize() < max) {
                camino.addLast(v);
                dfsCiclos(inicio, v, camino, max, ciclos, vistos);
                camino.eliminarUltimo();
            }
        }
    }

    private String canonico(SimpleLinkedList<String> camino) {
        int n = camino.getSize(), menor = 0;
        for (int i = 1; i < n; i++) {
            if (camino.obtenerValorNodo(i).compareTo(camino.obtenerValorNodo(menor)) < 0) menor = i;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(camino.obtenerValorNodo((menor + i) % n)).append(" -> ");
        return sb.append(camino.obtenerValorNodo(menor)).toString();
    }

    public SimpleLinkedList<String> buscarRuta(String desde, String hasta) {
        SimpleLinkedList<String> ruta = new SimpleLinkedList<>();
        if (!existeVertice(desde) || !existeVertice(hasta)) return ruta;
        TablaHash<String, String> padre = new TablaHash<>();
        Cola<String> cola = new Cola<>();
        cola.encolar(desde);
        padre.put(desde, desde);
        while (!cola.isEmpty()) {
            String u = cola.desencolar();
            if (u.equals(hasta)) break;
            for (Arista a : aristasDe(u)) {
                if (!padre.containsKey(a.destino)) {
                    padre.put(a.destino, u);
                    cola.encolar(a.destino);
                }
            }
        }
        if (!padre.containsKey(hasta)) return ruta;
        String x = hasta;
        while (!x.equals(desde)) {
            ruta.addFirst(x);
            x = padre.get(x);
        }
        ruta.addFirst(desde);
        return ruta;
    }

    //Vértices alcanzables desde v (a quién le puede llegar dinero de v, directa o indirectamente)
    public SimpleLinkedList<String> alcanzables(String v) {
        SimpleLinkedList<String> r = new SimpleLinkedList<>();
        if (!existeVertice(v)) return r;
        TablaHash<String, Boolean> visitado = new TablaHash<>();
        Cola<String> cola = new Cola<>();
        cola.encolar(v);
        visitado.put(v, true);
        while (!cola.isEmpty()) {
            String u = cola.desencolar();
            for (Arista a : aristasDe(u)) {
                if (!visitado.containsKey(a.destino)) {
                    visitado.put(a.destino, true);
                    r.addLast(a.destino);
                    cola.encolar(a.destino);
                }
            }
        }
        return r;
    }

    public SimpleLinkedList<String> vertices() { return adyacencia.claves(); }

    public int cantidadVertices() { return adyacencia.getSize(); }

    public int cantidadAristas() { return cantidadAristas; }



}
