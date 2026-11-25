package estructuras;

public class ColaEnlazada<T> implements Cola<T> {
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int cantidad;

    @Override
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (ultimo == null) primero = nuevo;
        else ultimo.siguiente = nuevo;
        ultimo = nuevo;
        cantidad++;
    }

    @Override
    public T desencolar() {
        if (estaVacia()) throw new EstructuraVaciaException("No se puede desencolar de una cola vacía");
        T valor = primero.valor;
        primero = primero.siguiente;
        if (primero == null) ultimo = null;
        cantidad--;
        return valor;
    }

    @Override
    public T frente() {
        if (estaVacia()) throw new EstructuraVaciaException("Una cola vacía no tiene frente");
        return primero.valor;
    }

    @Override
    public boolean estaVacia() {
        return primero == null;
    }

    @Override
    public int tamanio() {
        return cantidad;
    }

    @Override
    public void vaciar() {
        primero = null;
        ultimo = null;
        cantidad = 0;
    }
}
