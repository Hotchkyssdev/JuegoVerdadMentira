package juego.pantallas.componentes.dibujo;

import estructuras.ListaDinamica;
import juego.reglas.personajes.Personaje;
import juego.reglas.ronda.Relacion;

public interface DatosDelDigrafo {
    ListaDinamica<Personaje> getPersonajes();

    ListaDinamica<Relacion> getRelaciones();

    ListaDinamica<Personaje> getOrdenDeAparicion();

    String getTextoDeGlobo(Personaje personaje);

    Boolean getMarca(Personaje personaje);

    boolean tieneAviso(Personaje personaje);

    Personaje getSeleccionado();

    ListaDinamica<Personaje> getRecorridoResaltado();
}
