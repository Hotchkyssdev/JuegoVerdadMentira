package tokens;

public final class Normalizador {
    private static final String CON_TILDE = "áéíóúüàèìòùâêîôû";
    private static final String SIN_TILDE = "aeiouuaeiouaeiou";

    private Normalizador() {
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        StringBuilder resultado = new StringBuilder(texto.length());
        String minusculas = texto.toLowerCase();
        for (int i = 0; i < minusculas.length(); i++) resultado.append(sinTilde(minusculas.charAt(i)));
        return resultado.toString();
    }

    public static char sinTilde(char letra) {
        int posicion = CON_TILDE.indexOf(letra);
        return posicion < 0 ? letra : SIN_TILDE.charAt(posicion);
    }

    public static boolean esLetra(char c) {
        return Character.isLetterOrDigit(c);
    }

    public static boolean esEspacio(char c) {
        return Character.isWhitespace(c);
    }
}
