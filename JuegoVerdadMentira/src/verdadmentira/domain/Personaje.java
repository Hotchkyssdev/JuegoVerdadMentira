package verdadmentira.domain;

import verdadmentira.enumeration.Accesorio;
import verdadmentira.enumeration.TipoDePersonaje;

import java.awt.Color;

public class Personaje {
    private final String nombre;
    private final Color colorRopa;
    private final Color colorPelo;
    private final boolean peloLargo;
    private final Accesorio accesorio;
    private final String colorDeRemera;
    private TipoDePersonaje tipo;

    public Personaje(String nombre, Color colorRopa, String colorDeRemera, Color colorPelo, boolean peloLargo, Accesorio accesorio) {
        this.nombre = nombre;
        this.colorRopa = colorRopa;
        this.colorPelo = colorPelo;
        this.peloLargo = peloLargo;
        this.accesorio = accesorio;
        this.colorDeRemera = colorDeRemera;
    }

    public String getNombre() {
        return nombre;
    }

    public Color getColorRopa() {
        return colorRopa;
    }

    public Color getColorPelo() {
        return colorPelo;
    }

    public boolean tienePeloLargo() {
        return peloLargo;
    }

    public Accesorio getAccesorio() {
        return accesorio;
    }

    public String descripcionDelAccesorio() {
        return switch (accesorio) {
            case ANTEOJOS -> "tiene anteojos";
            case MONIO -> "tiene un moño";
            case GORRA -> "tiene gorra";
            case VINCHA -> "tiene vincha";
            case BARBA -> "tiene barba";
        };
    }

    public String descripcionDeLaRemera() {
        return "tiene la remera " + colorDeRemera;
    }

    public TipoDePersonaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoDePersonaje tipo) {
        this.tipo = tipo;
    }

    public String descripcionReal() {
        if (tipo == TipoDePersonaje.VERAZ) return "Siempre dice la verdad";
        if (tipo == TipoDePersonaje.MENTIROSO) return "Siempre miente";
        return "Puede hacer las dos cosas";
    }
}
