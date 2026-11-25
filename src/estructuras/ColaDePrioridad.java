package estructuras;

public class ColaDePrioridad<T> {
    private final ListaDinamica<T> deMayorAMenor = new ListaDinamica<>();
    private final Comparador<? super T> comparador;

    public ColaDePrioridad(Comparador<? super T> comparador) {
        if (comparador == null) throw new IllegalArgumentException("La cola de prioridad necesita un comparador");
        this.comparador = comparador;
    }

    public void encolar(T valor) {
        int posicion = deMayorAMenor.tamanio();
        while (posicion > 0 && comparador.comparar(deMayorAMenor.obtener(posicion - 1), valor) <= 0) posicion--;
        deMayorAMenor.insertar(posicion, valor);
    }

    public T desencolarMinimo() {
        if (estaVacia()) throw new EstructuraVaciaException("La cola de prioridad está vacía");
        return deMayorAMenor.eliminar(deMayorAMenor.tamanio() - 1);
    }

    public boolean estaVacia() {
        return deMayorAMenor.estaVacia();
    }

    public int tamanio() {
        return deMayorAMenor.tamanio();
    }
}
