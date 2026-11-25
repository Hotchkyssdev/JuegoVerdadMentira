package juego.pantallas.componentes.dibujo;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.estilo.Estilo;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.partida.Juego;
import juego.reglas.personajes.Personaje;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public final class DibujoDeArbol {
    private static final int ANCHO_DE_FOTO_COMPACTA = 22;
    private static final int ANCHO_DE_FOTO_GRANDE = 64;
    private static final int MARGEN_DE_FOTO_GRANDE = 6;

    private enum Estado { ELEGIDO, PENDIENTE, FANTASMA }

    private DibujoDeArbol() {
    }

    private static int radio(boolean compacto) {
        return compacto ? 8 : 13;
    }

    private static int primerNivel(boolean compacto) {
        return compacto ? 14 : MARGEN_DE_FOTO_GRANDE + Avatar.alto(ANCHO_DE_FOTO_GRANDE, true) - radio(false);
    }

    private static int entreNiveles(boolean compacto, Rectangle zona) {
        if (compacto) return Math.max(18, Math.min(24, (zona.height - 30) / Juego.RONDAS));
        return Math.max(36, Math.min(56, (zona.height - primerNivel(false) - 70) / Juego.RONDAS));
    }

    private static double separacion(int nivel) {
        if (nivel == 0) return 0.19;
        return nivel == 1 ? 0.12 : 0.09;
    }

    public static Point2D centroDeRaiz(Rectangle zona, boolean compacto) {
        return new Point2D.Double(zona.getCenterX(), zona.y + primerNivel(compacto));
    }

    public static void dibujar(Graphics2D g, Rectangle zona, Personaje personaje, ArbolDecisiones arbol,
                               boolean compacto, Boolean pendiente, boolean conFoto) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        int radio = radio(compacto);
        int paso = entreNiveles(compacto, zona);
        ListaDinamica<Boolean> camino = arbol.getCamino();
        boolean conPendiente = compacto && camino.tamanio() < Juego.RONDAS;
        int niveles = camino.tamanio() + (conPendiente ? 1 : 0);
        double x = zona.getCenterX();
        int y = zona.y + primerNivel(compacto);
        for (int nivel = 0; nivel < niveles; nivel++) {
            double abertura = Math.max(radio * 1.6, zona.width * separacion(nivel));
            int yHijo = y + paso;
            boolean esPendiente = nivel == camino.tamanio();
            Boolean eleccion = esPendiente ? pendiente : camino.obtener(nivel);
            if (eleccion == null) {
                ramaFantasma(g2, x, y, x - abertura, yHijo, false, radio, compacto);
                ramaFantasma(g2, x, y, x + abertura, yHijo, true, radio, compacto);
                break;
            }
            double xElegido = eleccion ? x + abertura : x - abertura;
            double xOtro = eleccion ? x - abertura : x + abertura;
            ramaFantasma(g2, x, y, xOtro, yHijo, !eleccion, radio, compacto);
            g2.setColor(Estilo.texto(Estilo.colorDeMarca(eleccion)));
            if (esPendiente) {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Trazos.lineaPunteada(g2, x, y + radio, xElegido, yHijo - radio, 4, 4);
            } else {
                g2.setStroke(new BasicStroke(compacto ? 2.2f : 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(x, y + radio, xElegido, yHijo - radio));
            }
            nodo(g2, xElegido, yHijo, eleccion, esPendiente ? Estado.PENDIENTE : Estado.ELEGIDO, radio, compacto);
            x = xElegido;
            y = yHijo;
        }
        raiz(g2, zona, personaje, compacto, conFoto || !camino.estaVacia());
        if (!compacto) resumen(g2, zona, camino, paso);
        g2.dispose();
    }

    private static void ramaFantasma(Graphics2D g, double x, int y, double xHijo, int yHijo, boolean veraz, int radio, boolean compacto) {
        g.setColor(Estilo.GRIS);
        g.setStroke(new BasicStroke(1.3f));
        Trazos.lineaPunteada(g, x, y + radio, xHijo, yHijo - radio, 4, 4);
        nodo(g, xHijo, yHijo, veraz, Estado.FANTASMA, radio, compacto);
    }

    private static void raiz(Graphics2D g, Rectangle zona, Personaje personaje, boolean compacto, boolean conFoto) {
        double x = zona.getCenterX();
        int y = zona.y + primerNivel(compacto);
        if (!compacto) {
            Avatar.pintar(g, personaje, x - ANCHO_DE_FOTO_GRANDE / 2.0, zona.y + MARGEN_DE_FOTO_GRANDE, ANCHO_DE_FOTO_GRANDE, true, null);
            return;
        }
        if (conFoto) {
            Avatar.pintar(g, personaje, x - ANCHO_DE_FOTO_COMPACTA / 2.0, y - Avatar.alto(ANCHO_DE_FOTO_COMPACTA, false) / 2.0,
                    ANCHO_DE_FOTO_COMPACTA, false, null);
            return;
        }
        int radio = radio(true);
        g.setColor(personaje.getColor());
        g.fill(new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2));
        g.setFont(Estilo.MINI_NEGRITA);
        g.setColor(Estilo.textoSobre(personaje.getColor()));
        FontMetrics medidas = g.getFontMetrics();
        String inicial = personaje.getNombre().substring(0, 1);
        g.drawString(inicial, (float) (x - medidas.stringWidth(inicial) / 2.0), y + medidas.getAscent() / 2f - 1);
    }

    private static void nodo(Graphics2D g, double x, int y, boolean veraz, Estado estado, int radio, boolean compacto) {
        Ellipse2D circulo = new Ellipse2D.Double(x - radio, y - radio, radio * 2, radio * 2);
        Color color = Estilo.colorDeMarca(veraz);
        Color relleno;
        Color borde;
        Color letra;
        if (estado == Estado.ELEGIDO) {
            relleno = color;
            borde = color;
            letra = Color.WHITE;
        } else if (estado == Estado.PENDIENTE) {
            relleno = Estilo.mezclar(Estilo.TARJETA, color, 0.25);
            borde = Estilo.texto(color);
            letra = Estilo.texto(color);
        } else {
            relleno = Estilo.TARJETA;
            borde = Estilo.GRIS;
            letra = Estilo.GRIS;
        }
        g.setColor(relleno);
        g.fill(circulo);
        g.setColor(borde);
        if (estado == Estado.PENDIENTE) {
            g.setStroke(new BasicStroke(1.6f));
            Trazos.circuloPunteado(g, x, y, radio, 8);
        } else {
            g.setStroke(new BasicStroke(estado == Estado.ELEGIDO ? 2f : 1.3f));
            g.draw(circulo);
        }
        g.setFont(compacto ? Estilo.MINI_NEGRITA : Estilo.CHICA_NEGRITA);
        g.setColor(letra);
        FontMetrics medidas = g.getFontMetrics();
        String inicial = veraz ? "V" : "M";
        g.drawString(inicial, (float) (x - medidas.stringWidth(inicial) / 2.0), y + medidas.getAscent() / 2f - 1);
    }

    private static void resumen(Graphics2D g, Rectangle zona, ListaDinamica<Boolean> camino, int paso) {
        g.setFont(Estilo.CHICA);
        int yTexto = zona.y + primerNivel(false) + paso * Juego.RONDAS + radio(false) + 22;
        for (int i = 0; i < camino.tamanio(); i++) {
            boolean eleccion = camino.obtener(i);
            String linea = "Ronda " + (i + 1) + ": " + (eleccion ? "veraz" : "mentiroso");
            g.setColor(Estilo.texto(Estilo.colorDeMarca(eleccion)));
            FontMetrics medidas = g.getFontMetrics();
            g.drawString(linea, (int) (zona.getCenterX() - medidas.stringWidth(linea) / 2.0), yTexto);
            yTexto += 16;
        }
    }
}
