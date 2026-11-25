package juego.reglas.partida;

import juego.reglas.DatoInvalidoException;

public final class Decision {
    public static final String VERAZ = "V";
    public static final String MENTIROSO = "M";

    private final String etiqueta;
    private final Boolean eligioVeraz;

    private Decision(String etiqueta, Boolean eligioVeraz) {
        if (etiqueta == null || etiqueta.trim().isEmpty()) throw new DatoInvalidoException("Una decisión necesita etiqueta");
        this.etiqueta = etiqueta;
        this.eligioVeraz = eligioVeraz;
    }

    public static Decision raiz(String nombre) {
        return new Decision(nombre, null);
    }

    public static Decision ronda(boolean eligioVeraz) {
        return new Decision(eligioVeraz ? VERAZ : MENTIROSO, eligioVeraz);
    }

    public static Decision desdeEtiqueta(String etiqueta, boolean esRaiz) {
        if (esRaiz) return raiz(etiqueta);
        if (VERAZ.equals(etiqueta)) return ronda(true);
        if (MENTIROSO.equals(etiqueta)) return ronda(false);
        throw new DatoInvalidoException("Una ronda se marca con " + VERAZ + " o " + MENTIROSO + ", no con «" + etiqueta + "»");
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Boolean getEligioVeraz() {
        return eligioVeraz;
    }

    public boolean esRaiz() {
        return eligioVeraz == null;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (otro == null || getClass() != otro.getClass()) return false;
        Decision d = (Decision) otro;
        return etiqueta.equals(d.etiqueta) && (eligioVeraz == null ? d.eligioVeraz == null : eligioVeraz.equals(d.eligioVeraz));
    }

    @Override
    public int hashCode() {
        return etiqueta.hashCode() * 31 + (eligioVeraz == null ? 0 : eligioVeraz.hashCode());
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
