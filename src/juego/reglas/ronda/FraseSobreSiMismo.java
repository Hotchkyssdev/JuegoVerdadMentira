package juego.reglas.ronda;

import estructuras.ListaDinamica;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;

public class FraseSobreSiMismo extends Afirmacion {
    public FraseSobreSiMismo(Personaje emisor) {
        super(emisor);
    }

    @Override
    public boolean esVerdadera(Marcas supuesto) {
        return supuesto.diceLaVerdad(getEmisor());
    }

    @Override
    public String getTexto(FormatoDeNombre formato) {
        return "Yo digo la verdad.";
    }

    @Override
    public ListaDinamica<String> getConsecuencias(FormatoDeNombre formato) {
        return new ListaDinamica<>();
    }

    @Override
    public ListaDinamica<Personaje> getNombrados() {
        return ListaDinamica.de(getEmisor());
    }

    @Override
    public boolean hablaDeSiMismo() {
        return true;
    }

    @Override
    void agregarAlGrafo(GrafoDeRonda grafo) {
        grafo.agregarRelacion(getEmisor(), getEmisor(), TipoDeRelacion.DICE_QUE_DICE_LA_VERDAD, PESO_SIMPLE);
    }
}
