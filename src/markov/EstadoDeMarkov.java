package markov;

import estructuras.ListaDinamica;

import java.util.Random;

public class EstadoDeMarkov implements Comparable<EstadoDeMarkov> {
    private final String contexto;
    private final ListaDinamica<Sucesor> sucesores = new ListaDinamica<>();
    private int total;

    EstadoDeMarkov(String contexto) {
        if (contexto == null) throw new IllegalArgumentException("El contexto no puede ser nulo");
        this.contexto = contexto;
    }

    public String getContexto() {
        return contexto;
    }

    void registrar(String simbolo) {
        Sucesor existente = sucesores.buscar(s -> s.getSimbolo().equals(simbolo));
        if (existente == null) {
            existente = new Sucesor(simbolo);
            sucesores.agregar(existente);
        }
        existente.sumarUno();
        total++;
    }

    public boolean tieneSucesores() {
        return total > 0;
    }

    public ListaDinamica<Sucesor> getSucesores() {
        return sucesores.copia();
    }

    String elegir(Random azar) {
        int sorteo = azar.nextInt(total);
        for (int i = 0; i < sucesores.tamanio(); i++) {
            Sucesor s = sucesores.obtener(i);
            sorteo -= s.getFrecuencia();
            if (sorteo < 0) return s.getSimbolo();
        }
        return sucesores.ultimo().getSimbolo();
    }

    @Override
    public int compareTo(EstadoDeMarkov otro) {
        return contexto.compareTo(otro.contexto);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (otro == null || getClass() != otro.getClass()) return false;
        return contexto.equals(((EstadoDeMarkov) otro).contexto);
    }

    @Override
    public int hashCode() {
        return contexto.hashCode();
    }
}
