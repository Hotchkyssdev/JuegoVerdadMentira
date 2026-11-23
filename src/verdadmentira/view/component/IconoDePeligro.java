package verdadmentira.view.component;

import verdadmentira.view.style.Estilo;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class IconoDePeligro implements Icon {
    public static final Color AMARILLO = new Color(0xF2C230);
    private static final Color CONTORNO = new Color(0x5A4300);

    private final int tamanio;

    public IconoDePeligro(int tamanio) {
        this.tamanio = tamanio;
    }

    @Override
    public void paintIcon(Component componente, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        g2.translate(x, y);
        double t = tamanio;
        Path2D triangulo = new Path2D.Double();
        triangulo.moveTo(t / 2, 1);
        triangulo.lineTo(t - 1, t - 2);
        triangulo.lineTo(1, t - 2);
        triangulo.closePath();
        g2.setColor(AMARILLO);
        g2.fill(triangulo);
        g2.setColor(CONTORNO);
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(triangulo);
        g2.setStroke(new BasicStroke((float) Math.max(1.8, t / 9), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) Math.round(t / 2), (int) Math.round(t * 0.36), (int) Math.round(t / 2), (int) Math.round(t * 0.62));
        double punto = Math.max(2.2, t / 7.5);
        g2.fill(new Ellipse2D.Double(t / 2 - punto / 2, t * 0.73, punto, punto));
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return tamanio;
    }

    @Override
    public int getIconHeight() {
        return tamanio;
    }
}
