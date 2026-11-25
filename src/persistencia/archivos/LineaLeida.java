package persistencia.archivos;

public final class LineaLeida {
    private final int numero;
    private final String texto;

    LineaLeida(int numero, String texto) {
        this.numero = numero;
        this.texto = texto;
    }

    public int getNumero() {
        return numero;
    }

    public String getTexto() {
        return texto;
    }
}
