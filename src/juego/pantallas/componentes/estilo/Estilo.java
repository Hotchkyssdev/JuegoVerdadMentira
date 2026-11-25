package juego.pantallas.componentes.estilo;

import estructuras.Diccionario;
import estructuras.ListaDinamica;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Personaje;
import persistencia.Recursos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class Estilo {
    public static final Color ROJO = new Color(0xC2453F);
    public static final Color VERDE = new Color(0x2C9A5A);
    public static final Color AMARILLO = new Color(0xF2C230);
    public static final Color FONDO = new Color(0x13161B);
    public static final Color TARJETA = new Color(0x1E232B);
    public static final Color BORDE = new Color(0x323A46);
    public static final Color TINTA = new Color(0xE8EAED);
    public static final Color TINTA_SUAVE = new Color(0x9AA4B2);
    public static final Color GRIS = new Color(0x56606E);
    public static final Color PRINCIPAL = new Color(0xD9DEE5);
    public static final Color ENCABEZADO = new Color(0x2A313B);
    public static final Color ROJO_CLARO = new Color(0x3B2326);
    public static final Color VERDE_CLARO = new Color(0x1D3528);
    public static final Color AMARILLO_CLARO = new Color(0x3A3320);

    private static final String LETRA_INCLUIDA = "/juego/pantallas/recursos/fuentes/Kalam-Bold.ttf";
    private static final String LETRA_DEL_SISTEMA = "Segoe Print";
    private static final Font LETRA_A_MANO = letraAMano();

    public static final Font TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    public static final Font TEXTO = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FRASE = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
    public static final Font CHICA = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font CHICA_NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font MINI_NEGRITA = new Font(Font.SANS_SERIF, Font.BOLD, 10);
    public static final Font NOMBRE = LETRA_A_MANO.deriveFont(15f);

    private static final Diccionario<String, Color> COLORES_DE_NOMBRES = coloresDeNombres();

    private Estilo() {
    }

    private static Font letraAMano() {
        Font delSistema = new Font(LETRA_DEL_SISTEMA, Font.BOLD, 15);
        if (LETRA_DEL_SISTEMA.equals(delSistema.getFamily())) return delSistema;
        Font incluida = Recursos.fuente(LETRA_INCLUIDA);
        return incluida == null ? new Font(Font.SANS_SERIF, Font.BOLD, 15) : incluida.deriveFont(15f);
    }

    private static Diccionario<String, Color> coloresDeNombres() {
        Diccionario<String, Color> colores = new Diccionario<>();
        ListaDinamica<Personaje> elenco = Elenco.nuevo();
        for (int i = 0; i < elenco.tamanio(); i++) colores.poner(elenco.obtener(i).getNombre(), elenco.obtener(i).getColor());
        return colores;
    }

    public static Color colorDeNombre(String nombre) {
        Color color = COLORES_DE_NOMBRES.obtener(nombre);
        return color == null ? TINTA : mezclar(color, Color.WHITE, 0.12);
    }

    public static Color colorDeNombre(Personaje p) {
        return mezclar(p.getColor(), Color.WHITE, 0.12);
    }

    public static Color texto(Color color) {
        return mezclar(color, Color.WHITE, 0.3);
    }

    public static Color textoSobre(Color fondo) {
        double luminancia = (0.299 * fondo.getRed() + 0.587 * fondo.getGreen() + 0.114 * fondo.getBlue()) / 255;
        return luminancia > 0.6 ? new Color(0x14171C) : Color.WHITE;
    }

    public static Color colorDeMarca(Boolean diceLaVerdad) {
        if (diceLaVerdad == null) return GRIS;
        return diceLaVerdad ? VERDE : ROJO;
    }

    public static Color conAlfa(Color c, double alfa) {
        int a = (int) Math.round(Math.max(0, Math.min(1, alfa)) * 255);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static Color mezclar(Color a, Color b, double proporcionDeB) {
        double t = Math.max(0, Math.min(1, proporcionDeB));
        return new Color(
                (int) Math.round(a.getRed() * (1 - t) + b.getRed() * t),
                (int) Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
                (int) Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
    }

    public static String nombre(Personaje p) {
        return "{" + p.getNombre() + "}";
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}
