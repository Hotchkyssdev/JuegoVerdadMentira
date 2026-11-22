import java.util.ArrayList;
import java.util.List;

public class ArbolDecisiones {
    public static class Nodo {
        private final String nombre;
        private final boolean eligioVeraz;
        private Nodo izquierdo, derecho;

        public Nodo(String nombre, boolean eligioVeraz) {
            this.nombre = nombre; this.eligioVeraz = eligioVeraz;
        }

        public String getNombre() { 
            return nombre; 
        }

        public boolean isEligioVeraz() { 
            return eligioVeraz; 
        }

        public Nodo getIzquierdo() { 
            return izquierdo; 
        }

        public Nodo getDerecho() { 
            return derecho; 
        }
    }

    private Nodo raiz;
    public void insertar(String nombre, boolean veraz) {
        Nodo nuevo = new Nodo(nombre, veraz);
        if (raiz == null) { 
            raiz = nuevo; 
            return; 
        }
        insertarRecursivo(raiz, nuevo);
    }

    private void insertarRecursivo(Nodo actual, Nodo nuevo) {
        if (nuevo.eligioVeraz) {
            if (actual.derecho == null) actual.derecho = nuevo;
            else insertarRecursivo(actual.derecho, nuevo);
        } else {
            if (actual.izquierdo == null) actual.izquierdo = nuevo;
            else insertarRecursivo(actual.izquierdo, nuevo);
        }
    }

    public Nodo getRaiz() { 
        return raiz;
    }

    public List<String> obtenerRecorrido() {
        List<String> lista = new ArrayList<>(); 
        preOrden(raiz, lista); 
        return lista;
    }

    private void preOrden(Nodo n, List<String> lista) {
        if (n == null) return;
        lista.add(n.nombre + " -> " + (n.eligioVeraz ? "VERAZ (derecha)" : "MENTIROSO (izquierda)"));
        preOrden(n.izquierdo, lista); preOrden(n.derecho, lista);
    }
}