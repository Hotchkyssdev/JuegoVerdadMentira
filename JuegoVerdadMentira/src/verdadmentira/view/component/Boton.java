package verdadmentira.view.component;

import verdadmentira.view.style.Estilo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Boton extends JButton {
    private final Color color;
    private final boolean relleno;
    private boolean seleccionado;

    public Boton(String texto, Color color, boolean relleno) {
        super(texto);
        this.color = color;
        this.relleno = relleno;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setFont(Estilo.NEGRITA);
        setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 36, fm.getHeight() + 18);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        boolean lleno = relleno || seleccionado;
        Color fondo;
        Color texto;
        Color borde;
        if (!isEnabled()) {
            fondo = Estilo.mezclar(Estilo.TARJETA, Estilo.GRIS, 0.25);
            texto = Estilo.GRIS;
            borde = fondo;
        } else if (lleno) {
            fondo = getModel().isRollover() ? Estilo.mezclar(color, Estilo.FONDO, 0.15) : color;
            texto = Estilo.textoSobre(fondo);
            borde = fondo;
        } else {
            Color visible = Estilo.texto(color);
            fondo = getModel().isRollover() ? Estilo.mezclar(Estilo.TARJETA, visible, 0.14) : Estilo.TARJETA;
            texto = visible;
            borde = visible;
        }
        if (isEnabled() && getModel().isPressed()) fondo = Estilo.mezclar(fondo, Color.BLACK, 0.15);
        int arco = 16;
        g2.setColor(fondo);
        g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arco, arco);
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arco, arco);
        g2.setFont(getFont());
        g2.setColor(texto);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
