package juego.reglas.personajes;

import estructuras.Diccionario;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;

public class Marcas {
    private final Diccionario<Personaje, Boolean> marcas;

    public Marcas() {
        marcas = new Diccionario<>();
    }

    private Marcas(Diccionario<Personaje, Boolean> marcas) {
        this.marcas = marcas;
    }

    public void marcar(Personaje personaje, boolean diceLaVerdad) {
        if (personaje == null) throw new DatoInvalidoException("No se puede marcar a un personaje nulo");
        marcas.poner(personaje, diceLaVerdad);
    }

    public Boolean de(Personaje personaje) {
        return marcas.obtener(personaje);
    }

    public boolean diceLaVerdad(Personaje personaje) {
        Boolean marca = marcas.obtener(personaje);
        if (marca == null) throw new DatoInvalidoException(personaje + " no está marcado");
        return marca;
    }

    public boolean tiene(Personaje personaje) {
        return marcas.contieneClave(personaje);
    }

    public void quitar(Personaje personaje) {
        marcas.quitar(personaje);
    }

    public int cantidad() {
        return marcas.tamanio();
    }

    public boolean estaVacia() {
        return marcas.estaVacio();
    }

    public ListaDinamica<Personaje> personajes() {
        return marcas.claves();
    }

    public void vaciar() {
        marcas.vaciar();
    }

    public void reemplazarCon(Marcas otras) {
        marcas.vaciar();
        agregarTodas(otras);
    }

    public void agregarTodas(Marcas otras) {
        ListaDinamica<Personaje> personajes = otras.personajes();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            marcas.poner(p, otras.de(p));
        }
    }

    public Marcas copia() {
        return new Marcas(marcas.copia());
    }
}
