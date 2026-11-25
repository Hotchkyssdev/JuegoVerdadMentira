package arboles;

import estructuras.ListaDinamica;

public class Trie<V> {
    private final NodoTrie<V> raiz = new NodoTrie<>('\0');
    private int cantidad;

    public void insertar(String clave, V valor) {
        if (clave == null || clave.isEmpty()) throw new IllegalArgumentException("El trie no acepta claves vacías");
        NodoTrie<V> actual = raiz;
        for (int i = 0; i < clave.length(); i++) actual = actual.hijoOCrear(clave.charAt(i));
        if (!actual.esTerminal()) cantidad++;
        actual.marcarTerminal(clave, valor);
    }

    public boolean contiene(String clave) {
        NodoTrie<V> nodo = nodoDe(clave);
        return nodo != null && nodo.esTerminal();
    }

    public V buscar(String clave) {
        NodoTrie<V> nodo = nodoDe(clave);
        return nodo != null && nodo.esTerminal() ? nodo.getValor() : null;
    }

    public ListaDinamica<String> conPrefijo(String prefijo) {
        ListaDinamica<String> claves = new ListaDinamica<>();
        NodoTrie<V> inicio = nodoDe(prefijo);
        if (inicio != null) juntarClaves(inicio, claves);
        return claves;
    }

    private void juntarClaves(NodoTrie<V> nodo, ListaDinamica<String> claves) {
        if (nodo.esTerminal()) claves.agregar(nodo.getClave());
        ListaDinamica<NodoTrie<V>> hijos = nodo.getHijos();
        for (int i = 0; i < hijos.tamanio(); i++) juntarClaves(hijos.obtener(i), claves);
    }

    public Coincidencia<V> coincidenciaMasLarga(String texto, int desde) {
        NodoTrie<V> actual = raiz;
        NodoTrie<V> ultimaTerminal = null;
        int largoDeLaUltima = 0;
        for (int i = desde; i < texto.length(); i++) {
            actual = actual.hijo(texto.charAt(i));
            if (actual == null) break;
            if (actual.esTerminal()) {
                ultimaTerminal = actual;
                largoDeLaUltima = i - desde + 1;
            }
        }
        if (ultimaTerminal == null) return null;
        return new Coincidencia<>(ultimaTerminal.getClave(), ultimaTerminal.getValor(), desde, largoDeLaUltima);
    }

    public int tamanio() {
        return cantidad;
    }

    public boolean estaVacio() {
        return cantidad == 0;
    }

    private NodoTrie<V> nodoDe(String texto) {
        if (texto == null) return null;
        NodoTrie<V> actual = raiz;
        for (int i = 0; i < texto.length() && actual != null; i++) actual = actual.hijo(texto.charAt(i));
        return actual;
    }
}
