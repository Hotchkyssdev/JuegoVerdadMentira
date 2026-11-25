package arboles;

import estructuras.ColaEnlazada;
import estructuras.ListaDinamica;

public class ArbolBinario<T> {
    private static class Cursor {
        private int posicion;
    }

    protected NodoBinario<T> raiz;

    public ArbolBinario() {
    }

    public ArbolBinario(T valorDeLaRaiz) {
        raiz = new NodoBinario<>(valorDeLaRaiz);
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public NodoBinario<T> getRaiz() {
        return raiz;
    }

    public void insertar(T valor) {
        NodoBinario<T> nuevo = new NodoBinario<>(valor);
        if (raiz == null) {
            raiz = nuevo;
            return;
        }
        ColaEnlazada<NodoBinario<T>> pendientes = new ColaEnlazada<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            NodoBinario<T> actual = pendientes.desencolar();
            if (actual.getIzquierdo() == null) {
                actual.setIzquierdo(nuevo);
                return;
            }
            if (actual.getDerecho() == null) {
                actual.setDerecho(nuevo);
                return;
            }
            pendientes.encolar(actual.getIzquierdo());
            pendientes.encolar(actual.getDerecho());
        }
    }

    public NodoBinario<T> insertarIzquierdo(NodoBinario<T> padre, T valor) {
        validarHueco(padre, padre == null ? null : padre.getIzquierdo(), "izquierdo");
        NodoBinario<T> nuevo = new NodoBinario<>(valor);
        padre.setIzquierdo(nuevo);
        return nuevo;
    }

    public NodoBinario<T> insertarDerecho(NodoBinario<T> padre, T valor) {
        validarHueco(padre, padre == null ? null : padre.getDerecho(), "derecho");
        NodoBinario<T> nuevo = new NodoBinario<>(valor);
        padre.setDerecho(nuevo);
        return nuevo;
    }

    private static void validarHueco(NodoBinario<?> padre, NodoBinario<?> hijo, String lado) {
        if (padre == null) throw new IllegalArgumentException("Hace falta un nodo padre para insertar");
        if (hijo != null) throw new IllegalStateException("El hijo " + lado + " ya está ocupado");
    }

    public boolean buscar(T valor) {
        return buscar(raiz, valor);
    }

    private boolean buscar(NodoBinario<T> nodo, T valor) {
        if (nodo == null) return false;
        if (iguales(nodo.getValor(), valor)) return true;
        return buscar(nodo.getIzquierdo(), valor) || buscar(nodo.getDerecho(), valor);
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(NodoBinario<T> nodo) {
        if (nodo == null) return 0;
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }

    public int cantidadDeNodos() {
        return cantidadDeNodos(raiz);
    }

    private int cantidadDeNodos(NodoBinario<T> nodo) {
        if (nodo == null) return 0;
        return 1 + cantidadDeNodos(nodo.getIzquierdo()) + cantidadDeNodos(nodo.getDerecho());
    }

    public int cantidadDeHojas() {
        return cantidadDeHojas(raiz);
    }

    private int cantidadDeHojas(NodoBinario<T> nodo) {
        if (nodo == null) return 0;
        if (nodo.esHoja()) return 1;
        return cantidadDeHojas(nodo.getIzquierdo()) + cantidadDeHojas(nodo.getDerecho());
    }

    public ListaDinamica<T> preOrden() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        preOrden(raiz, recorrido);
        return recorrido;
    }

    private void preOrden(NodoBinario<T> nodo, ListaDinamica<T> recorrido) {
        if (nodo == null) return;
        recorrido.agregar(nodo.getValor());
        preOrden(nodo.getIzquierdo(), recorrido);
        preOrden(nodo.getDerecho(), recorrido);
    }

    public ListaDinamica<T> inOrden() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        inOrden(raiz, recorrido);
        return recorrido;
    }

    private void inOrden(NodoBinario<T> nodo, ListaDinamica<T> recorrido) {
        if (nodo == null) return;
        inOrden(nodo.getIzquierdo(), recorrido);
        recorrido.agregar(nodo.getValor());
        inOrden(nodo.getDerecho(), recorrido);
    }

    public ListaDinamica<T> postOrden() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        postOrden(raiz, recorrido);
        return recorrido;
    }

    private void postOrden(NodoBinario<T> nodo, ListaDinamica<T> recorrido) {
        if (nodo == null) return;
        postOrden(nodo.getIzquierdo(), recorrido);
        postOrden(nodo.getDerecho(), recorrido);
        recorrido.agregar(nodo.getValor());
    }

    public ListaDinamica<T> porNiveles() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        if (raiz == null) return recorrido;
        ColaEnlazada<NodoBinario<T>> pendientes = new ColaEnlazada<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            NodoBinario<T> actual = pendientes.desencolar();
            recorrido.agregar(actual.getValor());
            if (actual.getIzquierdo() != null) pendientes.encolar(actual.getIzquierdo());
            if (actual.getDerecho() != null) pendientes.encolar(actual.getDerecho());
        }
        return recorrido;
    }

    public ListaDinamica<T> preOrdenConVacios() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        preOrdenConVacios(raiz, recorrido);
        return recorrido;
    }

    private void preOrdenConVacios(NodoBinario<T> nodo, ListaDinamica<T> recorrido) {
        if (nodo == null) {
            recorrido.agregar(null);
            return;
        }
        recorrido.agregar(nodo.getValor());
        preOrdenConVacios(nodo.getIzquierdo(), recorrido);
        preOrdenConVacios(nodo.getDerecho(), recorrido);
    }

    public static <T> ArbolBinario<T> desdePreOrdenConVacios(ListaDinamica<T> valores) {
        ArbolBinario<T> arbol = new ArbolBinario<>();
        Cursor cursor = new Cursor();
        arbol.raiz = reconstruir(valores, cursor);
        if (cursor.posicion != valores.tamanio()) {
            throw new IllegalArgumentException("Sobran " + (valores.tamanio() - cursor.posicion) + " valores al reconstruir el árbol");
        }
        return arbol;
    }

    private static <T> NodoBinario<T> reconstruir(ListaDinamica<T> valores, Cursor cursor) {
        if (cursor.posicion >= valores.tamanio()) {
            throw new IllegalArgumentException("Faltan valores para reconstruir el árbol");
        }
        T valor = valores.obtener(cursor.posicion);
        cursor.posicion++;
        if (valor == null) return null;
        NodoBinario<T> nodo = new NodoBinario<>(valor);
        nodo.setIzquierdo(reconstruir(valores, cursor));
        nodo.setDerecho(reconstruir(valores, cursor));
        return nodo;
    }

    protected static boolean iguales(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
