package co.edu.uniquindio.proyectofinal.proyectofinal.logica;

import co.edu.uniquindio.proyectofinal.proyectofinal.Estructuras.*;
import co.edu.uniquindio.proyectofinal.proyectofinal.model.ResultadoRendimiento;import java.util.Random;

public final class ComparadorRendimiento {

    private ComparadorRendimiento() { }

    public static SimpleLinkedList<ResultadoRendimiento> comparar(int n) {
        if (n < 100 || n > 50_000) throw new IllegalArgumentException("Use un tamaño entre 100 y 50.000");
        Random rnd = new Random(2026);
        int[] datos = new int[n];
        for (int i = 0; i < n; i++) datos[i] = rnd.nextInt(n * 10);
        int consultas = Math.min(n, 2000);
        int[] buscados = new int[consultas];
        for (int i = 0; i < consultas; i++) buscados[i] = datos[rnd.nextInt(n)];

        SimpleLinkedList<ResultadoRendimiento> r = new SimpleLinkedList<>();
        r.addLast(busquedaPorClave(n, datos, buscados));
        r.addLast(siguienteProgramada(n, datos));
        r.addLast(busquedaPorRango(n, datos));
        r.addLast(topK(n, datos));
        r.addLast(deshacer(n));
        return r;
    }

    /** Buscar usuarios por cédula: TablaHash vs recorrer una lista. */
    private static ResultadoRendimiento busquedaPorClave(int n, int[] datos, int[] buscados) {
        TablaHash<String, Integer> hash = new TablaHash<>();
        SimpleLinkedList<String> lista = new SimpleLinkedList<>();
        for (int i = 0; i < n; i++) {
            String clave = "U" + i + "-" + datos[i];
            hash.put(clave, datos[i]);
            lista.addLast(clave);
        }
        String[] claves = new String[buscados.length];
        Random rnd = new Random(7);
        for (int i = 0; i < claves.length; i++) { int j = rnd.nextInt(n); claves[i] = "U" + j + "-" + datos[j]; }

        long t0 = System.nanoTime();
        int encontrados = 0;
        for (String c : claves) if (hash.get(c) != null) encontrados++;
        long tHash = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (String c : claves) if (lista.buscar(x -> x.equals(c)) != null) encontrados--;
        long tLista = System.nanoTime() - t0;
        if (encontrados != 0) throw new IllegalStateException("Las dos estructuras no coinciden");

        return new ResultadoRendimiento("Buscar " + claves.length + " usuarios por cédula", n,
                "Tabla hash", "O(1) promedio", ms(tHash), "Lista enlazada", "O(n)", ms(tLista));
    }

    /** Sacar siempre la operación programada más próxima: montículo vs buscar el mínimo en una lista. */
    private static ResultadoRendimiento siguienteProgramada(int n, int[] datos) {
        int m = Math.min(n, 5000);
        ColaPrioridad<Integer> heap = new ColaPrioridad<>();
        SimpleLinkedList<Integer> lista = new SimpleLinkedList<>();
        for (int i = 0; i < m; i++) { heap.encolar(datos[i]); lista.addLast(datos[i]); }

        long t0 = System.nanoTime();
        long sumaA = 0;
        while (!heap.isEmpty()) sumaA += heap.desencolar();
        long tHeap = System.nanoTime() - t0;

        t0 = System.nanoTime();
        long sumaB = 0;
        while (!lista.isEmpty()) {
            Integer menor = null;
            for (Integer x : lista) if (menor == null || x < menor) menor = x;
            lista.eliminarValorNodo(menor);
            sumaB += menor;
        }
        long tLista = System.nanoTime() - t0;
        if (sumaA != sumaB) throw new IllegalStateException("Las dos estructuras no coinciden");

        return new ResultadoRendimiento("Ejecutar " + m + " programadas en orden de fecha", n,
                "Cola de prioridad (heap)", "O(log n) c/u", ms(tHeap), "Lista (buscar el mínimo)", "O(n) c/u", ms(tLista));
    }

    /** Usuarios con puntos en un rango: árbol con poda vs filtrar toda la lista. */
    private static ResultadoRendimiento busquedaPorRango(int n, int[] datos) {
        ArbolBinario<Integer> arbol = new ArbolBinario<>();
        SimpleLinkedList<Integer> lista = new SimpleLinkedList<>();
        for (int d : datos) {
            if (!arbol.existe(d)) { arbol.insertar(d); lista.addLast(d); }
        }
        int consultas = 300;
        Random rnd = new Random(11);
        int[] mins = new int[consultas];
        for (int i = 0; i < consultas; i++) mins[i] = rnd.nextInt(n * 10);
        int ancho = n / 10 + 1;

        long t0 = System.nanoTime();
        long a = 0;
        for (int min : mins) a += arbol.buscarRango(min, min + ancho).getSize();
        long tArbol = System.nanoTime() - t0;

        t0 = System.nanoTime();
        long b = 0;
        for (int min : mins) b += lista.contar(x -> x >= min && x <= min + ancho);
        long tLista = System.nanoTime() - t0;
        if (a != b) throw new IllegalStateException("Las dos estructuras no coinciden");

        return new ResultadoRendimiento(consultas + " búsquedas por rango de puntos", n,
                "Árbol binario de búsqueda", "O(log n + k)", ms(tArbol), "Lista (recorrer todo)", "O(n)", ms(tLista));
    }

    /** Las 10 transacciones de mayor valor: árbol (inorden inverso) vs recorrer la lista k veces. */
    private static ResultadoRendimiento topK(int n, int[] datos) {
        ArbolBinario<Integer> arbol = new ArbolBinario<>();
        SimpleLinkedList<Integer> lista = new SimpleLinkedList<>();
        for (int d : datos) {
            if (!arbol.existe(d)) { arbol.insertar(d); lista.addLast(d); }
        }
        int k = 10, repeticiones = 200;

        long t0 = System.nanoTime();
        long a = 0;
        for (int r = 0; r < repeticiones; r++) for (Integer x : arbol.mayores(k)) a += x;
        long tArbol = System.nanoTime() - t0;

        t0 = System.nanoTime();
        long b = 0;
        for (int r = 0; r < repeticiones; r++) {
            int limite = Integer.MAX_VALUE;
            for (int i = 0; i < k; i++) {
                int mayor = Integer.MIN_VALUE;
                for (Integer x : lista) if (x < limite && x > mayor) mayor = x;
                b += mayor;
                limite = mayor;
            }
        }
        long tLista = System.nanoTime() - t0;
        if (a != b) throw new IllegalStateException("Las dos estructuras no coinciden");

        return new ResultadoRendimiento("Top 10 de mayor valor (" + repeticiones + " veces)", n,
                "Árbol binario de búsqueda", "O(altura + k)", ms(tArbol), "Lista (k recorridos)", "O(k·n)", ms(tLista));
    }

    /** Deshacer la última operación: pila (tope) vs quitar el último de una lista simple. */
    private static ResultadoRendimiento deshacer(int n) {
        int m = Math.min(n, 10_000);
        Pila<Integer> pila = new Pila<>();
        SimpleLinkedList<Integer> lista = new SimpleLinkedList<>();
        for (int i = 0; i < m; i++) { pila.push(i); lista.addLast(i); }

        long t0 = System.nanoTime();
        long a = 0;
        while (!pila.isEmpty()) a += pila.pop();
        long tPila = System.nanoTime() - t0;

        t0 = System.nanoTime();
        long b = 0;
        while (!lista.isEmpty()) b += lista.eliminarUltimo();
        long tLista = System.nanoTime() - t0;
        if (a != b) throw new IllegalStateException("Las dos estructuras no coinciden");

        return new ResultadoRendimiento("Deshacer " + m + " operaciones (la última primero)", n,
                "Pila", "O(1) c/u", ms(tPila), "Lista simple (eliminar último)", "O(n) c/u", ms(tLista));
    }

    private static double ms(long nanos) { return nanos / 1_000_000.0; }



}
