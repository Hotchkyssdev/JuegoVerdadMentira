package markov;

public final class Generacion {
    private final String texto;
    private final int simbolos;
    private final int retrocesos;
    private final boolean cortadaPorLargo;
    private final int intentos;

    Generacion(String texto, int simbolos, int retrocesos, boolean cortadaPorLargo, int intentos) {
        if (texto == null || texto.trim().isEmpty()) throw new IllegalArgumentException("Una generación no puede estar vacía");
        this.texto = texto;
        this.simbolos = simbolos;
        this.retrocesos = retrocesos;
        this.cortadaPorLargo = cortadaPorLargo;
        this.intentos = intentos;
    }

    public String getTexto() {
        return texto;
    }

    public int getSimbolos() {
        return simbolos;
    }

    public int getRetrocesos() {
        return retrocesos;
    }

    public boolean fueCortadaPorLargo() {
        return cortadaPorLargo;
    }

    public int getIntentos() {
        return intentos;
    }
}
