package juego.pantallas;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.dibujo.Avatar;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Personaje;
import juego.reglas.ronda.Afirmacion;
import juego.reglas.ronda.FraseCompuesta;
import juego.reglas.ronda.FraseSimple;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EscenaPractica extends EscenaBase {
    private static final class Paso {
        private final String texto;
        private final Boolean mcLovin;
        private final Boolean roz;
        private final Boolean lisa;
        private final boolean contradiccion;

        private Paso(String texto, Boolean mcLovin, Boolean roz, Boolean lisa, boolean contradiccion) {
            this.texto = texto;
            this.mcLovin = mcLovin;
            this.roz = roz;
            this.lisa = lisa;
            this.contradiccion = contradiccion;
        }
    }

    private final int paso;
    private final ListaDinamica<Paso> pasos = new ListaDinamica<>();
    private final ListaDinamica<Afirmacion> frases = new ListaDinamica<>();

    public EscenaPractica(Sesion sesion, int paso) {
        super(sesion);
        ListaDinamica<Personaje> elenco = Elenco.nuevo();
        Personaje mcLovin = elenco.obtener(0);
        Personaje roz = elenco.obtener(1);
        Personaje lisa = elenco.obtener(3);
        frases.agregar(new FraseSimple(mcLovin, roz, true));
        frases.agregar(new FraseSimple(roz, lisa, false));
        frases.agregar(new FraseCompuesta(lisa, mcLovin, true, roz, false));
        String m = Estilo.nombre(mcLovin);
        String r = Estilo.nombre(roz);
        String l = Estilo.nombre(lisa);
        pasos.agregar(new Paso("Acá hay tres personajes y cada uno dice una frase. Hay una sola forma de marcarlos en la que "
                + "todas las frases cierran. Vamos a encontrarla paso a paso.", null, null, null, false));
        pasos.agregar(new Paso("Probemos una suposición: *" + m + " miente*.", false, null, null, false));
        pasos.agregar(new Paso("Si " + m + " miente, su frase es falsa. Dijo «" + r + " dice la verdad», así que *" + r + " miente*.",
                false, false, null, false));
        pasos.agregar(new Paso(r + " miente, así que su frase también es falsa. Dijo «" + l + " miente», entonces *"
                + l + " dice la verdad*.", false, false, true, false));
        pasos.agregar(new Paso(l + " dice la verdad, así que su frase es cierta: «" + m + " dice la verdad, pero " + r + " no». "
                + "¡Pero habíamos supuesto que " + m + " miente! Es una *contradicción*: la suposición estaba mal.", false, false, true, true));
        pasos.agregar(new Paso("Entonces *" + m + " dice la verdad*. Su frase es cierta, así que *" + r + " dice la verdad*.",
                true, true, null, false));
        pasos.agregar(new Paso(r + " dice la verdad, así que «" + l + " miente» es cierto: *" + l + " miente*.", true, true, false, false));
        pasos.agregar(new Paso("Comprobemos: " + l + " miente, así que su frase tiene que ser falsa. «" + m + " dice la verdad, pero "
                + r + " no» es falsa, porque " + r + " dice la verdad. *Todo cierra.*\n\nEn el juego son cinco personajes "
                + "y tres rondas, y uno de ellos cambia de una ronda a otra: encontrarlo es el objetivo.", true, true, false, false));
        this.paso = Math.max(0, Math.min(paso, pasos.tamanio() - 1));
    }

    @Override
    public void alEntrar() {
        quitarBotones();
        botonesDeCabecera(() -> irA(new EscenaMenu(sesion)));
        int y = alto() - MARGEN - Boton.alto();
        boolean ultimo = paso == pasos.tamanio() - 1;
        Boton anterior = agregarBoton(new Boton("Anterior", Estilo.TINTA_SUAVE, false, () -> irA(new EscenaPractica(sesion, paso - 1)))
                .ubicar(ancho() - MARGEN - 330, y, 140, Boton.alto()));
        anterior.setHabilitado(paso > 0);
        agregarBoton(new Boton(ultimo ? "Empezar a jugar" : "Siguiente", Estilo.PRINCIPAL, true,
                () -> irA(ultimo ? new EscenaRonda(sesion, sesion.nuevoJuego(), 0) : new EscenaPractica(sesion, paso + 1)))
                .ubicar(ancho() - MARGEN - 180, y, 180, Boton.alto()));
    }

    @Override
    protected void actualizarEscena(double dt) {
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        encabezado(g, "Práctica", "Paso " + (paso + 1) + " de " + pasos.tamanio()
                + ". Así se razona una ronda: se supone algo y se sigue hasta que todo cierre o aparezca una contradicción.",
                Estilo.TINTA, 820);
        Paso actual = pasos.obtener(paso);
        int anchoColumna = (ancho() - 2 * MARGEN - 2 * 24) / 3;
        for (int i = 0; i < frases.tamanio(); i++) {
            Afirmacion frase = frases.obtener(i);
            int x = MARGEN + i * (anchoColumna + 24);
            Rectangle globo = new Rectangle(x, 110, anchoColumna, 110);
            boolean avisar = actual.contradiccion && i == 2;
            Tarjeta.dibujar(g, globo, Estilo.TARJETA, avisar ? Estilo.texto(Estilo.ROJO) : Estilo.BORDE, avisar ? 3f : 1.5f);
            TextoRico.dibujar(g, Estilo.nombre(frase.getEmisor()) + " dice:", Estilo.CHICA_NEGRITA, Estilo.TINTA_SUAVE, x + 14, globo.y + 10, anchoColumna - 28);
            TextoRico.dibujar(g, frase.getTexto(Estilo::nombre), Estilo.FRASE, Estilo.TINTA, x + 14, globo.y + 34, anchoColumna - 28);
            if (avisar) {
                g.setFont(Estilo.CHICA_NEGRITA);
                g.setColor(Estilo.texto(Estilo.ROJO));
                g.drawString("Esto contradice lo que supusimos.", x + 14, globo.y + globo.height - 12);
            }
            g.setColor(Estilo.TARJETA);
            g.setStroke(new BasicStroke(1.5f));
            Avatar.pintar(g, frase.getEmisor(), x + 20, 236, 110, true, marcaDe(actual, i));
        }
        Rectangle explicacion = new Rectangle(MARGEN, 400, ancho() - 2 * MARGEN, 220);
        Tarjeta.dibujar(g, explicacion);
        TextoRico.dibujar(g, actual.texto, Estilo.FRASE, Estilo.TINTA, explicacion.x + 20, explicacion.y + 20, explicacion.width - 40);
    }

    private static Boolean marcaDe(Paso paso, int indice) {
        if (indice == 0) return paso.mcLovin;
        return indice == 1 ? paso.roz : paso.lisa;
    }
}
