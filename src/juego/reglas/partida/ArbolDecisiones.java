package juego.reglas.partida;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;

public class ArbolDecisiones {
    private final ArbolBinario<Decision> arbol;
    private NodoBinario<Decision> ultimo;

    public ArbolDecisiones(String nombre) {
        arbol = new ArbolBinario<>(Decision.raiz(nombre));
        ultimo = arbol.getRaiz();
    }

    private ArbolDecisiones(ArbolBinario<Decision> arbol, NodoBinario<Decision> ultimo) {
        this.arbol = arbol;
        this.ultimo = ultimo;
    }

    public static ArbolDecisiones desde(ArbolBinario<Decision> arbol) {
        if (arbol == null || arbol.estaVacio()) throw new DatoInvalidoException("El árbol de decisiones no puede estar vacío");
        NodoBinario<Decision> actual = arbol.getRaiz();
        if (!actual.getValor().esRaiz()) throw new DatoInvalidoException("La raíz tiene que ser el nombre del personaje");
        while (!actual.esHoja()) {
            if (actual.getIzquierdo() != null && actual.getDerecho() != null) {
                throw new DatoInvalidoException("En cada ronda se elige un solo lado");
            }
            NodoBinario<Decision> hijo = actual.getIzquierdo() != null ? actual.getIzquierdo() : actual.getDerecho();
            Boolean eligio = hijo.getValor().getEligioVeraz();
            if (eligio == null || eligio != (hijo == actual.getDerecho())) {
                throw new DatoInvalidoException("Veraz va a la derecha y mentiroso a la izquierda");
            }
            actual = hijo;
        }
        if (arbol.altura() - 1 > Juego.RONDAS) throw new DatoInvalidoException("Hay más rondas que las del juego");
        return new ArbolDecisiones(arbol, actual);
    }

    public void agregarRonda(boolean eligioVeraz) {
        if (cantidadDeRondas() >= Juego.RONDAS) throw new DatoInvalidoException("Ya se registraron las " + Juego.RONDAS + " rondas");
        Decision decision = Decision.ronda(eligioVeraz);
        ultimo = eligioVeraz ? arbol.insertarDerecho(ultimo, decision) : arbol.insertarIzquierdo(ultimo, decision);
    }

    public String getNombre() {
        return arbol.getRaiz().getValor().getEtiqueta();
    }

    public ListaDinamica<Boolean> getCamino() {
        ListaDinamica<Decision> niveles = arbol.porNiveles();
        ListaDinamica<Boolean> camino = new ListaDinamica<>();
        for (int i = 1; i < niveles.tamanio(); i++) camino.agregar(niveles.obtener(i).getEligioVeraz());
        return camino;
    }

    public int cantidadDeRondas() {
        return arbol.altura() - 1;
    }

    public ArbolBinario<Decision> getArbol() {
        return arbol;
    }
}
