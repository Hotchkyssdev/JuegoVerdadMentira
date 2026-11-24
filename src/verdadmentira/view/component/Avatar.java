package verdadmentira.view.component;

import verdadmentira.domain.Personaje;
import verdadmentira.view.style.Estilo;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Avatar extends JComponent {
    private static final Color PAPEL = new Color(0xFBF8F1);
    private static final Color BORDE_DEL_PAPEL = new Color(0xDDD6C8);
    private static final Color TINTA_DE_LAPICERA = new Color(0x2B3A67);
    private static final String CARPETA_DE_FOTOS = "/verdadmentira/view/fotos/";
    private static final Map<String, Optional<BufferedImage>> FOTOS = new HashMap<>();
    private static final Map<String, BufferedImage> FOTOS_ESCALADAS = new HashMap<>();

    private final Personaje personaje;
    private final int ancho;
    private final boolean conNombre;
    private Boolean marca;

    public Avatar(Personaje personaje, int ancho, boolean conNombre) {
        this.personaje = personaje;
        this.ancho = ancho;
        this.conNombre = conNombre;
        Dimension tamanio = new Dimension(ancho, alto(ancho, conNombre));
        setPreferredSize(tamanio);
        setMinimumSize(tamanio);
        setMaximumSize(tamanio);
    }

    public static int alto(int ancho, boolean conNombre) {
        return (int) Math.round(ancho * (conNombre ? 1.22 : 1.08));
    }

    public void setMarca(Boolean marca) {
        this.marca = marca;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        g2.translate((getWidth() - ancho) / 2, (getHeight() - alto(ancho, conNombre)) / 2);
        pintar(g2, personaje, ancho, conNombre, marca);
        g2.dispose();
    }

    public static void pintar(Graphics2D g, Personaje personaje, int w, boolean conNombre, Boolean marca) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int h = alto(w, conNombre);
        int sombra = Math.max(2, w / 40);
        int anchoTarjeta = w - sombra;
        int altoTarjeta = h - sombra;

        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRect(sombra, sombra, anchoTarjeta, altoTarjeta);
        g2.setColor(PAPEL);
        g2.fillRect(0, 0, anchoTarjeta, altoTarjeta);

        int margen = Math.max(4, Math.round(anchoTarjeta * 0.065f));
        int lado = anchoTarjeta - 2 * margen;
        g2.setColor(personaje.getColor());
        g2.fillRect(margen, margen, lado, lado);
        double escalaDePantalla = Math.max(1, Math.ceil(g2.getTransform().getScaleX() * 4) / 4);
        int ladoEnPixeles = (int) Math.round(lado * escalaDePantalla);
        fotoEscalada(personaje, ladoEnPixeles, ladoEnPixeles)
                .ifPresent(foto -> g2.drawImage(foto, margen, margen, lado, lado, null));
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(margen, margen, lado - 1, lado - 1);

        if (marca != null) {
            float grosor = Math.max(3f, w / 28f);
            g2.setColor(Estilo.colorDeMarca(marca));
            g2.setStroke(new BasicStroke(grosor));
            g2.drawRect(Math.round(grosor / 2), Math.round(grosor / 2),
                    Math.round(anchoTarjeta - grosor), Math.round(altoTarjeta - grosor));
        } else {
            g2.setColor(BORDE_DEL_PAPEL);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(0, 0, anchoTarjeta - 1, altoTarjeta - 1);
        }

        if (conNombre) {
            int pie = altoTarjeta - margen - lado;
            g2.setColor(TINTA_DE_LAPICERA);
            g2.setFont(Estilo.NOMBRE.deriveFont((float) Math.max(12, w / 7.5)));
            FontMetrics fm = g2.getFontMetrics();
            String nombre = personaje.getNombre();
            int base = margen + lado + (pie + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(nombre, (anchoTarjeta - fm.stringWidth(nombre)) / 2, base);
        }
        g2.dispose();
    }

    private static Optional<BufferedImage> fotoEscalada(Personaje personaje, int ancho, int alto) {
        String archivo = nombreDeArchivo(personaje);
        Optional<BufferedImage> original = FOTOS.computeIfAbsent(archivo, Avatar::leerFoto);
        if (original.isEmpty() || ancho <= 0 || alto <= 0) return original;
        return Optional.of(FOTOS_ESCALADAS.computeIfAbsent(archivo + "@" + ancho + "x" + alto,
                clave -> cubrir(original.get(), ancho, alto)));
    }

    private static String nombreDeArchivo(Personaje personaje) {
        String sinTildes = Normalizer.normalize(personaje.getNombre(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT) + ".png";
    }

    private static Optional<BufferedImage> leerFoto(String archivo) {
        try (InputStream entrada = Avatar.class.getResourceAsStream(CARPETA_DE_FOTOS + archivo)) {
            return entrada == null ? Optional.empty() : Optional.ofNullable(ImageIO.read(entrada));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static BufferedImage cubrir(BufferedImage original, int ancho, int alto) {
        double escala = Math.max((double) ancho / original.getWidth(), (double) alto / original.getHeight());
        int anchoEscalado = (int) Math.ceil(original.getWidth() * escala);
        int altoEscalado = (int) Math.ceil(original.getHeight() * escala);
        Image suavizada = original.getScaledInstance(anchoEscalado, altoEscalado, Image.SCALE_SMOOTH);
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(suavizada, (ancho - anchoEscalado) / 2, 0, null);
        g.dispose();
        return resultado;
    }
}
