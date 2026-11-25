package juego.reglas.ronda;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;

public class FraseSobreLasFotos extends Afirmacion {
    private final Personaje objetivo;
    private final String descripcion;
    private final boolean cierta;

    public FraseSobreLasFotos(Personaje emisor, Personaje objetivo, String descripcion, boolean cierta) {
        super(emisor);
        if (objetivo == null) throw new DatoInvalidoException("La frase sobre las fotos habla de alguien");
        if (descripcion == null || descripcion.trim().isEmpty()) throw new DatoInvalidoException("Falta qué se ve en la foto");
        this.objetivo = objetivo;
        this.descripcion = descripcion.trim();
        this.cierta = cierta;
    }

    @Override
    public boolean esVerdadera(Marcas supuesto) {
        return cierta;
    }

    @Override
    public String getTexto(FormatoDeNombre formato) {
        return formato.formatear(objetivo) + " " + descripcion + ".";
    }

    @Override
    public ListaDinamica<String> getConsecuencias(FormatoDeNombre formato) {
        String quien = formato.formatear(getEmisor());
        return ListaDinamica.de(quien + " dice la verdad si la foto coincide",
                quien + " miente si la foto no coincide");
    }

    @Override
    public ListaDinamica<Personaje> getNombrados() {
        return ListaDinamica.de(objetivo);
    }

    @Override
    public boolean sePuedeRevisarCon(Marcas marcas) {
        return marcas.tiene(getEmisor());
    }

    @Override
    public boolean esComprobableConLasFotos() {
        return true;
    }

    @Override
    void agregarAlGrafo(GrafoDeRonda grafo) {
        grafo.agregarRelacion(getEmisor(), objetivo, TipoDeRelacion.DESCRIBE_UN_RASGO, PESO_SIMPLE);
    }
}
