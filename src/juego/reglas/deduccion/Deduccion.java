package juego.reglas.deduccion;

import estructuras.ListaDinamica;
import juego.reglas.personajes.Marcas;
import tokens.Token;

public class Deduccion {
    private final Marcas marcas;
    private final ListaDinamica<String> errores;
    private final ListaDinamica<Token<Significado>> tokens;

    Deduccion(Marcas marcas, ListaDinamica<String> errores, ListaDinamica<Token<Significado>> tokens) {
        this.marcas = marcas;
        this.errores = errores;
        this.tokens = tokens;
    }

    public boolean esValida() {
        return errores.estaVacia() && !marcas.estaVacia();
    }

    public Marcas getMarcas() {
        return marcas.copia();
    }

    public ListaDinamica<String> getErrores() {
        return errores.copia();
    }

    public ListaDinamica<Token<Significado>> getTokens() {
        return tokens.copia();
    }
}
