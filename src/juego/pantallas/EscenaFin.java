package juego.pantallas;

import estructuras.ColaEnlazada;
import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.dibujo.Avatar;
import juego.pantallas.componentes.dibujo.DibujoDeArbol;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.partida.ArbolDecisiones;
import juego.reglas.partida.Juego;
import juego.reglas.partida.Resultado;
import juego.reglas.personajes.Personaje;
import juego.reglas.registro.Progreso;
import juego.reglas.registro.Puntaje;
import juego.reglas.registro.Ranking;
import motor.Temporizador;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EscenaFin extends EscenaBase {
    private static final double INTERVALO_DE_REVELACION = 0.75;
    private static final int X_TABLA = 500;
    private static final int Y_TABLA = 112;
    private static final int ALTO_DE_FILA = 56;

    private final Juego juego;
    private final Resultado resultado;
    private final ListaDinamica<Personaje> personajes;
    private final ColaEnlazada<Integer> rondasPorRevelar = new ColaEnlazada<>();
    private final Temporizador reloj = new Temporizador(INTERVALO_DE_REVELACION);
    private final ArbolDecisiones loQueHizo;
    private int rondasReveladas;
    private int posicionEnElRanking;
    private boolean guardado;

    public EscenaFin(Sesion sesion, Juego juego, Resultado resultado) {
        super(sesion);
        this.juego = juego;
        this.resultado = resultado;
        this.personajes = juego.getPersonajes();
        for (int r = 0; r < Juego.RONDAS; r++) rondasPorRevelar.encolar(r);
        Personaje ambos = resultado.getAmbos();
        loQueHizo = new ArbolDecisiones(ambos.getNombre());
        for (int r = 0; r < Juego.RONDAS; r++) loQueHizo.agregarRonda(juego.dijoLaVerdad(r, ambos));
    }

    @Override
    public void alEntrar() {
        guardarUnaVez();
        quitarBotones();
        int y = alto() - MARGEN - Boton.alto();
        int x = ancho() - MARGEN;
        agregarBoton(new Boton("Jugar de nuevo", Estilo.PRINCIPAL, true, () -> irA(new EscenaRonda(sesion, sesion.nuevoJuego(), 0)))
                .ubicar(x - 160, y, 160, Boton.alto()));
        agregarBoton(new Boton("Volver al inicio", Estilo.TINTA_SUAVE, false, () -> irA(new EscenaMenu(sesion)))
                .ubicar(x - 160 - 10 - 160, y, 160, Boton.alto()));
        agregarBoton(new Boton("Ver ranking", Estilo.TINTA_SUAVE, false, () -> irA(new EscenaRanking(sesion)))
                .ubicar(x - 160 - 10 - 160 - 10 - 130, y, 130, Boton.alto()));
    }

    private void guardarUnaVez() {
        if (guardado) return;
        guardado = true;
        Progreso progreso = sesion.getProgreso();
        Puntaje puntaje = new Puntaje(progreso.getJugador(), resultado.total(), progreso.siguienteNumeroDePartida());
        posicionEnElRanking = sesion.getAlmacen().getRanking().agregar(puntaje);
        progreso.registrarPartida(resultado.gano());
        juego.registrarRelacionesUsadas();
        sesion.getAlmacen().guardarFinDePartida(juego.getArboles());
    }

    @Override
    protected void actualizarEscena(double dt) {
        if (rondasPorRevelar.estaVacia()) return;
        if (reloj.avanzar(dt)) {
            rondasPorRevelar.desencolar();
            rondasReveladas++;
        }
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        Personaje ambos = resultado.getAmbos();
        String detalle = resultado.gano()
                ? "Lo encontraste: el que podía hacer las dos cosas era " + Estilo.nombre(ambos) + "."
                : "Elegiste a " + Estilo.nombre(resultado.getElegido()) + ", pero el que podía hacer las dos cosas era " + Estilo.nombre(ambos) + ".";
        encabezado(g, resultado.gano() ? "¡Ganaste!" : "Perdiste", detalle, Estilo.texto(resultado.gano() ? Estilo.VERDE : Estilo.ROJO), 900);

        Rectangle marcado = new Rectangle(MARGEN, Y_TABLA, 220, 290);
        Rectangle real = new Rectangle(MARGEN + 230, Y_TABLA, 220, 290);
        arbolConTitulo(g, "Lo que marcaste", marcado, juego.getArbol(ambos), ambos);
        arbolConTitulo(g, "Lo que hizo en realidad", real, loQueHizo, ambos);
        dibujarPuntaje(g, new Rectangle(MARGEN, Y_TABLA + 304, 450, 220));
        dibujarTabla(g);
    }

    private void arbolConTitulo(Graphics2D g, String titulo, Rectangle zona, ArbolDecisiones arbol, Personaje p) {
        Tarjeta.dibujar(g, zona);
        Tarjeta.titulo(g, titulo, zona.x + 12, zona.y + 20);
        DibujoDeArbol.dibujar(g, new Rectangle(zona.x, zona.y + 26, zona.width, zona.height - 30), p, arbol, false, null, true);
    }

    private void dibujarPuntaje(Graphics2D g, Rectangle zona) {
        Tarjeta.dibujar(g, zona);
        StringBuilder texto = new StringBuilder();
        for (int r = 0; r < Juego.RONDAS; r++) {
            texto.append("Ronda ").append(r + 1).append(": ").append(resultado.aciertosDeRonda(r)).append(" aciertos × ")
                    .append(Resultado.PUNTOS_POR_ACIERTO * (r + 1)).append('\n');
        }
        texto.append("Encontrarlo: +").append(resultado.bonus()).append('\n');
        texto.append("Pistas usadas: ").append(resultado.getPistasUsadas()).append(" (−").append(resultado.descuentoPorPistas()).append(")\n");
        texto.append(posicionEnElRanking > 0
                ? "Entraste al ranking en el puesto *" + posicionEnElRanking + "*."
                : "No alcanzó para entrar al ranking de los " + Ranking.MAXIMO + " mejores.");
        Tarjeta.titulo(g, "Puntaje", zona.x + 16, zona.y + 24);
        g.setFont(Estilo.TITULO.deriveFont(40f));
        g.setColor(Estilo.TINTA);
        String total = String.valueOf(resultado.total());
        g.drawString(total, zona.x + zona.width - 16 - g.getFontMetrics().stringWidth(total), zona.y + 56);
        TextoRico.dibujar(g, texto.toString(), Estilo.TEXTO, Estilo.TINTA, zona.x + 16, zona.y + 34, zona.width - 120);
    }

    private void dibujarTabla(Graphics2D g) {
        int anchoTotal = ancho() - MARGEN - X_TABLA;
        int anchoNombre = 150;
        int anchoTipo = 150;
        int anchoRonda = (anchoTotal - anchoNombre - anchoTipo) / Juego.RONDAS;
        int y = Y_TABLA;
        encabezadoDeCelda(g, "Personaje", X_TABLA, y, anchoNombre);
        encabezadoDeCelda(g, "Qué era", X_TABLA + anchoNombre, y, anchoTipo);
        for (int r = 0; r < Juego.RONDAS; r++) encabezadoDeCelda(g, "Ronda " + (r + 1), X_TABLA + anchoNombre + anchoTipo + r * anchoRonda, y, anchoRonda);
        y += 36;
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            celda(g, X_TABLA, y, anchoNombre, Estilo.TARJETA);
            Avatar.pintar(g, p, X_TABLA + 8, y + 8, 36, false, null);
            g.setFont(Estilo.NOMBRE);
            g.setColor(Estilo.TINTA);
            g.drawString(p.getNombre(), X_TABLA + 54, y + 34);
            boolean esAmbos = p.equals(resultado.getAmbos());
            celda(g, X_TABLA + anchoNombre, y, anchoTipo, esAmbos ? Estilo.AMARILLO_CLARO : Estilo.TARJETA);
            TextoRico.dibujar(g, p.descripcionReal(), Estilo.CHICA, Estilo.TINTA, X_TABLA + anchoNombre + 10, y + 10, anchoTipo - 20);
            for (int r = 0; r < Juego.RONDAS; r++) {
                int x = X_TABLA + anchoNombre + anchoTipo + r * anchoRonda;
                if (r >= rondasReveladas) {
                    celda(g, x, y, anchoRonda, Estilo.TARJETA);
                    textoCentrado(g, "?", x, y, anchoRonda, Estilo.TINTA_SUAVE);
                    continue;
                }
                boolean marcado = juego.getDecision(r, p);
                boolean realidad = juego.dijoLaVerdad(r, p);
                celda(g, x, y, anchoRonda, marcado == realidad ? Estilo.VERDE_CLARO : Estilo.ROJO_CLARO);
                TextoRico.dibujar(g, "Tu marca: *" + comoTexto(marcado) + "*\nEra: *" + comoTexto(realidad) + "*",
                        Estilo.CHICA, Estilo.TINTA, x + 10, y + 10, anchoRonda - 20);
            }
            y += ALTO_DE_FILA;
        }
        encabezadoDeCelda(g, "Aciertos", X_TABLA, y, anchoNombre + anchoTipo);
        for (int r = 0; r < Juego.RONDAS; r++) {
            String texto = r < rondasReveladas ? resultado.aciertosDeRonda(r) + " de " + personajes.tamanio() : "?";
            encabezadoDeCelda(g, texto, X_TABLA + anchoNombre + anchoTipo + r * anchoRonda, y, anchoRonda);
        }
        g.setColor(Estilo.BORDE);
        g.drawRect(X_TABLA, Y_TABLA, anchoNombre + anchoTipo + Juego.RONDAS * anchoRonda, y + 36 - Y_TABLA);
    }

    private static String comoTexto(boolean diceLaVerdad) {
        return diceLaVerdad ? "veraz" : "mentiroso";
    }

    private static void encabezadoDeCelda(Graphics2D g, String texto, int x, int y, int ancho) {
        celda(g, x, y, ancho, Estilo.ENCABEZADO);
        g.setFont(Estilo.NEGRITA);
        g.setColor(Estilo.TINTA);
        FontMetrics medidas = g.getFontMetrics();
        g.drawString(texto, x + (ancho - medidas.stringWidth(texto)) / 2, y + 24);
    }

    private static void celda(Graphics2D g, int x, int y, int ancho, Color fondo) {
        g.setColor(fondo);
        g.fillRect(x + 1, y + 1, ancho - 2, ALTO_DE_FILA - 2);
    }

    private static void textoCentrado(Graphics2D g, String texto, int x, int y, int ancho, Color color) {
        g.setFont(Estilo.TEXTO);
        g.setColor(color);
        FontMetrics medidas = g.getFontMetrics();
        g.drawString(texto, x + (ancho - medidas.stringWidth(texto)) / 2, y + ALTO_DE_FILA / 2 + 5);
    }
}
