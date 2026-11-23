package verdadmentira.view.component;

import verdadmentira.domain.ArbolDecisiones;

import javax.swing.*;
import java.awt.*;

public class PanelArbol extends JPanel {
    private ArbolDecisiones.Nodo raiz;

    public PanelArbol() { 
        setPreferredSize(new Dimension(650, 300)); 
        setBackground(Color.WHITE); 
    }

    public void setRaiz(ArbolDecisiones.Nodo raiz) { 
        this.raiz = raiz; 
        repaint(); 
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (raiz == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        dibujar(g2, raiz, getWidth()/2, 35, Math.max(85, getWidth()/4));
        g2.dispose();
    }

    private void dibujar(Graphics2D g, ArbolDecisiones.Nodo n, int x, int y, int sep) {
        if (n == null) return;
        int r = 25;
        if (n.getIzquierdo() != null) {
            int nx=x-sep, ny=y+75; g.setColor(new Color(190,55,55));
            g.drawLine(x,y+r,nx,ny-r); g.drawString("Mentiroso", (x+nx)/2-28,(y+ny)/2);
            dibujar(g,n.getIzquierdo(),nx,ny,Math.max(35,sep/2));
        }

        if (n.getDerecho() != null) {
            int nx=x+sep, ny=y+75; g.setColor(new Color(35,145,75));
            g.drawLine(x,y+r,nx,ny-r); g.drawString("Veraz",(x+nx)/2-15,(y+ny)/2);
            dibujar(g,n.getDerecho(),nx,ny,Math.max(35,sep/2));
        }

        g.setColor(n.isEligioVeraz()?new Color(35,145,75):new Color(190,55,55));
        g.fillOval(x-r,y-r,r*2,r*2); g.setColor(Color.WHITE);
        FontMetrics fm=g.getFontMetrics(); g.drawString(n.getNombre(),x-fm.stringWidth(n.getNombre())/2,y+4);
    }
}