package persistencia;

import estructuras.ListaDinamica;
import grafos.Grafo;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.partida.Juego;
import juego.reglas.personajes.Elenco;
import juego.reglas.registro.Progreso;
import juego.reglas.registro.Ranking;
import markov.Corpus;
import persistencia.archivos.ArchivoDeCorpus;
import persistencia.archivos.ArchivoDeDatos;
import persistencia.archivos.ArchivoDeProgreso;
import persistencia.archivos.ArchivoDeRanking;
import persistencia.archivos.ArchivoDeRelaciones;
import persistencia.archivos.ArchivoDeUltimaPartida;

import java.io.File;

public class Almacen {
    public static final String CARPETA_POR_DEFECTO = "datos";

    private final ArchivoDeRanking archivoDeRanking;
    private final ArchivoDeRelaciones archivoDeRelaciones;
    private final ArchivoDeCorpus archivoDeCorpus;
    private final ArchivoDeProgreso archivoDeProgreso;
    private final ArchivoDeUltimaPartida archivoDeUltimaPartida;
    private final ListaDinamica<String> avisos = new ListaDinamica<>();

    private Ranking ranking;
    private Grafo<String> relaciones;
    private Corpus corpus;
    private Progreso progreso;
    private ListaDinamica<ArbolDecisiones> ultimaPartida;

    public Almacen(String carpeta) {
        this(new File(carpeta));
    }

    public Almacen(File carpeta) {
        ListaDinamica<String> nombres = Elenco.nombres();
        archivoDeRanking = new ArchivoDeRanking(carpeta);
        archivoDeRelaciones = new ArchivoDeRelaciones(carpeta, nombres, Juego.relacionesIniciales());
        archivoDeCorpus = new ArchivoDeCorpus(carpeta);
        archivoDeProgreso = new ArchivoDeProgreso(carpeta);
        archivoDeUltimaPartida = new ArchivoDeUltimaPartida(carpeta, nombres);
    }

    public void cargarTodo() {
        avisos.vaciar();
        ranking = archivoDeRanking.cargar(avisos);
        relaciones = archivoDeRelaciones.cargar(avisos);
        corpus = archivoDeCorpus.cargar(avisos);
        progreso = archivoDeProgreso.cargar(avisos);
        ultimaPartida = archivoDeUltimaPartida.cargar(avisos);
    }

    public ListaDinamica<ArchivoDeDatos<?>> getArchivos() {
        ListaDinamica<ArchivoDeDatos<?>> archivos = new ListaDinamica<>();
        archivos.agregar(archivoDeRanking);
        archivos.agregar(archivoDeRelaciones);
        archivos.agregar(archivoDeCorpus);
        archivos.agregar(archivoDeProgreso);
        archivos.agregar(archivoDeUltimaPartida);
        return archivos;
    }

    public void guardarProgreso() {
        archivoDeProgreso.guardar(progreso, avisos);
    }

    public void guardarFinDePartida(ListaDinamica<ArbolDecisiones> arboles) {
        ultimaPartida = arboles;
        archivoDeRanking.guardar(ranking, avisos);
        archivoDeRelaciones.guardar(relaciones, avisos);
        archivoDeProgreso.guardar(progreso, avisos);
        archivoDeUltimaPartida.guardar(arboles, avisos);
    }

    public ListaDinamica<String> getAvisos() {
        return avisos.copia();
    }

    public void descartarAvisos() {
        avisos.vaciar();
    }

    public Ranking getRanking() {
        return ranking;
    }

    public Grafo<String> getRelaciones() {
        return relaciones;
    }

    public Corpus getCorpus() {
        return corpus;
    }

    public Progreso getProgreso() {
        return progreso;
    }

    public ListaDinamica<ArbolDecisiones> getUltimaPartida() {
        return ultimaPartida.copia();
    }
}
