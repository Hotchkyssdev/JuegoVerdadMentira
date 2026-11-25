package verificador;

import estructuras.ColaEnlazada;
import estructuras.ListaDinamica;
import estructuras.PilaEnlazada;

public class PruebaMisEstructuras {

    // Punto de entrada
    public static void main(String[] args) {
        boolean lista = VerificadorEstructuras.verificarLista(new ListaDinamica<Integer>());
        boolean pila = VerificadorEstructuras.verificarPila(new PilaEnlazada<Integer>());
        boolean cola = VerificadorEstructuras.verificarCola(new ColaEnlazada<Integer>());
        if (!(lista && pila && cola)) {
            System.exit(1);
        }
    }
}
