package juego.reglas.partida;

import estructuras.Diccionario;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Personaje;

public class Pista {
    public enum Tipo { CONTRADICCION, TODO_CIERRA, CAMINO, FOTOS, CICLO, SIN_DATOS }

    private final Tipo tipo;
    private final String mensaje;
    private final Diccionario<Personaje, String> avisos;
    private final ListaDinamica<Personaje> recorrido;
    private final Personaje sugerido;

    Pista(Tipo tipo, String mensaje, Diccionario<Personaje, String> avisos, ListaDinamica<Personaje> recorrido, Personaje sugerido) {
        if (tipo == null || mensaje == null || mensaje.trim().isEmpty()) throw new DatoInvalidoException("Una pista necesita tipo y mensaje");
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.avisos = avisos == null ? new Diccionario<>() : avisos;
        this.recorrido = recorrido == null ? new ListaDinamica<>() : recorrido;
        this.sugerido = sugerido;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean esContradiccion() {
        return tipo == Tipo.CONTRADICCION;
    }

    public String avisoPara(Personaje personaje) {
        return avisos.obtener(personaje);
    }

    public ListaDinamica<Personaje> conAviso() {
        return avisos.claves();
    }

    public ListaDinamica<Personaje> getRecorrido() {
        return recorrido.copia();
    }

    public Personaje getSugerido() {
        return sugerido;
    }
}
