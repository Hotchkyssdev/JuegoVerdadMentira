package juego.reglas.registro;

import arboles.ArbolBinarioDeBusqueda;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;

public class Ranking {
    public static final int MAXIMO = 10;

    private final ArbolBinarioDeBusqueda<Puntaje> arbol;

    public Ranking() {
        arbol = new ArbolBinarioDeBusqueda<>();
    }

    public Ranking(ArbolBinarioDeBusqueda<Puntaje> arbol) {
        if (arbol == null) throw new DatoInvalidoException("El ranking necesita un árbol");
        this.arbol = arbol;
        while (arbol.cantidadDeNodos() > MAXIMO) arbol.eliminar(arbol.maximo());
    }

    public int agregar(Puntaje puntaje) {
        if (!arbol.agregar(puntaje)) return posicionDe(puntaje);
        if (arbol.cantidadDeNodos() > MAXIMO) arbol.eliminar(arbol.maximo());
        return posicionDe(puntaje);
    }

    public int posicionDe(Puntaje puntaje) {
        return mejores().indiceDe(puntaje) + 1;
    }

    public ListaDinamica<Puntaje> mejores() {
        return arbol.inOrdenIterativo();
    }

    public int cantidad() {
        return arbol.cantidadDeNodos();
    }

    public boolean estaVacio() {
        return arbol.estaVacio();
    }

    public ArbolBinarioDeBusqueda<Puntaje> getArbol() {
        return arbol;
    }
}
