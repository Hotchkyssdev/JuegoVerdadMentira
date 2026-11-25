package juego.reglas.registro;

import juego.reglas.DatoInvalidoException;
import juego.reglas.rumores.ModoDeMarkov;
import markov.ModeloMarkov;

public class Progreso {
    public static final String JUGADOR_POR_DEFECTO = "Detective";
    public static final int ORDEN_POR_DEFECTO = 3;

    private int partidasJugadas;
    private int partidasGanadas;
    private int rachaActual;
    private int mejorRacha;
    private String jugador = JUGADOR_POR_DEFECTO;
    private int ordenDeMarkov = ORDEN_POR_DEFECTO;
    private ModoDeMarkov modoDeMarkov = ModoDeMarkov.TOKENS;

    public void registrarPartida(boolean gano) {
        partidasJugadas++;
        if (gano) {
            partidasGanadas++;
            rachaActual++;
            mejorRacha = Math.max(mejorRacha, rachaActual);
        } else {
            rachaActual = 0;
        }
    }

    public int siguienteNumeroDePartida() {
        return partidasJugadas + 1;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public void setPartidasJugadas(int partidasJugadas) {
        if (partidasJugadas < 0) throw new DatoInvalidoException("Las partidas jugadas no pueden ser negativas");
        this.partidasJugadas = partidasJugadas;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    public void setPartidasGanadas(int partidasGanadas) {
        if (partidasGanadas < 0) throw new DatoInvalidoException("Las partidas ganadas no pueden ser negativas");
        this.partidasGanadas = partidasGanadas;
    }

    public int getRachaActual() {
        return rachaActual;
    }

    public void setRachaActual(int rachaActual) {
        if (rachaActual < 0) throw new DatoInvalidoException("La racha no puede ser negativa");
        this.rachaActual = rachaActual;
    }

    public int getMejorRacha() {
        return mejorRacha;
    }

    public void setMejorRacha(int mejorRacha) {
        if (mejorRacha < 0) throw new DatoInvalidoException("La mejor racha no puede ser negativa");
        this.mejorRacha = mejorRacha;
    }

    public String getJugador() {
        return jugador;
    }

    public void setJugador(String jugador) {
        this.jugador = Puntaje.validarJugador(jugador);
    }

    public int getOrdenDeMarkov() {
        return ordenDeMarkov;
    }

    public void setOrdenDeMarkov(int orden) {
        if (orden < ModeloMarkov.ORDEN_MINIMO || orden > ModeloMarkov.ORDEN_MAXIMO) {
            throw new DatoInvalidoException("El orden de Markov va de " + ModeloMarkov.ORDEN_MINIMO + " a " + ModeloMarkov.ORDEN_MAXIMO);
        }
        this.ordenDeMarkov = orden;
    }

    public ModoDeMarkov getModoDeMarkov() {
        return modoDeMarkov;
    }

    public void setModoDeMarkov(ModoDeMarkov modo) {
        if (modo == null) throw new DatoInvalidoException("El modo de Markov no puede ser nulo");
        this.modoDeMarkov = modo;
    }

    public void validarCoherencia() {
        if (partidasGanadas > partidasJugadas) throw new DatoInvalidoException("Hay más partidas ganadas que jugadas");
        if (mejorRacha > partidasGanadas || rachaActual > mejorRacha) throw new DatoInvalidoException("Las rachas no cierran con las partidas ganadas");
    }
}
