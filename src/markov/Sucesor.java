package markov;

public class Sucesor {
    private final String simbolo;
    private int frecuencia;

    Sucesor(String simbolo) {
        if (simbolo == null || simbolo.isEmpty()) throw new IllegalArgumentException("Un sucesor necesita un símbolo");
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public int getFrecuencia() {
        return frecuencia;
    }

    void sumarUno() {
        frecuencia++;
    }
}
