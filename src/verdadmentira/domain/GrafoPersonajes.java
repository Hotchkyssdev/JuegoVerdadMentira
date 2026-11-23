package verdadmentira.domain;

import java.util.ArrayList;
import java.util.List;

public class GrafoPersonajes {
    public static class Arista {
        private final Personaje origen;
        private final Personaje destino;
        private final boolean afirmaQueDiceVerdad;

        public Arista(Personaje origen, Personaje destino, boolean afirmaQueDiceVerdad) {
            this.origen = origen;
            this.destino = destino;
            this.afirmaQueDiceVerdad = afirmaQueDiceVerdad;
        }

        public Personaje getOrigen() {
            return origen;
        }

        public Personaje getDestino() {
            return destino;
        }

        public boolean afirmaQueDiceVerdad() {
            return afirmaQueDiceVerdad;
        }
    }

    private final List<Arista> aristas = new ArrayList<>();

    public void agregarRelacion(Personaje origen, Personaje destino, boolean afirmaQueDiceVerdad) {
        aristas.add(new Arista(origen, destino, afirmaQueDiceVerdad));
    }

    public List<Arista> getAristas() {
        return aristas;
    }
}
