package markov;

import java.util.Random;

public class CadenaDeComportamiento {
    private final double sigueDiciendoLaVerdad;
    private final double pasaADecirLaVerdad;
    private boolean diceLaVerdad;

    public CadenaDeComportamiento(double sigueDiciendoLaVerdad, double pasaADecirLaVerdad) {
        validarProbabilidad(sigueDiciendoLaVerdad);
        validarProbabilidad(pasaADecirLaVerdad);
        this.sigueDiciendoLaVerdad = sigueDiciendoLaVerdad;
        this.pasaADecirLaVerdad = pasaADecirLaVerdad;
    }

    private static void validarProbabilidad(double p) {
        if (p < 0 || p > 1) throw new IllegalArgumentException("Una probabilidad va de 0 a 1: " + p);
    }

    public boolean iniciar(Random azar) {
        diceLaVerdad = azar.nextBoolean();
        return diceLaVerdad;
    }

    public boolean siguiente(Random azar) {
        double probabilidad = diceLaVerdad ? sigueDiciendoLaVerdad : pasaADecirLaVerdad;
        diceLaVerdad = azar.nextDouble() < probabilidad;
        return diceLaVerdad;
    }
}
