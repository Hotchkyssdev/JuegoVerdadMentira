package juego.reglas.ronda;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;

public class FraseCompuesta extends Afirmacion {
    private final Personaje primero;
    private final boolean afirmaDelPrimero;
    private final Personaje segundo;
    private final boolean afirmaDelSegundo;

    public FraseCompuesta(Personaje emisor, Personaje primero, boolean afirmaDelPrimero, Personaje segundo, boolean afirmaDelSegundo) {
        super(emisor);
        if (primero == null || segundo == null || primero.equals(segundo) || primero.equals(emisor) || segundo.equals(emisor)) {
            throw new DatoInvalidoException("Una frase compuesta habla de otros dos personajes distintos");
        }
        this.primero = primero;
        this.afirmaDelPrimero = afirmaDelPrimero;
        this.segundo = segundo;
        this.afirmaDelSegundo = afirmaDelSegundo;
    }

    @Override
    public boolean esVerdadera(Marcas supuesto) {
        return supuesto.diceLaVerdad(primero) == afirmaDelPrimero && supuesto.diceLaVerdad(segundo) == afirmaDelSegundo;
    }

    @Override
    public String getTexto(FormatoDeNombre formato) {
        String a = formato.formatear(primero);
        String b = formato.formatear(segundo);
        if (afirmaDelPrimero == afirmaDelSegundo) return a + " y " + b + (afirmaDelPrimero ? " dicen la verdad." : " mienten.");
        return a + estado(afirmaDelPrimero) + ", pero " + b + " no.";
    }

    @Override
    public ListaDinamica<String> getConsecuencias(FormatoDeNombre formato) {
        String a = formato.formatear(primero);
        String b = formato.formatear(segundo);
        if (afirmaDelPrimero == afirmaDelSegundo) {
            return ListaDinamica.de(
                    a + " y " + b + (afirmaDelPrimero ? " dicen la verdad" : " mienten"),
                    "al menos uno entre " + a + " y " + b + estado(!afirmaDelPrimero));
        }
        return ListaDinamica.de(
                a + estado(afirmaDelPrimero) + " y " + b + estado(afirmaDelSegundo),
                a + estado(!afirmaDelPrimero) + ", o " + b + estado(!afirmaDelSegundo) + " (al menos una de las dos)");
    }

    @Override
    public ListaDinamica<Personaje> getNombrados() {
        return ListaDinamica.de(primero, segundo);
    }

    @Override
    public boolean esCompuesta() {
        return true;
    }

    @Override
    void agregarAlGrafo(GrafoDeRonda grafo) {
        grafo.agregarRelacion(getEmisor(), primero, relacion(afirmaDelPrimero), PESO_COMPUESTO);
        grafo.agregarRelacion(getEmisor(), segundo, relacion(afirmaDelSegundo), PESO_COMPUESTO);
        grafo.agregarDeduccion(getEmisor(), primero, PESO_COMPUESTO);
        grafo.agregarDeduccion(getEmisor(), segundo, PESO_COMPUESTO);
    }
}
