package juego.pantallas.componentes.estilo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public final class Tarjeta {
    private Tarjeta() {
    }

    public static void dibujar(Graphics2D g, Rectangle zona) {
        dibujar(g, zona, Estilo.TARJETA, Estilo.BORDE, 1.2f);
    }

    public static void dibujar(Graphics2D g, Rectangle zona, Color fondo, Color borde, float grosor) {
        g.setColor(fondo);
        g.fillRoundRect(zona.x, zona.y, zona.width, zona.height, 14, 14);
        g.setColor(borde);
        g.setStroke(new BasicStroke(grosor));
        g.drawRoundRect(zona.x, zona.y, zona.width, zona.height, 14, 14);
    }

    public static void titulo(Graphics2D g, String texto, int x, int y) {
        g.setFont(Estilo.CHICA_NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString(texto.toUpperCase(), x, y);
    }
}
