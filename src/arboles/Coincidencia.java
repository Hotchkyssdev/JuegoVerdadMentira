package arboles;

public final class Coincidencia<V> {
    private final String clave;
    private final V valor;
    private final int inicio;
    private final int largo;

    public Coincidencia(String clave, V valor, int inicio, int largo) {
        if (largo <= 0) throw new IllegalArgumentException("Una coincidencia tiene al menos un carácter");
        this.clave = clave;
        this.valor = valor;
        this.inicio = inicio;
        this.largo = largo;
    }

    public String getClave() {
        return clave;
    }

    public V getValor() {
        return valor;
    }

    public int getInicio() {
        return inicio;
    }

    public int getLargo() {
        return largo;
    }

    public int getFin() {
        return inicio + largo;
    }
}
