package juego.pantallas;

import estructuras.ListaDinamica;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.controles.CampoDeTexto;
import juego.pantallas.componentes.dibujo.Avatar;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.DatoInvalidoException;
import juego.reglas.personajes.Elenco;
import juego.reglas.personajes.Personaje;
import juego.reglas.registro.Progreso;
import juego.reglas.registro.Puntaje;
import juego.reglas.rumores.ModoDeMarkov;
import markov.Generacion;
import markov.ModeloMarkov;
import ui.Texto;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EscenaMenu extends EscenaBase {
    private static final int AVISOS_VISIBLES = 3;
    private static final Rectangle RESUMEN = new Rectangle(60, 262, 520, 262);
    private static final Rectangle RUMOR = new Rectangle(600, 262, 540, 262);
    private static final Rectangle AVISOS = new Rectangle(60, 612, 1080, 92);

    private final ListaDinamica<Personaje> elenco = Elenco.nuevo();
    private final CampoDeTexto nombre = new CampoDeTexto("Tu nombre", Puntaje.LARGO_MAXIMO_DEL_NOMBRE);
    private final ListaDinamica<Boton> botonesDeOrden = new ListaDinamica<>();
    private Boton porCaracteres;
    private Boton porTokens;
    private Generacion rumor;
    private String errorDeNombre;

    public EscenaMenu(Sesion sesion) {
        super(sesion);
    }

    @Override
    public void alEntrar() {
        quitarBotones();
        botonesDeOrden.vaciar();
        botonesDeCabecera(null);
        nombre.setTexto(sesion.getProgreso().getJugador());
        nombre.ubicar(330, 548, 220, Boton.alto());
        int x = RUMOR.x + 20;
        int y = RUMOR.y + RUMOR.height - 20 - Boton.alto();
        for (int k = ModeloMarkov.ORDEN_MINIMO; k <= ModeloMarkov.ORDEN_MAXIMO; k++) {
            int orden = k;
            Boton b = agregarBoton(new Boton("k = " + k, Estilo.PRINCIPAL, false, () -> cambiarOrden(orden)).ubicar(x, y, 58, Boton.alto()));
            botonesDeOrden.agregar(b);
            x += 62;
        }
        x += 8;
        porCaracteres = agregarBoton(new Boton("Caracteres", Estilo.PRINCIPAL, false, () -> cambiarModo(ModoDeMarkov.CARACTERES)).ubicar(x, y, 102, Boton.alto()));
        porTokens = agregarBoton(new Boton("Tokens", Estilo.PRINCIPAL, false, () -> cambiarModo(ModoDeMarkov.TOKENS)).ubicar(x + 106, y, 76, Boton.alto()));
        int xOtro = x + 106 + 76 + 10;
        agregarBoton(new Boton("Otro", Estilo.TINTA_SUAVE, false, this::otroRumor).ubicar(xOtro, y, RUMOR.x + RUMOR.width - 20 - xOtro, Boton.alto()));

        int xb = 566;
        agregarBoton(new Boton("Practicar primero", Estilo.TINTA_SUAVE, false, () -> irA(new EscenaPractica(sesion, 0))).ubicar(xb, 548, 160, Boton.alto()));
        agregarBoton(new Boton("Ranking", Estilo.TINTA_SUAVE, false, () -> irA(new EscenaRanking(sesion))).ubicar(xb + 170, 548, 100, Boton.alto()));
        agregarBoton(new Boton("Empezar", Estilo.PRINCIPAL, true, this::empezar).ubicar(xb + 280, 548, 120, Boton.alto()));
        if (!sesion.getAlmacen().getAvisos().estaVacia()) {
            agregarBoton(new Boton("Entendido", Estilo.TINTA_SUAVE, false, this::descartarAvisos)
                    .ubicar(AVISOS.x + AVISOS.width - 120, AVISOS.y + AVISOS.height - 46, 108, Boton.alto()));
        }
        refrescarSeleccion();
        otroRumor();
    }

    private void cambiarOrden(int orden) {
        sesion.getProgreso().setOrdenDeMarkov(orden);
        sesion.reconfigurarRumores();
        refrescarSeleccion();
        otroRumor();
    }

    private void cambiarModo(ModoDeMarkov modo) {
        sesion.getProgreso().setModoDeMarkov(modo);
        sesion.reconfigurarRumores();
        refrescarSeleccion();
        otroRumor();
    }

    private void refrescarSeleccion() {
        Progreso progreso = sesion.getProgreso();
        for (int i = 0; i < botonesDeOrden.tamanio(); i++) {
            botonesDeOrden.obtener(i).setSeleccionado(i + ModeloMarkov.ORDEN_MINIMO == progreso.getOrdenDeMarkov());
        }
        porCaracteres.setSeleccionado(progreso.getModoDeMarkov() == ModoDeMarkov.CARACTERES);
        porTokens.setSeleccionado(progreso.getModoDeMarkov() == ModoDeMarkov.TOKENS);
    }

    private void otroRumor() {
        rumor = sesion.nuevoRumor();
    }

    private void descartarAvisos() {
        sesion.getAlmacen().descartarAvisos();
        alEntrar();
    }

    private void empezar() {
        try {
            sesion.getProgreso().setJugador(nombre.getTexto());
        } catch (DatoInvalidoException e) {
            errorDeNombre = e.getMessage();
            nombre.setFoco(true);
            return;
        }
        sesion.getAlmacen().guardarProgreso();
        irA(new EscenaRonda(sesion, sesion.nuevoJuego(), 0));
    }

    @Override
    protected void alHacerClick(int x, int y) {
        nombre.atenderClick(x, y);
    }

    @Override
    protected void actualizarEscena(double dt) {
        if (nombre.actualizar(getEntrada(), dt)) errorDeNombre = null;
        if (nombre.confirmo(getEntrada())) empezar();
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        Texto.centrado(g, "Verdad o mentira", ancho() / 2.0, 78, 42, Estilo.TINTA);
        g.setFont(Estilo.TEXTO.deriveFont(16f));
        g.setColor(Estilo.TINTA_SUAVE);
        centrado(g, "Un juego de lógica: descubrí quién miente y quién es capaz de hacer las dos cosas.", 108);

        int anchoFoto = 96;
        int separacion = 18;
        int total = elenco.tamanio() * anchoFoto + (elenco.tamanio() - 1) * separacion;
        int x = (ancho() - total) / 2;
        for (int i = 0; i < elenco.tamanio(); i++) {
            Avatar.pintar(g, elenco.obtener(i), x, 128, anchoFoto, true, null);
            x += anchoFoto + separacion;
        }

        Tarjeta.dibujar(g, RESUMEN);
        TextoRico.dibujar(g, Textos.RESUMEN, Estilo.TEXTO, Estilo.TINTA, RESUMEN.x + 20, RESUMEN.y + 18, RESUMEN.width - 40);
        Progreso p = sesion.getProgreso();
        String estadisticas = "Partidas: " + p.getPartidasJugadas() + "   Ganadas: " + p.getPartidasGanadas()
                + "   Racha: " + p.getRachaActual() + "   Mejor racha: " + p.getMejorRacha();
        g.setFont(Estilo.CHICA_NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString(estadisticas, RESUMEN.x + 20, RESUMEN.y + RESUMEN.height - 18);

        dibujarRumor(g);

        g.setFont(Estilo.NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString("Jugás como", 238, 572);
        nombre.dibujar(g);
        if (errorDeNombre != null) {
            g.setFont(Estilo.CHICA_NEGRITA);
            g.setColor(Estilo.texto(Estilo.ROJO));
            g.drawString(errorDeNombre, 330, 600);
        }
        dibujarAvisos(g);
    }

    private void dibujarRumor(Graphics2D g) {
        Tarjeta.dibujar(g, RUMOR);
        int x = RUMOR.x + 20;
        int ancho = RUMOR.width - 40;
        g.setFont(Estilo.CHICA_NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString("RUMOR DEL PASILLO · LO INVENTA UN MODELO DE MARKOV", x, RUMOR.y + 26);
        int y = RUMOR.y + 40;
        y += TextoRico.dibujar(g, "«" + rumor.getTexto() + (rumor.fueCortadaPorLargo() ? "…»" : "»"),
                Estilo.FRASE, Estilo.TINTA, x, y, ancho);
        String detalle = "Modelo " + sesion.getRumores().generador().getDescripcion()
                + " · " + sesion.getRumores().cantidadDeEstados() + " estados en un ABB de altura " + sesion.getRumores().alturaDeLaTabla()
                + "\n" + rumor.getSimbolos() + " símbolos · " + rumor.getRetrocesos() + " retrocesos de contexto"
                + (rumor.fueCortadaPorLargo() ? " · cortado por largo" : "")
                + (rumor.getIntentos() > 1 ? " · " + rumor.getIntentos() + " intentos" : "");
        TextoRico.dibujar(g, detalle, Estilo.CHICA, Estilo.TINTA_SUAVE, x, Math.max(y + 8, RUMOR.y + RUMOR.height - 110), ancho);
    }

    private void dibujarAvisos(Graphics2D g) {
        ListaDinamica<String> avisos = sesion.getAlmacen().getAvisos();
        if (avisos.estaVacia()) return;
        Tarjeta.dibujar(g, AVISOS, Estilo.AMARILLO_CLARO, Estilo.AMARILLO, 1.4f);
        StringBuilder texto = new StringBuilder("*Datos:* ");
        for (int i = 0; i < avisos.tamanio() && i < AVISOS_VISIBLES; i++) {
            if (i > 0) texto.append('\n');
            texto.append(avisos.obtener(i));
        }
        if (avisos.tamanio() > AVISOS_VISIBLES) texto.append("\n(y ").append(avisos.tamanio() - AVISOS_VISIBLES).append(" avisos más)");
        TextoRico.dibujar(g, texto.toString(), Estilo.CHICA, Estilo.TINTA, AVISOS.x + 14, AVISOS.y + 10, AVISOS.width - 150);
    }

    private void centrado(Graphics2D g, String texto, int y) {
        FontMetrics medidas = g.getFontMetrics();
        g.drawString(texto, (ancho() - medidas.stringWidth(texto)) / 2, y);
    }
}
