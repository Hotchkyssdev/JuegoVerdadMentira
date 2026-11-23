package verdadmentira.domain;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Afirmacion {
    public record Referencia(Personaje objetivo, boolean diceVerdad) { }

    public record HechoVisible(Personaje objetivo, String descripcion, boolean cierto) { }

    private final Personaje emisor;
    private final List<Referencia> referencias;
    private final HechoVisible hecho;

    public Afirmacion(Personaje emisor, List<Referencia> referencias) {
        this.emisor = emisor;
        this.referencias = List.copyOf(referencias);
        this.hecho = null;
    }

    public Afirmacion(Personaje emisor, HechoVisible hecho) {
        this.emisor = emisor;
        this.referencias = List.of();
        this.hecho = hecho;
    }

    public boolean esHechoVisible() {
        return hecho != null;
    }

    public HechoVisible getHecho() {
        return hecho;
    }

    public Personaje getEmisor() {
        return emisor;
    }

    public List<Referencia> getReferencias() {
        return referencias;
    }

    public boolean hablaDeSiMismo() {
        return referencias.stream().anyMatch(r -> r.objetivo() == emisor);
    }

    public boolean esVerdadera(Map<Personaje, Boolean> dicenVerdad) {
        if (hecho != null) return hecho.cierto();
        for (Referencia r : referencias) {
            if (dicenVerdad.get(r.objetivo()) != r.diceVerdad()) return false;
        }
        return true;
    }

    public String[] getConsecuencias(Function<Personaje, String> formatoDeNombre) {
        if (hecho != null || hablaDeSiMismo()) return null;
        if (referencias.size() == 1) {
            Referencia r = referencias.get(0);
            String nombre = formatoDeNombre.apply(r.objetivo());
            return new String[]{nombre + estado(r.diceVerdad()), nombre + estado(!r.diceVerdad())};
        }
        Referencia a = referencias.get(0);
        Referencia b = referencias.get(1);
        String nombreA = formatoDeNombre.apply(a.objetivo());
        String nombreB = formatoDeNombre.apply(b.objetivo());
        if (a.diceVerdad() == b.diceVerdad()) {
            return new String[]{
                    nombreA + " y " + nombreB + (a.diceVerdad() ? " dicen la verdad" : " mienten"),
                    "al menos uno entre " + nombreA + " y " + nombreB + estado(!a.diceVerdad())
            };
        }
        return new String[]{
                nombreA + estado(a.diceVerdad()) + " y " + nombreB + estado(b.diceVerdad()),
                nombreA + estado(!a.diceVerdad()) + ", o " + nombreB + estado(!b.diceVerdad()) + " (al menos una de las dos)"
        };
    }

    private static String estado(boolean diceVerdad) {
        return diceVerdad ? " dice la verdad" : " miente";
    }

    public String getTexto() {
        return getTexto(Personaje::getNombre);
    }

    public String getTexto(Function<Personaje, String> formatoDeNombre) {
        if (hecho != null) return formatoDeNombre.apply(hecho.objetivo()) + " " + hecho.descripcion() + ".";
        if (referencias.size() == 1) {
            Referencia r = referencias.get(0);
            if (r.objetivo() == emisor) return r.diceVerdad() ? "Yo digo la verdad." : "Yo miento.";
            return formatoDeNombre.apply(r.objetivo()) + (r.diceVerdad() ? " dice la verdad." : " miente.");
        }
        Referencia a = referencias.get(0);
        Referencia b = referencias.get(1);
        String nombreA = formatoDeNombre.apply(a.objetivo());
        String nombreB = formatoDeNombre.apply(b.objetivo());
        if (a.diceVerdad() == b.diceVerdad()) {
            return nombreA + " y " + nombreB + (a.diceVerdad() ? " dicen la verdad." : " mienten.");
        }
        return nombreA + (a.diceVerdad() ? " dice la verdad" : " miente") + ", pero " + nombreB + " no.";
    }
}
