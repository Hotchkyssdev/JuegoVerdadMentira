package verdadmentira.view.component;

import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

public class TextoAjustable extends JLabel {
    private static final double HOLGURA_POR_ESCALADO = 0.08;
    private static final int HOLGURA_MINIMA = 4;

    private final boolean seguirAlPadre;
    private int anchoMaximo;

    public TextoAjustable(String html, Font fuente, int anchoMaximo, boolean seguirAlPadre) {
        this.seguirAlPadre = seguirAlPadre;
        setFont(fuente);
        setVerticalAlignment(TOP);
        setHorizontalAlignment(LEADING);
        setAnchoMaximo(anchoMaximo);
        setHtml(html);
    }

    public void setHtml(String html) {
        setText("<html>" + html + "</html>");
    }

    public void setAnchoMaximo(int anchoMaximo) {
        this.anchoMaximo = anchoMaximo;
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        View vista = (View) getClientProperty(BasicHTML.propertyKey);
        if (vista == null) return super.getPreferredSize();
        int anchoDisponible = anchoDisponible();
        int anchoParaMedir = (int) Math.floor(anchoDisponible * (1 - HOLGURA_POR_ESCALADO));
        int anchoNatural = (int) Math.ceil(vista.getPreferredSpan(View.X_AXIS));
        int anchoDeTexto = Math.max(1, Math.min(anchoNatural, anchoParaMedir));
        vista.setSize(anchoDeTexto, 0);
        int alto = (int) Math.ceil(vista.getPreferredSpan(View.Y_AXIS));
        int anchoConHolgura = (int) Math.ceil(anchoDeTexto * (1 + HOLGURA_POR_ESCALADO)) + HOLGURA_MINIMA;
        return new Dimension(Math.min(anchoDisponible, anchoConHolgura), alto);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension preferido = getPreferredSize();
        return new Dimension(seguirAlPadre ? Integer.MAX_VALUE : preferido.width, preferido.height);
    }

    private int anchoDisponible() {
        Container padre = getParent();
        if (!seguirAlPadre || padre == null || padre.getWidth() <= 0) return anchoMaximo;
        Insets margenDelPadre = padre.getInsets();
        return padre.getWidth() - margenDelPadre.left - margenDelPadre.right;
    }
}
