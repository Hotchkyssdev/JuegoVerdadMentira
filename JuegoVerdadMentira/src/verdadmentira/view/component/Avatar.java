package verdadmentira.view.component;

import verdadmentira.domain.Personaje;
import verdadmentira.view.style.Estilo;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class Avatar extends JComponent {
    private static final Color CONTORNO = new Color(0x3A3F47);

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
        int margen = Math.max(5, w / 16);
        int pie = conNombre ? (int) (w * 0.2) : margen;
        g2.setColor(Estilo.MARCO);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g2.setColor(marca == null ? Estilo.BORDE : Estilo.colorDeMarca(marca));
        g2.setStroke(new BasicStroke(marca == null ? 1.5f : 4f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, 10, 10);

        int fw = w - 2 * margen;
        int fh = h - margen - pie;
        g2.setColor(Estilo.CIELO);
        g2.fillRect(margen, margen, fw, fh);
        Shape recorteAnterior = g2.getClip();
        g2.clip(new RoundRectangle2D.Double(margen, margen, fw, fh, 2, 2));
        dibujarPersona(g2, personaje, margen + fw / 2.0, margen + fh * 0.44, fw * 0.21);
        g2.setClip(recorteAnterior);

        if (conNombre) {
            g2.setColor(Estilo.colorDeNombre(personaje));
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(11, w / 9)));
            FontMetrics fm = g2.getFontMetrics();
            String nombre = personaje.getNombre();
            g2.drawString(nombre, (w - fm.stringWidth(nombre)) / 2, margen + fh + (pie + fm.getAscent()) / 2 - 2);
        }
        g2.dispose();
    }

    private static void dibujarPersona(Graphics2D g, Personaje personaje, double cx, double cy, double r) {
        g.setStroke(new BasicStroke((float) Math.max(1.2, r / 12)));

        if (personaje.tienePeloLargo()) {
            g.setColor(personaje.getColorPelo());
            g.fill(new RoundRectangle2D.Double(cx - r * 1.2, cy - r * 1.15, r * 2.4, r * 2.7, r * 1.2, r * 1.2));
        }

        double hombrosY = cy + r * 1.25;
        RoundRectangle2D cuerpo = new RoundRectangle2D.Double(cx - r * 1.7, hombrosY, r * 3.4, r * 3, r * 1.4, r * 1.4);
        g.setColor(personaje.getColorRopa());
        g.fill(cuerpo);
        g.setColor(CONTORNO);
        g.draw(cuerpo);

        g.setColor(Estilo.PIEL);
        g.fill(new RoundRectangle2D.Double(cx - r * 0.32, cy + r * 0.7, r * 0.64, r * 0.75, r * 0.2, r * 0.2));
        g.setColor(Estilo.mezclar(personaje.getColorRopa(), Color.WHITE, 0.25));
        int[] cuelloX = {(int) (cx - r * 0.45), (int) cx, (int) (cx + r * 0.45)};
        int[] cuelloY = {(int) hombrosY, (int) (hombrosY + r * 0.45), (int) hombrosY};
        g.fillPolygon(cuelloX, cuelloY, 3);

        Ellipse2D cara = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2.05);
        g.setColor(Estilo.PIEL);
        g.fill(cara);

        g.setColor(personaje.getColorPelo());
        g.fillArc((int) (cx - r * 1.08), (int) (cy - r * 1.2), (int) (r * 2.16), (int) (r * 1.7), 0, 180);
        g.fillOval((int) (cx - r * 1.05), (int) (cy - r * 0.55), (int) (r * 0.45), (int) (r * 0.7));
        g.fillOval((int) (cx + r * 0.6), (int) (cy - r * 0.55), (int) (r * 0.45), (int) (r * 0.7));

        g.setColor(CONTORNO);
        g.draw(cara);

        double ojo = Math.max(2.5, r * 0.17);
        g.fill(new Ellipse2D.Double(cx - r * 0.42 - ojo / 2, cy + r * 0.05, ojo, ojo * 1.15));
        g.fill(new Ellipse2D.Double(cx + r * 0.42 - ojo / 2, cy + r * 0.05, ojo, ojo * 1.15));

        g.setColor(new Color(0xF2A7A0));
        g.fill(new Ellipse2D.Double(cx - r * 0.78, cy + r * 0.38, r * 0.34, r * 0.22));
        g.fill(new Ellipse2D.Double(cx + r * 0.44, cy + r * 0.38, r * 0.34, r * 0.22));

        g.setColor(CONTORNO);
        g.drawArc((int) (cx - r * 0.25), (int) (cy + r * 0.3), (int) (r * 0.5), (int) (r * 0.35), 200, 140);

        dibujarAccesorio(g, personaje, cx, cy, r);
    }

    private static void dibujarAccesorio(Graphics2D g, Personaje personaje, double cx, double cy, double r) {
        switch (personaje.getAccesorio()) {
            case ANTEOJOS -> {
                g.setStroke(new BasicStroke((float) Math.max(1.4, r / 9)));
                g.setColor(CONTORNO);
                double lente = r * 0.5;
                g.draw(new Ellipse2D.Double(cx - r * 0.42 - lente / 2, cy + r * 0.12 - lente / 2, lente, lente));
                g.draw(new Ellipse2D.Double(cx + r * 0.42 - lente / 2, cy + r * 0.12 - lente / 2, lente, lente));
                g.drawLine((int) (cx - r * 0.17), (int) (cy + r * 0.1), (int) (cx + r * 0.17), (int) (cy + r * 0.1));
            }
            case MONIO -> {
                double bx = cx + r * 0.7;
                double by = cy - r * 0.85;
                double t = r * 0.42;
                Path2D izquierda = new Path2D.Double();
                izquierda.moveTo(bx, by);
                izquierda.lineTo(bx - t, by - t * 0.7);
                izquierda.lineTo(bx - t, by + t * 0.7);
                izquierda.closePath();
                Path2D derecha = new Path2D.Double();
                derecha.moveTo(bx, by);
                derecha.lineTo(bx + t, by - t * 0.7);
                derecha.lineTo(bx + t, by + t * 0.7);
                derecha.closePath();
                g.setColor(new Color(0xE05A7A));
                g.fill(izquierda);
                g.fill(derecha);
                g.setColor(new Color(0xB8405E));
                g.fill(new Ellipse2D.Double(bx - t * 0.28, by - t * 0.28, t * 0.56, t * 0.56));
            }
            case GORRA -> {
                Color gorra = new Color(0x2F6FB0);
                g.setColor(gorra);
                g.fillArc((int) (cx - r * 1.1), (int) (cy - r * 1.3), (int) (r * 2.2), (int) (r * 1.75), 0, 180);
                g.fill(new RoundRectangle2D.Double(cx - r * 0.2, cy - r * 0.48, r * 1.55, r * 0.3, r * 0.3, r * 0.3));
                g.setColor(gorra.darker());
                g.fill(new Ellipse2D.Double(cx - r * 0.12, cy - r * 1.36, r * 0.24, r * 0.2));
            }
            case VINCHA -> {
                g.setStroke(new BasicStroke((float) Math.max(3, r * 0.24), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(0x3FA7A0));
                g.drawArc((int) (cx - r * 1.02), (int) (cy - r * 1.08), (int) (r * 2.04), (int) (r * 1.5), 20, 140);
            }
            case BARBA -> {
                g.setColor(personaje.getColorPelo());
                Path2D barba = new Path2D.Double();
                barba.moveTo(cx - r * 0.98, cy + r * 0.15);
                barba.quadTo(cx - r * 0.9, cy + r * 1.25, cx, cy + r * 1.25);
                barba.quadTo(cx + r * 0.9, cy + r * 1.25, cx + r * 0.98, cy + r * 0.15);
                barba.quadTo(cx + r * 0.6, cy + r * 0.75, cx, cy + r * 0.62);
                barba.quadTo(cx - r * 0.6, cy + r * 0.75, cx - r * 0.98, cy + r * 0.15);
                barba.closePath();
                g.fill(barba);
                g.setColor(Estilo.PIEL);
                g.fill(new Ellipse2D.Double(cx - r * 0.28, cy + r * 0.62, r * 0.56, r * 0.22));
                g.setColor(CONTORNO);
                g.drawArc((int) (cx - r * 0.25), (int) (cy + r * 0.48), (int) (r * 0.5), (int) (r * 0.35), 200, 140);
            }
        }
    }
}
