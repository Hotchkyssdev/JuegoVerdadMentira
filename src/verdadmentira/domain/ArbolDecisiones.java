package verdadmentira.domain;

import java.util.ArrayList;
import java.util.List;

public class ArbolDecisiones {
    public static class Nodo {
        private final String etiqueta;
        private final Boolean eligioVeraz;
        private Nodo izquierdo;
        private Nodo derecho;

        public Nodo(String etiqueta, Boolean eligioVeraz) {
            this.etiqueta = etiqueta;
            this.eligioVeraz = eligioVeraz;
        }

        public String getEtiqueta() {
            return etiqueta;
        }

        public Boolean getEligioVeraz() {
            return eligioVeraz;
        }

        public Nodo getIzquierdo() {
            return izquierdo;
        }

        public Nodo getDerecho() {
            return derecho;
        }

        public Nodo getHijo() {
            return izquierdo != null ? izquierdo : derecho;
        }
    }

    private final Nodo raiz;
    private Nodo ultimo;

    public ArbolDecisiones(String nombre) {
        raiz = new Nodo(nombre, null);
        ultimo = raiz;
    }

    public void agregarRonda(boolean eligioVeraz) {
        Nodo nuevo = new Nodo(eligioVeraz ? "Veraz" : "Mentiroso", eligioVeraz);
        if (eligioVeraz) ultimo.derecho = nuevo;
        else ultimo.izquierdo = nuevo;
        ultimo = nuevo;
    }

    public Nodo getRaiz() {
        return raiz;
    }

    public List<Boolean> getCamino() {
        List<Boolean> camino = new ArrayList<>();
        for (Nodo n = raiz.getHijo(); n != null; n = n.getHijo()) camino.add(n.eligioVeraz);
        return camino;
    }
}
