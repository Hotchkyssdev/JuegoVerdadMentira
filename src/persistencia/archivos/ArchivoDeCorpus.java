package persistencia.archivos;

import estructuras.ListaDinamica;
import markov.Corpus;

import java.io.File;

/**
 * corpus.txt tiene los rumores con los que se entrena el modelo de Markov,
 * un rumor por línea, por ejemplo «Dicen que Lisa escondió las llaves del
 * laboratorio.». Las líneas que empiezan con «@ » declaran una expresión de
 * varias palabras que el tokenizador tiene que tratar como un solo token,
 * por ejemplo «@ sala de profesores»: el trie la elige por coincidencia más
 * larga antes que la palabra «sala» sola. Una línea de más de 200
 * caracteres o una expresión de una sola palabra se considera dañada. Si no
 * queda ningún rumor válido, se usa el corpus inicial.
 */
public class ArchivoDeCorpus extends ArchivoDeTexto<Corpus> {
    public static final String NOMBRE = "corpus.txt";
    private static final String MARCA_DE_EXPRESION = "@";
    private static final int LARGO_MAXIMO = 200;

    private static final String EXPRESIONES_INICIALES =
            "sala de profesores\n"
            + "máquina de café\n"
            + "a escondidas\n"
            + "la semana pasada\n"
            + "el kiosco de la esquina\n"
            + "de golpe\n";

    private static final String RUMORES_INICIALES =
            "Dicen que Lisa escondió las llaves del laboratorio en una maceta.\n"
            + "Dicen que Albert se durmió en la sala de profesores con la boca abierta.\n"
            + "Dicen que McLovin compró tres alfajores en el kiosco de la esquina y no convidó a nadie.\n"
            + "Dicen que Ferb arregló la máquina de café con un clip y un chicle.\n"
            + "Dicen que Roz tiene una libreta con los cumpleaños de todo el curso.\n"
            + "Cuentan que Albert perdió el paraguas la semana pasada y culpó al viento.\n"
            + "Cuentan que Lisa toca el saxo a escondidas en el gimnasio.\n"
            + "Cuentan que Ferb no habló en toda la mañana y después cantó un tango entero.\n"
            + "Cuentan que McLovin se anotó en el torneo de ajedrez sin saber mover el caballo.\n"
            + "Cuentan que Roz regó las plantas del patio a las seis de la mañana.\n"
            + "Se comenta que la máquina de café hace ruidos raros cuando pasa Albert.\n"
            + "Se comenta que en la sala de profesores alguien dejó un paquete de facturas.\n"
            + "Se comenta que McLovin y Ferb se cambiaron las mochilas la semana pasada.\n"
            + "Se comenta que Lisa ganó la rifa del kiosco de la esquina dos veces seguidas.\n"
            + "Se comenta que Roz encontró un gato en el depósito y lo llamó Profesor.\n"
            + "Alguien vio a Ferb pintando un mural a escondidas en el pasillo.\n"
            + "Alguien vio a Albert comiendo una medialuna de golpe, sin respirar.\n"
            + "Alguien vio a Lisa y a Roz anotando cosas en un cuaderno verde.\n"
            + "Alguien vio a McLovin practicando un saludo frente al espejo del baño.\n"
            + "Alguien vio a Roz llevando una caja enorme a la sala de profesores.\n"
            + "Parece que el cuaderno verde de Lisa tiene planos del colegio.\n"
            + "Parece que Albert guarda caramelos de menta en todos los bolsillos.\n"
            + "Parece que Ferb sabe abrir la ventana del laboratorio desde afuera.\n"
            + "Parece que McLovin tiene un primo que trabaja en el kiosco de la esquina.\n"
            + "Parece que Roz se aprendió de memoria el horario de todos los colectivos.\n"
            + "Me dijeron que la semana pasada se cortó la luz y nadie notó nada.\n"
            + "Me dijeron que Lisa y Albert discutieron por el último lugar del estacionamiento.\n"
            + "Me dijeron que Ferb tiene un pez que se llama Martes.\n"
            + "Me dijeron que McLovin llegó tarde porque se quedó mirando una paloma.\n"
            + "Me dijeron que Roz dejó una nota misteriosa en la máquina de café.\n"
            + "Dicen que la nota de la máquina de café decía que alguien se llevó las tizas.\n"
            + "Dicen que las tizas aparecieron de golpe en la mochila de Albert.\n"
            + "Dicen que Ferb y Lisa armaron un robot con latas en el recreo.\n"
            + "Dicen que el robot de latas saluda cuando pasa McLovin.\n"
            + "Cuentan que Roz y McLovin se pelearon por el control del proyector.\n"
            + "Cuentan que el gato del depósito duerme en la silla de Albert.\n"
            + "Cuentan que Lisa organizó una colecta para el gato del depósito.\n"
            + "Se comenta que Ferb escribe poemas en las servilletas del bar.\n"
            + "Se comenta que Albert sabe silbar con las dos manos.\n"
            + "Se comenta que Roz nunca usa el ascensor, ni siquiera con la caja enorme.\n"
            + "Alguien vio a Lisa saliendo del laboratorio con un frasco que brillaba.\n"
            + "Alguien vio a McLovin vendiendo figuritas repetidas a escondidas.\n"
            + "Parece que el frasco que brillaba era jugo de naranja con una linterna.\n"
            + "Me dijeron que Albert y Ferb van a abrir un puesto de empanadas.\n"
            + "Me dijeron que en la sala de profesores hay un cajón que nadie pudo abrir.\n";

    public ArchivoDeCorpus(File carpeta) {
        super(carpeta, NOMBRE);
    }

    @Override
    public Corpus datosIniciales() {
        Corpus corpus = new Corpus();
        ListaDinamica<String> expresiones = lineasDe(EXPRESIONES_INICIALES);
        for (int i = 0; i < expresiones.tamanio(); i++) corpus.agregarExpresion(expresiones.obtener(i));
        ListaDinamica<String> rumores = lineasDe(RUMORES_INICIALES);
        for (int i = 0; i < rumores.tamanio(); i++) corpus.agregarLinea(rumores.obtener(i));
        return corpus;
    }

    @Override
    protected Corpus interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos) {
        Corpus corpus = new Corpus();
        for (int i = 0; i < lineas.tamanio(); i++) {
            LineaLeida linea = lineas.obtener(i);
            String texto = linea.getTexto();
            try {
                if (texto.length() > LARGO_MAXIMO) throw new LineaDanadaException("tiene más de " + LARGO_MAXIMO + " caracteres.");
                if (tieneCaracteresDeControl(texto)) throw new LineaDanadaException("tiene caracteres que no son texto.");
                if (texto.startsWith(MARCA_DE_EXPRESION)) {
                    String expresion = texto.substring(MARCA_DE_EXPRESION.length()).trim();
                    if (expresion.indexOf(' ') < 0) throw new LineaDanadaException("una expresión tiene que tener más de una palabra.");
                    corpus.agregarExpresion(expresion);
                } else {
                    corpus.agregarLinea(texto);
                }
            } catch (LineaDanadaException e) {
                avisarLineaDanada(avisos, linea, e.getMessage());
            }
        }
        if (corpus.estaVacio()) {
            avisos.agregar(NOMBRE + " no tiene ningún rumor válido: se usa el corpus inicial.");
            return datosIniciales();
        }
        return corpus;
    }

    private static boolean tieneCaracteresDeControl(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isISOControl(texto.charAt(i))) return true;
        }
        return false;
    }

    @Override
    protected ListaDinamica<String> escribir(Corpus corpus) {
        ListaDinamica<String> lineas = new ListaDinamica<>();
        ListaDinamica<String> expresiones = corpus.getExpresiones();
        for (int i = 0; i < expresiones.tamanio(); i++) lineas.agregar(MARCA_DE_EXPRESION + " " + expresiones.obtener(i));
        lineas.agregarTodos(corpus.getLineas());
        return lineas;
    }

    @Override
    protected String encabezado() {
        return "Corpus de rumores para el modelo de Markov, un rumor por línea.\n"
                + "Las líneas con @ son expresiones de varias palabras que cuentan como un solo token.";
    }
}
