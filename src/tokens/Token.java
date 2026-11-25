package tokens;

public final class Token<V> {
    private final String texto;
    private final V valor;
    private final int inicio;
    private final boolean reconocido;

    private Token(String texto, V valor, int inicio, boolean reconocido) {
        if (texto == null || texto.isEmpty()) throw new IllegalArgumentException("Un token no puede estar vacío");
        this.texto = texto;
        this.valor = valor;
        this.inicio = inicio;
        this.reconocido = reconocido;
    }

    static <V> Token<V> reconocido(String texto, V valor, int inicio) {
        return new Token<>(texto, valor, inicio, true);
    }

    static <V> Token<V> desconocido(String texto, int inicio) {
        return new Token<>(texto, null, inicio, false);
    }

    public String getTexto() {
        return texto;
    }

    public V getValor() {
        return valor;
    }

    public int getInicio() {
        return inicio;
    }

    public boolean esReconocido() {
        return reconocido;
    }

    @Override
    public String toString() {
        return reconocido ? texto : "¿" + texto + "?";
    }
}
