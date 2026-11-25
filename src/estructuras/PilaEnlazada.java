package estructuras;

public class PilaEnlazada<T> implements Pila<T> {
    private static class Nodo<T> {
        private final T valor;
        private final Nodo<T> debajo;

        private Nodo(T valor, Nodo<T> debajo) {
            this.valor = valor;
            this.debajo = debajo;
        }
    }

    private Nodo<T> cima;
    private int cantidad;

    @Override
    public void apilar(T elemento) {
        cima = new Nodo<>(elemento, cima);
        cantidad++;
    }

    @Override
    public T desapilar() {
        if (estaVacia()) throw new EstructuraVaciaException("No se puede desapilar de una pila vacía");
        T valor = cima.valor;
        cima = cima.debajo;
        cantidad--;
        return valor;
    }

    @Override
    public T tope() {
        if (estaVacia()) throw new EstructuraVaciaException("Una pila vacía no tiene tope");
        return cima.valor;
    }

    @Override
    public boolean estaVacia() {
        return cima == null;
    }

    @Override
    public int tamanio() {
        return cantidad;
    }

    @Override
    public void vaciar() {
        cima = null;
        cantidad = 0;
    }
}
