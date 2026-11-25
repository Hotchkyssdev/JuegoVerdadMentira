package markov;

public class ModeloSinEntrenarException extends RuntimeException {
    public ModeloSinEntrenarException() {
        super("El modelo de Markov no tiene nada entrenado: el corpus no tiene líneas válidas");
    }
}
