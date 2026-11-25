package juego.reglas.rumores;

import juego.reglas.DatoInvalidoException;
import markov.Corpus;
import markov.Generacion;
import markov.GeneradorDeTexto;
import markov.ModeloMarkov;
import markov.ModeloMarkovDeCaracteres;
import markov.ModeloMarkovDeTokens;
import tokens.TokenizadorPorTrie;

import java.util.Random;

public class Rumores {
    private final Corpus corpus;
    private final TokenizadorPorTrie<String> tokenizador;
    private final Random azar;
    private ModeloMarkov modelo;

    public Rumores(Corpus corpus, Random azar) {
        if (corpus == null || corpus.estaVacio()) throw new DatoInvalidoException("Los rumores necesitan un corpus con líneas");
        this.corpus = corpus;
        this.azar = azar;
        this.tokenizador = ModeloMarkovDeTokens.tokenizadorPara(corpus);
    }

    public void configurar(int orden, ModoDeMarkov modo) {
        ModeloMarkov nuevo = modo == ModoDeMarkov.TOKENS
                ? new ModeloMarkovDeTokens(orden, tokenizador)
                : new ModeloMarkovDeCaracteres(orden);
        nuevo.entrenar(corpus.getLineas());
        modelo = nuevo;
    }

    public Generacion nuevo() {
        return generador().generar(azar);
    }

    public GeneradorDeTexto generador() {
        if (modelo == null) throw new DatoInvalidoException("Falta configurar el orden y el modo de los rumores");
        return modelo;
    }

    public int cantidadDeEstados() {
        return modelo == null ? 0 : modelo.cantidadDeEstados();
    }

    public int alturaDeLaTabla() {
        return modelo == null ? 0 : modelo.alturaDeLaTabla();
    }
}
