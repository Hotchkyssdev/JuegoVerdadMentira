package juego.pantallas.componentes.controles;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Superposicion {
    private static final int MARGEN = 24;

    private final String titulo;
    private final String marcado;
    private final int ancho;
    private final ListaDinamica<Boton> botones = new ListaDinamica<>();
    private boolean abierta = true;

    public Superposicion(String titulo, String marcado, int ancho) {
        this.titulo = titulo;
        this.marcado = marcado;
        this.ancho = ancho;
    }

    public Superposicion conBoton(String texto, Color color, boolean relleno, Runnable accion) {
        botones.agregar(new Boton(texto, color, relleno, () -> {
            abierta = false;
            accion.run();
        }));
        return this;
    }

    public boolean estaAbierta() {
        return abierta;
    }

    public void cerrar() {
        abierta = false;
    }

    public void atenderClick(int x, int y) {
        for (int i = 0; i < botones.tamanio(); i++) {
            if (botones.obtener(i).atenderClick(x, y)) return;
        }
    }

    public void dibujar(Graphics2D g, int anchoDePantalla, int altoDePantalla, int mouseX, int mouseY) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, anchoDePantalla, altoDePantalla);
        int anchoDeTexto = ancho - 2 * MARGEN;
        int altoDeTexto = TextoRico.alto(g, marcado, Estilo.TEXTO, anchoDeTexto);
        int alto = MARGEN + 34 + altoDeTexto + 20 + Boton.alto() + MARGEN;
        Rectangle zona = new Rectangle((anchoDePantalla - ancho) / 2, Math.max(10, (altoDePantalla - alto) / 2), ancho, alto);
        Tarjeta.dibujar(g, zona);
        g.setFont(Estilo.TITULO.deriveFont(22f));
        g.setColor(Estilo.TINTA);
        g.drawString(titulo, zona.x + MARGEN, zona.y + MARGEN + 20);
        TextoRico.dibujar(g, marcado, Estilo.TEXTO, Estilo.TINTA, zona.x + MARGEN, zona.y + MARGEN + 34, anchoDeTexto);
        int x = zona.x + zona.width - MARGEN;
        int y = zona.y + zona.height - MARGEN - Boton.alto();
        for (int i = botones.tamanio() - 1; i >= 0; i--) {
            Boton b = botones.obtener(i);
            int anchoBoton = b.anchoNatural(g);
            x -= anchoBoton;
            b.ubicar(x, y, anchoBoton, Boton.alto());
            b.dibujar(g, mouseX, mouseY);
            x -= 10;
        }
    }
}
