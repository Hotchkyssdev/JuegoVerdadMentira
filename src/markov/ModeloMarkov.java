package markov;

import arboles.ArbolBinarioDeBusqueda;
import estructuras.ListaDinamica;

import java.util.Random;

public abstract class ModeloMarkov implements GeneradorDeTexto {
    public static final int ORDEN_MINIMO = 1;
    public static final int ORDEN_MAXIMO = 4;
    protected static final String INICIO = "\u0002";
    protected static final String FIN = "\u0003";
    private static final String SEPARADOR = "\u001F";
    private static final int INTENTOS = 25;

    private final int orden;
    private final ArbolBinarioDeBusqueda<EstadoDeMarkov> tabla = new ArbolBinarioDeBusqueda<>();
    private int secuenciasEntrenadas;

    protected ModeloMarkov(int orden) {
        if (orden < ORDEN_MINIMO || orden > ORDEN_MAXIMO) {
            throw new IllegalArgumentException("El orden tiene que estar entre " + ORDEN_MINIMO + " y " + ORDEN_MAXIMO + ": " + orden);
        }
        this.orden = orden;
    }

    protected abstract ListaDinamica<String> separar(String linea);

    protected abstract String unir(ListaDinamica<String> simbolos);

    protected abstract int largoMaximo();

    public void entrenar(ListaDinamica<String> lineas) {
        for (int i = 0; i < lineas.tamanio(); i++) {
            ListaDinamica<String> simbolos = separar(lineas.obtener(i));
            if (!simbolos.estaVacia()) entrenarSecuencia(simbolos);
        }
    }

    private void entrenarSecuencia(ListaDinamica<String> simbolos) {
        ListaDinamica<String> secuencia = new ListaDinamica<>();
        for (int i = 0; i < orden; i++) secuencia.agregar(INICIO);
        secuencia.agregarTodos(simbolos);
        secuencia.agregar(FIN);
        for (int i = orden; i < secuencia.tamanio(); i++) {
            String siguiente = secuencia.obtener(i);
            for (int largo = 0; largo <= orden; largo++) {
                estado(clave(secuencia, i - largo, i)).registrar(siguiente);
            }
        }
        secuenciasEntrenadas++;
    }

    private EstadoDeMarkov estado(String contexto) {
        EstadoDeMarkov buscado = new EstadoDeMarkov(contexto);
        EstadoDeMarkov existente = tabla.obtener(buscado);
        if (existente != null) return existente;
        tabla.agregar(buscado);
        return buscado;
    }

    private static String clave(ListaDinamica<String> secuencia, int desde, int hasta) {
        StringBuilder clave = new StringBuilder();
        for (int i = desde; i < hasta; i++) {
            if (i > desde) clave.append(SEPARADOR);
            clave.append(secuencia.obtener(i));
        }
        return clave.toString();
    }

    @Override
    public Generacion generar(Random azar) {
        if (secuenciasEntrenadas == 0) throw new ModeloSinEntrenarException();
        for (int intento = 1; intento <= INTENTOS; intento++) {
            Generacion generacion = generarUnaVez(azar, intento);
            if (generacion != null) return generacion;
        }
        throw new ModeloSinEntrenarException();
    }

    private Generacion generarUnaVez(Random azar, int intento) {
        ListaDinamica<String> contexto = new ListaDinamica<>();
        for (int i = 0; i < orden; i++) contexto.agregar(INICIO);
        ListaDinamica<String> salida = new ListaDinamica<>();
        int retrocesos = 0;
        boolean cortada = false;
        while (true) {
            EstadoDeMarkov estado = null;
            for (int largo = orden; largo >= 0 && estado == null; largo--) {
                EstadoDeMarkov candidato = tabla.obtener(new EstadoDeMarkov(clave(contexto, orden - largo, orden)));
                if (candidato != null && candidato.tieneSucesores()) estado = candidato;
                else retrocesos++;
            }
            String siguiente = estado.elegir(azar);
            if (siguiente.equals(FIN)) break;
            salida.agregar(siguiente);
            contexto.eliminar(0);
            contexto.agregar(siguiente);
            if (salida.tamanio() >= largoMaximo()) {
                cortada = true;
                break;
            }
        }
        String texto = unir(salida);
        if (texto.trim().isEmpty()) return null;
        return new Generacion(texto, salida.tamanio(), retrocesos, cortada, intento);
    }

    public ListaDinamica<String> sucesoresDe(ListaDinamica<String> contexto) {
        ListaDinamica<String> resultado = new ListaDinamica<>();
        EstadoDeMarkov estado = tabla.obtener(new EstadoDeMarkov(clave(contexto, 0, contexto.tamanio())));
        if (estado == null) return resultado;
        ListaDinamica<Sucesor> sucesores = estado.getSucesores();
        for (int i = 0; i < sucesores.tamanio(); i++) resultado.agregar(sucesores.obtener(i).getSimbolo());
        return resultado;
    }

    public int cantidadDeEstados() {
        return tabla.cantidadDeNodos();
    }

    public int alturaDeLaTabla() {
        return tabla.altura();
    }

    @Override
    public int getOrden() {
        return orden;
    }
}
