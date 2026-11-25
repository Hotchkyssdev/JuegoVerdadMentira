package juego.pantallas;

import juego.reglas.partida.Juego;
import juego.reglas.registro.Progreso;
import juego.reglas.rumores.Rumores;
import markov.Generacion;
import persistencia.Almacen;

import java.util.Random;

public class Sesion {
    public static final int ANCHO = 1200;
    public static final int ALTO = 720;

    private final Almacen almacen;
    private final Random azar = new Random();
    private final Rumores rumores;

    public Sesion(Almacen almacen) {
        if (almacen == null) throw new IllegalArgumentException("La sesión necesita el almacén de datos");
        this.almacen = almacen;
        almacen.cargarTodo();
        rumores = new Rumores(almacen.getCorpus(), azar);
        rumores.configurar(getProgreso().getOrdenDeMarkov(), getProgreso().getModoDeMarkov());
    }

    public Almacen getAlmacen() {
        return almacen;
    }

    public Progreso getProgreso() {
        return almacen.getProgreso();
    }

    public Rumores getRumores() {
        return rumores;
    }

    public Generacion nuevoRumor() {
        return rumores.nuevo();
    }

    public void reconfigurarRumores() {
        rumores.configurar(getProgreso().getOrdenDeMarkov(), getProgreso().getModoDeMarkov());
        almacen.guardarProgreso();
    }

    public Juego nuevoJuego() {
        return new Juego(almacen.getRelaciones(), azar);
    }
}
