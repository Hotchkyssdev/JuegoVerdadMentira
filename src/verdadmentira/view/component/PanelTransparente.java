package verdadmentira.view.component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class PanelTransparente extends JPanel {
    private float opacidad = 1f;

    public PanelTransparente(JComponent contenido) {
        super(new BorderLayout());
        setOpaque(false);
        add(contenido, BorderLayout.CENTER);
    }

    public void setOpacidad(double opacidad) {
        this.opacidad = (float) Math.max(0, Math.min(1, opacidad));
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (opacidad <= 0f) return;
        if (opacidad >= 1f) {
            super.paint(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
        super.paint(g2);
        g2.dispose();
    }
}
