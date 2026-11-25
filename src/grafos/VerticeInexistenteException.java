package grafos;

public class VerticeInexistenteException extends RuntimeException {
    public VerticeInexistenteException(Object vertice) {
        super("El vértice " + vertice + " no está en el grafo");
    }
}
