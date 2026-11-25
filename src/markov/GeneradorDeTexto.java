package markov;

import java.util.Random;

public interface GeneradorDeTexto {
    Generacion generar(Random azar);

    int getOrden();

    String getDescripcion();
}
