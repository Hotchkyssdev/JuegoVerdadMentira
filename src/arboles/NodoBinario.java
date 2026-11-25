package arboles;

public class NodoBinario<T> {
    private T valor;
    private NodoBinario<T> izquierdo;
    private NodoBinario<T> derecho;

    public NodoBinario(T valor) {
        this.valor = valor;
    }

    public T getValor() {
        return valor;
    }

    void setValor(T valor) {
        this.valor = valor;
    }

    public NodoBinario<T> getIzquierdo() {
        return izquierdo;
    }

    void setIzquierdo(NodoBinario<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoBinario<T> getDerecho() {
        return derecho;
    }

    void setDerecho(NodoBinario<T> derecho) {
        this.derecho = derecho;
    }

    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }
}
