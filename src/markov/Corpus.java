package markov;

import estructuras.ListaDinamica;

public class Corpus {
    private final ListaDinamica<String> lineas = new ListaDinamica<>();
    private final ListaDinamica<String> expresiones = new ListaDinamica<>();

    public void agregarLinea(String linea) {
        if (linea == null || linea.trim().isEmpty()) throw new IllegalArgumentException("Una línea del corpus no puede estar vacía");
        lineas.agregar(linea.trim());
    }

    public void agregarExpresion(String expresion) {
        if (expresion == null || expresion.trim().indexOf(' ') < 0) {
            throw new IllegalArgumentException("Una expresión tiene que tener más de una palabra: " + expresion);
        }
        expresiones.agregar(expresion.trim());
    }

    public ListaDinamica<String> getLineas() {
        return lineas.copia();
    }

    public ListaDinamica<String> getExpresiones() {
        return expresiones.copia();
    }

    public boolean estaVacio() {
        return lineas.estaVacia();
    }
}
