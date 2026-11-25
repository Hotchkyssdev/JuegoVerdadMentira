package juego.reglas.deduccion;

import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Personaje;

public final class Significado {
    public enum Tipo { PERSONAJE, VERAZ, MENTIROSO, CONECTOR }

    private final Tipo tipo;
    private final Personaje personaje;

    private Significado(Tipo tipo, Personaje personaje) {
        this.tipo = tipo;
        this.personaje = personaje;
    }

    public static Significado de(Personaje personaje) {
        if (personaje == null) throw new DatoInvalidoException("Falta el personaje");
        return new Significado(Tipo.PERSONAJE, personaje);
    }

    public static Significado veredicto(boolean diceLaVerdad) {
        return new Significado(diceLaVerdad ? Tipo.VERAZ : Tipo.MENTIROSO, null);
    }

    public static Significado conector() {
        return new Significado(Tipo.CONECTOR, null);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Personaje getPersonaje() {
        return personaje;
    }
}
