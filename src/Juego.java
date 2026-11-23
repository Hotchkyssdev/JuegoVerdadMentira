package src;

import java.util.*;

public class Juego {
    private final List<Personaje> personajes = new ArrayList<>();
    private final List<List<Afirmacion>> afirmaciones = new ArrayList<>();
    private final List<Map<String, Boolean>> decisiones = new ArrayList<>();
    private final List<Map<String, Boolean>> comportamientosReales = new ArrayList<>();
    private final List<GrafoPersonajes> grafos = new ArrayList<>();
    private final List<ArbolDecisiones> arboles = new ArrayList<>();
    private final Random random = new Random();

    // Cadena de Markov para controlar al personaje AMBOS.
    private final CadenaMarkov cadenaMarkov = new CadenaMarkov();

    public Juego() {
        personajes.add(new Personaje("Juan"));
        personajes.add(new Personaje("Rita"));
        personajes.add(new Personaje("Gustavo"));
        personajes.add(new Personaje("Sofía"));
        personajes.add(new Personaje("Francisco"));

        List<Personaje> mezcla = new ArrayList<>(personajes);
        Collections.shuffle(mezcla);

        mezcla.get(0).setTipo(Personaje.Tipo.VERAZ);
        mezcla.get(1).setTipo(Personaje.Tipo.VERAZ);
        mezcla.get(2).setTipo(Personaje.Tipo.MENTIROSO);
        mezcla.get(3).setTipo(Personaje.Tipo.MENTIROSO);
        mezcla.get(4).setTipo(Personaje.Tipo.AMBOS);

        // Inicializamos la cadena de Markov para la primera ronda.
        cadenaMarkov.iniciar();

        for (int r = 0; r < 3; r++) {
            generarRonda(r);
        }
    }

    private void generarRonda(int ronda) {
        Map<String, Boolean> realidad = new LinkedHashMap<>();

        // Determinamos el comportamiento real de cada personaje.
        for (Personaje p : personajes) {
            boolean verdad;

            if (p.getTipo() == Personaje.Tipo.VERAZ) {
                verdad = true;
            } else if (p.getTipo() == Personaje.Tipo.MENTIROSO) {
                verdad = false;
            } else {
                // El personaje AMBOS utiliza la cadena de Markov.
                if (ronda == 0) {
                    verdad = cadenaMarkov.getEstadoActual();
                } else {
                    verdad = cadenaMarkov.siguienteEstado();
                }
            }

            p.setDiceVerdadEstaRonda(verdad);
            realidad.put(p.getNombre(), verdad);
        }

        // Guardamos el comportamiento real de esta ronda.
        comportamientosReales.add(realidad);

        List<Afirmacion> lista = new ArrayList<>();
        GrafoPersonajes grafo = new GrafoPersonajes();

        // Generamos las afirmaciones de los personajes.
        for (Personaje emisor : personajes) {
            List<Personaje> posibles = new ArrayList<>(personajes);
            posibles.remove(emisor);

            Personaje objetivo = posibles.get(random.nextInt(posibles.size()));

            boolean afirmaQueMiente = random.nextBoolean();

            boolean afirmacionVerdadera = afirmaQueMiente
                    ? !realidad.get(objetivo.getNombre())
                    : realidad.get(objetivo.getNombre());

            // El emisor debe realizar una afirmación acorde
            // con su comportamiento real en esta ronda.
            if (afirmacionVerdadera != realidad.get(emisor.getNombre())) {
                afirmaQueMiente = !afirmaQueMiente;
            }

            Afirmacion a = new Afirmacion(
                    emisor,
                    objetivo,
                    afirmaQueMiente);

            lista.add(a);

            grafo.agregarRelacion(
                    emisor,
                    objetivo,
                    afirmaQueMiente
                            ? "afirma que miente"
                            : "afirma que dice la verdad");
        }

        afirmaciones.add(lista);
        grafos.add(grafo);
        decisiones.add(new LinkedHashMap<>());
        arboles.add(new ArbolDecisiones());
    }

    public List<Personaje> getPersonajes() {
        return personajes;
    }

    public List<Afirmacion> getAfirmaciones(int r) {
        return afirmaciones.get(r);
    }

    public GrafoPersonajes getGrafo(int r) {
        return grafos.get(r);
    }

    public ArbolDecisiones getArbol(int r) {
        return arboles.get(r);
    }

    public Map<String, Boolean> getDecisiones(int r) {
        return decisiones.get(r);
    }

    public boolean comportamientoReal(int r, Personaje p) {
        return comportamientosReales.get(r).get(p.getNombre());
    }

    public void registrarDecision(int r, Personaje p, boolean veraz) {
        decisiones.get(r).put(p.getNombre(), veraz);
    }

    public void construirArbol(int r) {
        ArbolDecisiones arbol = new ArbolDecisiones();

        for (Personaje p : personajes) {
            arbol.insertar(
                    p.getNombre(),
                    decisiones.get(r).get(p.getNombre()));
        }

        arboles.set(r, arbol);
    }

    public Personaje getPersonajeAmbos() {
        for (Personaje p : personajes) {
            if (p.getTipo() == Personaje.Tipo.AMBOS) {
                return p;
            }
        }

        return null;
    }
}