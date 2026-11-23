package verdadmentira.domain;

import verdadmentira.enumeration.RasgoVisible;
import verdadmentira.enumeration.TipoDePersonaje;

import java.awt.Color;

public class Personaje {
    private final String nombre;
    private final Color color;
    private final String nombreDelColor;
    private final RasgoVisible rasgo;
    private TipoDePersonaje tipo;

    public Personaje(String nombre, Color color, String nombreDelColor, RasgoVisible rasgo) {
        this.nombre = nombre;
        this.color = color;
        this.nombreDelColor = nombreDelColor;
        this.rasgo = rasgo;
    }

    public String getNombre() {
        return nombre;
    }

    public Color getColor() {
        return color;
    }

    public String descripcionDelRasgo() {
        return rasgo.getDescripcion();
    }

    public String descripcionDelFondo() {
        return "tiene el fondo " + nombreDelColor;
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
