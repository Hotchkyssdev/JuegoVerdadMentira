package verdadmentira.domain;

import verdadmentira.enumeration.TipoDeRelacion;

import java.util.ArrayList;
import java.util.List;

public class GrafoPersonajes {
    public static class Arista {
        private final Personaje origen;
        private final Personaje destino;
        private final TipoDeRelacion tipo;

        public Arista(Personaje origen, Personaje destino, TipoDeRelacion tipo) {
            this.origen = origen;
            this.destino = destino;
            this.tipo = tipo;
        }

        public Personaje getOrigen() {
            return origen;
        }

        public Personaje getDestino() {
            return destino;
        }

        public TipoDeRelacion getTipo() {
            return tipo;
        }
    }

    private final List<Arista> aristas = new ArrayList<>();

    public void agregarRelacion(Personaje origen, Personaje destino, boolean afirmaQueDiceVerdad) {
        aristas.add(new Arista(origen, destino,
                afirmaQueDiceVerdad ? TipoDeRelacion.DICE_QUE_DICE_LA_VERDAD : TipoDeRelacion.DICE_QUE_MIENTE));
    }

    public void agregarDescripcion(Personaje origen, Personaje descripto) {
        aristas.add(new Arista(origen, descripto, TipoDeRelacion.DESCRIBE_UN_RASGO));
    }

    public List<Arista> getAristas() {
        return aristas;
    }
}
