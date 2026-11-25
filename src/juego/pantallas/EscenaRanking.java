package juego.pantallas;

import arboles.ArbolBinarioDeBusqueda;
import arboles.NodoBinario;
import estructuras.Diccionario;
import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.dibujo.DibujoDeArbol;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Personaje;
import juego.reglas.registro.Puntaje;
import juego.reglas.registro.Ranking;
import persistencia.archivos.ArchivoDeDatos;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class EscenaRanking extends EscenaBase {
    private static final Rectangle TABLA = new Rectangle(24, 96, 360, 400);
    private static final Rectangle ABB = new Rectangle(396, 96, 780, 400);
    private static final Rectangle ULTIMA = new Rectangle(24, 508, 820, 140);
    private static final Rectangle ARCHIVOS = new Rectangle(856, 508, 320, 140);
    private static final int RADIO = 17;
    private static final int ALTO_DEL_DIBUJO = 190;

    private static final class Contador {
        private int valor;
    }

    private final Ranking ranking;
    private final ListaDinamica<Personaje> elenco = Elenco.nuevo();

    public EscenaRanking(Sesion sesion) {
        super(sesion);
        this.ranking = sesion.getAlmacen().getRanking();
    }

    @Override
    public void alEntrar() {
        quitarBotones();
        botonesDeCabecera(null);
        agregarBoton(new Boton("Volver al inicio", Estilo.PRINCIPAL, true, () -> irA(new EscenaMenu(sesion)))
                .ubicar(ancho() - MARGEN - 170, alto() - MARGEN - Boton.alto() + 6, 170, Boton.alto()));
    }

    @Override
    protected void actualizarEscena(double dt) {
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        encabezado(g, "Ranking", "Los " + Ranking.MAXIMO + " mejores puntajes viven en un ABB: se listan con su in-orden iterativo "
                + "y se guardan en pre-orden en datos/ranking.txt.", Estilo.TINTA, 960);
        dibujarTabla(g);
        dibujarArbol(g);
        dibujarUltimaPartida(g);
        dibujarArchivos(g);
    }

    private void dibujarTabla(Graphics2D g) {
        Tarjeta.dibujar(g, TABLA);
        Tarjeta.titulo(g, "Mejores puntajes", TABLA.x + 16, TABLA.y + 24);
        ListaDinamica<Puntaje> mejores = ranking.mejores();
        if (mejores.estaVacia()) {
            TextoRico.dibujar(g, "Todavía no hay partidas terminadas.", Estilo.TEXTO, Estilo.TINTA_SUAVE, TABLA.x + 16, TABLA.y + 44, TABLA.width - 32);
            return;
        }
        int y = TABLA.y + 56;
        for (int i = 0; i < mejores.tamanio(); i++) {
            Puntaje p = mejores.obtener(i);
            g.setFont(i == 0 ? Estilo.NEGRITA : Estilo.TEXTO);
            g.setColor(i == 0 ? Estilo.AMARILLO : Estilo.TINTA);
            g.drawString((i + 1) + ".", TABLA.x + 16, y);
            g.drawString(p.getJugador(), TABLA.x + 46, y);
            g.setColor(Estilo.TINTA_SUAVE);
            g.drawString("partida " + p.getPartida(), TABLA.x + 210, y);
            g.setColor(i == 0 ? Estilo.AMARILLO : Estilo.TINTA);
            String puntos = String.valueOf(p.getPuntos());
            g.drawString(puntos, TABLA.x + TABLA.width - 16 - g.getFontMetrics().stringWidth(puntos), y);
            y += 33;
        }
    }

    private void dibujarArbol(Graphics2D g) {
        Tarjeta.dibujar(g, ABB);
        ArbolBinarioDeBusqueda<Puntaje> arbol = ranking.getArbol();
        Tarjeta.titulo(g, "El ABB del ranking · altura " + arbol.altura() + " · " + arbol.cantidadDeNodos() + " nodos · "
                + arbol.cantidadDeHojas() + " hojas", ABB.x + 16, ABB.y + 24);
        if (!arbol.estaVacio()) {
            Diccionario<Puntaje, Point2D> posiciones = new Diccionario<>();
            double pasoX = (ABB.width - 60.0) / arbol.cantidadDeNodos();
            double pasoY = arbol.altura() <= 1 ? 0 : (ALTO_DEL_DIBUJO - 2.0 * RADIO) / (arbol.altura() - 1);
            ubicar(arbol.getRaiz(), 0, new Contador(), pasoX, pasoY, posiciones);
            dibujarAristas(g, arbol.getRaiz(), posiciones);
            dibujarNodos(g, arbol.getRaiz(), posiciones, true);
        }
        String recorridos = "*Pre-orden* (así se guarda): " + puntos(arbol.preOrden())
                + "\n*In-orden* (de mejor a peor): " + puntos(arbol.inOrden())
                + "\n*Post-orden*: " + puntos(arbol.postOrden())
                + "\n*Por niveles*: " + puntos(arbol.porNiveles());
        TextoRico.dibujar(g, recorridos, Estilo.CHICA, Estilo.TINTA, ABB.x + 16, ABB.y + 280, ABB.width - 32);
    }

    private void ubicar(NodoBinario<Puntaje> nodo, int nivel, Contador orden, double pasoX, double pasoY,
                        Diccionario<Puntaje, Point2D> posiciones) {
        if (nodo == null) return;
        ubicar(nodo.getIzquierdo(), nivel + 1, orden, pasoX, pasoY, posiciones);
        posiciones.poner(nodo.getValor(), new Point2D.Double(ABB.x + 30 + (orden.valor + 0.5) * pasoX, ABB.y + 58 + nivel * pasoY));
        orden.valor++;
        ubicar(nodo.getDerecho(), nivel + 1, orden, pasoX, pasoY, posiciones);
    }

    private void dibujarAristas(Graphics2D g, NodoBinario<Puntaje> nodo, Diccionario<Puntaje, Point2D> posiciones) {
        if (nodo == null) return;
        Point2D desde = posiciones.obtener(nodo.getValor());
        g.setColor(Estilo.GRIS);
        g.setStroke(new BasicStroke(1.6f));
        if (nodo.getIzquierdo() != null) g.draw(new Line2D.Double(desde, posiciones.obtener(nodo.getIzquierdo().getValor())));
        if (nodo.getDerecho() != null) g.draw(new Line2D.Double(desde, posiciones.obtener(nodo.getDerecho().getValor())));
        dibujarAristas(g, nodo.getIzquierdo(), posiciones);
        dibujarAristas(g, nodo.getDerecho(), posiciones);
    }

    private void dibujarNodos(Graphics2D g, NodoBinario<Puntaje> nodo, Diccionario<Puntaje, Point2D> posiciones, boolean esRaiz) {
        if (nodo == null) return;
        Point2D centro = posiciones.obtener(nodo.getValor());
        double x = centro.getX();
        double y = centro.getY();
        Ellipse2D circulo = new Ellipse2D.Double(x - RADIO, y - RADIO, RADIO * 2, RADIO * 2);
        g.setColor(esRaiz ? Estilo.AMARILLO_CLARO : Estilo.ENCABEZADO);
        g.fill(circulo);
        g.setColor(esRaiz ? Estilo.AMARILLO : Estilo.TINTA_SUAVE);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(circulo);
        g.setFont(Estilo.CHICA_NEGRITA);
        g.setColor(Estilo.TINTA);
        FontMetrics medidas = g.getFontMetrics();
        String puntos = String.valueOf(nodo.getValor().getPuntos());
        g.drawString(puntos, (float) (x - medidas.stringWidth(puntos) / 2.0), (float) (y + medidas.getAscent() / 2.0 - 1));
        g.setFont(Estilo.MINI_NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        String jugador = nodo.getValor().getJugador();
        g.drawString(jugador, (float) (x - g.getFontMetrics().stringWidth(jugador) / 2.0), (float) (y + RADIO + 12));
        dibujarNodos(g, nodo.getIzquierdo(), posiciones, false);
        dibujarNodos(g, nodo.getDerecho(), posiciones, false);
    }

    private static String puntos(ListaDinamica<Puntaje> lista) {
        if (lista.estaVacia()) return "vacío";
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < lista.tamanio(); i++) {
            if (i > 0) texto.append(", ");
            texto.append(lista.obtener(i).getPuntos());
        }
        return texto.toString();
    }

    private void dibujarUltimaPartida(Graphics2D g) {
        Tarjeta.dibujar(g, ULTIMA);
        Tarjeta.titulo(g, "Tu última partida (reconstruida desde datos/ultima-partida.txt)", ULTIMA.x + 16, ULTIMA.y + 22);
        ListaDinamica<ArbolDecisiones> arboles = sesion.getAlmacen().getUltimaPartida();
        if (arboles.estaVacia()) {
            TextoRico.dibujar(g, "Todavía no terminaste ninguna partida.", Estilo.TEXTO, Estilo.TINTA_SUAVE, ULTIMA.x + 16, ULTIMA.y + 40, ULTIMA.width - 32);
            return;
        }
        int ancho = (ULTIMA.width - 32) / Math.max(5, arboles.tamanio());
        for (int i = 0; i < arboles.tamanio(); i++) {
            ArbolDecisiones arbol = arboles.obtener(i);
            Personaje p = elenco.buscar(e -> e.getNombre().equals(arbol.getNombre()));
            if (p == null) continue;
            Rectangle zona = new Rectangle(ULTIMA.x + 16 + i * ancho, ULTIMA.y + 30, ancho, ULTIMA.height - 36);
            DibujoDeArbol.dibujar(g, zona, p, arbol, true, null, true);
        }
    }

    private void dibujarArchivos(Graphics2D g) {
        Tarjeta.dibujar(g, ARCHIVOS);
        Tarjeta.titulo(g, "Archivos en datos/", ARCHIVOS.x + 16, ARCHIVOS.y + 22);
        ListaDinamica<ArchivoDeDatos<?>> archivos = sesion.getAlmacen().getArchivos();
        int y = ARCHIVOS.y + 44;
        for (int i = 0; i < archivos.tamanio(); i++) {
            ArchivoDeDatos<?> archivo = archivos.obtener(i);
            boolean existe = archivo.existe();
            g.setFont(Estilo.CHICA_NEGRITA);
            g.setColor(existe ? Estilo.texto(Estilo.VERDE) : Estilo.texto(Estilo.ROJO));
            g.drawString(existe ? "✓" : "✗", ARCHIVOS.x + 16, y);
            g.setColor(Estilo.TINTA);
            g.setFont(Estilo.CHICA);
            g.drawString(archivo.getNombre(), ARCHIVOS.x + 34, y);
            y += 19;
        }
    }
}
