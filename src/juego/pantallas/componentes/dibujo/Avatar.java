package juego.pantallas.componentes.dibujo;

import estructuras.Diccionario;
import juego.pantallas.componentes.estilo.Estilo;
import juego.reglas.personajes.Personaje;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public final class Avatar {
    private static final Color PAPEL = new Color(0xFBF8F1);
    private static final Color BORDE_DEL_PAPEL = new Color(0xDDD6C8);
    private static final Color TINTA_DE_LAPICERA = new Color(0x2B3A67);
    private static final String CARPETA_DE_FOTOS = "/juego/pantallas/recursos/fotos/";
    private static final Diccionario<String, Image> FOTOS = new Diccionario<>();
    private static final Diccionario<String, BufferedImage> ESCALADAS = new Diccionario<>();

    private Avatar() {
    }

    public static int alto(int ancho, boolean conNombre) {
        return (int) Math.round(ancho * (conNombre ? 1.22 : 1.08));
    }

    public static void pintar(Graphics2D g, Personaje personaje, double x, double y, int w, boolean conNombre, Boolean marca) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        pintar(g2, personaje, w, conNombre, marca);
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
        double escala = Math.max(1, Math.ceil(g2.getTransform().getScaleX() * 4) / 4);
        BufferedImage foto = fotoEscalada(personaje, (int) Math.round(lado * escala));
        if (foto != null) g2.drawImage(foto, margen, margen, lado, lado, null);
        g2.setColor(new Color(0, 0, 0, 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(margen, margen, lado - 1, lado - 1);

        if (marca != null) {
            float grosor = Math.max(3f, w / 28f);
            g2.setColor(Estilo.colorDeMarca(marca));
            g2.setStroke(new BasicStroke(grosor));
            g2.drawRect(Math.round(grosor / 2), Math.round(grosor / 2), Math.round(anchoTarjeta - grosor), Math.round(altoTarjeta - grosor));
        } else {
            g2.setColor(BORDE_DEL_PAPEL);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(0, 0, anchoTarjeta - 1, altoTarjeta - 1);
        }

        if (conNombre) {
            int pie = altoTarjeta - margen - lado;
            g2.setColor(TINTA_DE_LAPICERA);
            g2.setFont(Estilo.NOMBRE.deriveFont((float) Math.max(12, w / 7.5)));
            FontMetrics medidas = g2.getFontMetrics();
            String nombre = personaje.getNombre();
            int base = margen + lado + (pie + medidas.getAscent() - medidas.getDescent()) / 2;
            g2.drawString(nombre, (anchoTarjeta - medidas.stringWidth(nombre)) / 2, base);
        }
        g2.dispose();
    }

    private static BufferedImage fotoEscalada(Personaje personaje, int lado) {
        if (lado <= 0) return null;
        String archivo = personaje.getNombre().toLowerCase() + ".png";
        String clave = archivo + "@" + lado;
        BufferedImage guardada = ESCALADAS.obtener(clave);
        if (guardada != null) return guardada;
        Image original = original(archivo);
        if (original == null) return null;
        BufferedImage escalada = cubrir(original, lado);
        ESCALADAS.poner(clave, escalada);
        return escalada;
    }

    private static Image original(String archivo) {
        if (FOTOS.contieneClave(archivo)) return FOTOS.obtener(archivo);
        var ubicacion = Avatar.class.getResource(CARPETA_DE_FOTOS + archivo);
        Image imagen = null;
        if (ubicacion != null) {
            ImageIcon icono = new ImageIcon(ubicacion);
            if (icono.getIconWidth() > 0) imagen = icono.getImage();
        }
        FOTOS.poner(archivo, imagen);
        return imagen;
    }

    private static BufferedImage cubrir(Image original, int lado) {
        int ancho = original.getWidth(null);
        int alto = original.getHeight(null);
        double escala = Math.max((double) lado / ancho, (double) lado / alto);
        int anchoEscalado = (int) Math.ceil(ancho * escala);
        int altoEscalado = (int) Math.ceil(alto * escala);
        Image suavizada = original.getScaledInstance(anchoEscalado, altoEscalado, Image.SCALE_SMOOTH);
        BufferedImage resultado = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(new ImageIcon(suavizada).getImage(), (lado - anchoEscalado) / 2, 0, null);
        g.dispose();
        return resultado;
    }
}
