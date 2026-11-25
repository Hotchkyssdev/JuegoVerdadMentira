package juego.reglas.registro;

import juego.reglas.DatoInvalidoException;

public class Puntaje implements Comparable<Puntaje> {
    public static final int LARGO_MAXIMO_DEL_NOMBRE = 16;
    private static final String CARACTERES_PROHIBIDOS = ";|=";

    private final String jugador;
    private final int puntos;
    private final int partida;

    public Puntaje(String jugador, int puntos, int partida) {
        this.jugador = validarJugador(jugador);
        if (puntos < 0) throw new DatoInvalidoException("El puntaje no puede ser negativo: " + puntos);
        if (partida < 1) throw new DatoInvalidoException("El número de partida empieza en 1: " + partida);
        this.puntos = puntos;
        this.partida = partida;
    }

    public static String validarJugador(String jugador) {
        if (jugador == null || jugador.trim().isEmpty()) throw new DatoInvalidoException("El nombre del jugador no puede estar vacío");
        String limpio = jugador.trim();
        if (limpio.length() > LARGO_MAXIMO_DEL_NOMBRE) {
            throw new DatoInvalidoException("El nombre del jugador tiene hasta " + LARGO_MAXIMO_DEL_NOMBRE + " letras");
        }
        for (int i = 0; i < limpio.length(); i++) {
            if (CARACTERES_PROHIBIDOS.indexOf(limpio.charAt(i)) >= 0) {
                throw new DatoInvalidoException("El nombre no puede tener «" + limpio.charAt(i) + "»");
            }
        }
        return limpio;
    }

    public String getJugador() {
        return jugador;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getPartida() {
        return partida;
    }

    @Override
    public int compareTo(Puntaje otro) {
        if (puntos != otro.puntos) return Integer.compare(otro.puntos, puntos);
        if (partida != otro.partida) return Integer.compare(partida, otro.partida);
        return jugador.compareTo(otro.jugador);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (otro == null || getClass() != otro.getClass()) return false;
        Puntaje p = (Puntaje) otro;
        return puntos == p.puntos && partida == p.partida && jugador.equals(p.jugador);
    }

    @Override
    public int hashCode() {
        return (jugador.hashCode() * 31 + puntos) * 31 + partida;
    }

    @Override
    public String toString() {
        return jugador + " (" + puntos + ")";
    }
}
