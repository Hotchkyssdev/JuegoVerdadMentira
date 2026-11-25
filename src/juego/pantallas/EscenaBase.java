package juego.pantallas;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.controles.Superposicion;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.TextoRico;
import motor.Entrada;
import motor.Escena;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class EscenaBase extends Escena {
    protected static final int MARGEN = 24;

    protected final Sesion sesion;
    protected double tiempo;
    private final ListaDinamica<Boton> botones = new ListaDinamica<>();
    private Superposicion superposicion;

    protected EscenaBase(Sesion sesion) {
        if (sesion == null) throw new IllegalArgumentException("Una escena necesita la sesión");
        this.sesion = sesion;
    }

    protected abstract void actualizarEscena(double dt);

    protected abstract void dibujarEscena(Graphics2D g);

    protected void alHacerClick(int x, int y) {
    }

    protected int ancho() {
        return getContexto().getAncho();
    }

    protected int alto() {
        return getContexto().getAlto();
    }

    protected int mouseX() {
        return getEntrada().getMouseX();
    }

    protected int mouseY() {
        return getEntrada().getMouseY();
    }

    protected Boton agregarBoton(Boton boton) {
        botones.agregar(boton);
        return boton;
    }

    protected void quitarBotones() {
        botones.vaciar();
    }

    protected void mostrar(Superposicion nueva) {
        superposicion = nueva;
    }

    protected boolean haySuperposicion() {
        return superposicion != null && superposicion.estaAbierta();
    }

    protected void irA(Escena escena) {
        getContexto().irA(escena);
    }

    @Override
    public final void actualizar(double dt) {
        tiempo += dt;
        Entrada entrada = getEntrada();
        if (haySuperposicion()) {
            if (entrada.huboClick()) superposicion.atenderClick(entrada.getMouseX(), entrada.getMouseY());
            return;
        }
        if (entrada.huboClick() && !atenderBotones(entrada.getMouseX(), entrada.getMouseY())) {
            alHacerClick(entrada.getMouseX(), entrada.getMouseY());
        }
        actualizarEscena(dt);
    }

    private boolean atenderBotones(int x, int y) {
        ListaDinamica<Boton> copia = botones.copia();
        for (int i = 0; i < copia.tamanio(); i++) {
            if (copia.obtener(i).atenderClick(x, y)) return true;
        }
        return false;
    }

    @Override
    public final void dibujar(Graphics2D g) {
        Estilo.suavizar(g);
        g.setColor(Estilo.FONDO);
        g.fillRect(0, 0, ancho(), alto());
        dibujarEscena(g);
        int x = haySuperposicion() ? -1 : mouseX();
        int y = haySuperposicion() ? -1 : mouseY();
        for (int i = 0; i < botones.tamanio(); i++) botones.obtener(i).dibujar(g, x, y);
        if (haySuperposicion()) superposicion.dibujar(g, ancho(), alto(), mouseX(), mouseY());
    }

    protected int encabezado(Graphics2D g, String titulo, String subtitulo, Color colorDelTitulo, int anchoDisponible) {
        g.setFont(Estilo.TITULO);
        g.setColor(colorDelTitulo);
        g.drawString(titulo, MARGEN, MARGEN + 26);
        int y = MARGEN + 38;
        if (subtitulo != null) y += TextoRico.dibujar(g, subtitulo, Estilo.TEXTO, Estilo.TINTA_SUAVE, MARGEN, y, anchoDisponible);
        return y;
    }

    protected void botonesDeCabecera(Runnable alIrAlInicio) {
        int anchoReglas = 150;
        int x = ancho() - MARGEN - anchoReglas;
        agregarBoton(new Boton("Cómo se juega", Estilo.TINTA_SUAVE, false, this::mostrarReglas).ubicar(x, MARGEN - 6, anchoReglas, Boton.alto()));
        if (alIrAlInicio != null) {
            int anchoInicio = 84;
            agregarBoton(new Boton("Inicio", Estilo.TINTA_SUAVE, false, alIrAlInicio).ubicar(x - 10 - anchoInicio, MARGEN - 6, anchoInicio, Boton.alto()));
        }
    }

    protected void mostrarReglas() {
        mostrar(new Superposicion("Cómo se juega", Textos.REGLAS, 760)
                .conBoton("Entendido", Estilo.PRINCIPAL, true, () -> { }));
    }
}
