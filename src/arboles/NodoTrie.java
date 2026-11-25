package arboles;

import estructuras.ListaDinamica;

class NodoTrie<V> {
    private final char letra;
    private final ListaDinamica<NodoTrie<V>> hijos = new ListaDinamica<>();
    private boolean terminal;
    private String clave;
    private V valor;

    NodoTrie(char letra) {
        this.letra = letra;
    }

    char getLetra() {
        return letra;
    }

    NodoTrie<V> hijo(char letra) {
        for (int i = 0; i < hijos.tamanio(); i++) {
            NodoTrie<V> hijo = hijos.obtener(i);
            if (hijo.letra == letra) return hijo;
        }
        return null;
    }

    NodoTrie<V> hijoOCrear(char letra) {
        NodoTrie<V> existente = hijo(letra);
        if (existente != null) return existente;
        NodoTrie<V> nuevo = new NodoTrie<>(letra);
        int posicion = 0;
        while (posicion < hijos.tamanio() && hijos.obtener(posicion).letra < letra) posicion++;
        hijos.insertar(posicion, nuevo);
        return nuevo;
    }

    ListaDinamica<NodoTrie<V>> getHijos() {
        return hijos;
    }

    boolean esTerminal() {
        return terminal;
    }

    String getClave() {
        return clave;
    }

    V getValor() {
        return valor;
    }

    void marcarTerminal(String clave, V valor) {
        this.terminal = true;
        this.clave = clave;
        this.valor = valor;
    }
}
