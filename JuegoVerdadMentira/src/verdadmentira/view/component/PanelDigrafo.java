package verdadmentira.view.component;

import verdadmentira.domain.GrafoPersonajes;
import verdadmentira.domain.Personaje;
import verdadmentira.view.PantallaRonda;
import verdadmentira.view.style.Estilo;

import javax.swing.CellRendererPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelDigrafo extends JPanel {
    private static final int MARGEN = 10;
    private static final int SEPARACION_GLOBO = 12;
    private static final int COLA = 10;
    private static final int ALTO_NOMBRE = 34;
    private static final double RADIO_DEL_HALO = 0.78;
    private static final float[] PUNTEADO = {7f, 6f};

    private enum Estado { APAGADA, NORMAL, ENTRANTE, SALIENTE }

    private enum Lado { ARRIBA, DERECHA, IZQUIERDA }

    private final PantallaRonda ronda;
    private final CellRendererPane pintor = new CellRendererPane();
    private final JLabel textoDeGlobo = new JLabel();
    private final Map<Personaje, Rectangle> fotos = new HashMap<>();
    private final Map<Personaje, Rectangle> globos = new HashMap<>();
    private final Map<Personaje, Lado> lados = new HashMap<>();
    private final Map<Personaje, Integer> anchosDeTexto = new HashMap<>();
    private Personaje bajoElMouse;

    public PanelDigrafo(PantallaRonda ronda) {
        super(null);
        this.ronda = ronda;
        setOpaque(false);
        add(pintor);
        textoDeGlobo.setFont(Estilo.TEXTO);
        textoDeGlobo.setVerticalAlignment(JLabel.TOP);
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Personaje p = personajeEn(e.getPoint());
                if (p != null) ronda.seleccionar(p);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Personaje p = personajeEn(e.getPoint());
                if (p != bajoElMouse) {
                    bajoElMouse = p;
                    setCursor(Cursor.getPredefinedCursor(p == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bajoElMouse = null;
                repaint();
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 420);
    }

    private Personaje personajeEn(Point punto) {
        for (Personaje p : ronda.getPersonajes()) {
            if (fotos.containsKey(p) && (fotos.get(p).contains(punto) || globos.get(p).contains(punto))) return p;
        }
        return null;
    }

    private String htmlDeGlobo(Personaje p, int anchoTexto) {
        String texto = ronda.getAfirmacion(p).getTexto(Estilo::nombreHtml);
        if (anchoTexto <= 0) return "<html><nobr>" + texto + "</nobr></html>";
        return "<html><div style='width:" + anchoTexto + "px'>" + texto + "</div></html>";
    }

    private void calcularUbicaciones() {
        fotos.clear();
        globos.clear();
        lados.clear();
        int w = getWidth();
        int h = getHeight();
        int anchoFoto = (int) Math.max(56, Math.min(84, Math.min(w, h) * 0.15));
        int altoFoto = Avatar.alto(anchoFoto, false) + ALTO_NOMBRE;
        int anchoGlobo = (int) Math.max(140, Math.min(215, w * 0.17));
        int anchoTexto = anchoGlobo - 26;

        Map<Personaje, Dimension> tamaniosDeGlobo = new HashMap<>();
        int altoGloboMaximo = 0;
        for (Personaje p : ronda.getPersonajes()) {
            textoDeGlobo.setText(htmlDeGlobo(p, 0));
            Dimension texto = textoDeGlobo.getPreferredSize();
            if (texto.width > anchoTexto) {
                int anchoDelDiv = anchoTexto;
                do {
                    textoDeGlobo.setText(htmlDeGlobo(p, anchoDelDiv));
                    texto = textoDeGlobo.getPreferredSize();
                    anchoDelDiv -= 8;
                } while (texto.width > anchoTexto && anchoDelDiv > 60);
                anchosDeTexto.put(p, anchoDelDiv + 8);
            } else {
                anchosDeTexto.put(p, 0);
            }
            Dimension globo = new Dimension(texto.width + 26, texto.height + 18);
            tamaniosDeGlobo.put(p, globo);
            altoGloboMaximo = Math.max(altoGloboMaximo, globo.height);
        }

        List<Personaje> personajes = ronda.getPersonajes();
        int altoGloboDeArriba = tamaniosDeGlobo.get(personajes.get(0)).height + COLA;
        int margenInferior = (int) Math.ceil(Math.max(anchoFoto, altoFoto) * RADIO_DEL_HALO - altoFoto / 2.0) + MARGEN;
        double radioY = (h - MARGEN - margenInferior - altoFoto - SEPARACION_GLOBO - altoGloboDeArriba) / (1 + Math.sin(Math.toRadians(54)));
        radioY = Math.max(40, radioY);
        double cy = MARGEN + altoGloboDeArriba + SEPARACION_GLOBO + altoFoto / 2.0 + radioY;
        double radioX = (w / 2.0 - MARGEN - anchoFoto / 2.0 - SEPARACION_GLOBO - COLA - anchoGlobo) / Math.cos(Math.toRadians(18));
        radioX = Math.max(60, Math.min(radioX, radioY * 2.4));
        double cx = w / 2.0;

        for (int i = 0; i < personajes.size(); i++) {
            Personaje p = personajes.get(i);
            double angulo = Math.toRadians(-90 + i * 360.0 / personajes.size());
            int x = (int) Math.round(cx + radioX * Math.cos(angulo) - anchoFoto / 2.0);
            int y = (int) Math.round(cy + radioY * Math.sin(angulo) - altoFoto / 2.0);
            Rectangle foto = new Rectangle(x, y, anchoFoto, altoFoto);
            fotos.put(p, foto);
            Dimension g = tamaniosDeGlobo.get(p);
            Lado lado;
            Rectangle globo;
            if (Math.abs(Math.cos(angulo)) < 0.3) {
                lado = Lado.ARRIBA;
                globo = new Rectangle((int) foto.getCenterX() - g.width / 2, foto.y - SEPARACION_GLOBO - g.height - COLA + 4, g.width, g.height);
            } else if (Math.cos(angulo) > 0) {
                lado = Lado.DERECHA;
                globo = new Rectangle(foto.x + foto.width + SEPARACION_GLOBO + COLA - 4, (int) foto.getCenterY() - g.height / 2, g.width, g.height);
            } else {
                lado = Lado.IZQUIERDA;
                globo = new Rectangle(foto.x - SEPARACION_GLOBO - COLA + 4 - g.width, (int) foto.getCenterY() - g.height / 2, g.width, g.height);
            }
            globo.x = Math.max(0, Math.min(globo.x, w - globo.width - 1));
            globo.y = Math.max(0, Math.min(globo.y, h - globo.height - 1));
            globos.put(p, globo);
            lados.put(p, lado);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        calcularUbicaciones();
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);

        Personaje seleccionado = ronda.getSeleccionado();
        List<GrafoPersonajes.Arista> aristas = new ArrayList<>(ronda.getGrafo().getAristas());
        aristas.sort(Comparator.comparing(a -> estadoDe(a, seleccionado)));
        for (GrafoPersonajes.Arista a : aristas) dibujarArista(g2, a, estadoDe(a, seleccionado));

        for (Personaje p : ronda.getPersonajes()) {
            Rectangle foto = fotos.get(p);
            if (p == seleccionado || p == bajoElMouse) {
                double radio = Math.max(foto.width, foto.height) * RADIO_DEL_HALO;
                Color c = p.getColorRopa();
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), p == seleccionado ? 90 : 45));
                g2.fill(new Ellipse2D.Double(foto.getCenterX() - radio, foto.getCenterY() - radio, radio * 2, radio * 2));
            }
        }
        for (Personaje p : ronda.getPersonajes()) {
            Rectangle foto = fotos.get(p);
            Graphics2D gf = (Graphics2D) g2.create();
            gf.translate(foto.x, foto.y);
            Avatar.pintar(gf, p, foto.width, false, ronda.getMarca(p));
            gf.dispose();
            dibujarNombre(g2, p, foto, p == seleccionado);
            dibujarGlobo(g2, p, p == seleccionado);
        }
        g2.dispose();
    }

    private void dibujarNombre(Graphics2D g, Personaje p, Rectangle foto, boolean seleccionado) {
        g.setFont(seleccionado ? Estilo.TITULO.deriveFont(21f) : Estilo.NEGRITA.deriveFont(17f));
        FontMetrics fm = g.getFontMetrics();
        String nombre = p.getNombre();
        int anchoTexto = fm.stringWidth(nombre);
        double cx = foto.getCenterX();
        int base = foto.y + foto.height - ALTO_NOMBRE + 6 + (ALTO_NOMBRE - 6 + fm.getAscent() - fm.getDescent()) / 2;
        if (seleccionado) {
            int ancho = anchoTexto + 22;
            int alto = fm.getHeight() + 4;
            int x = (int) (cx - ancho / 2.0);
            int y = base - fm.getAscent() - 2;
            g.setColor(p.getColorRopa());
            g.fillRoundRect(x, y, ancho, alto, alto, alto);
            g.setColor(Estilo.textoSobre(p.getColorRopa()));
        } else {
            g.setColor(Estilo.TINTA);
        }
        g.drawString(nombre, (float) (cx - anchoTexto / 2.0), base);
    }

    private void dibujarGlobo(Graphics2D g, Personaje p, boolean seleccionado) {
        Rectangle globo = globos.get(p);
        Rectangle foto = fotos.get(p);
        Area forma = new Area(new RoundRectangle2D.Double(globo.x, globo.y, globo.width, globo.height, 18, 18));
        Path2D cola = new Path2D.Double();
        switch (lados.get(p)) {
            case ARRIBA -> {
                double x = foto.getCenterX();
                cola.moveTo(x - 9, globo.getMaxY() - 2);
                cola.lineTo(x, foto.y - 2);
                cola.lineTo(x + 9, globo.getMaxY() - 2);
            }
            case DERECHA -> {
                double y = globo.getCenterY();
                cola.moveTo(globo.x + 2, y - 9);
                cola.lineTo(foto.getMaxX() + 2, foto.getCenterY());
                cola.lineTo(globo.x + 2, y + 9);
            }
            case IZQUIERDA -> {
                double y = globo.getCenterY();
                cola.moveTo(globo.getMaxX() - 2, y - 9);
                cola.lineTo(foto.x - 2, foto.getCenterY());
                cola.lineTo(globo.getMaxX() - 2, y + 9);
            }
        }
        cola.closePath();
        forma.add(new Area(cola));
        g.setColor(Estilo.TARJETA);
        g.fill(forma);
        g.setColor(seleccionado ? p.getColorRopa() : Estilo.BORDE);
        g.setStroke(new BasicStroke(seleccionado ? 2.6f : 1.4f));
        g.draw(forma);

        textoDeGlobo.setText(htmlDeGlobo(p, anchosDeTexto.get(p)));
        textoDeGlobo.setForeground(Estilo.TINTA);
        pintor.paintComponent(g, textoDeGlobo, this, globo.x + 13, globo.y + 9, globo.width - 26, globo.height - 18);
    }

    private Estado estadoDe(GrafoPersonajes.Arista a, Personaje seleccionado) {
        if (seleccionado == null) return Estado.NORMAL;
        if (a.getOrigen() == seleccionado) return Estado.SALIENTE;
        if (a.getDestino() == seleccionado) return Estado.ENTRANTE;
        return Estado.APAGADA;
    }

    private void dibujarArista(Graphics2D g, GrafoPersonajes.Arista a, Estado estado) {
        Color color = Estilo.texto(a.afirmaQueDiceVerdad() ? Estilo.VERDE : Estilo.ROJO);
        float opacidad = switch (estado) {
            case APAGADA -> 0.16f;
            case NORMAL -> 0.85f;
            case ENTRANTE, SALIENTE -> 1f;
        };
        float grosor = switch (estado) {
            case SALIENTE -> 4.5f;
            case ENTRANTE -> 3f;
            default -> 2.4f;
        };
        Composite anterior = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        g.setColor(color);
        g.setStroke(estado == Estado.ENTRANTE
                ? new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, PUNTEADO, 0f)
                : new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Rectangle desde = agrandar(fotos.get(a.getOrigen()), 5);
        if (a.getOrigen() == a.getDestino()) {
            double r = 18;
            boolean haciaLaDerecha = desde.getCenterX() < getWidth() / 2.0;
            double cx = haciaLaDerecha ? desde.getMaxX() + r - 6 : desde.getMinX() - r + 6;
            double cy = desde.getMinY() + desde.height * 0.35;
            g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
            g.setComposite(anterior);
            return;
        }

        Rectangle hasta = agrandar(fotos.get(a.getDestino()), 7);
        Point2D p0 = new Point2D.Double(desde.getCenterX(), desde.getCenterY());
        Point2D p2 = new Point2D.Double(hasta.getCenterX(), hasta.getCenterY());
        double dx = p2.getX() - p0.getX();
        double dy = p2.getY() - p0.getY();
        double largo = Math.hypot(dx, dy);
        double curva = Math.min(45, largo * 0.12);
        Point2D control = new Point2D.Double((p0.getX() + p2.getX()) / 2 - dy / largo * curva, (p0.getY() + p2.getY()) / 2 + dx / largo * curva);

        double inicio = 0;
        while (inicio < 1 && desde.contains(punto(p0, control, p2, inicio))) inicio += 0.005;
        double fin = 1;
        while (fin > inicio && hasta.contains(punto(p0, control, p2, fin))) fin -= 0.005;

        Path2D camino = new Path2D.Double();
        Point2D primero = punto(p0, control, p2, inicio);
        camino.moveTo(primero.getX(), primero.getY());
        for (double t = inicio; t <= fin; t += 0.01) {
            Point2D p = punto(p0, control, p2, t);
            camino.lineTo(p.getX(), p.getY());
        }
        Point2D punta = punto(p0, control, p2, fin);
        camino.lineTo(punta.getX(), punta.getY());
        g.draw(camino);
        g.setStroke(new BasicStroke(1f));
        dibujarPunta(g, punto(p0, control, p2, fin - 0.03), punta, 8 + grosor * 1.6);
        g.setComposite(anterior);

    }

    private static Rectangle agrandar(Rectangle r, int margen) {
        return new Rectangle(r.x - margen, r.y - margen, r.width + 2 * margen, r.height + 2 * margen);
    }

    private static Point2D punto(Point2D p0, Point2D control, Point2D p2, double t) {
        double u = 1 - t;
        return new Point2D.Double(
                u * u * p0.getX() + 2 * u * t * control.getX() + t * t * p2.getX(),
                u * u * p0.getY() + 2 * u * t * control.getY() + t * t * p2.getY());
    }

    private static void dibujarPunta(Graphics2D g, Point2D desde, Point2D punta, double largo) {
        double angulo = Math.atan2(punta.getY() - desde.getY(), punta.getX() - desde.getX());
        double abertura = Math.toRadians(26);
        Path2D triangulo = new Path2D.Double();
        triangulo.moveTo(punta.getX(), punta.getY());
        triangulo.lineTo(punta.getX() - largo * Math.cos(angulo - abertura), punta.getY() - largo * Math.sin(angulo - abertura));
        triangulo.lineTo(punta.getX() - largo * Math.cos(angulo + abertura), punta.getY() - largo * Math.sin(angulo + abertura));
        triangulo.closePath();
        g.fill(triangulo);
    }
}
