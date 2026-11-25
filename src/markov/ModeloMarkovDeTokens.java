package markov;

import estructuras.ListaDinamica;
import tokens.Normalizador;
import tokens.Token;
import tokens.TokenizadorPorTrie;

public class ModeloMarkovDeTokens extends ModeloMarkov {
    private static final int LARGO_MAXIMO = 32;
    private static final String SIN_ESPACIO_ANTES = ".,;:!?…";
    private static final String SIN_ESPACIO_DESPUES = "¿¡";

    private final TokenizadorPorTrie<String> tokenizador;

    public ModeloMarkovDeTokens(int orden, TokenizadorPorTrie<String> tokenizador) {
        super(orden);
        if (tokenizador == null) throw new IllegalArgumentException("El modelo por tokens necesita un tokenizador");
        this.tokenizador = tokenizador;
    }

    public static TokenizadorPorTrie<String> tokenizadorPara(Corpus corpus) {
        TokenizadorPorTrie<String> tokenizador = new TokenizadorPorTrie<>();
        ListaDinamica<String> lineas = corpus.getLineas();
        for (int i = 0; i < lineas.tamanio(); i++) agregarPalabras(tokenizador, lineas.obtener(i));
        ListaDinamica<String> expresiones = corpus.getExpresiones();
        for (int i = 0; i < expresiones.tamanio(); i++) tokenizador.agregar(expresiones.obtener(i), expresiones.obtener(i));
        return tokenizador;
    }

    private static void agregarPalabras(TokenizadorPorTrie<String> tokenizador, String linea) {
        int i = 0;
        while (i < linea.length()) {
            char c = linea.charAt(i);
            if (Normalizador.esEspacio(c)) {
                i++;
                continue;
            }
            if (!Normalizador.esLetra(c)) {
                if (!tokenizador.conoce(String.valueOf(c))) tokenizador.agregar(String.valueOf(c), String.valueOf(c));
                i++;
                continue;
            }
            int fin = i;
            while (fin < linea.length() && Normalizador.esLetra(linea.charAt(fin))) fin++;
            String palabra = linea.substring(i, fin);
            boolean alPrincipio = i == 0;
            if (!alPrincipio) tokenizador.agregar(palabra, palabra);
            else if (!tokenizador.conoce(palabra)) tokenizador.agregar(palabra, palabra.toLowerCase());
            i = fin;
        }
    }

    @Override
    protected ListaDinamica<String> separar(String linea) {
        ListaDinamica<Token<String>> tokens = tokenizador.tokenizar(linea);
        ListaDinamica<String> simbolos = new ListaDinamica<>();
        for (int i = 0; i < tokens.tamanio(); i++) {
            Token<String> token = tokens.obtener(i);
            simbolos.agregar(token.esReconocido() ? token.getValor() : token.getTexto());
        }
        return simbolos;
    }

    @Override
    protected String unir(ListaDinamica<String> simbolos) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < simbolos.tamanio(); i++) {
            String simbolo = simbolos.obtener(i);
            boolean pegado = texto.length() == 0
                    || SIN_ESPACIO_ANTES.indexOf(simbolo.charAt(0)) >= 0
                    || SIN_ESPACIO_DESPUES.indexOf(texto.charAt(texto.length() - 1)) >= 0;
            if (!pegado) texto.append(' ');
            texto.append(simbolo);
        }
        if (texto.length() > 0) texto.setCharAt(0, Character.toUpperCase(texto.charAt(0)));
        return texto.toString();
    }

    @Override
    protected int largoMaximo() {
        return LARGO_MAXIMO;
    }

    public int contarTokens(String linea) {
        return separar(linea).tamanio();
    }

    @Override
    public String getDescripcion() {
        return "por tokens, k = " + getOrden();
    }
}
