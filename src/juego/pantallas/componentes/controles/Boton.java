package juego.pantallas.componentes.controles;

import juego.pantallas.componentes.estilo.Estilo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Boton {
    private static final int ARCO = 16;
    private static final int RELLENO_HORIZONTAL = 18;
    private static final int ALTO = 38;

    private final Rectangle zona;
    private String texto;
    private final Color color;
    private final boolean relleno;
    private final Runnable accion;
    private boolean habilitado = true;
    private boolean seleccionado;
    private Font fuente = Estilo.NEGRITA;

    public Boton(String texto, Color color, boolean relleno, Runnable accion) {
        if (texto == null || accion == null) throw new IllegalArgumentException("Un botón necesita texto y acción");
        this.texto = texto;
        this.color = color;
        this.relleno = relleno;
        this.accion = accion;
        this.zona = new Rectangle(0, 0, 0, ALTO);
    }

    public static int anchoPara(Graphics2D g, String texto) {
        return g.getFontMetrics(Estilo.NEGRITA).stringWidth(texto) + 2 * RELLENO_HORIZONTAL;
    }

    public static int alto() {
        return ALTO;
    }

    public Boton ubicar(int x, int y, int ancho, int alto) {
        zona.setBounds(x, y, ancho, alto);
        return this;
    }

    public int anchoNatural(Graphics2D g) {
        return g.getFontMetrics(fuente).stringWidth(texto) + 2 * RELLENO_HORIZONTAL;
    }

    public Rectangle getZona() {
        return zona;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public boolean estaHabilitado() {
        return habilitado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    public void setFuente(Font fuente) {
        this.fuente = fuente;
    }

    public boolean atenderClick(int x, int y) {
        if (!habilitado || zona.width == 0 || !zona.contains(x, y)) return false;
        accion.run();
        return true;
    }

    public void dibujar(Graphics2D g, int mouseX, int mouseY) {
        if (zona.width == 0) return;
        boolean encima = habilitado && zona.contains(mouseX, mouseY);
        boolean lleno = relleno || seleccionado;
        Color fondo;
        Color colorDelTexto;
        Color borde;
        if (!habilitado) {
            fondo = Estilo.mezclar(Estilo.TARJETA, Estilo.GRIS, 0.25);
            colorDelTexto = Estilo.GRIS;
            borde = fondo;
        } else if (lleno) {
            fondo = encima ? Estilo.mezclar(color, Estilo.FONDO, 0.15) : color;
            colorDelTexto = Estilo.textoSobre(fondo);
            borde = fondo;
        } else {
            Color visible = Estilo.texto(color);
            fondo = encima ? Estilo.mezclar(Estilo.TARJETA, visible, 0.14) : Estilo.TARJETA;
            colorDelTexto = visible;
            borde = visible;
        }
        g.setColor(fondo);
        g.fillRoundRect(zona.x + 1, zona.y + 1, zona.width - 2, zona.height - 2, ARCO, ARCO);
        g.setColor(borde);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(zona.x + 1, zona.y + 1, zona.width - 3, zona.height - 3, ARCO, ARCO);
        g.setFont(fuente);
        g.setColor(colorDelTexto);
        FontMetrics medidas = g.getFontMetrics();
        int x = zona.x + (zona.width - medidas.stringWidth(texto)) / 2;
        int y = zona.y + (zona.height - medidas.getHeight()) / 2 + medidas.getAscent();
        g.drawString(texto, x, y);
    }
}
