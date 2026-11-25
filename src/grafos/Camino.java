package grafos;

import estructuras.ListaDinamica;

public final class Camino<T> {
    private final ListaDinamica<T> vertices;
    private final double costo;

    private Camino(ListaDinamica<T> vertices, double costo) {
        this.vertices = vertices;
        this.costo = costo;
    }

    static <T> Camino<T> de(ListaDinamica<T> vertices, double costo) {
        return new Camino<>(vertices, costo);
    }

    public static <T> Camino<T> inexistente() {
        return new Camino<>(new ListaDinamica<>(), Double.POSITIVE_INFINITY);
    }

    public boolean existe() {
        return !vertices.estaVacia();
    }

    public ListaDinamica<T> getVertices() {
        return vertices.copia();
    }

    public double getCosto() {
        return costo;
    }

    public int pasos() {
        return Math.max(0, vertices.tamanio() - 1);
    }

    @Override
    public String toString() {
        return existe() ? vertices.unir(" → ") : "sin camino";
    }
}
