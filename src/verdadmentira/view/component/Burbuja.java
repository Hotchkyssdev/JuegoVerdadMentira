package verdadmentira.view.component;

import verdadmentira.domain.Afirmacion;
import verdadmentira.domain.Personaje;
import verdadmentira.view.style.Estilo;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class Burbuja extends JPanel {
    private static final int COLA = 12;
    private static final int ANCHO_TEXTO = 430;

    private final Personaje personaje;
    private final JLabel consecuencias;
    private final JLabel aviso;
    private boolean resaltada;

    public Burbuja(Afirmacion afirmacion) {
        personaje = afirmacion.getEmisor();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(8, 14, 9 + COLA, 14));

        JLabel nombre = new JLabel("<html>" + Estilo.nombreHtml(personaje) + " dice:</html>");
        nombre.setFont(Estilo.CHICA_NEGRITA);
        nombre.setForeground(Estilo.TINTA_SUAVE);
        JLabel frase = new TextoAjustable(afirmacion.getTexto(Estilo::nombreHtml), Estilo.FRASE, ANCHO_TEXTO, false);
        frase.setForeground(Estilo.TINTA);

        consecuencias = new TextoAjustable("<br>" + consecuenciasHtml(afirmacion), Estilo.CHICA, ANCHO_TEXTO, false);
        consecuencias.setForeground(Estilo.TINTA_SUAVE);
        consecuencias.setVisible(false);

        aviso = new JLabel();
        aviso.setFont(Estilo.CHICA_NEGRITA);
        aviso.setForeground(Estilo.texto(Estilo.ROJO));
        aviso.setVisible(false);

        for (JLabel etiqueta : new JLabel[]{nombre, frase, consecuencias, aviso}) etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(nombre);
        add(Box.createVerticalStrut(2));
        add(frase);
        add(consecuencias);
        add(aviso);
    }


    public static String consecuenciasHtml(Afirmacion afirmacion) {
        if (afirmacion.esHechoVisible()) {
            String quien = afirmacion.getEmisor().getNombre();
            return "Esto lo podés comprobar mirando las fotos. <b>Si es cierto, " + quien + " dice la verdad</b>; "
                    + "<b>si es falso, " + quien + " miente</b>.";
        }
        String[] consecuencias = afirmacion.getConsecuencias(Estilo::nombreHtml);
        if (consecuencias == null) {
            return "Esta frase no da pistas: un veraz y un mentiroso dirían lo mismo.";
        }
        String quien = afirmacion.getEmisor().getNombre();
        return "<b>Si " + quien + " dice la verdad:</b> "
                + consecuencias[0] + ".<br>"
                + "<b>Si " + quien + " miente:</b> "
                + consecuencias[1] + ".";
    }

    public void setResaltada(boolean resaltada) {
        this.resaltada = resaltada;
        consecuencias.setVisible(resaltada);
        revalidate();
        repaint();
    }

    public void setAviso(String texto) {
        aviso.setText(texto == null ? "" : "<html><br>" + texto + "</html>");
        aviso.setVisible(texto != null);
        revalidate();
        repaint();
    }

    public boolean tieneAviso() {
        return aviso.isVisible();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        double w = getWidth() - 3;
        double h = getHeight() - 3 - COLA;
        Area forma = new Area(new RoundRectangle2D.Double(1.5, 1.5, w, h, 20, 20));
        Path2D cola = new Path2D.Double();
        cola.moveTo(30, h - 2);
        cola.lineTo(38, h + COLA);
        cola.lineTo(54, h - 2);
        cola.closePath();
        forma.add(new Area(cola));
        g2.setColor(Estilo.TARJETA);
        g2.fill(forma);
        if (tieneAviso()) g2.setColor(Estilo.texto(Estilo.ROJO));
        else g2.setColor(resaltada ? personaje.getColor() : Estilo.BORDE);
        g2.setStroke(new BasicStroke(resaltada || tieneAviso() ? 3f : 1.5f));
        g2.draw(forma);
        g2.dispose();
    }
}
