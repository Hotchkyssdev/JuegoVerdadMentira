package juego.reglas.deduccion;

import estructuras.ListaDinamica;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;
import tokens.Normalizador;
import tokens.Token;
import tokens.TokenizadorPorTrie;

public class InterpreteDeDeducciones {
    private static final int PALABRAS_PARA_SUGERIR = 3;

    private final TokenizadorPorTrie<Significado> tokenizador = new TokenizadorPorTrie<>();

    public InterpreteDeDeducciones(ListaDinamica<Personaje> personajes) {
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            tokenizador.agregar(p.getNombre(), Significado.de(p));
            ListaDinamica<String> apodos = p.getApodos();
            for (int j = 0; j < apodos.tamanio(); j++) tokenizador.agregar(apodos.obtener(j), Significado.de(p));
        }
        agregarVeredictos(true, "dice la verdad|dicen la verdad|veraz|veraces|es veraz|son veraces|no miente|no mienten|v");
        agregarVeredictos(false, "miente|mienten|mentiroso|mentirosa|mentirosos|es mentiroso|es mentirosa|son mentirosos"
                + "|no dice la verdad|no dicen la verdad|m");
        agregarFormas("y|e|,|.|tambien|pero", Significado.conector());
    }

    private void agregarVeredictos(boolean diceLaVerdad, String formas) {
        agregarFormas(formas, Significado.veredicto(diceLaVerdad));
    }

    private void agregarFormas(String formas, Significado significado) {
        int inicio = 0;
        while (inicio <= formas.length()) {
            int fin = formas.indexOf('|', inicio);
            if (fin < 0) fin = formas.length();
            tokenizador.agregar(formas.substring(inicio, fin), significado);
            inicio = fin + 1;
        }
    }

    public Deduccion interpretar(String texto) {
        ListaDinamica<Token<Significado>> tokens = tokenizador.tokenizar(texto);
        Marcas marcas = new Marcas();
        ListaDinamica<String> errores = new ListaDinamica<>();
        ListaDinamica<Personaje> pendientes = new ListaDinamica<>();
        for (int i = 0; i < tokens.tamanio(); i++) {
            Token<Significado> token = tokens.obtener(i);
            if (!token.esReconocido()) {
                errores.agregar("No entiendo «" + token.getTexto() + "».");
                continue;
            }
            Significado significado = token.getValor();
            if (significado.getTipo() == Significado.Tipo.PERSONAJE) {
                if (!pendientes.contiene(significado.getPersonaje())) pendientes.agregar(significado.getPersonaje());
            } else if (significado.getTipo() == Significado.Tipo.VERAZ || significado.getTipo() == Significado.Tipo.MENTIROSO) {
                if (pendientes.estaVacia()) {
                    errores.agregar("«" + token.getTexto() + "»: falta decir de quién.");
                    continue;
                }
                boolean veraz = significado.getTipo() == Significado.Tipo.VERAZ;
                for (int j = 0; j < pendientes.tamanio(); j++) marcas.marcar(pendientes.obtener(j), veraz);
                pendientes.vaciar();
            }
        }
        if (!pendientes.estaVacia()) {
            errores.agregar("Falta decir si " + pendientes.unir(" y ") + (pendientes.tamanio() == 1 ? " miente o dice la verdad." : " mienten o dicen la verdad."));
        }
        if (tokens.estaVacia()) errores.agregar("Escribí, por ejemplo: «lisa miente, ferb y roz dicen la verdad».");
        return new Deduccion(marcas, errores, tokens);
    }

    public ListaDinamica<String> sugerencias(String texto) {
        String normalizado = Normalizador.normalizar(texto);
        if (normalizado.isEmpty() || Normalizador.esEspacio(normalizado.charAt(normalizado.length() - 1))) {
            return new ListaDinamica<>();
        }
        ListaDinamica<String> mejores = new ListaDinamica<>();
        int inicio = normalizado.length();
        for (int palabras = 0; palabras < PALABRAS_PARA_SUGERIR && inicio > 0; palabras++) {
            inicio = inicioDePalabraAnterior(normalizado, inicio);
            ListaDinamica<String> candidatas = tokenizador.sugerencias(normalizado.substring(inicio));
            if (!candidatas.estaVacia()) mejores = candidatas;
        }
        return mejores.filtrar(s -> s.length() > 1);
    }

    private static int inicioDePalabraAnterior(String texto, int desde) {
        int i = desde;
        while (i > 0 && Normalizador.esEspacio(texto.charAt(i - 1))) i--;
        while (i > 0 && !Normalizador.esEspacio(texto.charAt(i - 1)) && texto.charAt(i - 1) != ',') i--;
        return i;
    }

    public int tamanioDelVocabulario() {
        return tokenizador.tamanioDelVocabulario();
    }
}
