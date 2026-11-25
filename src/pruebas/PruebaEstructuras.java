package pruebas;

import arboles.ArbolBinario;
import arboles.ArbolBinarioDeBusqueda;
import arboles.Trie;
import estructuras.ColaDePrioridad;
import estructuras.ColaEnlazada;
import estructuras.EstructuraVaciaException;
import estructuras.ListaDinamica;
import estructuras.PilaEnlazada;
import grafos.Camino;
import grafos.Grafo;
import juego.reglas.deduccion.Deduccion;
import juego.reglas.deduccion.InterpreteDeDeducciones;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.partida.Juego;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Personaje;
import juego.reglas.registro.Puntaje;
import juego.reglas.registro.Ranking;
import markov.Corpus;
import markov.Generacion;
import markov.ModeloMarkovDeCaracteres;
import markov.ModeloMarkovDeTokens;
import markov.ModeloSinEntrenarException;
import persistencia.Almacen;
import tokens.Token;
import tokens.TokenizadorPorTrie;

import java.util.Random;

public class PruebaEstructuras {
    private int total;
    private int fallas;

    public static void main(String[] args) {
        PruebaEstructuras prueba = new PruebaEstructuras();
        prueba.arboles();
        prueba.abb();
        prueba.trie();
        prueba.grafos();
        prueba.markov();
        prueba.tads();
        prueba.reglas();
        prueba.persistencia();
        System.out.println();
        System.out.println((prueba.total - prueba.fallas) + " de " + prueba.total + " comprobaciones correctas"
                + (prueba.fallas == 0 ? "  ->  OK" : "  ->  HAY FALLAS"));
        if (prueba.fallas > 0) System.exit(1);
    }

    private void comprobar(String descripcion, boolean correcto) {
        total++;
        if (!correcto) fallas++;
        System.out.println((correcto ? "  [ok]    " : "  [FALLA] ") + descripcion);
    }

    private void seccion(String nombre) {
        System.out.println();
        System.out.println(nombre);
    }

    private void arboles() {
        seccion("Árbol binario genérico");
        ArbolBinario<Integer> vacio = new ArbolBinario<>();
        comprobar("árbol vacío: altura 0, 0 nodos, 0 hojas",
                vacio.altura() == 0 && vacio.cantidadDeNodos() == 0 && vacio.cantidadDeHojas() == 0);
        comprobar("árbol vacío: los cuatro recorridos devuelven listas vacías",
                vacio.preOrden().estaVacia() && vacio.inOrden().estaVacia() && vacio.postOrden().estaVacia() && vacio.porNiveles().estaVacia());
        comprobar("árbol vacío: buscar no encuentra nada", !vacio.buscar(1));

        ArbolBinario<Integer> uno = new ArbolBinario<>(7);
        comprobar("árbol de un nodo: altura 1, 1 nodo y 1 hoja",
                uno.altura() == 1 && uno.cantidadDeNodos() == 1 && uno.cantidadDeHojas() == 1);
        comprobar("árbol de un nodo: los recorridos dan [7]", "[7]".equals(uno.preOrden().toString())
                && "[7]".equals(uno.inOrden().toString()) && "[7]".equals(uno.postOrden().toString()) && "[7]".equals(uno.porNiveles().toString()));

        ArbolBinario<Integer> completo = new ArbolBinario<>();
        for (int i = 1; i <= 6; i++) completo.insertar(i);
        comprobar("insertar por niveles arma 1(2(4,5),3(6)): pre-orden [1, 2, 4, 5, 3, 6]", "[1, 2, 4, 5, 3, 6]".equals(completo.preOrden().toString()));
        comprobar("in-orden [4, 2, 5, 1, 6, 3]", "[4, 2, 5, 1, 6, 3]".equals(completo.inOrden().toString()));
        comprobar("post-orden [4, 5, 2, 6, 3, 1]", "[4, 5, 2, 6, 3, 1]".equals(completo.postOrden().toString()));
        comprobar("por niveles [1, 2, 3, 4, 5, 6]", "[1, 2, 3, 4, 5, 6]".equals(completo.porNiveles().toString()));
        comprobar("altura 3, 6 nodos y 3 hojas", completo.altura() == 3 && completo.cantidadDeNodos() == 6 && completo.cantidadDeHojas() == 3);

        ArbolBinario<Integer> reconstruido = ArbolBinario.desdePreOrdenConVacios(completo.preOrdenConVacios());
        comprobar("reconstruir desde pre-orden con vacíos da el mismo árbol",
                reconstruido.inOrden().toString().equals(completo.inOrden().toString())
                        && reconstruido.preOrden().toString().equals(completo.preOrden().toString()));

        ArbolDecisiones decisiones = new ArbolDecisiones("Roz");
        decisiones.agregarRonda(false);
        decisiones.agregarRonda(true);
        comprobar("árbol de decisiones: camino [false, true] por niveles", "[false, true]".equals(decisiones.getCamino().toString()));
    }

    private void abb() {
        seccion("Árbol binario de búsqueda");
        ArbolBinarioDeBusqueda<Integer> abb = new ArbolBinarioDeBusqueda<>();
        comprobar("ABB vacío: mínimo nulo y eliminar devuelve false", abb.minimo() == null && !abb.eliminar(3));
        abb.insertar(50);
        abb.insertar(30);
        abb.insertar(70);
        abb.insertar(20);
        abb.insertar(40);
        abb.insertar(60);
        abb.insertar(80);
        abb.insertar(35);
        comprobar("in-orden ordenado [20, 30, 35, 40, 50, 60, 70, 80]", "[20, 30, 35, 40, 50, 60, 70, 80]".equals(abb.inOrden().toString()));
        comprobar("in-orden iterativo con Pila igual al recursivo", abb.inOrdenIterativo().toString().equals(abb.inOrden().toString()));
        comprobar("un valor repetido no se inserta", !abb.agregar(40) && abb.cantidadDeNodos() == 8);
        abb.eliminar(20);
        comprobar("eliminar una hoja (20)", "[30, 35, 40, 50, 60, 70, 80]".equals(abb.inOrden().toString()));
        abb.eliminar(40);
        comprobar("eliminar un nodo con un hijo (40)", "[30, 35, 50, 60, 70, 80]".equals(abb.inOrden().toString()));
        abb.eliminar(50);
        comprobar("eliminar la raíz con dos hijos (50): sube el sucesor 60",
                "[30, 35, 60, 70, 80]".equals(abb.inOrden().toString()) && abb.getRaiz().getValor() == 60);
        ArbolBinarioDeBusqueda<Integer> copia = ArbolBinarioDeBusqueda.desdePreOrden(abb.preOrden());
        comprobar("reconstruir desde pre-orden conserva la forma", copia.preOrden().toString().equals(abb.preOrden().toString()));

        Ranking ranking = new Ranking();
        for (int i = 1; i <= 12; i++) ranking.agregar(new Puntaje("J" + i, i * 10, i));
        comprobar("el ranking se queda con los 10 mejores (borra los peores del ABB)",
                ranking.cantidad() == 10 && ranking.mejores().primero().getPuntos() == 120 && ranking.mejores().ultimo().getPuntos() == 30);
        comprobar("empate de puntos: va primero la partida más vieja",
                new Puntaje("A", 50, 2).compareTo(new Puntaje("B", 50, 5)) < 0);
    }

    private void trie() {
        seccion("Trie y tokenización");
        Trie<String> trie = new Trie<>();
        comprobar("trie sin claves: no hay coincidencias", trie.coincidenciaMasLarga("hola", 0) == null && trie.conPrefijo("h").estaVacia());
        trie.insertar("dice", "a");
        trie.insertar("dice la verdad", "b");
        trie.insertar("di", "c");
        comprobar("búsqueda exacta", trie.contiene("dice") && !trie.contiene("dic") && "b".equals(trie.buscar("dice la verdad")));
        comprobar("búsqueda por prefijo «di» devuelve las tres claves", trie.conPrefijo("di").tamanio() == 3);
        comprobar("coincidencia más larga: «dice la verdad» gana a «dice»",
                trie.coincidenciaMasLarga("dice la verdad hoy", 0).getLargo() == "dice la verdad".length());
        comprobar("trie sin coincidencias para «zeta»", trie.coincidenciaMasLarga("zeta", 0) == null);

        InterpreteDeDeducciones interprete = new InterpreteDeDeducciones(Elenco.nuevo());
        Deduccion deduccion = interprete.interpretar("Lisa y mc lovin mienten, Ferb dice la verdad");
        Personaje lisa = buscar("Lisa");
        Personaje mcLovin = buscar("McLovin");
        Personaje ferb = buscar("Ferb");
        comprobar("la deducción marca a Lisa y McLovin mentirosos y a Ferb veraz",
                deduccion.esValida() && !deduccion.getMarcas().de(lisa) && !deduccion.getMarcas().de(mcLovin) && deduccion.getMarcas().de(ferb));
        Deduccion rara = interprete.interpretar("Lisa xyz");
        comprobar("una palabra que no está en el vocabulario se informa como error", !rara.esValida() && rara.getErrores().tamanio() == 2);
        TokenizadorPorTrie<String> sinVocabulario = new TokenizadorPorTrie<>();
        ListaDinamica<Token<String>> tokens = sinVocabulario.tokenizar("nada coincide");
        comprobar("tokenizador sin vocabulario: todo queda como desconocido",
                tokens.tamanio() == 2 && !tokens.obtener(0).esReconocido() && !tokens.obtener(1).esReconocido());
    }

    private void grafos() {
        seccion("Grafo");
        Grafo<String> sinAristas = new Grafo<>();
        sinAristas.agregarVertice("A");
        sinAristas.agregarVertice("B");
        comprobar("grafo sin aristas: BFS y DFS desde A solo visitan A",
                "[A]".equals(sinAristas.bfs("A").toString()) && "[A]".equals(sinAristas.dfs("A").toString()));
        comprobar("grafo sin aristas: no hay ciclo y hay 2 componentes", !sinAristas.tieneCiclo() && sinAristas.componentesConexas() == 2);
        Camino<String> inexistente = sinAristas.caminoMinimo("A", "B");
        comprobar("camino inexistente entre A y B", !inexistente.existe() && Double.isInfinite(inexistente.getCosto()));

        Grafo<String> g = new Grafo<>();
        g.agregarVertice("A");
        g.agregarVertice("B");
        g.agregarVertice("C");
        g.agregarVertice("D");
        g.agregarArista("A", "B", 4);
        g.agregarArista("A", "C", 1);
        g.agregarArista("C", "B", 2);
        g.agregarArista("B", "D", 1);
        comprobar("BFS desde A: [A, B, C, D]", "[A, B, C, D]".equals(g.bfs("A").toString()));
        comprobar("DFS desde A: [A, B, D, C]", "[A, B, D, C]".equals(g.dfs("A").toString()));
        Camino<String> camino = g.caminoMinimo("A", "D");
        comprobar("Dijkstra A→D va por C: A → C → B → D con costo 4", "A → C → B → D".equals(camino.toString()) && camino.getCosto() == 4);
        comprobar("sin ciclos en el grafo acíclico", !g.tieneCiclo());
        g.agregarArista("D", "A", 1);
        comprobar("al cerrar D→A aparece el ciclo", g.tieneCiclo() && g.buscarCiclo().primero().equals(g.buscarCiclo().ultimo()));
    }

    private void markov() {
        seccion("Markov");
        ModeloMarkovDeCaracteres sinEntrenar = new ModeloMarkovDeCaracteres(2);
        boolean lanzo = false;
        try {
            sinEntrenar.generar(new Random(1));
        } catch (ModeloSinEntrenarException e) {
            lanzo = true;
        }
        comprobar("un modelo sin entrenar avisa en vez de devolver algo vacío", lanzo);

        ModeloMarkovDeCaracteres modelo = new ModeloMarkovDeCaracteres(3);
        modelo.entrenar(ListaDinamica.de("abc"));
        comprobar("Markov sin sucesores: el contexto «zzz» no tiene sucesores en la tabla",
                modelo.sucesoresDe(ListaDinamica.de("z", "z")).estaVacia());
        Generacion generacion = modelo.generar(new Random(3));
        comprobar("aun así la generación nunca sale vacía", !generacion.getTexto().isEmpty());

        boolean ordenInvalido = false;
        try {
            new ModeloMarkovDeCaracteres(5);
        } catch (IllegalArgumentException e) {
            ordenInvalido = true;
        }
        comprobar("el orden k fuera de 1..4 se rechaza", ordenInvalido);

        Corpus corpus = new Corpus();
        corpus.agregarExpresion("sala de profesores");
        corpus.agregarLinea("Dicen que Lisa durmió en la sala de profesores.");
        ModeloMarkovDeTokens porTokens = new ModeloMarkovDeTokens(2, ModeloMarkovDeTokens.tokenizadorPara(corpus));
        comprobar("la expresión «sala de profesores» cuenta como un token: 8 tokens en total",
                porTokens.contarTokens("Dicen que Lisa durmió en la sala de profesores.") == 8);
        porTokens.entrenar(corpus.getLineas());
        comprobar("con una sola línea, el modelo por tokens la repite entera",
                "Dicen que Lisa durmió en la sala de profesores.".equals(porTokens.generar(new Random(5)).getTexto()));
    }

    private void tads() {
        seccion("Lista, Pila y Cola");
        PilaEnlazada<Integer> pila = new PilaEnlazada<>();
        boolean vaciaLanza = false;
        try {
            pila.desapilar();
        } catch (EstructuraVaciaException e) {
            vaciaLanza = true;
        }
        comprobar("desapilar una pila vacía lanza EstructuraVaciaException", vaciaLanza);
        pila.apilar(1);
        pila.apilar(2);
        comprobar("la pila devuelve en orden inverso", pila.desapilar() == 2 && pila.desapilar() == 1);
        ColaEnlazada<Integer> cola = new ColaEnlazada<>();
        cola.encolar(1);
        cola.encolar(2);
        comprobar("la cola devuelve en orden de llegada", cola.desencolar() == 1 && cola.desencolar() == 2 && cola.estaVacia());
        ColaDePrioridad<Integer> prioridad = new ColaDePrioridad<>(Integer::compare);
        prioridad.encolar(5);
        prioridad.encolar(1);
        prioridad.encolar(3);
        comprobar("la cola de prioridad saca primero el menor", prioridad.desencolarMinimo() == 1 && prioridad.desencolarMinimo() == 3);
        ListaDinamica<String> lista = new ListaDinamica<>();
        lista.agregar("b");
        lista.insertar(0, "a");
        comprobar("la lista inserta y busca", lista.indiceDe("b") == 1 && lista.contiene("a") && !lista.contiene("z"));
    }

    private void reglas() {
        seccion("Reglas del juego");
        Juego juego = new Juego(Juego.relacionesIniciales(), new Random(11));
        boolean unicas = true;
        for (int r = 0; r < Juego.RONDAS; r++) unicas = unicas && juego.getAfirmaciones(r).tamanio() == 5;
        comprobar("cada ronda tiene una frase por personaje", unicas);
        comprobar("la primera ronda tiene una frase que se comprueba con las fotos",
                juego.getAfirmaciones(0).algunoCumple(a -> a.esComprobableConLasFotos()));
        boolean puntajeInvalido = false;
        try {
            new Puntaje("nombre;con;separador", 10, 1);
        } catch (IllegalArgumentException e) {
            puntajeInvalido = true;
        }
        comprobar("un puntaje con «;» en el nombre se rechaza", puntajeInvalido);
    }

    private void persistencia() {
        seccion("Persistencia");
        String carpeta = "out/datos-de-prueba-" + System.nanoTime();
        Almacen almacen = new Almacen(carpeta);
        almacen.cargarTodo();
        comprobar("sin archivos, el juego arranca con datos iniciales y los crea",
                almacen.getAvisos().tamanio() == 5 && almacen.getArchivos().todosCumplen(a -> a.existe()));
        almacen.getRanking().agregar(new Puntaje("Ana", 120, 1));
        almacen.getRanking().agregar(new Puntaje("Beto", 90, 2));
        almacen.guardarFinDePartida(new ListaDinamica<>());
        Almacen otra = new Almacen(carpeta);
        otra.cargarTodo();
        comprobar("el ranking se reconstruye desde el archivo", otra.getAvisos().estaVacia()
                && otra.getRanking().cantidad() == 2 && "Ana".equals(otra.getRanking().mejores().primero().getJugador()));
    }

    private static Personaje buscar(String nombre) {
        return Elenco.nuevo().buscar(p -> p.getNombre().equals(nombre));
    }
}
