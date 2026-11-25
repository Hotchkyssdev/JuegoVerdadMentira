package juego.reglas.rumores;

import juego.reglas.DatoInvalidoException;

public enum ModoDeMarkov {
    CARACTERES("caracteres"),
    TOKENS("tokens");

    private final String nombre;

    ModoDeMarkov(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public static ModoDeMarkov desdeNombre(String nombre) {
        if (CARACTERES.nombre.equals(nombre)) return CARACTERES;
        if (TOKENS.nombre.equals(nombre)) return TOKENS;
        throw new DatoInvalidoException("Modo de Markov desconocido: " + nombre);
    }
}
