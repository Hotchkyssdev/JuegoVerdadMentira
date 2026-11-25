package estructuras;

import java.util.Random;

public class ListaDinamica<T> implements Lista<T> {
    private static final int CAPACIDAD_INICIAL = 8;

    private Object[] elementos = new Object[CAPACIDAD_INICIAL];
    private int cantidad;

    public static <T> ListaDinamica<T> de(T primero) {
        ListaDinamica<T> lista = new ListaDinamica<>();
        lista.agregar(primero);
        return lista;
    }

    public static <T> ListaDinamica<T> de(T primero, T segundo) {
        ListaDinamica<T> lista = de(primero);
        lista.agregar(segundo);
        return lista;
    }

    @Override
    public void agregar(T elemento) {
        asegurarCapacidad(cantidad + 1);
        elementos[cantidad] = elemento;
        cantidad++;
    }

    public void agregarTodos(Lista<? extends T> otra) {
        for (int i = 0; i < otra.tamanio(); i++) agregar(otra.obtener(i));
    }

    @Override
    public void insertar(int indice, T elemento) {
        if (indice < 0 || indice > cantidad) {
            throw new IndexOutOfBoundsException("Posición " + indice + " fuera de una lista de " + cantidad);
        }
        asegurarCapacidad(cantidad + 1);
        for (int i = cantidad; i > indice; i--) elementos[i] = elementos[i - 1];
        elementos[indice] = elemento;
        cantidad++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T obtener(int indice) {
        validarIndice(indice);
        return (T) elementos[indice];
    }

    public void reemplazar(int indice, T elemento) {
        validarIndice(indice);
        elementos[indice] = elemento;
    }

    @Override
    public T eliminar(int indice) {
        T eliminado = obtener(indice);
        for (int i = indice; i < cantidad - 1; i++) elementos[i] = elementos[i + 1];
        cantidad--;
        elementos[cantidad] = null;
        return eliminado;
    }

    public boolean quitar(T elemento) {
        int indice = indiceDe(elemento);
        if (indice < 0) return false;
        eliminar(indice);
        return true;
    }

    @Override
    public int indiceDe(T elemento) {
        for (int i = 0; i < cantidad; i++) {
            Object actual = elementos[i];
            if (actual == null ? elemento == null : actual.equals(elemento)) return i;
        }
        return -1;
    }

    public boolean contiene(T elemento) {
        return indiceDe(elemento) >= 0;
    }

    public T primero() {
        if (estaVacia()) throw new EstructuraVaciaException("La lista está vacía: no tiene primero");
        return obtener(0);
    }

    public T ultimo() {
        if (estaVacia()) throw new EstructuraVaciaException("La lista está vacía: no tiene último");
        return obtener(cantidad - 1);
    }

    public T buscar(Criterio<? super T> criterio) {
        for (int i = 0; i < cantidad; i++) {
            T elemento = obtener(i);
            if (criterio.cumple(elemento)) return elemento;
        }
        return null;
    }

    public ListaDinamica<T> filtrar(Criterio<? super T> criterio) {
        ListaDinamica<T> resultado = new ListaDinamica<>();
        for (int i = 0; i < cantidad; i++) {
            T elemento = obtener(i);
            if (criterio.cumple(elemento)) resultado.agregar(elemento);
        }
        return resultado;
    }

    public boolean algunoCumple(Criterio<? super T> criterio) {
        for (int i = 0; i < cantidad; i++) {
            if (criterio.cumple(obtener(i))) return true;
        }
        return false;
    }

    public boolean todosCumplen(Criterio<? super T> criterio) {
        for (int i = 0; i < cantidad; i++) {
            if (!criterio.cumple(obtener(i))) return false;
        }
        return true;
    }

    public int contar(Criterio<? super T> criterio) {
        int total = 0;
        for (int i = 0; i < cantidad; i++) {
            if (criterio.cumple(obtener(i))) total++;
        }
        return total;
    }

    public void ordenar(Comparador<? super T> comparador) {
        for (int i = 1; i < cantidad; i++) {
            T actual = obtener(i);
            int j = i - 1;
            while (j >= 0 && comparador.comparar(obtener(j), actual) > 0) {
                elementos[j + 1] = elementos[j];
                j--;
            }
            elementos[j + 1] = actual;
        }
    }

    public void mezclar(Random azar) {
        for (int i = cantidad - 1; i > 0; i--) {
            int j = azar.nextInt(i + 1);
            Object auxiliar = elementos[i];
            elementos[i] = elementos[j];
            elementos[j] = auxiliar;
        }
    }

    public ListaDinamica<T> copia() {
        ListaDinamica<T> copia = new ListaDinamica<>();
        copia.agregarTodos(this);
        return copia;
    }

    public ListaDinamica<T> invertida() {
        ListaDinamica<T> resultado = new ListaDinamica<>();
        for (int i = cantidad - 1; i >= 0; i--) resultado.agregar(obtener(i));
        return resultado;
    }

    @Override
    public int tamanio() {
        return cantidad;
    }

    @Override
    public boolean estaVacia() {
        return cantidad == 0;
    }

    @Override
    public void vaciar() {
        for (int i = 0; i < cantidad; i++) elementos[i] = null;
        cantidad = 0;
    }

    public String unir(String separador) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < cantidad; i++) {
            if (i > 0) texto.append(separador);
            texto.append(elementos[i]);
        }
        return texto.toString();
    }

    @Override
    public String toString() {
        return "[" + unir(", ") + "]";
    }

    private void asegurarCapacidad(int necesaria) {
        if (necesaria <= elementos.length) return;
        Object[] nuevos = new Object[Math.max(necesaria, elementos.length * 2)];
        for (int i = 0; i < cantidad; i++) nuevos[i] = elementos[i];
        elementos = nuevos;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IndexOutOfBoundsException("Posición " + indice + " fuera de una lista de " + cantidad);
        }
    }
}
