package src;

public class Personaje {
    public enum Tipo {
        VERAZ, MENTIROSO, AMBOS
    }

    private final String nombre;
    private Tipo tipo;
    private boolean diceVerdadEstaRonda;

    public Personaje(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public boolean diceVerdadEstaRonda() {
        return diceVerdadEstaRonda;
    }

    public void setDiceVerdadEstaRonda(boolean valor) {
        diceVerdadEstaRonda = valor;
    }

    public String descripcionReal() {
        if (tipo == Tipo.VERAZ)
            return "Veraz";
        if (tipo == Tipo.MENTIROSO)
            return "Mentiroso";
        return "Ambos";
    }
}