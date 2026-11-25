package grafos;

import estructuras.ColaDePrioridad;
import estructuras.ColaEnlazada;
import estructuras.ListaDinamica;

public class Grafo<T> {
    private static final int SIN_VISITAR = 0;
    private static final int EN_CURSO = 1;
    private static final int TERMINADO = 2;

    private static class Vertice<T> {
        private final T valor;
        private final ListaDinamica<Arista<T>> salientes = new ListaDinamica<>();

        private Vertice(T valor) {
            this.valor = valor;
        }
    }

    private static class Distancia {
        private final int indice;
        private final double valor;

        private Distancia(int indice, double valor) {
            this.indice = indice;
            this.valor = valor;
        }
    }

    private final ListaDinamica<Vertice<T>> vertices = new ListaDinamica<>();

    public boolean agregarVertice(T valor) {
        if (valor == null) throw new IllegalArgumentException("Un vértice no puede ser nulo");
        if (indiceDe(valor) >= 0) return false;
        vertices.agregar(new Vertice<>(valor));
        return true;
    }

    public boolean contieneVertice(T valor) {
        return indiceDe(valor) >= 0;
    }

    public Arista<T> agregarArista(T origen, T destino, double peso) {
        Vertice<T> desde = vertice(origen);
        vertice(destino);
        Arista<T> existente = arista(origen, destino);
        if (existente != null) {
            existente.setPeso(peso);
            return existente;
        }
        Arista<T> nueva = new Arista<>(desde.valor, destino, peso);
        desde.salientes.agregar(nueva);
        return nueva;
    }

    public Arista<T> arista(T origen, T destino) {
        ListaDinamica<Arista<T>> salientes = vertice(origen).salientes;
        for (int i = 0; i < salientes.tamanio(); i++) {
            Arista<T> a = salientes.obtener(i);
            if (a.getDestino().equals(destino)) return a;
        }
        return null;
    }

    public ListaDinamica<T> getVertices() {
        ListaDinamica<T> valores = new ListaDinamica<>();
        for (int i = 0; i < vertices.tamanio(); i++) valores.agregar(vertices.obtener(i).valor);
        return valores;
    }

    public ListaDinamica<Arista<T>> aristasDe(T origen) {
        return vertice(origen).salientes.copia();
    }

    public ListaDinamica<Arista<T>> getAristas() {
        ListaDinamica<Arista<T>> todas = new ListaDinamica<>();
        for (int i = 0; i < vertices.tamanio(); i++) todas.agregarTodos(vertices.obtener(i).salientes);
        return todas;
    }

    public int cantidadDeVertices() {
        return vertices.tamanio();
    }

    public int cantidadDeAristas() {
        int total = 0;
        for (int i = 0; i < vertices.tamanio(); i++) total += vertices.obtener(i).salientes.tamanio();
        return total;
    }

    public ListaDinamica<T> bfs(T origen) {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        ListaDinamica<Boolean> visitados = marcas(false);
        ColaEnlazada<Integer> pendientes = new ColaEnlazada<>();
        int inicio = indiceObligatorio(origen);
        visitados.reemplazar(inicio, true);
        pendientes.encolar(inicio);
        while (!pendientes.estaVacia()) {
            Vertice<T> actual = vertices.obtener(pendientes.desencolar());
            recorrido.agregar(actual.valor);
            for (int i = 0; i < actual.salientes.tamanio(); i++) {
                int vecino = indiceDe(actual.salientes.obtener(i).getDestino());
                if (!visitados.obtener(vecino)) {
                    visitados.reemplazar(vecino, true);
                    pendientes.encolar(vecino);
                }
            }
        }
        return recorrido;
    }

    public ListaDinamica<T> dfs(T origen) {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        dfs(indiceObligatorio(origen), marcas(false), recorrido);
        return recorrido;
    }

    private void dfs(int indice, ListaDinamica<Boolean> visitados, ListaDinamica<T> recorrido) {
        if (visitados.obtener(indice)) return;
        visitados.reemplazar(indice, true);
        Vertice<T> actual = vertices.obtener(indice);
        recorrido.agregar(actual.valor);
        for (int i = 0; i < actual.salientes.tamanio(); i++) {
            dfs(indiceDe(actual.salientes.obtener(i).getDestino()), visitados, recorrido);
        }
    }

    public Camino<T> caminoMinimo(T origen, T destino) {
        int inicio = indiceObligatorio(origen);
        int fin = indiceObligatorio(destino);
        ListaDinamica<Double> distancias = new ListaDinamica<>();
        ListaDinamica<Integer> anteriores = new ListaDinamica<>();
        ListaDinamica<Boolean> cerrados = marcas(false);
        for (int i = 0; i < vertices.tamanio(); i++) {
            distancias.agregar(Double.POSITIVE_INFINITY);
            anteriores.agregar(-1);
        }
        distancias.reemplazar(inicio, 0.0);
        ColaDePrioridad<Distancia> frontera = new ColaDePrioridad<>((a, b) -> Double.compare(a.valor, b.valor));
        frontera.encolar(new Distancia(inicio, 0));
        while (!frontera.estaVacia()) {
            Distancia actual = frontera.desencolarMinimo();
            if (cerrados.obtener(actual.indice)) continue;
            cerrados.reemplazar(actual.indice, true);
            if (actual.indice == fin) break;
            ListaDinamica<Arista<T>> salientes = vertices.obtener(actual.indice).salientes;
            for (int i = 0; i < salientes.tamanio(); i++) {
                Arista<T> arista = salientes.obtener(i);
                int vecino = indiceDe(arista.getDestino());
                double propuesta = actual.valor + arista.getPeso();
                if (propuesta < distancias.obtener(vecino)) {
                    distancias.reemplazar(vecino, propuesta);
                    anteriores.reemplazar(vecino, actual.indice);
                    frontera.encolar(new Distancia(vecino, propuesta));
                }
            }
        }
        if (Double.isInfinite(distancias.obtener(fin))) return Camino.inexistente();
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        for (int i = fin; i != -1; i = anteriores.obtener(i)) recorrido.insertar(0, vertices.obtener(i).valor);
        return Camino.de(recorrido, distancias.obtener(fin));
    }

    public boolean tieneCiclo() {
        return !buscarCiclo().estaVacia();
    }

    public ListaDinamica<T> buscarCiclo() {
        ListaDinamica<Integer> estados = new ListaDinamica<>();
        ListaDinamica<Integer> padres = new ListaDinamica<>();
        for (int i = 0; i < vertices.tamanio(); i++) {
            estados.agregar(SIN_VISITAR);
            padres.agregar(-1);
        }
        for (int i = 0; i < vertices.tamanio(); i++) {
            if (estados.obtener(i) == SIN_VISITAR) {
                ListaDinamica<T> ciclo = buscarCiclo(i, estados, padres);
                if (!ciclo.estaVacia()) return ciclo;
            }
        }
        return new ListaDinamica<>();
    }

    private ListaDinamica<T> buscarCiclo(int indice, ListaDinamica<Integer> estados, ListaDinamica<Integer> padres) {
        estados.reemplazar(indice, EN_CURSO);
        ListaDinamica<Arista<T>> salientes = vertices.obtener(indice).salientes;
        for (int i = 0; i < salientes.tamanio(); i++) {
            int vecino = indiceDe(salientes.obtener(i).getDestino());
            if (estados.obtener(vecino) == EN_CURSO) return armarCiclo(indice, vecino, padres);
            if (estados.obtener(vecino) == SIN_VISITAR) {
                padres.reemplazar(vecino, indice);
                ListaDinamica<T> ciclo = buscarCiclo(vecino, estados, padres);
                if (!ciclo.estaVacia()) return ciclo;
            }
        }
        estados.reemplazar(indice, TERMINADO);
        return new ListaDinamica<>();
    }

    private ListaDinamica<T> armarCiclo(int ultimo, int primero, ListaDinamica<Integer> padres) {
        ListaDinamica<T> ciclo = new ListaDinamica<>();
        for (int i = ultimo; i != primero; i = padres.obtener(i)) ciclo.insertar(0, vertices.obtener(i).valor);
        ciclo.insertar(0, vertices.obtener(primero).valor);
        ciclo.agregar(vertices.obtener(primero).valor);
        return ciclo;
    }

    public int componentesConexas() {
        ListaDinamica<Boolean> visitados = marcas(false);
        int componentes = 0;
        for (int i = 0; i < vertices.tamanio(); i++) {
            if (visitados.obtener(i)) continue;
            componentes++;
            ColaEnlazada<Integer> pendientes = new ColaEnlazada<>();
            visitados.reemplazar(i, true);
            pendientes.encolar(i);
            while (!pendientes.estaVacia()) {
                int actual = pendientes.desencolar();
                for (int j = 0; j < vertices.tamanio(); j++) {
                    if (visitados.obtener(j)) continue;
                    if (arista(vertices.obtener(actual).valor, vertices.obtener(j).valor) != null
                            || arista(vertices.obtener(j).valor, vertices.obtener(actual).valor) != null) {
                        visitados.reemplazar(j, true);
                        pendientes.encolar(j);
                    }
                }
            }
        }
        return componentes;
    }

    private ListaDinamica<Boolean> marcas(boolean valorInicial) {
        ListaDinamica<Boolean> marcas = new ListaDinamica<>();
        for (int i = 0; i < vertices.tamanio(); i++) marcas.agregar(valorInicial);
        return marcas;
    }

    private int indiceDe(T valor) {
        for (int i = 0; i < vertices.tamanio(); i++) {
            if (vertices.obtener(i).valor.equals(valor)) return i;
        }
        return -1;
    }

    private int indiceObligatorio(T valor) {
        int indice = indiceDe(valor);
        if (indice < 0) throw new VerticeInexistenteException(valor);
        return indice;
    }

    private Vertice<T> vertice(T valor) {
        return vertices.obtener(indiceObligatorio(valor));
    }
}
