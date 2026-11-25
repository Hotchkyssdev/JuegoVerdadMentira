package persistencia.archivos;

import estructuras.ListaDinamica;
import juego.reglas.DatoInvalidoException;
import juego.reglas.registro.Progreso;
import juego.reglas.rumores.ModoDeMarkov;

import java.io.File;

/**
 * progreso.txt guarda el avance del jugador entre partidas, una clave por
 * línea con el formato «clave=valor», por ejemplo «partidasJugadas=4».
 * Claves: jugador, partidasJugadas, partidasGanadas, rachaActual,
 * mejorRacha, ordenDeMarkov (de 1 a 4) y modoDeMarkov (caracteres o
 * tokens). Una clave desconocida o un valor inválido se avisan y se deja el
 * valor por defecto. Si los contadores no cierran entre sí (más ganadas que
 * jugadas, por ejemplo), se avisa y se vuelve a cero.
 */
public class ArchivoDeProgreso extends ArchivoDeTexto<Progreso> {
    public static final String NOMBRE = "progreso.txt";
    private static final String SEPARADOR = "=";

    public ArchivoDeProgreso(File carpeta) {
        super(carpeta, NOMBRE);
    }

    @Override
    public Progreso datosIniciales() {
        return new Progreso();
    }

    @Override
    protected Progreso interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos) {
        Progreso progreso = new Progreso();
        for (int i = 0; i < lineas.tamanio(); i++) {
            LineaLeida linea = lineas.obtener(i);
            try {
                String[] campos = linea.getTexto().split(SEPARADOR, 2);
                if (campos.length != 2) throw new LineaDanadaException("tiene que tener «clave=valor».");
                aplicar(progreso, campos[0].trim(), campos[1].trim());
            } catch (LineaDanadaException e) {
                avisarLineaDanada(avisos, linea, e.getMessage());
            } catch (DatoInvalidoException e) {
                avisarLineaDanada(avisos, linea, e.getMessage() + ".");
            }
        }
        try {
            progreso.validarCoherencia();
        } catch (DatoInvalidoException e) {
            avisos.agregar(NOMBRE + ": " + e.getMessage() + ". Los contadores vuelven a cero.");
            Progreso limpio = new Progreso();
            limpio.setJugador(progreso.getJugador());
            limpio.setOrdenDeMarkov(progreso.getOrdenDeMarkov());
            limpio.setModoDeMarkov(progreso.getModoDeMarkov());
            return limpio;
        }
        return progreso;
    }

    private static void aplicar(Progreso progreso, String clave, String valor) throws LineaDanadaException {
        switch (clave) {
            case "jugador":
                progreso.setJugador(valor);
                break;
            case "partidasJugadas":
                progreso.setPartidasJugadas(entero(valor, clave));
                break;
            case "partidasGanadas":
                progreso.setPartidasGanadas(entero(valor, clave));
                break;
            case "rachaActual":
                progreso.setRachaActual(entero(valor, clave));
                break;
            case "mejorRacha":
                progreso.setMejorRacha(entero(valor, clave));
                break;
            case "ordenDeMarkov":
                progreso.setOrdenDeMarkov(entero(valor, clave));
                break;
            case "modoDeMarkov":
                progreso.setModoDeMarkov(ModoDeMarkov.desdeNombre(valor));
                break;
            default:
                throw new LineaDanadaException("la clave «" + clave + "» no existe.");
        }
    }

    @Override
    protected ListaDinamica<String> escribir(Progreso p) {
        ListaDinamica<String> lineas = new ListaDinamica<>();
        lineas.agregar("jugador" + SEPARADOR + p.getJugador());
        lineas.agregar("partidasJugadas" + SEPARADOR + p.getPartidasJugadas());
        lineas.agregar("partidasGanadas" + SEPARADOR + p.getPartidasGanadas());
        lineas.agregar("rachaActual" + SEPARADOR + p.getRachaActual());
        lineas.agregar("mejorRacha" + SEPARADOR + p.getMejorRacha());
        lineas.agregar("ordenDeMarkov" + SEPARADOR + p.getOrdenDeMarkov());
        lineas.agregar("modoDeMarkov" + SEPARADOR + p.getModoDeMarkov().getNombre());
        return lineas;
    }

    @Override
    protected String encabezado() {
        return "Progreso del jugador entre partidas.\nFormato: clave=valor";
    }
}
