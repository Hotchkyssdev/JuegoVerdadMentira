package juego.reglas.partida;

import estructuras.Diccionario;
import estructuras.ListaDinamica;
import grafos.Arista;
import grafos.Camino;
import grafos.Grafo;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;
import juego.reglas.personajes.TipoDePersonaje;
import juego.reglas.ronda.Afirmacion;
import juego.reglas.ronda.FormatoDeNombre;
import juego.reglas.ronda.FraseCompuesta;
import juego.reglas.ronda.FraseSimple;
import juego.reglas.ronda.FraseSobreLasFotos;
import juego.reglas.ronda.FraseSobreSiMismo;
import juego.reglas.ronda.GrafoDeRonda;
import markov.CadenaDeComportamiento;

import java.util.Random;

public class Juego {
    public static final int RONDAS = 3;
    public static final int PISTAS_POR_PARTIDA = 3;
    private static final int INTENTOS_POR_RONDA = 50000;
    private static final int PRIMERA_RONDA_CON_FRASES_SOBRE_SI_MISMO = 1;
    private static final double PROBABILIDAD_DE_HABLAR_DE_SI_MISMO = 0.25;

    private enum Forma { SIMPLE, COMPUESTA, SOBRE_SI_MISMO, SOBRE_LAS_FOTOS }

    private final ListaDinamica<Personaje> personajes = Elenco.nuevo();
    private final ListaDinamica<Marcas> realidad = new ListaDinamica<>();
    private final ListaDinamica<ListaDinamica<Afirmacion>> afirmaciones = new ListaDinamica<>();
    private final ListaDinamica<GrafoDeRonda> grafos = new ListaDinamica<>();
    private final ListaDinamica<Marcas> decisiones = new ListaDinamica<>();
    private final Diccionario<Personaje, ArbolDecisiones> arboles = new Diccionario<>();
    private final Grafo<String> relaciones;
    private final Random azar;
    private int pistasRestantes = PISTAS_POR_PARTIDA;

    public Juego(Grafo<String> relaciones, Random azar) {
        if (relaciones == null) throw new DatoInvalidoException("El juego necesita el grafo de relaciones");
        if (azar == null) throw new DatoInvalidoException("El juego necesita un generador al azar");
        this.relaciones = relaciones;
        this.azar = azar;
        for (int i = 0; i < personajes.tamanio(); i++) relaciones.agregarVertice(personajes.obtener(i).getNombre());
        repartirRoles();
        ListaDinamica<Boolean> estadosDelAmbos = estadosDelAmbos();
        for (int r = 0; r < RONDAS; r++) {
            Marcas dicenLaVerdad = new Marcas();
            for (int i = 0; i < personajes.tamanio(); i++) {
                Personaje p = personajes.obtener(i);
                boolean valor = p.getTipo() == TipoDePersonaje.AMBOS ? estadosDelAmbos.obtener(r) : p.getTipo() == TipoDePersonaje.VERAZ;
                dicenLaVerdad.marcar(p, valor);
            }
            ListaDinamica<Afirmacion> lista = generarAfirmaciones(dicenLaVerdad, r);
            realidad.agregar(dicenLaVerdad);
            afirmaciones.agregar(lista);
            grafos.agregar(new GrafoDeRonda(personajes, lista));
            decisiones.agregar(new Marcas());
        }
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            arboles.poner(p, new ArbolDecisiones(p.getNombre()));
        }
    }

    public static Grafo<String> relacionesIniciales() {
        Grafo<String> grafo = new Grafo<>();
        ListaDinamica<String> nombres = Elenco.nombres();
        for (int i = 0; i < nombres.tamanio(); i++) grafo.agregarVertice(nombres.obtener(i));
        for (int i = 0; i < nombres.tamanio(); i++) {
            for (int j = 0; j < nombres.tamanio(); j++) {
                if (i != j) grafo.agregarArista(nombres.obtener(i), nombres.obtener(j), 0);
            }
        }
        return grafo;
    }

    private void repartirRoles() {
        ListaDinamica<Personaje> mezcla = personajes.copia();
        mezcla.mezclar(azar);
        mezcla.obtener(0).setTipo(TipoDePersonaje.VERAZ);
        mezcla.obtener(1).setTipo(TipoDePersonaje.VERAZ);
        mezcla.obtener(2).setTipo(TipoDePersonaje.MENTIROSO);
        mezcla.obtener(3).setTipo(TipoDePersonaje.MENTIROSO);
        mezcla.obtener(4).setTipo(TipoDePersonaje.AMBOS);
    }

    private ListaDinamica<Boolean> estadosDelAmbos() {
        CadenaDeComportamiento cadena = new CadenaDeComportamiento(0.7, 0.4);
        ListaDinamica<Boolean> estados = new ListaDinamica<>();
        do {
            estados.vaciar();
            estados.agregar(cadena.iniciar(azar));
            for (int r = 1; r < RONDAS; r++) estados.agregar(cadena.siguiente(azar));
        } while (todosIguales(estados));
        return estados;
    }

    private static boolean todosIguales(ListaDinamica<Boolean> valores) {
        Boolean primero = valores.primero();
        return valores.todosCumplen(v -> v.equals(primero));
    }

    private int maximoDeCompuestas(int ronda) {
        if (ronda == 0) return 0;
        return ronda == 1 ? 2 : 5;
    }

    private double probabilidadDeCompuesta(int ronda) {
        if (ronda == 0) return 0.25;
        return ronda == 1 ? 0.4 : 0.5;
    }

    private ListaDinamica<Afirmacion> generarAfirmaciones(Marcas dicenLaVerdad, int ronda) {
        for (int intento = 0; intento < INTENTOS_POR_RONDA; intento++) {
            ListaDinamica<Afirmacion> lista = new ListaDinamica<>();
            boolean puedeHablarDeSiMismo = ronda >= PRIMERA_RONDA_CON_FRASES_SOBRE_SI_MISMO;
            Personaje conFoto = ronda == 0 ? personajes.obtener(azar.nextInt(personajes.tamanio())) : null;
            int compuestas = 0;
            for (int i = 0; i < personajes.tamanio(); i++) {
                Personaje emisor = personajes.obtener(i);
                Forma forma = Forma.SIMPLE;
                if (emisor.equals(conFoto)) forma = Forma.SOBRE_LAS_FOTOS;
                else if (puedeHablarDeSiMismo && azar.nextDouble() < PROBABILIDAD_DE_HABLAR_DE_SI_MISMO) forma = Forma.SOBRE_SI_MISMO;
                else if (compuestas < maximoDeCompuestas(ronda) && azar.nextDouble() < probabilidadDeCompuesta(ronda)) forma = Forma.COMPUESTA;
                Afirmacion a = afirmacionAlAzar(emisor, dicenLaVerdad, forma);
                if (a.hablaDeSiMismo()) puedeHablarDeSiMismo = false;
                if (a.esCompuesta()) compuestas++;
                lista.agregar(a);
            }
            if (cantidadDeSoluciones(lista) == 1) return lista;
        }
        throw new RondaImposibleException(ronda, INTENTOS_POR_RONDA);
    }

    private Afirmacion afirmacionAlAzar(Personaje emisor, Marcas dicenLaVerdad, Forma forma) {
        boolean emisorDiceLaVerdad = dicenLaVerdad.diceLaVerdad(emisor);
        if (forma == Forma.SOBRE_SI_MISMO) return new FraseSobreSiMismo(emisor);
        ListaDinamica<Personaje> otros = otrosPorAfinidad(emisor);
        if (forma == Forma.SOBRE_LAS_FOTOS) return fraseSobreLasFotos(emisor, otros.obtener(0), emisorDiceLaVerdad);
        if (forma == Forma.SIMPLE) {
            Personaje objetivo = otros.obtener(0);
            return new FraseSimple(emisor, objetivo, dicenLaVerdad.diceLaVerdad(objetivo) == emisorDiceLaVerdad);
        }
        Personaje a = otros.obtener(0);
        Personaje b = otros.obtener(1);
        boolean afirmaA = dicenLaVerdad.diceLaVerdad(a);
        boolean afirmaB = dicenLaVerdad.diceLaVerdad(b);
        if (!emisorDiceLaVerdad) {
            do {
                afirmaA = azar.nextBoolean();
                afirmaB = azar.nextBoolean();
            } while (afirmaA == dicenLaVerdad.diceLaVerdad(a) && afirmaB == dicenLaVerdad.diceLaVerdad(b));
        }
        return new FraseCompuesta(emisor, a, afirmaA, b, afirmaB);
    }

    private ListaDinamica<Personaje> otrosPorAfinidad(Personaje emisor) {
        ListaDinamica<Personaje> candidatos = new ListaDinamica<>();
        ListaDinamica<Double> pesos = new ListaDinamica<>();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje otro = personajes.obtener(i);
            if (otro.equals(emisor)) continue;
            Arista<String> arista = relaciones.arista(emisor.getNombre(), otro.getNombre());
            if (arista == null) continue;
            candidatos.agregar(otro);
            pesos.agregar(1.0 / (1.0 + arista.getPeso()));
        }
        if (candidatos.tamanio() < 2) {
            ListaDinamica<Personaje> todos = personajes.filtrar(p -> !p.equals(emisor));
            todos.mezclar(azar);
            return todos;
        }
        ListaDinamica<Personaje> orden = new ListaDinamica<>();
        while (!candidatos.estaVacia()) {
            double total = 0;
            for (int i = 0; i < pesos.tamanio(); i++) total += pesos.obtener(i);
            double sorteo = azar.nextDouble() * total;
            int elegido = pesos.tamanio() - 1;
            for (int i = 0; i < pesos.tamanio(); i++) {
                sorteo -= pesos.obtener(i);
                if (sorteo < 0) {
                    elegido = i;
                    break;
                }
            }
            orden.agregar(candidatos.eliminar(elegido));
            pesos.eliminar(elegido);
        }
        return orden;
    }

    private Afirmacion fraseSobreLasFotos(Personaje emisor, Personaje objetivo, boolean emisorDiceLaVerdad) {
        boolean hablaDelRasgo = azar.nextBoolean();
        Personaje descripto = objetivo;
        if (!emisorDiceLaVerdad) {
            ListaDinamica<Personaje> otros = personajes.filtrar(p -> !p.equals(objetivo));
            descripto = otros.obtener(azar.nextInt(otros.tamanio()));
        }
        String descripcion = hablaDelRasgo ? descripto.descripcionDelRasgo() : descripto.descripcionDelFondo();
        return new FraseSobreLasFotos(emisor, objetivo, descripcion, emisorDiceLaVerdad);
    }

    private int cantidadDeSoluciones(ListaDinamica<Afirmacion> lista) {
        int soluciones = 0;
        int n = personajes.tamanio();
        Marcas supuesto = new Marcas();
        for (int mascara = 0; mascara < (1 << n); mascara++) {
            for (int i = 0; i < n; i++) supuesto.marcar(personajes.obtener(i), ((mascara >> i) & 1) == 1);
            boolean cierra = true;
            for (int i = 0; i < lista.tamanio() && cierra; i++) {
                Afirmacion a = lista.obtener(i);
                if (a.esVerdadera(supuesto) != supuesto.diceLaVerdad(a.getEmisor())) cierra = false;
            }
            if (cierra) soluciones++;
        }
        return soluciones;
    }

    public ListaDinamica<Personaje> getPersonajes() {
        return personajes.copia();
    }

    public ListaDinamica<Afirmacion> getAfirmaciones(int ronda) {
        return afirmaciones.obtener(ronda).copia();
    }

    public Afirmacion getAfirmacion(int ronda, Personaje personaje) {
        return afirmaciones.obtener(ronda).buscar(a -> a.getEmisor().equals(personaje));
    }

    public GrafoDeRonda getGrafo(int ronda) {
        return grafos.obtener(ronda);
    }

    public ArbolDecisiones getArbol(Personaje personaje) {
        return arboles.obtener(personaje);
    }

    public ListaDinamica<ArbolDecisiones> getArboles() {
        ListaDinamica<ArbolDecisiones> lista = new ListaDinamica<>();
        for (int i = 0; i < personajes.tamanio(); i++) lista.agregar(arboles.obtener(personajes.obtener(i)));
        return lista;
    }

    public void registrarDecisiones(int ronda, Marcas marcas) {
        if (marcas.cantidad() != personajes.tamanio()) throw new DatoInvalidoException("Hay que marcar a todos antes de confirmar");
        decisiones.obtener(ronda).reemplazarCon(marcas);
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            arboles.obtener(p).agregarRonda(marcas.diceLaVerdad(p));
        }
    }

    public boolean getDecision(int ronda, Personaje personaje) {
        return decisiones.obtener(ronda).diceLaVerdad(personaje);
    }

    public boolean dijoLaVerdad(int ronda, Personaje personaje) {
        return realidad.obtener(ronda).diceLaVerdad(personaje);
    }

    public Personaje getPersonajeAmbos() {
        Personaje ambos = personajes.buscar(p -> p.getTipo() == TipoDePersonaje.AMBOS);
        if (ambos == null) throw new IllegalStateException("No hay personaje que pueda hacer las dos cosas");
        return ambos;
    }

    public int getPistasRestantes() {
        return pistasRestantes;
    }

    public Pista pedirPista(int ronda, Marcas marcas, FormatoDeNombre formato) {
        if (pistasRestantes <= 0) throw new DatoInvalidoException("No quedan pistas en esta partida");
        Pista pista = armarPista(ronda, marcas, formato);
        pistasRestantes--;
        return pista;
    }

    private Pista armarPista(int ronda, Marcas marcas, FormatoDeNombre formato) {
        ListaDinamica<Afirmacion> frases = afirmaciones.obtener(ronda);
        Diccionario<Personaje, String> avisos = new Diccionario<>();
        int revisadas = 0;
        for (int i = 0; i < frases.tamanio(); i++) {
            Afirmacion a = frases.obtener(i);
            if (!a.sePuedeRevisarCon(marcas)) continue;
            revisadas++;
            Personaje emisor = a.getEmisor();
            if (a.esVerdadera(marcas) != marcas.diceLaVerdad(emisor)) {
                avisos.poner(emisor, "No cierra con lo que marcaste: si " + formato.formatear(emisor)
                        + (marcas.diceLaVerdad(emisor) ? " dice la verdad, su frase tendría que ser cierta." : " miente, su frase tendría que ser falsa."));
            }
        }
        if (!avisos.estaVacio()) {
            String mensaje = avisos.tamanio() == 1
                    ? "Hay una frase que no cierra con lo que marcaste: su globo está en rojo."
                    : "Hay " + avisos.tamanio() + " frases que no cierran con lo que marcaste: sus globos están en rojo.";
            return new Pista(Pista.Tipo.CONTRADICCION, mensaje, avisos, null, avisos.claves().primero());
        }
        ListaDinamica<Personaje> sinMarcar = personajes.filtrar(p -> !marcas.tiene(p));
        if (sinMarcar.estaVacia()) {
            return new Pista(Pista.Tipo.TODO_CIERRA, "Todo lo que marcaste cierra (" + revisadas + " frases revisadas).", null, null, null);
        }
        Camino<Personaje> mejor = Camino.inexistente();
        ListaDinamica<Personaje> marcados = marcas.personajes();
        for (int i = 0; i < marcados.tamanio(); i++) {
            for (int j = 0; j < sinMarcar.tamanio(); j++) {
                Camino<Personaje> camino = grafos.obtener(ronda).caminoDeDeduccion(marcados.obtener(i), sinMarcar.obtener(j));
                if (camino.existe() && camino.getCosto() < mejor.getCosto()) mejor = camino;
            }
        }
        String prefijo = revisadas > 0 ? "Lo marcado no se contradice. " : "";
        if (mejor.existe()) {
            ListaDinamica<Personaje> recorrido = mejor.getVertices();
            Personaje destino = recorrido.ultimo();
            return new Pista(Pista.Tipo.CAMINO, prefijo + "Seguí por " + formato.formatear(destino) + ": "
                    + nombres(recorrido, formato, " → ") + " (" + mejor.pasos() + (mejor.pasos() == 1 ? " paso)." : " pasos)."),
                    null, recorrido, destino);
        }
        Afirmacion conFoto = frases.buscar(Afirmacion::esComprobableConLasFotos);
        if (conFoto != null && !marcas.tiene(conFoto.getEmisor())) {
            return new Pista(Pista.Tipo.FOTOS, prefijo + "Empezá por " + formato.formatear(conFoto.getEmisor())
                    + ": su frase se comprueba mirando las fotos.", null, ListaDinamica.de(conFoto.getEmisor()), conFoto.getEmisor());
        }
        ListaDinamica<Personaje> ciclo = grafos.obtener(ronda).ciclo();
        if (!ciclo.estaVacia()) {
            Personaje inicio = ciclo.primero();
            return new Pista(Pista.Tipo.CICLO, prefijo + "Las frases forman un círculo: " + nombres(ciclo, formato, " → ")
                    + ". Suponé que " + formato.formatear(inicio) + " dice la verdad y seguilo: si volvés con una contradicción, miente.",
                    null, ciclo, inicio);
        }
        return new Pista(Pista.Tipo.SIN_DATOS, prefijo + "Suponé algo de " + formato.formatear(sinMarcar.primero())
                + " y seguí lo que implica.", null, null, sinMarcar.primero());
    }

    private static String nombres(ListaDinamica<Personaje> lista, FormatoDeNombre formato, String separador) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < lista.tamanio(); i++) {
            if (i > 0) texto.append(separador);
            texto.append(formato.formatear(lista.obtener(i)));
        }
        return texto.toString();
    }

    public Resultado resultado(Personaje elegido) {
        if (elegido == null) throw new DatoInvalidoException("Hay que elegir a alguien");
        ListaDinamica<Integer> aciertos = new ListaDinamica<>();
        for (int r = 0; r < RONDAS; r++) {
            int ronda = r;
            aciertos.agregar(personajes.contar(p -> getDecision(ronda, p) == dijoLaVerdad(ronda, p)));
        }
        return new Resultado(elegido, getPersonajeAmbos(), aciertos, PISTAS_POR_PARTIDA - pistasRestantes);
    }

    public void registrarRelacionesUsadas() {
        for (int r = 0; r < afirmaciones.tamanio(); r++) {
            ListaDinamica<Afirmacion> frases = afirmaciones.obtener(r);
            for (int i = 0; i < frases.tamanio(); i++) {
                Afirmacion a = frases.obtener(i);
                ListaDinamica<Personaje> nombrados = a.getNombrados();
                for (int j = 0; j < nombrados.tamanio(); j++) {
                    Personaje nombrado = nombrados.obtener(j);
                    if (nombrado.equals(a.getEmisor())) continue;
                    Arista<String> arista = relaciones.arista(a.getEmisor().getNombre(), nombrado.getNombre());
                    if (arista != null) arista.setPeso(arista.getPeso() + 1);
                }
            }
        }
    }

    public Grafo<String> getRelaciones() {
        return relaciones;
    }
}
