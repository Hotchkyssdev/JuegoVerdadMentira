package estructuras;

public class Diccionario<K, V> {
    private static class Entrada<K, V> {
        private final K clave;
        private V valor;

        private Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private final ListaDinamica<Entrada<K, V>> entradas = new ListaDinamica<>();

    public void poner(K clave, V valor) {
        if (clave == null) throw new IllegalArgumentException("El diccionario no acepta claves nulas");
        int indice = indiceDe(clave);
        if (indice >= 0) entradas.obtener(indice).valor = valor;
        else entradas.agregar(new Entrada<>(clave, valor));
    }

    public V obtener(K clave) {
        int indice = indiceDe(clave);
        return indice < 0 ? null : entradas.obtener(indice).valor;
    }

    public boolean contieneClave(K clave) {
        return indiceDe(clave) >= 0;
    }

    public boolean quitar(K clave) {
        int indice = indiceDe(clave);
        if (indice < 0) return false;
        entradas.eliminar(indice);
        return true;
    }

    public ListaDinamica<K> claves() {
        ListaDinamica<K> claves = new ListaDinamica<>();
        for (int i = 0; i < entradas.tamanio(); i++) claves.agregar(entradas.obtener(i).clave);
        return claves;
    }

    public int tamanio() {
        return entradas.tamanio();
    }

    public boolean estaVacio() {
        return entradas.estaVacia();
    }

    public void vaciar() {
        entradas.vaciar();
    }

    public Diccionario<K, V> copia() {
        Diccionario<K, V> copia = new Diccionario<>();
        for (int i = 0; i < entradas.tamanio(); i++) {
            Entrada<K, V> entrada = entradas.obtener(i);
            copia.poner(entrada.clave, entrada.valor);
        }
        return copia;
    }

    private int indiceDe(K clave) {
        if (clave == null) return -1;
        for (int i = 0; i < entradas.tamanio(); i++) {
            if (entradas.obtener(i).clave.equals(clave)) return i;
        }
        return -1;
    }
}
