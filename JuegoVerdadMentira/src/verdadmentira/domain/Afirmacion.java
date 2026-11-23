package verdadmentira.domain;

public class Afirmacion {
    private final Personaje emisor, objetivo;
    private final boolean afirmaQueMiente;

    public Afirmacion(Personaje emisor, Personaje objetivo, boolean afirmaQueMiente) {
        this.emisor = emisor;
        this.objetivo = objetivo;
        this.afirmaQueMiente = afirmaQueMiente;
    }

    public Personaje getEmisor() { 
        return emisor; 
    }

    public Personaje getObjetivo() { 
        return objetivo; 
    }

    public String getTexto() {
        return emisor.getNombre() + ": " + objetivo.getNombre()
                + (afirmaQueMiente ? " está mintiendo." : " está diciendo la verdad.");
    }
}