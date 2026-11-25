package juego.pantallas.componentes.controles;

import juego.pantallas.componentes.estilo.Estilo;
import motor.Entrada;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class CampoDeTexto {
    private static final double PARPADEO = 0.55;

    private final Rectangle zona = new Rectangle();
    private final StringBuilder texto = new StringBuilder();
    private final String ayuda;
    private final int largoMaximo;
    private boolean conFoco;
    private double reloj;

    public CampoDeTexto(String ayuda, int largoMaximo) {
        if (largoMaximo <= 0) throw new IllegalArgumentException("El largo máximo tiene que ser positivo");
        this.ayuda = ayuda;
        this.largoMaximo = largoMaximo;
    }

    public void ubicar(int x, int y, int ancho, int alto) {
        zona.setBounds(x, y, ancho, alto);
    }

    public Rectangle getZona() {
        return zona;
    }

    public String getTexto() {
        return texto.toString();
    }

    public void setTexto(String nuevo) {
        texto.setLength(0);
        if (nuevo != null) texto.append(nuevo.length() > largoMaximo ? nuevo.substring(0, largoMaximo) : nuevo);
    }

    public void vaciar() {
        texto.setLength(0);
    }

    public boolean tieneFoco() {
        return conFoco;
    }

    public void setFoco(boolean foco) {
        conFoco = foco;
        reloj = 0;
    }

    public boolean atenderClick(int x, int y) {
        conFoco = zona.contains(x, y);
        reloj = 0;
        return conFoco;
    }

    public boolean actualizar(Entrada entrada, double dt) {
        reloj += dt;
        if (!conFoco) return false;
        boolean cambio = false;
        boolean mayuscula = entrada.estaPresionada(KeyEvent.VK_SHIFT);
        for (int codigo = KeyEvent.VK_A; codigo <= KeyEvent.VK_Z; codigo++) {
            if (entrada.fuePresionada(codigo)) cambio |= escribir((char) (mayuscula ? codigo : codigo + ('a' - 'A')));
        }
        for (int codigo = KeyEvent.VK_0; codigo <= KeyEvent.VK_9; codigo++) {
            if (entrada.fuePresionada(codigo)) cambio |= escribir((char) codigo);
        }
        if (entrada.fuePresionada(KeyEvent.VK_SPACE)) cambio |= escribir(' ');
        if (entrada.fuePresionada(KeyEvent.VK_COMMA)) cambio |= escribir(',');
        if (entrada.fuePresionada(KeyEvent.VK_PERIOD)) cambio |= escribir('.');
        if (entrada.fuePresionada(KeyEvent.VK_BACK_SPACE) && texto.length() > 0) {
            texto.deleteCharAt(texto.length() - 1);
            cambio = true;
        }
        if (cambio) reloj = 0;
        return cambio;
    }

    public boolean confirmo(Entrada entrada) {
        return conFoco && entrada.fuePresionada(KeyEvent.VK_ENTER);
    }

    private boolean escribir(char c) {
        if (texto.length() >= largoMaximo) return false;
        texto.append(c);
        return true;
    }

    public void dibujar(Graphics2D g) {
        g.setColor(Estilo.mezclar(Estilo.TARJETA, Estilo.FONDO, 0.4));
        g.fillRoundRect(zona.x, zona.y, zona.width, zona.height, 12, 12);
        g.setColor(conFoco ? Estilo.PRINCIPAL : Estilo.BORDE);
        g.setStroke(new BasicStroke(conFoco ? 2f : 1.4f));
        g.drawRoundRect(zona.x, zona.y, zona.width, zona.height, 12, 12);
        g.setFont(Estilo.TEXTO);
        FontMetrics medidas = g.getFontMetrics();
        int base = zona.y + (zona.height - medidas.getHeight()) / 2 + medidas.getAscent();
        int x = zona.x + 12;
        String visible = texto.toString();
        while (medidas.stringWidth(visible) > zona.width - 30 && visible.length() > 0) visible = visible.substring(1);
        if (texto.length() == 0 && !conFoco) {
            g.setColor(Estilo.GRIS);
            g.drawString(ayuda, x, base);
        } else {
            g.setColor(Estilo.TINTA);
            g.drawString(visible, x, base);
        }
        if (conFoco && (reloj % (2 * PARPADEO)) < PARPADEO) {
            int cursor = x + medidas.stringWidth(visible) + 1;
            g.drawLine(cursor, zona.y + 8, cursor, zona.y + zona.height - 8);
        }
    }
}
