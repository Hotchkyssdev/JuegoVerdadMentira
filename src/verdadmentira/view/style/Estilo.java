package verdadmentira.view.style;

import verdadmentira.domain.Personaje;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public final class Estilo {
    public static final Color ROJO = new Color(0xC2453F);
    public static final Color VERDE = new Color(0x2C9A5A);

    public static Color FONDO;
    public static Color TARJETA;
    public static Color BORDE;
    public static Color TINTA;
    public static Color TINTA_SUAVE;
    public static Color GRIS;
    public static Color PRINCIPAL;
    public static Color ENCABEZADO;
    public static Color ROJO_CLARO;
    public static Color VERDE_CLARO;
    public static Color AMARILLO_CLARO;

    private static final String LETRA_INCLUIDA = "/verdadmentira/view/fuentes/Kalam-Bold.ttf";
    private static final Font LETRA_A_MANO = letraAMano();
    private static final String LETRA_DE_NOMBRES = LETRA_A_MANO.getFamily();

    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 28);
    public static final Font TEXTO = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font NEGRITA = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FRASE = new Font("SansSerif", Font.PLAIN, 17);
    public static final Font CHICA = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font CHICA_NEGRITA = new Font("SansSerif", Font.BOLD, 12);
    public static final Font MINI_NEGRITA = new Font("SansSerif", Font.BOLD, 10);
    public static final Font NOMBRE = LETRA_A_MANO.deriveFont(15f);

    private static boolean oscuro;

    static {
        usarTemaOscuro(true);
    }

    private Estilo() {
    }

    public static void usarTemaOscuro(boolean valor) {
        oscuro = valor;
        if (oscuro) {
            FONDO = new Color(0x13161B);
            TARJETA = new Color(0x1E232B);
            BORDE = new Color(0x323A46);
            TINTA = new Color(0xE8EAED);
            TINTA_SUAVE = new Color(0x9AA4B2);
            GRIS = new Color(0x56606E);
            PRINCIPAL = new Color(0xD9DEE5);
            ENCABEZADO = new Color(0x2A313B);
            ROJO_CLARO = new Color(0x3B2326);
            VERDE_CLARO = new Color(0x1D3528);
            AMARILLO_CLARO = new Color(0x3A3320);
        } else {
            FONDO = new Color(0xF4F1EA);
            TARJETA = Color.WHITE;
            BORDE = new Color(0xE2DDD3);
            TINTA = new Color(0x1F2A37);
            TINTA_SUAVE = new Color(0x5B6573);
            GRIS = new Color(0xB8BDC4);
            PRINCIPAL = new Color(0x1F2A37);
            ENCABEZADO = new Color(0x1F2A37);
            ROJO_CLARO = new Color(0xF6DEDC);
            VERDE_CLARO = new Color(0xDCF0E3);
            AMARILLO_CLARO = new Color(0xFFF4D6);
        }
    }

    private static Font letraAMano() {
        List<String> disponibles = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        if (disponibles.contains("Segoe Print")) return new Font("Segoe Print", Font.BOLD, 15);
        try (InputStream entrada = Estilo.class.getResourceAsStream(LETRA_INCLUIDA)) {
            if (entrada == null) return new Font(Font.SANS_SERIF, Font.BOLD, 15);
            Font incluida = Font.createFont(Font.TRUETYPE_FONT, entrada);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(incluida);
            return incluida.deriveFont(15f);
        } catch (IOException | FontFormatException e) {
            return new Font(Font.SANS_SERIF, Font.BOLD, 15);
        }
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    public static Color texto(Color color) {
        return oscuro ? mezclar(color, Color.WHITE, 0.3) : color;
    }

    public static Color colorDeNombre(Personaje p) {
        return oscuro ? mezclar(p.getColor(), Color.WHITE, 0.12) : mezclar(p.getColor(), Color.BLACK, 0.42);
    }

    public static Color textoSobre(Color fondo) {
        double luminancia = (0.299 * fondo.getRed() + 0.587 * fondo.getGreen() + 0.114 * fondo.getBlue()) / 255;
        return luminancia > 0.6 ? new Color(0x14171C) : Color.WHITE;
    }

    public static Color colorDeMarca(Boolean diceVerdad) {
        if (diceVerdad == null) return GRIS;
        return diceVerdad ? VERDE : ROJO;
    }

    public static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String nombreHtml(Personaje p) {
        return "<b><font face='" + LETRA_DE_NOMBRES + "'>" + p.getNombre() + "</font></b>";
    }

    public static JLabel resumen() {
        String html = "<html><div style='width:620px'>"
                + "<p><b>Cinco personajes.</b> Dos siempre dicen la verdad, dos siempre mienten "
                + "y uno puede hacer las dos cosas.</p><br>"
                + "<p><b>Tres rondas.</b> En cada una, todos dicen una frase. "
                + "Marcá quién miente y quién dice la verdad: hay una sola combinación que cierra.</p><br>"
                + "<p><b>Para arrancar.</b> En la primera ronda alguien dice algo que se comprueba mirando las fotos.</p><br>"
                + "<p><b>Para ganar.</b> Al final, elegí al único que cambió de una ronda a otra.</p>"
                + "</div></html>";
        JLabel etiqueta = new JLabel(html);
        etiqueta.setFont(TEXTO.deriveFont(16f));
        etiqueta.setForeground(TINTA);
        return etiqueta;
    }

    public static JLabel reglas() {
        String rojo = hex(texto(ROJO));
        String verde = hex(texto(VERDE));
        String html = "<html><div style='width:640px'>"
                + "<p><b>Los personajes.</b> Hay cinco. Al empezar se sortea quién es quién:</p>"
                + "<ul>"
                + "<li><b>Dos veraces</b>: siempre dicen la verdad.</li>"
                + "<li><b>Dos mentirosos</b>: siempre mienten.</li>"
                + "<li><b>Uno que puede hacer las dos cosas</b>: en cada ronda elige si miente o no, y en algún momento cambia.</li>"
                + "</ul>"
                + "<p>Los veraces y los mentirosos no cambian durante las tres rondas.</p><br>"
                + "<p><b>Cada ronda.</b> Cada personaje dice una frase. Si en esa ronda dice la verdad, la frase es cierta; "
                + "si miente, es falsa. Hacé clic en un personaje para ver sus flechas y qué implica su frase, "
                + "y marcalo en el panel de la derecha: <font color='" + verde + "'><b>dice la verdad</b></font> o "
                + "<font color='" + rojo + "'><b>miente</b></font>. "
                + "Hay una sola combinación que cierra con todas las frases. La primera ronda es la más simple.</p><br>"
                + "<p><b>Las flechas.</b> Salen de quien habla y llegan a quien nombra: "
                + "<font color='" + verde + "'><b>verde</b></font> si dice que dice la verdad, "
                + "<font color='" + rojo + "'><b>roja</b></font> si dice que miente y "
                + "<font color='" + hex(TINTA_SUAVE) + "'><b>gris</b></font> si habla de algo que se ve en su foto. "
                + "Si alguien habla de sí mismo, la flecha da la vuelta y vuelve a él. "
                + "Al elegir a alguien, la línea gruesa es lo que dice y la punteada, lo que dicen de él.</p><br>"
                + "<p><b>El árbol de cada personaje</b> está siempre en la franja de abajo: cada nivel es una ronda, "
                + "a la izquierda mentiroso y a la derecha veraz. El juego no te dice si acertaste.</p><br>"
                + "<p><b>Probar y volver atrás.</b> Podés suponer algo, marcarlo y seguir lo que implica. "
                + "Si llegás a una contradicción, usá <b>Deshacer</b> o <b>Deshacer todo</b> para volver atrás.</p><br>"
                + "<p><b>Pistas.</b> Tenés tres por partida: te marcan las frases que no cierran con lo que marcaste.</p><br>"
                + "<p><b>Para arrancar.</b> En la primera ronda, uno de los personajes dice algo que podés comprobar "
                + "mirando las fotos (por ejemplo, quién tiene barba o de qué color es el fondo de una foto). "
                + "Si es cierto, ese personaje dice la verdad; si es falso, miente.</p><br>"
                + "<p><b>Al final</b> elegís quién es el que puede hacer las dos cosas: "
                + "es el único que cambia de una ronda a otra. Si lo encontrás, ganás.</p><br>"
                + "<p><font color='" + hex(TINTA_SUAVE) + "'><b>Pista:</b> suponé que alguien dice la verdad y seguí lo que eso implica. "
                + "Si llegás a una contradicción, ese personaje miente.</font></p>"
                + "</div></html>";
        JLabel etiqueta = new JLabel(html);
        etiqueta.setFont(TEXTO);
        etiqueta.setForeground(TINTA);
        return etiqueta;
    }

    public static Color mezclar(Color a, Color b, double proporcionDeB) {
        double t = Math.max(0, Math.min(1, proporcionDeB));
        return new Color(
                (int) Math.round(a.getRed() * (1 - t) + b.getRed() * t),
                (int) Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
                (int) Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
