package juego.reglas.ronda;

import estructuras.ListaDinamica;
import grafos.Camino;
import grafos.Grafo;
import juego.reglas.personajes.Personaje;

public class GrafoDeRonda {
    private final Grafo<Personaje> frases = new Grafo<>();
    private final Grafo<Personaje> deduccion = new Grafo<>();
    private final ListaDinamica<Relacion> relaciones = new ListaDinamica<>();

    public GrafoDeRonda(ListaDinamica<Personaje> personajes, ListaDinamica<Afirmacion> afirmaciones) {
        for (int i = 0; i < personajes.tamanio(); i++) {
            frases.agregarVertice(personajes.obtener(i));
            deduccion.agregarVertice(personajes.obtener(i));
        }
        for (int i = 0; i < afirmaciones.tamanio(); i++) afirmaciones.obtener(i).agregarAlGrafo(this);
    }

    void agregarRelacion(Personaje origen, Personaje destino, TipoDeRelacion tipo, double peso) {
        relaciones.agregar(new Relacion(origen, destino, tipo));
        if (!origen.equals(destino)) frases.agregarArista(origen, destino, peso);
    }

    void agregarDeduccion(Personaje origen, Personaje destino, double peso) {
        deduccion.agregarArista(origen, destino, peso);
    }

    public ListaDinamica<Relacion> getRelaciones() {
        return relaciones.copia();
    }

    public ListaDinamica<Personaje> ordenDeAparicion(Personaje inicio) {
        ListaDinamica<Personaje> orden = frases.dfs(inicio);
        ListaDinamica<Personaje> todos = frases.getVertices();
        for (int i = 0; i < todos.tamanio(); i++) {
            if (orden.contiene(todos.obtener(i))) continue;
            ListaDinamica<Personaje> resto = frases.dfs(todos.obtener(i));
            for (int j = 0; j < resto.tamanio(); j++) {
                if (!orden.contiene(resto.obtener(j))) orden.agregar(resto.obtener(j));
            }
        }
        return orden;
    }

    public ListaDinamica<Personaje> ciclo() {
        return frases.buscarCiclo();
    }

    public int componentes() {
        return frases.componentesConexas();
    }

    public Camino<Personaje> caminoDeDeduccion(Personaje desde, Personaje hasta) {
        return deduccion.caminoMinimo(desde, hasta);
    }
}
