package juego.reglas.ronda;

import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Personaje;

public final class Relacion {
    private final Personaje origen;
    private final Personaje destino;
    private final TipoDeRelacion tipo;

    public Relacion(Personaje origen, Personaje destino, TipoDeRelacion tipo) {
        if (origen == null || destino == null || tipo == null) throw new DatoInvalidoException("Una relación necesita origen, destino y tipo");
        this.origen = origen;
        this.destino = destino;
        this.tipo = tipo;
    }

    public Personaje getOrigen() {
        return origen;
    }

    public Personaje getDestino() {
        return destino;
    }

    public TipoDeRelacion getTipo() {
        return tipo;
    }

    public boolean esBucle() {
        return origen.equals(destino);
    }
}
