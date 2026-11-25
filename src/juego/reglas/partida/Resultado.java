package juego.reglas.partida;

import estructuras.ListaDinamica;
import juego.reglas.personajes.Personaje;

public class Resultado {
    public static final int PUNTOS_POR_ACIERTO = 10;
    public static final int BONUS_POR_ENCONTRARLO = 50;
    public static final int COSTO_POR_PISTA = 15;

    private final Personaje elegido;
    private final Personaje ambos;
    private final ListaDinamica<Integer> aciertosPorRonda;
    private final int pistasUsadas;

    Resultado(Personaje elegido, Personaje ambos, ListaDinamica<Integer> aciertosPorRonda, int pistasUsadas) {
        this.elegido = elegido;
        this.ambos = ambos;
        this.aciertosPorRonda = aciertosPorRonda;
        this.pistasUsadas = pistasUsadas;
    }

    public boolean gano() {
        return elegido.equals(ambos);
    }

    public Personaje getElegido() {
        return elegido;
    }

    public Personaje getAmbos() {
        return ambos;
    }

    public int aciertosDeRonda(int ronda) {
        return aciertosPorRonda.obtener(ronda);
    }

    public int puntosPorAciertos() {
        int total = 0;
        for (int r = 0; r < aciertosPorRonda.tamanio(); r++) total += aciertosPorRonda.obtener(r) * PUNTOS_POR_ACIERTO * (r + 1);
        return total;
    }

    public int bonus() {
        return gano() ? BONUS_POR_ENCONTRARLO : 0;
    }

    public int descuentoPorPistas() {
        return pistasUsadas * COSTO_POR_PISTA;
    }

    public int getPistasUsadas() {
        return pistasUsadas;
    }

    public int total() {
        return Math.max(0, puntosPorAciertos() + bonus() - descuentoPorPistas());
    }
}
