package verdadmentira.view.animacion;

import verdadmentira.view.style.Estilo;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class CapaDeTransicion extends JComponent {
    private Consumer<Graphics2D> dibujo;

    private CapaDeTransicion() {
        setOpaque(false);
        MouseAdapter bloqueo = new MouseAdapter() { };
        addMouseListener(bloqueo);
        addMouseMotionListener(bloqueo);
        addMouseWheelListener(bloqueo);
    }

    public static CapaDeTransicion de(JRootPane raiz) {
        if (raiz.getGlassPane() instanceof CapaDeTransicion capa) return capa;
        CapaDeTransicion capa = new CapaDeTransicion();
        raiz.setGlassPane(capa);
        return capa;
    }

    public static void quitar(JRootPane raiz) {
        if (raiz.getGlassPane() instanceof CapaDeTransicion capa) capa.terminar();
    }

    public void mostrar(Consumer<Graphics2D> dibujo) {
        this.dibujo = dibujo;
        setVisible(true);
        repaint();
    }

    public void terminar() {
        dibujo = null;
        setVisible(false);
    }

    public Rectangle ubicacionDe(Component componente) {
        return SwingUtilities.convertRectangle(componente.getParent(), componente.getBounds(), this);
    }

    public BufferedImage fotografiar(Component componente) {
        double escala = escalaDePantalla(componente.getGraphicsConfiguration());
        int ancho = Math.max(1, (int) Math.ceil(componente.getWidth() * escala));
        int alto = Math.max(1, (int) Math.ceil(componente.getHeight() * escala));
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.scale(escala, escala);
        componente.paint(g);
        g.dispose();
        return imagen;
    }

    private static double escalaDePantalla(GraphicsConfiguration configuracion) {
        return configuracion == null ? 1 : configuracion.getDefaultTransform().getScaleX();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (dibujo == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        dibujo.accept(g2);
        g2.dispose();
    }
}
