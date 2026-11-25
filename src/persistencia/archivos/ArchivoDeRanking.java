package persistencia.archivos;

import arboles.ArbolBinarioDeBusqueda;
import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.registro.Puntaje;
import juego.reglas.registro.Ranking;

import java.io.File;

/**
 * ranking.txt guarda el ABB del ranking recorrido en pre-orden, un nodo por
 * línea con el formato «puntos;partida;jugador», por ejemplo «185;7;Lauti».
 * Para reconstruirlo se insertan las líneas en el orden en que aparecen: como
 * el pre-orden pone a cada padre antes que sus hijos, el ABB queda con la
 * misma forma que tenía al guardarse. Los empates de puntos los ordena la
 * partida más vieja primero, así que no hay dos nodos iguales. Si hay más de
 * diez líneas válidas, se quedan los diez mejores.
 */
public class ArchivoDeRanking extends ArchivoDeTexto<Ranking> {
    public static final String NOMBRE = "ranking.txt";
    private static final String SEPARADOR = ";";

    public ArchivoDeRanking(File carpeta) {
        super(carpeta, NOMBRE);
    }

    @Override
    public Ranking datosIniciales() {
        return new Ranking();
    }

    @Override
    protected Ranking interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos) {
        ListaDinamica<Puntaje> preOrden = new ListaDinamica<>();
        for (int i = 0; i < lineas.tamanio(); i++) {
            LineaLeida linea = lineas.obtener(i);
            try {
                Puntaje puntaje = leer(linea.getTexto());
                if (preOrden.contiene(puntaje)) throw new LineaDanadaException("está repetida.");
                preOrden.agregar(puntaje);
            } catch (LineaDanadaException e) {
                avisarLineaDanada(avisos, linea, e.getMessage());
            }
        }
        return new Ranking(ArbolBinarioDeBusqueda.desdePreOrden(preOrden));
    }

    private static Puntaje leer(String texto) throws LineaDanadaException {
        String[] campos = texto.split(SEPARADOR, -1);
        if (campos.length != 3) throw new LineaDanadaException("tiene que tener «puntos;partida;jugador».");
        int puntos = entero(campos[0], "Los puntos");
        int partida = entero(campos[1], "La partida");
        try {
            return new Puntaje(campos[2], puntos, partida);
        } catch (DatoInvalidoException e) {
            throw new LineaDanadaException(e.getMessage() + ".");
        }
    }

    @Override
    protected ListaDinamica<String> escribir(Ranking ranking) {
        ListaDinamica<Puntaje> preOrden = ranking.getArbol().preOrden();
        ListaDinamica<String> lineas = new ListaDinamica<>();
        for (int i = 0; i < preOrden.tamanio(); i++) {
            Puntaje p = preOrden.obtener(i);
            lineas.agregar(p.getPuntos() + SEPARADOR + p.getPartida() + SEPARADOR + p.getJugador());
        }
        return lineas;
    }

    @Override
    protected String encabezado() {
        return "Ranking: ABB en pre-orden, una línea por nodo.\nFormato: puntos;partida;jugador";
    }
}
