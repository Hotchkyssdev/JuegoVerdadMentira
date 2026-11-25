package juego.reglas.partida;

public class RondaImposibleException extends RuntimeException {
    public RondaImposibleException(int ronda, int intentos) {
        super("No se pudo armar la ronda " + (ronda + 1) + " con una única solución en " + intentos + " intentos");
    }
}
