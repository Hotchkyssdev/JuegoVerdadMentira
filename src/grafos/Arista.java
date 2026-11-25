package grafos;

public class Arista<T> {
    private final T origen;
    private final T destino;
    private double peso;

    public Arista(T origen, T destino, double peso) {
        if (origen == null || destino == null) throw new IllegalArgumentException("Una arista necesita origen y destino");
        this.origen = origen;
        this.destino = destino;
        setPeso(peso);
    }

    public T getOrigen() {
        return origen;
    }

    public T getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        if (peso < 0 || Double.isNaN(peso) || Double.isInfinite(peso)) {
            throw new IllegalArgumentException("El peso de una arista tiene que ser un número no negativo: " + peso);
        }
        this.peso = peso;
    }

    @Override
    public String toString() {
        return origen + " -> " + destino + " (" + peso + ")";
    }
}
