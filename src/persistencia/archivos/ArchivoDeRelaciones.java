package persistencia.archivos;

import estructuras.ListaDinamica;
import grafos.Arista;
import grafos.Grafo;

import java.io.File;

/**
 * relaciones.txt guarda el grafo dirigido y ponderado de quién habla de
 * quién, una arista por línea: «origen;destino;peso», por ejemplo
 * «Lisa;Roz;3». El peso cuenta cuántas veces el origen nombró al destino en
 * partidas anteriores; al armar una ronda, cuanto más pesada es una arista,
 * menos probable es que se use. Si falta una arista, ese personaje no habla
 * del otro. Los nombres tienen que ser del elenco, el peso es un entero no
 * negativo y no hay aristas de un personaje a sí mismo.
 */
public class ArchivoDeRelaciones extends ArchivoDeTexto<Grafo<String>> {
    public static final String NOMBRE = "relaciones.txt";
    private static final String SEPARADOR = ";";

    private final ListaDinamica<String> nombresValidos;
    private final Grafo<String> iniciales;

    public ArchivoDeRelaciones(File carpeta, ListaDinamica<String> nombresValidos, Grafo<String> iniciales) {
        super(carpeta, NOMBRE);
        if (nombresValidos == null || nombresValidos.estaVacia()) throw new IllegalArgumentException("Faltan los nombres válidos");
        this.nombresValidos = nombresValidos.copia();
        this.iniciales = iniciales;
    }

    @Override
    public Grafo<String> datosIniciales() {
        Grafo<String> copia = new Grafo<>();
        ListaDinamica<String> vertices = iniciales.getVertices();
        for (int i = 0; i < vertices.tamanio(); i++) copia.agregarVertice(vertices.obtener(i));
        ListaDinamica<Arista<String>> aristas = iniciales.getAristas();
        for (int i = 0; i < aristas.tamanio(); i++) {
            Arista<String> a = aristas.obtener(i);
            copia.agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
        }
        return copia;
    }

    @Override
    protected Grafo<String> interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos) {
        Grafo<String> grafo = new Grafo<>();
        for (int i = 0; i < nombresValidos.tamanio(); i++) grafo.agregarVertice(nombresValidos.obtener(i));
        for (int i = 0; i < lineas.tamanio(); i++) {
            LineaLeida linea = lineas.obtener(i);
            try {
                String[] campos = linea.getTexto().split(SEPARADOR, -1);
                if (campos.length != 3) throw new LineaDanadaException("tiene que tener «origen;destino;peso».");
                String origen = nombreValido(campos[0]);
                String destino = nombreValido(campos[1]);
                if (origen.equals(destino)) throw new LineaDanadaException("un personaje no puede apuntarse a sí mismo.");
                int peso = entero(campos[2], "El peso");
                if (peso < 0) throw new LineaDanadaException("el peso no puede ser negativo.");
                if (grafo.arista(origen, destino) != null) throw new LineaDanadaException("la arista " + origen + " → " + destino + " está repetida.");
                grafo.agregarArista(origen, destino, peso);
            } catch (LineaDanadaException e) {
                avisarLineaDanada(avisos, linea, e.getMessage());
            }
        }
        if (grafo.cantidadDeAristas() == 0) {
            avisos.agregar(NOMBRE + " no tiene ninguna arista válida: se usan las relaciones iniciales.");
            return datosIniciales();
        }
        return grafo;
    }

    private String nombreValido(String texto) throws LineaDanadaException {
        String nombre = texto.trim();
        if (!nombresValidos.contiene(nombre)) throw new LineaDanadaException("«" + nombre + "» no es un personaje del juego.");
        return nombre;
    }

    @Override
    protected ListaDinamica<String> escribir(Grafo<String> grafo) {
        ListaDinamica<String> lineas = new ListaDinamica<>();
        ListaDinamica<Arista<String>> aristas = grafo.getAristas();
        for (int i = 0; i < aristas.tamanio(); i++) {
            Arista<String> a = aristas.obtener(i);
            lineas.agregar(a.getOrigen() + SEPARADOR + a.getDestino() + SEPARADOR + (int) a.getPeso());
        }
        return lineas;
    }

    @Override
    protected String encabezado() {
        return "Relaciones: grafo dirigido y ponderado, una arista por línea.\n"
                + "Formato: origen;destino;peso (veces que el origen ya nombró al destino)";
    }
}
