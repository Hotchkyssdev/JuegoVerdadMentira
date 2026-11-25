package juego.pantallas.componentes.estilo;

import estructuras.ListaDinamica;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public final class TextoRico {
    private static final double INTERLINEADO = 1.28;

    private static final class Pieza {
        private final String texto;
        private final Font fuente;
        private final Color color;
        private final boolean salto;

        private Pieza(String texto, Font fuente, Color color, boolean salto) {
            this.texto = texto;
            this.fuente = fuente;
            this.color = color;
            this.salto = salto;
        }
    }

    private TextoRico() {
    }

    public static int alto(Graphics2D g, String marcado, Font base, int ancho) {
        return recorrer(g, marcado, base, Estilo.TINTA, 0, 0, ancho, false);
    }

    public static int dibujar(Graphics2D g, String marcado, Font base, Color color, int x, int y, int ancho) {
        return recorrer(g, marcado, base, color, x, y, ancho, true);
    }

    public static int anchoNatural(Graphics2D g, String marcado, Font base) {
        ListaDinamica<Pieza> piezas = piezas(marcado, base, Estilo.TINTA);
        int ancho = 0;
        int linea = 0;
        for (int i = 0; i < piezas.tamanio(); i++) {
            Pieza p = piezas.obtener(i);
            if (p.salto) {
                ancho = Math.max(ancho, linea);
                linea = 0;
            } else {
                linea += g.getFontMetrics(p.fuente).stringWidth(p.texto);
            }
        }
        return Math.max(ancho, linea);
    }

    private static int recorrer(Graphics2D g, String marcado, Font base, Color color, int x, int y, int ancho, boolean dibujar) {
        ListaDinamica<Pieza> piezas = piezas(marcado, base, color);
        int altoDeLinea = (int) Math.ceil(g.getFontMetrics(base).getHeight() * INTERLINEADO);
        int ascenso = g.getFontMetrics(base).getAscent();
        int cursorX = 0;
        int cursorY = 0;
        for (int i = 0; i < piezas.tamanio(); i++) {
            Pieza p = piezas.obtener(i);
            if (p.salto) {
                cursorX = 0;
                cursorY += altoDeLinea;
                continue;
            }
            FontMetrics medidas = g.getFontMetrics(p.fuente);
            boolean esEspacio = p.texto.trim().isEmpty();
            int anchoPieza = medidas.stringWidth(p.texto);
            if (cursorX + anchoPieza > ancho && cursorX > 0) {
                cursorX = 0;
                cursorY += altoDeLinea;
                if (esEspacio) continue;
            }
            if (dibujar && !esEspacio) {
                g.setFont(p.fuente);
                g.setColor(p.color);
                g.drawString(p.texto, x + cursorX, y + cursorY + ascenso);
            }
            cursorX += anchoPieza;
        }
        return piezas.estaVacia() ? 0 : cursorY + altoDeLinea;
    }

    private static ListaDinamica<Pieza> piezas(String marcado, Font base, Color color) {
        ListaDinamica<Pieza> piezas = new ListaDinamica<>();
        Font negrita = base.deriveFont(Font.BOLD);
        Font deNombre = Estilo.NOMBRE.deriveFont(base.getSize2D() + 2f);
        boolean enNegrita = false;
        StringBuilder palabra = new StringBuilder();
        String nombre = null;
        for (int i = 0; i < marcado.length(); i++) {
            char c = marcado.charAt(i);
            if (nombre != null) {
                if (c == '}') {
                    piezas.agregar(new Pieza(nombre, deNombre, Estilo.colorDeNombre(nombre), false));
                    nombre = null;
                } else {
                    nombre += c;
                }
                continue;
            }
            if (c == '{' || c == '*' || c == ' ' || c == '\n') {
                cerrarPalabra(piezas, palabra, enNegrita ? negrita : base, color);
                if (c == '{') nombre = "";
                else if (c == '*') enNegrita = !enNegrita;
                else if (c == ' ') piezas.agregar(new Pieza(" ", base, color, false));
                else piezas.agregar(new Pieza("", base, color, true));
                continue;
            }
            palabra.append(c);
        }
        cerrarPalabra(piezas, palabra, enNegrita ? negrita : base, color);
        return piezas;
    }

    private static void cerrarPalabra(ListaDinamica<Pieza> piezas, StringBuilder palabra, Font fuente, Color color) {
        if (palabra.length() == 0) return;
        piezas.agregar(new Pieza(palabra.toString(), fuente, color, false));
        palabra.setLength(0);
    }
}
