import java.util.Random;

public class CadenaMarkov {
    private final Random random = new Random();

    // Estado actual: true = VERAZ, false = MENTIROSO
    private boolean estadoActual;

    // Matriz de transición:
    // Fila 0: estado actual VERAZ
    // Fila 1: estado actual MENTIROSO
    // Columna 0: próximo estado VERAZ
    // Columna 1: próximo estado MENTIROSO
    private final double[][] matrizTransicion = {
        {0.70, 0.30},
        {0.40, 0.60}
    };

    // Inicializa la cadena con un estado aleatorio.
    public boolean iniciar() {
        estadoActual = random.nextBoolean();
        return estadoActual;
    }

    // Calcula el siguiente estado según las probabilidades
    // de transición de la matriz.
    public boolean siguienteEstado() {

        int fila;

        if (estadoActual) {
            fila = 0;
        } else {
            fila = 1;
        }

        double probabilidad = random.nextDouble();

        if (probabilidad < matrizTransicion[fila][0]) {
            estadoActual = true;
        } else {
            estadoActual = false;
        }

        return estadoActual;
    }

    // Devuelve el estado actual de la cadena.
    public boolean getEstadoActual() {
        return estadoActual;
    }
}