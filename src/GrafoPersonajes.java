package src;

import java.util.ArrayList;
import java.util.List;

public class GrafoPersonajes {
    public static class Arista {
        private final Personaje origen, destino;
        private final String etiqueta;

        public Arista(Personaje origen, Personaje destino, String etiqueta) {
            this.origen = origen;
            this.destino = destino;
            this.etiqueta = etiqueta;
        }

        public Personaje getOrigen() {
            return origen;
        }

        public Personaje getDestino() {
            return destino;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    private final List<Arista> aristas = new ArrayList<>();

    public void agregarRelacion(Personaje a, Personaje b, String etiqueta) {
        aristas.add(new Arista(a, b, etiqueta));
    }

    public List<Arista> getAristas() {
        return aristas;
    }
}