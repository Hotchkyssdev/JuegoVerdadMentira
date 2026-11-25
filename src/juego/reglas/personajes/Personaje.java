package juego.reglas.personajes;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;

import java.awt.Color;

public class Personaje {
    private final String nombre;
    private final Color color;
    private final String nombreDelColor;
    private final RasgoVisible rasgo;
    private final ListaDinamica<String> apodos = new ListaDinamica<>();
    private TipoDePersonaje tipo;

    public Personaje(String nombre, Color color, String nombreDelColor, RasgoVisible rasgo) {
        if (nombre == null || nombre.trim().isEmpty()) throw new DatoInvalidoException("Un personaje necesita nombre");
        if (color == null) throw new DatoInvalidoException("Un personaje necesita color");
        if (nombreDelColor == null || nombreDelColor.trim().isEmpty()) throw new DatoInvalidoException("Falta el nombre del color");
        if (rasgo == null) throw new DatoInvalidoException("Un personaje necesita un rasgo visible");
        this.nombre = nombre.trim();
        this.color = color;
        this.nombreDelColor = nombreDelColor.trim();
        this.rasgo = rasgo;
    }

    public void agregarApodo(String apodo) {
        if (apodo == null || apodo.trim().isEmpty()) throw new DatoInvalidoException("Un apodo no puede estar vacío");
        apodos.agregar(apodo.trim());
    }

    public ListaDinamica<String> getApodos() {
        return apodos.copia();
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
        if (tipo == null) throw new DatoInvalidoException("El tipo de un personaje no puede ser nulo");
        this.tipo = tipo;
    }

    public String descripcionReal() {
        if (tipo == TipoDePersonaje.VERAZ) return "Siempre dice la verdad";
        if (tipo == TipoDePersonaje.MENTIROSO) return "Siempre miente";
        return "Puede hacer las dos cosas";
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (otro == null || getClass() != otro.getClass()) return false;
        return nombre.equals(((Personaje) otro).nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
