package juego.reglas.ronda;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;

public abstract class Afirmacion {
    protected static final double PESO_SIMPLE = 1;
    protected static final double PESO_COMPUESTO = 2;

    private final Personaje emisor;

    protected Afirmacion(Personaje emisor) {
        if (emisor == null) throw new DatoInvalidoException("Una afirmación necesita quién la dice");
        this.emisor = emisor;
    }

    public Personaje getEmisor() {
        return emisor;
    }

    public abstract boolean esVerdadera(Marcas supuesto);

    public abstract String getTexto(FormatoDeNombre formato);

    public abstract ListaDinamica<String> getConsecuencias(FormatoDeNombre formato);

    public abstract ListaDinamica<Personaje> getNombrados();

    abstract void agregarAlGrafo(GrafoDeRonda grafo);

    public String getTexto() {
        return getTexto(Personaje::getNombre);
    }

    public boolean nombraA(Personaje personaje) {
        return !personaje.equals(emisor) && getNombrados().contiene(personaje);
    }

    public boolean sePuedeRevisarCon(Marcas marcas) {
        return marcas.tiene(emisor) && getNombrados().todosCumplen(marcas::tiene);
    }

    public boolean esCompuesta() {
        return false;
    }

    public boolean hablaDeSiMismo() {
        return false;
    }

    public boolean esComprobableConLasFotos() {
        return false;
    }

    protected static String estado(boolean diceLaVerdad) {
        return diceLaVerdad ? " dice la verdad" : " miente";
    }

    protected static TipoDeRelacion relacion(boolean afirmaQueDiceLaVerdad) {
        return afirmaQueDiceLaVerdad ? TipoDeRelacion.DICE_QUE_DICE_LA_VERDAD : TipoDeRelacion.DICE_QUE_MIENTE;
    }
}
