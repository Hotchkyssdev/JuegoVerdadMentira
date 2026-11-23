package verdadmentira.domain;

import verdadmentira.enumeration.TipoDePersonaje;

public class Personaje {
    private final String nombre;
    private TipoDePersonaje tipo;
    private boolean diceVerdadEstaRonda;
    
    public Personaje(String nombre) { 
        this.nombre = nombre; 
    }

    public String getNombre() { 
        return nombre;
    }

    public TipoDePersonaje getTipo() { 
        return tipo; 
    }

    public void setTipo(TipoDePersonaje tipo) { 
        this.tipo = tipo; 
    }

    public boolean diceVerdadEstaRonda() { 
        return diceVerdadEstaRonda; 
    }

    public void setDiceVerdadEstaRonda(boolean valor) { 
        diceVerdadEstaRonda = valor; 
    }

    public String descripcionReal() {
        if (tipo == TipoDePersonaje.VERAZ) return "Veraz";
        if (tipo == TipoDePersonaje.MENTIROSO) return "Mentiroso";
        return "Ambos";
    }
}