package juego.reglas.ronda;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;

public class FraseSimple extends Afirmacion {
    private final Personaje objetivo;
    private final boolean afirmaQueDiceLaVerdad;

    public FraseSimple(Personaje emisor, Personaje objetivo, boolean afirmaQueDiceLaVerdad) {
        super(emisor);
        if (objetivo == null || objetivo.equals(emisor)) throw new DatoInvalidoException("Una frase simple habla de otro personaje");
        this.objetivo = objetivo;
        this.afirmaQueDiceLaVerdad = afirmaQueDiceLaVerdad;
    }

    @Override
    public boolean esVerdadera(Marcas supuesto) {
        return supuesto.diceLaVerdad(objetivo) == afirmaQueDiceLaVerdad;
    }

    @Override
    public String getTexto(FormatoDeNombre formato) {
        return formato.formatear(objetivo) + estado(afirmaQueDiceLaVerdad) + ".";
    }

    @Override
    public ListaDinamica<String> getConsecuencias(FormatoDeNombre formato) {
        String nombre = formato.formatear(objetivo);
        return ListaDinamica.de(nombre + estado(afirmaQueDiceLaVerdad), nombre + estado(!afirmaQueDiceLaVerdad));
    }

    @Override
    public ListaDinamica<Personaje> getNombrados() {
        return ListaDinamica.de(objetivo);
    }

    @Override
    void agregarAlGrafo(GrafoDeRonda grafo) {
        grafo.agregarRelacion(getEmisor(), objetivo, relacion(afirmaQueDiceLaVerdad), PESO_SIMPLE);
        grafo.agregarDeduccion(getEmisor(), objetivo, PESO_SIMPLE);
        grafo.agregarDeduccion(objetivo, getEmisor(), PESO_SIMPLE);
    }
}
