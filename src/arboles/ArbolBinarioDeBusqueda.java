package arboles;

import estructuras.ListaDinamica;
import estructuras.PilaEnlazada;

public class ArbolBinarioDeBusqueda<T extends Comparable<T>> extends ArbolBinario<T> {

    @Override
    public void insertar(T valor) {
        agregar(valor);
    }

    public boolean agregar(T valor) {
        if (valor == null) throw new IllegalArgumentException("El ABB no acepta valores nulos");
        if (raiz == null) {
            raiz = new NodoBinario<>(valor);
            return true;
        }
        return agregar(raiz, valor);
    }

    private boolean agregar(NodoBinario<T> nodo, T valor) {
        int comparacion = valor.compareTo(nodo.getValor());
        if (comparacion == 0) return false;
        if (comparacion < 0) {
            if (nodo.getIzquierdo() == null) {
                nodo.setIzquierdo(new NodoBinario<>(valor));
                return true;
            }
            return agregar(nodo.getIzquierdo(), valor);
        }
        if (nodo.getDerecho() == null) {
            nodo.setDerecho(new NodoBinario<>(valor));
            return true;
        }
        return agregar(nodo.getDerecho(), valor);
    }

    @Override
    public NodoBinario<T> insertarIzquierdo(NodoBinario<T> padre, T valor) {
        throw new UnsupportedOperationException("En un ABB la posición la decide el orden: usá insertar");
    }

    @Override
    public NodoBinario<T> insertarDerecho(NodoBinario<T> padre, T valor) {
        throw new UnsupportedOperationException("En un ABB la posición la decide el orden: usá insertar");
    }

    @Override
    public boolean buscar(T valor) {
        return obtener(valor) != null;
    }

    public T obtener(T clave) {
        if (clave == null) return null;
        NodoBinario<T> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.getValor());
            if (comparacion == 0) return actual.getValor();
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return null;
    }

    public boolean eliminar(T valor) {
        if (!buscar(valor)) return false;
        raiz = eliminar(raiz, valor);
        return true;
    }

    private NodoBinario<T> eliminar(NodoBinario<T> nodo, T valor) {
        if (nodo == null) return null;
        int comparacion = valor.compareTo(nodo.getValor());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), valor));
            return nodo;
        }
        if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), valor));
            return nodo;
        }
        if (nodo.getIzquierdo() == null) return nodo.getDerecho();
        if (nodo.getDerecho() == null) return nodo.getIzquierdo();
        T sucesor = minimo(nodo.getDerecho());
        nodo.setValor(sucesor);
        nodo.setDerecho(eliminar(nodo.getDerecho(), sucesor));
        return nodo;
    }

    public T minimo() {
        return raiz == null ? null : minimo(raiz);
    }

    private T minimo(NodoBinario<T> nodo) {
        if (nodo.getIzquierdo() == null) return nodo.getValor();
        return minimo(nodo.getIzquierdo());
    }

    public T maximo() {
        return raiz == null ? null : maximo(raiz);
    }

    private T maximo(NodoBinario<T> nodo) {
        if (nodo.getDerecho() == null) return nodo.getValor();
        return maximo(nodo.getDerecho());
    }

    public ListaDinamica<T> inOrdenIterativo() {
        ListaDinamica<T> recorrido = new ListaDinamica<>();
        PilaEnlazada<NodoBinario<T>> pendientes = new PilaEnlazada<>();
        NodoBinario<T> actual = raiz;
        while (actual != null || !pendientes.estaVacia()) {
            while (actual != null) {
                pendientes.apilar(actual);
                actual = actual.getIzquierdo();
            }
            actual = pendientes.desapilar();
            recorrido.agregar(actual.getValor());
            actual = actual.getDerecho();
        }
        return recorrido;
    }

    public static <T extends Comparable<T>> ArbolBinarioDeBusqueda<T> desdePreOrden(ListaDinamica<T> valores) {
        ArbolBinarioDeBusqueda<T> arbol = new ArbolBinarioDeBusqueda<>();
        for (int i = 0; i < valores.tamanio(); i++) arbol.agregar(valores.obtener(i));
        return arbol;
    }
}
