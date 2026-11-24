package verdadmentira.service;

import verdadmentira.domain.Afirmacion;
import verdadmentira.domain.ArbolDecisiones;
import verdadmentira.domain.GrafoPersonajes;
import verdadmentira.domain.Personaje;
import verdadmentira.enumeration.RasgoVisible;
import verdadmentira.enumeration.TipoDePersonaje;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Juego {
    public static final int RONDAS = 3;
    private static final int INTENTOS_POR_RONDA = 50000;
    private static final int[] MAXIMO_DE_COMPUESTAS = {0, 2, 5};
    private static final double[] PROBABILIDAD_DE_COMPUESTA = {0.25, 0.4, 0.5};
    private static final int PRIMERA_RONDA_CON_FRASES_SOBRE_SI_MISMO = 1;
    private static final double PROBABILIDAD_DE_HABLAR_DE_SI_MISMO = 0.25;

    private enum Forma { SIMPLE, COMPUESTA, SOBRE_SI_MISMO, HECHO_VISIBLE }

    private final List<Personaje> personajes = List.of(
            new Personaje("McLovin", new Color(0xF08A3E), "naranja", RasgoVisible.FLEQUILLO),
            new Personaje("Roz", new Color(0x7FA3CC), "celeste", RasgoVisible.PELO_GRIS),
            new Personaje("Albert", new Color(0xA5ADB8), "gris", RasgoVisible.BARBA),
            new Personaje("Lisa", new Color(0xE3B456), "amarillo", RasgoVisible.COLLAR_DE_PERLAS),
            new Personaje("Ferb", new Color(0xA28BC9), "violeta", RasgoVisible.PELO_VERDE)
    );
    private final List<Map<Personaje, Boolean>> realidad = new ArrayList<>();
    private final List<List<Afirmacion>> afirmaciones = new ArrayList<>();
    private final List<GrafoPersonajes> grafos = new ArrayList<>();
    private final List<Map<Personaje, Boolean>> decisiones = new ArrayList<>();
    private final Map<Personaje, ArbolDecisiones> arboles = new LinkedHashMap<>();
    private final CadenaMarkov cadenaMarkov = new CadenaMarkov();
    private final Random random = new Random();

    public Juego() {
        List<Personaje> mezcla = new ArrayList<>(personajes);
        Collections.shuffle(mezcla, random);
        mezcla.get(0).setTipo(TipoDePersonaje.VERAZ);
        mezcla.get(1).setTipo(TipoDePersonaje.VERAZ);
        mezcla.get(2).setTipo(TipoDePersonaje.MENTIROSO);
        mezcla.get(3).setTipo(TipoDePersonaje.MENTIROSO);
        mezcla.get(4).setTipo(TipoDePersonaje.AMBOS);

        boolean[] estadosDelAmbos = estadosDelAmbos();
        for (int r = 0; r < RONDAS; r++) {
            Map<Personaje, Boolean> dicenVerdad = new LinkedHashMap<>();
            for (Personaje p : personajes) {
                dicenVerdad.put(p, switch (p.getTipo()) {
                    case VERAZ -> true;
                    case MENTIROSO -> false;
                    case AMBOS -> estadosDelAmbos[r];
                });
            }
            List<Afirmacion> lista = generarAfirmaciones(dicenVerdad, r);
            realidad.add(dicenVerdad);
            afirmaciones.add(lista);
            grafos.add(armarGrafo(lista));
            decisiones.add(new LinkedHashMap<>());
        }
        for (Personaje p : personajes) arboles.put(p, new ArbolDecisiones(p.getNombre()));
    }

    private boolean[] estadosDelAmbos() {
        boolean[] estados = new boolean[RONDAS];
        do {
            estados[0] = cadenaMarkov.iniciar();
            for (int r = 1; r < RONDAS; r++) estados[r] = cadenaMarkov.siguienteEstado();
        } while (todosIguales(estados));
        return estados;
    }

    private static boolean todosIguales(boolean[] valores) {
        for (boolean v : valores) if (v != valores[0]) return false;
        return true;
    }

    private List<Afirmacion> generarAfirmaciones(Map<Personaje, Boolean> dicenVerdad, int ronda) {
        int maximoDeCompuestas = MAXIMO_DE_COMPUESTAS[Math.min(ronda, MAXIMO_DE_COMPUESTAS.length - 1)];
        double probabilidadDeCompuesta = PROBABILIDAD_DE_COMPUESTA[Math.min(ronda, PROBABILIDAD_DE_COMPUESTA.length - 1)];
        for (int intento = 0; intento < INTENTOS_POR_RONDA; intento++) {
            List<Afirmacion> lista = new ArrayList<>();
            boolean puedeHablarDeSiMismo = ronda >= PRIMERA_RONDA_CON_FRASES_SOBRE_SI_MISMO;
            Personaje conHechoVisible = ronda == 0 ? personajes.get(random.nextInt(personajes.size())) : null;
            int compuestas = 0;
            for (Personaje emisor : personajes) {
                Forma forma = Forma.SIMPLE;
                double sorteo = random.nextDouble();
                if (emisor == conHechoVisible) forma = Forma.HECHO_VISIBLE;
                else if (puedeHablarDeSiMismo && sorteo < PROBABILIDAD_DE_HABLAR_DE_SI_MISMO) forma = Forma.SOBRE_SI_MISMO;
                else if (compuestas < maximoDeCompuestas && random.nextDouble() < probabilidadDeCompuesta) forma = Forma.COMPUESTA;
                Afirmacion a = afirmacionAlAzar(emisor, dicenVerdad, forma);
                if (a.hablaDeSiMismo()) puedeHablarDeSiMismo = false;
                if (a.getReferencias().size() == 2) compuestas++;
                lista.add(a);
            }
            if (cantidadDeSoluciones(lista) == 1) return lista;
        }
        throw new IllegalStateException("No se pudo armar una ronda con una única solución");
    }

    private Afirmacion afirmacionAlAzar(Personaje emisor, Map<Personaje, Boolean> dicenVerdad, Forma forma) {
        boolean emisorDiceVerdad = dicenVerdad.get(emisor);
        if (forma == Forma.SOBRE_SI_MISMO) {
            return new Afirmacion(emisor, List.of(new Afirmacion.Referencia(emisor, true)));
        }
        List<Personaje> otros = new ArrayList<>(personajes);
        otros.remove(emisor);
        Collections.shuffle(otros, random);
        if (forma == Forma.HECHO_VISIBLE) {
            return hechoVisibleAlAzar(emisor, otros.get(0), emisorDiceVerdad);
        }
        if (forma == Forma.SIMPLE) {
            Personaje objetivo = otros.get(0);
            boolean afirma = dicenVerdad.get(objetivo) == emisorDiceVerdad;
            return new Afirmacion(emisor, List.of(new Afirmacion.Referencia(objetivo, afirma)));
        }
        Personaje a = otros.get(0);
        Personaje b = otros.get(1);
        boolean afirmaA = dicenVerdad.get(a);
        boolean afirmaB = dicenVerdad.get(b);
        if (!emisorDiceVerdad) {
            do {
                afirmaA = random.nextBoolean();
                afirmaB = random.nextBoolean();
            } while (afirmaA == dicenVerdad.get(a) && afirmaB == dicenVerdad.get(b));
        }
        return new Afirmacion(emisor, List.of(new Afirmacion.Referencia(a, afirmaA), new Afirmacion.Referencia(b, afirmaB)));
    }

    private Afirmacion hechoVisibleAlAzar(Personaje emisor, Personaje objetivo, boolean emisorDiceVerdad) {
        boolean hablaDelRasgo = random.nextBoolean();
        String descripcion;
        if (emisorDiceVerdad) {
            descripcion = hablaDelRasgo ? objetivo.descripcionDelRasgo() : objetivo.descripcionDelFondo();
        } else {
            List<Personaje> otros = new ArrayList<>(personajes);
            otros.remove(objetivo);
            Personaje otro = otros.get(random.nextInt(otros.size()));
            descripcion = hablaDelRasgo ? otro.descripcionDelRasgo() : otro.descripcionDelFondo();
        }
        return new Afirmacion(emisor, new Afirmacion.HechoVisible(objetivo, descripcion, emisorDiceVerdad));
    }

    private int cantidadDeSoluciones(List<Afirmacion> lista) {
        int soluciones = 0;
        int n = personajes.size();
        Map<Personaje, Boolean> supuesto = new HashMap<>();
        for (int mascara = 0; mascara < (1 << n); mascara++) {
            for (int i = 0; i < n; i++) supuesto.put(personajes.get(i), ((mascara >> i) & 1) == 1);
            boolean cierra = true;
            for (Afirmacion a : lista) {
                if (a.esVerdadera(supuesto) != supuesto.get(a.getEmisor())) {
                    cierra = false;
                    break;
                }
            }
            if (cierra) soluciones++;
        }
        return soluciones;
    }

    private static GrafoPersonajes armarGrafo(List<Afirmacion> lista) {
        GrafoPersonajes grafo = new GrafoPersonajes();
        for (Afirmacion a : lista) {
            if (a.esHechoVisible()) grafo.agregarDescripcion(a.getEmisor(), a.getHecho().objetivo());
            for (Afirmacion.Referencia r : a.getReferencias()) {
                grafo.agregarRelacion(a.getEmisor(), r.objetivo(), r.diceVerdad());
            }
        }
        return grafo;
    }

    public List<Personaje> getPersonajes() {
        return personajes;
    }

    public List<Afirmacion> getAfirmaciones(int ronda) {
        return afirmaciones.get(ronda);
    }

    public GrafoPersonajes getGrafo(int ronda) {
        return grafos.get(ronda);
    }

    public ArbolDecisiones getArbol(Personaje p) {
        return arboles.get(p);
    }

    public void registrarDecisiones(int ronda, Map<Personaje, Boolean> marcas) {
        decisiones.get(ronda).putAll(marcas);
        for (Personaje p : personajes) arboles.get(p).agregarRonda(marcas.get(p));
    }

    public boolean getDecision(int ronda, Personaje p) {
        return decisiones.get(ronda).get(p);
    }

    public boolean dijoLaVerdad(int ronda, Personaje p) {
        return realidad.get(ronda).get(p);
    }

    public Personaje getPersonajeAmbos() {
        for (Personaje p : personajes) {
            if (p.getTipo() == TipoDePersonaje.AMBOS) return p;
        }
        throw new IllegalStateException("No hay personaje que pueda hacer las dos cosas");
    }
}
