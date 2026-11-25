package persistencia.archivos;

import arboles.ArbolBinario;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.partida.Decision;

import java.io.File;

/**
 * ultima-partida.txt guarda el árbol de decisiones de cada personaje en la
 * última partida terminada, un árbol por línea, recorrido en pre-orden y con
 * «#» en cada hijo vacío. Los valores van separados por «;»: el primero es
 * el nombre del personaje (la raíz) y los siguientes son «V» (lo marcaste
 * veraz, hijo derecho) o «M» (mentiroso, hijo izquierdo). Por ejemplo, si
 * marcaste a Roz mentiroso y después veraz, la línea es «Roz;M;#;V;#;#;#».
 * Para reconstruirlo se lee en el mismo orden: cada valor es un nodo y cada
 * «#» cierra una rama.
 */
public class ArchivoDeUltimaPartida extends ArchivoDeTexto<ListaDinamica<ArbolDecisiones>> {
    public static final String NOMBRE = "ultima-partida.txt";
    private static final String SEPARADOR = ";";
    private static final String VACIO = "#";

    private final ListaDinamica<String> nombresValidos;

    public ArchivoDeUltimaPartida(File carpeta, ListaDinamica<String> nombresValidos) {
        super(carpeta, NOMBRE);
        this.nombresValidos = nombresValidos.copia();
    }

    @Override
    public ListaDinamica<ArbolDecisiones> datosIniciales() {
        return new ListaDinamica<>();
    }

    @Override
    protected ListaDinamica<ArbolDecisiones> interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos) {
        ListaDinamica<ArbolDecisiones> arboles = new ListaDinamica<>();
        ListaDinamica<String> leidos = new ListaDinamica<>();
        for (int i = 0; i < lineas.tamanio(); i++) {
            LineaLeida linea = lineas.obtener(i);
            try {
                ArbolDecisiones arbol = leer(linea.getTexto());
                if (leidos.contiene(arbol.getNombre())) throw new LineaDanadaException(arbol.getNombre() + " ya tenía árbol.");
                leidos.agregar(arbol.getNombre());
                arboles.agregar(arbol);
            } catch (LineaDanadaException e) {
                avisarLineaDanada(avisos, linea, e.getMessage());
            }
        }
        return arboles;
    }

    private ArbolDecisiones leer(String texto) throws LineaDanadaException {
        String[] campos = texto.split(SEPARADOR, -1);
        if (campos.length < 3) throw new LineaDanadaException("un árbol tiene al menos la raíz y sus dos hijos vacíos.");
        String nombre = campos[0].trim();
        if (!nombresValidos.contiene(nombre)) throw new LineaDanadaException("«" + nombre + "» no es un personaje del juego.");
        ListaDinamica<Decision> preOrden = new ListaDinamica<>();
        try {
            for (int i = 0; i < campos.length; i++) {
                String campo = campos[i].trim();
                preOrden.agregar(VACIO.equals(campo) ? null : Decision.desdeEtiqueta(campo, i == 0));
            }
            return ArbolDecisiones.desde(ArbolBinario.desdePreOrdenConVacios(preOrden));
        } catch (DatoInvalidoException e) {
            throw new LineaDanadaException(e.getMessage() + ".");
        } catch (IllegalArgumentException e) {
            throw new LineaDanadaException(e.getMessage() + ".");
        }
    }

    @Override
    protected ListaDinamica<String> escribir(ListaDinamica<ArbolDecisiones> arboles) {
        ListaDinamica<String> lineas = new ListaDinamica<>();
        for (int i = 0; i < arboles.tamanio(); i++) {
            ListaDinamica<Decision> preOrden = arboles.obtener(i).getArbol().preOrdenConVacios();
            StringBuilder linea = new StringBuilder();
            for (int j = 0; j < preOrden.tamanio(); j++) {
                if (j > 0) linea.append(SEPARADOR);
                Decision d = preOrden.obtener(j);
                linea.append(d == null ? VACIO : d.getEtiqueta());
            }
            lineas.agregar(linea.toString());
        }
        return lineas;
    }

    @Override
    protected String encabezado() {
        return "Última partida: un árbol de decisiones por línea, en pre-orden con # en los hijos vacíos.\n"
                + "Formato: Nombre;V|M|#;... (V = veraz, derecha; M = mentiroso, izquierda)";
    }
}
