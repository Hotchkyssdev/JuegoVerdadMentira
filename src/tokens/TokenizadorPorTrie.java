package tokens;

import arboles.Coincidencia;
import arboles.Trie;
import estructuras.ListaDinamica;

public class TokenizadorPorTrie<V> {
    private final Trie<V> vocabulario = new Trie<>();

    public void agregar(String forma, V valor) {
        String normalizada = Normalizador.normalizar(forma).trim();
        if (normalizada.isEmpty()) throw new IllegalArgumentException("No se puede agregar un token vacío");
        vocabulario.insertar(normalizada, valor);
    }

    public boolean conoce(String forma) {
        return vocabulario.contiene(Normalizador.normalizar(forma).trim());
    }

    public int tamanioDelVocabulario() {
        return vocabulario.tamanio();
    }

    public ListaDinamica<String> sugerencias(String prefijo) {
        String normalizado = Normalizador.normalizar(prefijo);
        if (normalizado.isEmpty()) return new ListaDinamica<>();
        return vocabulario.conPrefijo(normalizado);
    }

    public ListaDinamica<Token<V>> tokenizar(String texto) {
        ListaDinamica<Token<V>> tokens = new ListaDinamica<>();
        String normalizado = Normalizador.normalizar(texto);
        int i = 0;
        while (i < normalizado.length()) {
            if (Normalizador.esEspacio(normalizado.charAt(i))) {
                i++;
                continue;
            }
            Coincidencia<V> coincidencia = vocabulario.coincidenciaMasLarga(normalizado, i);
            if (coincidencia != null && terminaEnBorde(normalizado, coincidencia)) {
                tokens.agregar(Token.reconocido(coincidencia.getClave(), coincidencia.getValor(), i));
                i = coincidencia.getFin();
            } else {
                int fin = finDePalabra(normalizado, i);
                tokens.agregar(Token.desconocido(normalizado.substring(i, fin), i));
                i = fin;
            }
        }
        return tokens;
    }

    private static boolean terminaEnBorde(String texto, Coincidencia<?> coincidencia) {
        int fin = coincidencia.getFin();
        if (fin >= texto.length()) return true;
        char ultima = texto.charAt(fin - 1);
        char siguiente = texto.charAt(fin);
        return !(Normalizador.esLetra(ultima) && Normalizador.esLetra(siguiente));
    }

    private static int finDePalabra(String texto, int inicio) {
        int fin = inicio + 1;
        if (!Normalizador.esLetra(texto.charAt(inicio))) return fin;
        while (fin < texto.length() && Normalizador.esLetra(texto.charAt(fin))) fin++;
        return fin;
    }
}
