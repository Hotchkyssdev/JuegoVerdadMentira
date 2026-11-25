package juego.pantallas;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.controles.Superposicion;
import juego.pantallas.componentes.dibujo.DibujoDeArbol;
import juego.pantallas.componentes.estilo.Animacion;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.reglas.partida.Juego;
import juego.reglas.personajes.Personaje;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EscenaArboles extends EscenaBase {
    private static final double DURACION_DE_LA_SUBIDA = 0.8;
    private static final double ESCALONADO_DE_LA_SUBIDA = 0.07;
    private static final int Y_TARJETAS = 130;
    private static final int ALTO_TARJETAS = 360;

    private final Juego juego;
    private final int ronda;
    private final boolean eleccion;
    private final ListaDinamica<Personaje> personajes;

    public EscenaArboles(Sesion sesion, Juego juego, int ronda, boolean eleccion) {
        super(sesion);
        this.juego = juego;
        this.ronda = ronda;
        this.eleccion = eleccion;
        this.personajes = juego.getPersonajes();
    }

    @Override
    public void alEntrar() {
        quitarBotones();
        botonesDeCabecera(this::volverAlInicio);
        if (eleccion) {
            for (int i = 0; i < personajes.tamanio(); i++) {
                Personaje p = personajes.obtener(i);
                Rectangle tarjeta = tarjeta(i);
                agregarBoton(new Boton("Es " + p.getNombre(), Estilo.colorDeNombre(p), false,
                        () -> irA(new EscenaFin(sesion, juego, juego.resultado(p))))
                        .ubicar(tarjeta.x, tarjeta.y + tarjeta.height + 12, tarjeta.width, Boton.alto()));
            }
            return;
        }
        boolean ultima = ronda == Juego.RONDAS - 1;
        String texto = ultima ? "Elegir al que hace las dos cosas" : "Ir a la ronda " + (ronda + 2);
        int ancho = ultima ? 290 : 170;
        agregarBoton(new Boton(texto, Estilo.PRINCIPAL, true, () -> irA(ultima
                ? new EscenaArboles(sesion, juego, ronda, true)
                : new EscenaRonda(sesion, juego, ronda + 1)))
                .ubicar(ancho() - MARGEN - ancho, alto() - MARGEN - Boton.alto(), ancho, Boton.alto()));
    }

    private void volverAlInicio() {
        mostrar(new Superposicion("Volver al inicio", "Vas a dejar esta partida y no se va a guardar. ¿Volver al inicio?", 520)
                .conBoton("Seguir jugando", Estilo.TINTA_SUAVE, false, () -> { })
                .conBoton("Volver al inicio", Estilo.PRINCIPAL, true, () -> irA(new EscenaMenu(sesion))));
    }

    private Rectangle tarjeta(int indice) {
        int separacion = 12;
        int ancho = (ancho() - 2 * MARGEN - (personajes.tamanio() - 1) * separacion) / personajes.tamanio();
        return new Rectangle(MARGEN + indice * (ancho + separacion), Y_TARJETAS, ancho, ALTO_TARJETAS);
    }

    @Override
    protected void actualizarEscena(double dt) {
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        if (eleccion) {
            encabezado(g, "¿Quién puede mentir y también decir la verdad?",
                    "Es el único que cambió de una ronda a otra. Mirá tus árboles y elegilo.", Estilo.TINTA, 820);
        } else {
            encabezado(g, "Tus decisiones después de la ronda " + (ronda + 1),
                    "Cada árbol es un personaje y cada nivel es una ronda: a la izquierda lo marcaste *mentiroso*, "
                            + "a la derecha *veraz*. El juego no te dice si acertaste.", Estilo.TINTA, 820);
        }
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            double inicio = i * ESCALONADO_DE_LA_SUBIDA;
            double avance = eleccion ? 1 : Animacion.frenando(Animacion.tramo(tiempo, inicio, inicio + DURACION_DE_LA_SUBIDA));
            if (avance <= 0) continue;
            Rectangle tarjeta = tarjeta(i);
            tarjeta.y += (int) Math.round((1 - avance) * 160);
            Graphics2D gt = (Graphics2D) g.create();
            gt.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) avance));
            Tarjeta.dibujar(gt, tarjeta);
            DibujoDeArbol.dibujar(gt, new Rectangle(tarjeta.x, tarjeta.y + 8, tarjeta.width, tarjeta.height - 16), p, juego.getArbol(p), false, null, true);
            gt.dispose();
        }
    }
}
