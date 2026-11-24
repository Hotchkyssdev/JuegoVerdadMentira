package verdadmentira.view.component;

import verdadmentira.domain.ArbolDecisiones;
import verdadmentira.domain.Personaje;
import verdadmentira.service.Juego;
import verdadmentira.view.style.Estilo;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class PanelArbol extends JPanel {
    private static final double[] SEPARACION = {0.19, 0.12, 0.09};
    private static final float[] PUNTEADO = {4f, 4f};
    private static final int ANCHO_DE_FOTO_COMPACTA = 24;
    private static final int ANCHO_DE_FOTO_GRANDE = 70;
    private static final int MARGEN_DE_FOTO_GRANDE = 8;

    private final Personaje personaje;
    private final ArbolDecisiones arbol;
    private final boolean compacto;
    private final int radio;
    private final int primerNivel;
    private final int entreNiveles;
    private Boolean pendiente;
    private boolean fotoVisible;

    private PanelArbol(Personaje personaje, ArbolDecisiones arbol, boolean compacto) {
        this.personaje = personaje;
        this.arbol = arbol;
        this.compacto = compacto;
        radio = compacto ? 8 : 13;
        primerNivel = compacto ? 16 : MARGEN_DE_FOTO_GRANDE + Avatar.alto(ANCHO_DE_FOTO_GRANDE, true) - radio;
        entreNiveles = compacto ? 24 : 58;
        setOpaque(false);
        setPreferredSize(compacto ? new Dimension(140, 100) : new Dimension(200, 340));
        setMinimumSize(getPreferredSize());
    }

    public static PanelArbol grande(Personaje personaje, ArbolDecisiones arbol) {
        return new PanelArbol(personaje, arbol, false);
    }

    public static PanelArbol compacto(Personaje personaje, ArbolDecisiones arbol) {
        return new PanelArbol(personaje, arbol, true);
    }

    public Point getCentroDeRaiz() {
        return new Point(getWidth() / 2, primerNivel);
    }

    public void setFotoVisible(boolean visible) {
        fotoVisible = visible;
        repaint();
    }

    public void setPendiente(Boolean pendiente) {
        this.pendiente = pendiente;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int w = getWidth();
        List<Boolean> camino = arbol.getCamino();
        boolean conPendiente = compacto && camino.size() < Juego.RONDAS;
        int niveles = camino.size() + (conPendiente ? 1 : 0);

        double x = w / 2.0;
        int y = primerNivel;
        for (int nivel = 0; nivel < niveles; nivel++) {
            double separacion = Math.max(radio * 1.6, w * SEPARACION[Math.min(nivel, SEPARACION.length - 1)]);
            int yHijo = y + entreNiveles;
            boolean esPendiente = nivel == camino.size();
            Boolean eleccion = esPendiente ? pendiente : camino.get(nivel);
            if (eleccion == null) {
                dibujarRamaFantasma(g2, x, y, x - separacion, yHijo, false);
                dibujarRamaFantasma(g2, x, y, x + separacion, yHijo, true);
                break;
            }
            double xElegido = eleccion ? x + separacion : x - separacion;
            double xOtro = eleccion ? x - separacion : x + separacion;
            dibujarRamaFantasma(g2, x, y, xOtro, yHijo, !eleccion);

            Color color = Estilo.texto(Estilo.colorDeMarca(eleccion));
            g2.setColor(color);
            g2.setStroke(esPendiente ? punteado(2f) : new BasicStroke(compacto ? 2.2f : 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) x, y + radio, (int) xElegido, yHijo - radio);
            dibujarNodo(g2, xElegido, yHijo, eleccion, esPendiente ? Estado.PENDIENTE : Estado.ELEGIDO);

            x = xElegido;
            y = yHijo;
        }

        dibujarRaiz(g2, w / 2.0, primerNivel, fotoVisible || !camino.isEmpty());
        if (!compacto) dibujarResumen(g2, w, camino);
        g2.dispose();
    }

    private enum Estado { ELEGIDO, PENDIENTE, FANTASMA }

    private void dibujarRamaFantasma(Graphics2D g, double x, int y, double xHijo, int yHijo, boolean veraz) {
        g.setColor(Estilo.GRIS);
        g.setStroke(punteado(1.3f));
        g.drawLine((int) x, y + radio, (int) xHijo, yHijo - radio);
        dibujarNodo(g, xHijo, yHijo, veraz, Estado.FANTASMA);
    }

    private static Stroke punteado(float grosor) {
        return new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, PUNTEADO, 0f);
    }

    private void dibujarRaiz(Graphics2D g, double x, int y, boolean conFoto) {
        Color color = personaje.getColor();
        if (!compacto) {
            Graphics2D gf = (Graphics2D) g.create();
            gf.translate(x - ANCHO_DE_FOTO_GRANDE / 2.0, MARGEN_DE_FOTO_GRANDE);
            Avatar.pintar(gf, personaje, ANCHO_DE_FOTO_GRANDE, true, null);
            gf.dispose();
            return;
        }
        if (conFoto) {
            Graphics2D gf = (Graphics2D) g.create();
            gf.translate(x - ANCHO_DE_FOTO_COMPACTA / 2.0, y - Avatar.alto(ANCHO_DE_FOTO_COMPACTA, false) / 2.0);
            Avatar.pintar(gf, personaje, ANCHO_DE_FOTO_COMPACTA, false, null);
            gf.dispose();
            return;
        }
        if (compacto) {
            Ellipse2D circulo = new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2);
            g.setColor(color);
            g.fill(circulo);
            g.setFont(Estilo.MINI_NEGRITA);
            g.setColor(Estilo.textoSobre(color));
            FontMetrics fm = g.getFontMetrics();
            String inicial = personaje.getNombre().substring(0, 1);
            g.drawString(inicial, (float) (x - fm.stringWidth(inicial) / 2.0), y + fm.getAscent() / 2f - 1);
            return;
        }
        g.setFont(Estilo.NOMBRE);
        FontMetrics fm = g.getFontMetrics();
        String nombre = personaje.getNombre();
        int ancho = fm.stringWidth(nombre) + 22;
        int alto = 26;
        g.setColor(color);
        g.fillRoundRect((int) (x - ancho / 2.0), y - alto / 2, ancho, alto, alto, alto);
        g.setColor(Estilo.textoSobre(color));
        g.drawString(nombre, (float) (x - fm.stringWidth(nombre) / 2.0), y + fm.getAscent() / 2f - 2);
    }

    private void dibujarNodo(Graphics2D g, double x, int y, boolean veraz, Estado estado) {
        Ellipse2D circulo = new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2);
        Color color = Estilo.colorDeMarca(veraz);
        Color relleno;
        Color borde;
        Color letra;
        switch (estado) {
            case ELEGIDO -> {
                relleno = color;
                borde = color;
                letra = Color.WHITE;
            }
            case PENDIENTE -> {
                relleno = Estilo.mezclar(Estilo.TARJETA, color, 0.25);
                borde = Estilo.texto(color);
                letra = Estilo.texto(color);
            }
            default -> {
                relleno = Estilo.TARJETA;
                borde = Estilo.GRIS;
                letra = Estilo.GRIS;
            }
        }
        g.setColor(relleno);
        g.fill(circulo);
        g.setColor(borde);
        g.setStroke(estado == Estado.PENDIENTE ? punteado(1.6f) : new BasicStroke(estado == Estado.ELEGIDO ? 2f : 1.3f));
        g.draw(circulo);
        g.setFont(compacto ? Estilo.MINI_NEGRITA : Estilo.CHICA_NEGRITA);
        g.setColor(letra);
        FontMetrics fm = g.getFontMetrics();
        String inicial = veraz ? "V" : "M";
        g.drawString(inicial, (float) (x - fm.stringWidth(inicial) / 2.0), y + fm.getAscent() / 2f - 1);
    }

    private void dibujarResumen(Graphics2D g, int w, List<Boolean> camino) {
        g.setFont(Estilo.CHICA);
        int yTexto = primerNivel + entreNiveles * Juego.RONDAS + radio + 22;
        int ronda = 1;
        for (Boolean eleccion : camino) {
            String linea = "Ronda " + ronda + ": " + (eleccion ? "veraz" : "mentiroso");
            g.setColor(Estilo.texto(Estilo.colorDeMarca(eleccion)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(linea, (w - fm.stringWidth(linea)) / 2, yTexto);
            yTexto += 16;
            ronda++;
        }
    }
}
