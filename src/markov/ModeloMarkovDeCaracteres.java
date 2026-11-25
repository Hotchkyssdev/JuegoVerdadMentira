package markov;

import estructuras.ListaDinamica;

public class ModeloMarkovDeCaracteres extends ModeloMarkov {
    private static final int LARGO_MAXIMO = 160;

    public ModeloMarkovDeCaracteres(int orden) {
        super(orden);
    }

    @Override
    protected ListaDinamica<String> separar(String linea) {
        ListaDinamica<String> simbolos = new ListaDinamica<>();
        for (int i = 0; i < linea.length(); i++) simbolos.agregar(String.valueOf(linea.charAt(i)));
        return simbolos;
    }

    @Override
    protected String unir(ListaDinamica<String> simbolos) {
        return simbolos.unir("");
    }

    @Override
    protected int largoMaximo() {
        return LARGO_MAXIMO;
    }

    @Override
    public String getDescripcion() {
        return "por caracteres, k = " + getOrden();
    }
}
