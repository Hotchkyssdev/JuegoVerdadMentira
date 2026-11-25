package juego.pantallas;

import estructuras.ListaDinamica;
import estructuras.PilaEnlazada;
import juego.pantallas.componentes.controles.Boton;
import juego.pantallas.componentes.controles.CampoDeTexto;
import juego.pantallas.componentes.controles.Superposicion;
import juego.pantallas.componentes.dibujo.Avatar;
import juego.pantallas.componentes.dibujo.DatosDelDigrafo;
import juego.pantallas.componentes.dibujo.DibujoDeArbol;
import juego.pantallas.componentes.dibujo.DibujoDelDigrafo;
import juego.pantallas.componentes.estilo.Animacion;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.Tarjeta;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.deduccion.Deduccion;
import juego.reglas.deduccion.InterpreteDeDeducciones;
import juego.reglas.partida.Juego;
import juego.reglas.partida.Pista;
import juego.reglas.personajes.Marcas;
import juego.reglas.personajes.Personaje;
import juego.reglas.ronda.Afirmacion;
import juego.reglas.ronda.Relacion;
import markov.Generacion;
import motor.Entrada;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class EscenaRonda extends EscenaBase implements DatosDelDigrafo {
    private static final Rectangle GRAFO = new Rectangle(20, 104, 790, 396);
    private static final Rectangle DETALLE = new Rectangle(826, 104, 354, 396);
    private static final Rectangle CAMPO = new Rectangle(214, 512, 440, 36);
    private static final int Y_FRANJA = 558;
    private static final int ALTO_FRANJA = 102;
    private static final int Y_PIE = 672;
    private static final double FIN_DEL_DIFUMINADO = 0.45;
    private static final double DURACION_DEL_VUELO = 0.95;
    private static final double ESCALONADO_DEL_VUELO = 0.09;
    private static final int LADO_AL_ATERRIZAR = 22;
    private static final int SUGERENCIAS_VISIBLES = 4;

    private final Juego juego;
    private final int ronda;
    private final ListaDinamica<Personaje> personajes;
    private final Marcas marcas = new Marcas();
    private final PilaEnlazada<Marcas> historial = new PilaEnlazada<>();
    private final CampoDeTexto campo = new CampoDeTexto("Escribí tu deducción: «lisa y roz mienten, ferb dice la verdad»", 120);
    private final InterpreteDeDeducciones interprete;
    private final DibujoDelDigrafo digrafo;
    private final ListaDinamica<Personaje> ordenDeAparicion;
    private final Generacion rumor;
    private Personaje seleccionado;
    private Pista pista;
    private String mensaje = "";
    private Color colorDelMensaje = Estilo.TINTA;
    private ListaDinamica<String> sugerencias = new ListaDinamica<>();
    private boolean confirmando;
    private double tiempoDeConfirmacion;

    public EscenaRonda(Sesion sesion, Juego juego, int ronda) {
        super(sesion);
        this.juego = juego;
        this.ronda = ronda;
        this.personajes = juego.getPersonajes();
        this.interprete = new InterpreteDeDeducciones(personajes);
        Afirmacion conFoto = juego.getAfirmaciones(ronda).buscar(Afirmacion::esComprobableConLasFotos);
        Personaje inicio = conFoto != null ? conFoto.getEmisor() : personajes.primero();
        this.ordenDeAparicion = juego.getGrafo(ronda).ordenDeAparicion(inicio);
        this.seleccionado = inicio;
        this.digrafo = new DibujoDelDigrafo(this);
        this.rumor = sesion.nuevoRumor();
    }

    @Override
    public void alEntrar() {
        digrafo.ubicar(GRAFO.x, GRAFO.y, GRAFO.width, GRAFO.height);
        digrafo.animarEntrada();
        campo.ubicar(CAMPO.x, CAMPO.y, CAMPO.width, CAMPO.height);
        armarBotones();
    }

    private void armarBotones() {
        quitarBotones();
        if (confirmando) return;
        botonesDeCabecera(this::volverAlInicio);
        int abajo = DETALLE.y + DETALLE.height;
        int x = DETALLE.x + 16;
        int ancho = (DETALLE.width - 32 - 8) / 2;
        Boolean marca = marcas.de(seleccionado);
        agregarBoton(new Boton("Dice la verdad", Estilo.VERDE, false, () -> marcar(seleccionado, true))
                .ubicar(x, abajo - 92, ancho, 38)).setSeleccionado(Boolean.TRUE.equals(marca));
        agregarBoton(new Boton("Miente", Estilo.ROJO, false, () -> marcar(seleccionado, false))
                .ubicar(x + ancho + 8, abajo - 92, ancho, 38)).setSeleccionado(Boolean.FALSE.equals(marca));
        agregarBoton(new Boton("Anterior", Estilo.TINTA_SUAVE, false, () -> moverSeleccion(-1)).ubicar(x, abajo - 46, ancho, 34));
        agregarBoton(new Boton("Siguiente", Estilo.TINTA_SUAVE, false, () -> moverSeleccion(1)).ubicar(x + ancho + 8, abajo - 46, ancho, 34));

        agregarBoton(new Boton("Aplicar", Estilo.PRINCIPAL, false, this::aplicarDeduccion).ubicar(CAMPO.x + CAMPO.width + 8, CAMPO.y, 92, CAMPO.height));

        agregarBoton(new Boton("Deshacer todo", Estilo.AMARILLO, false, this::borrarMarcas)
                .ubicar(MARGEN, Y_PIE, 140, Boton.alto())).setHabilitado(!marcas.estaVacia());
        int xd = ancho() - MARGEN;
        Boton confirmar = agregarBoton(new Boton("Confirmar ronda", Estilo.PRINCIPAL, true, this::confirmar).ubicar(xd - 170, Y_PIE, 170, Boton.alto()));
        confirmar.setHabilitado(marcas.cantidad() == personajes.tamanio());
        agregarBoton(new Boton("Deshacer", Estilo.TINTA_SUAVE, false, this::deshacer)
                .ubicar(xd - 170 - 10 - 110, Y_PIE, 110, Boton.alto())).setHabilitado(!historial.estaVacia());
        agregarBoton(new Boton("Pista (" + juego.getPistasRestantes() + ")", Estilo.AMARILLO, false, this::usarPista)
                .ubicar(xd - 170 - 10 - 110 - 10 - 110, Y_PIE, 110, Boton.alto())).setHabilitado(juego.getPistasRestantes() > 0);
    }

    private void volverAlInicio() {
        mostrar(new Superposicion("Volver al inicio", "Vas a dejar esta partida y no se va a guardar. ¿Volver al inicio?", 520)
                .conBoton("Seguir jugando", Estilo.TINTA_SUAVE, false, () -> { })
                .conBoton("Volver al inicio", Estilo.PRINCIPAL, true, () -> irA(new EscenaMenu(sesion))));
    }

    private void marcar(Personaje p, boolean veraz) {
        historial.apilar(marcas.copia());
        if (Boolean.valueOf(veraz).equals(marcas.de(p))) marcas.quitar(p);
        else marcas.marcar(p, veraz);
        limpiarPista();
        mensaje = "";
        armarBotones();
    }

    private void deshacer() {
        if (historial.estaVacia()) return;
        marcas.reemplazarCon(historial.desapilar());
        limpiarPista();
        mensaje = "";
        armarBotones();
    }

    private void borrarMarcas() {
        if (marcas.estaVacia()) return;
        historial.apilar(marcas.copia());
        marcas.vaciar();
        limpiarPista();
        mensaje = "";
        armarBotones();
    }

    private void limpiarPista() {
        pista = null;
    }

    private void moverSeleccion(int paso) {
        int indice = personajes.indiceDe(seleccionado);
        seleccionar(personajes.obtener(Math.floorMod(indice + paso, personajes.tamanio())));
    }

    private void seleccionar(Personaje p) {
        seleccionado = p;
        armarBotones();
    }

    private void aplicarDeduccion() {
        Deduccion deduccion = interprete.interpretar(campo.getTexto());
        if (!deduccion.esValida()) {
            mostrarMensaje(deduccion.getErrores().unir(" "), Estilo.texto(Estilo.ROJO));
            return;
        }
        historial.apilar(marcas.copia());
        Marcas nuevas = deduccion.getMarcas();
        marcas.agregarTodas(nuevas);
        ListaDinamica<Personaje> marcados = nuevas.personajes();
        StringBuilder texto = new StringBuilder("Marcaste: ");
        for (int i = 0; i < marcados.tamanio(); i++) {
            Personaje p = marcados.obtener(i);
            if (i > 0) texto.append(", ");
            texto.append(Estilo.nombre(p)).append(nuevas.diceLaVerdad(p) ? " dice la verdad" : " miente");
        }
        campo.vaciar();
        sugerencias = new ListaDinamica<>();
        limpiarPista();
        mostrarMensaje(texto.append(" (").append(deduccion.getTokens().tamanio()).append(" tokens).").toString(), Estilo.TINTA);
        armarBotones();
    }

    private void usarPista() {
        if (juego.getPistasRestantes() <= 0) return;
        pista = juego.pedirPista(ronda, marcas, Estilo::nombre);
        Color color = pista.esContradiccion() ? Estilo.texto(Estilo.ROJO)
                : pista.getTipo() == Pista.Tipo.TODO_CIERRA ? Estilo.texto(Estilo.VERDE) : Estilo.AMARILLO;
        mostrarMensaje(pista.getMensaje(), color);
        if (pista.getSugerido() != null) seleccionado = pista.getSugerido();
        armarBotones();
    }

    private void mostrarMensaje(String texto, Color color) {
        mensaje = texto;
        colorDelMensaje = color;
    }

    private void confirmar() {
        if (marcas.cantidad() != personajes.tamanio()) return;
        confirmando = true;
        tiempoDeConfirmacion = 0;
        campo.setFoco(false);
        digrafo.completarEntrada();
        armarBotones();
    }

    private double finDelVuelo() {
        return FIN_DEL_DIFUMINADO + (personajes.tamanio() - 1) * ESCALONADO_DEL_VUELO + DURACION_DEL_VUELO;
    }

    private double avanceDelVuelo(int indice) {
        double inicio = FIN_DEL_DIFUMINADO + indice * ESCALONADO_DEL_VUELO;
        return Animacion.tramo(tiempoDeConfirmacion, inicio, inicio + DURACION_DEL_VUELO);
    }

    @Override
    protected void alHacerClick(int x, int y) {
        if (confirmando) return;
        if (digrafo.entradaEnCurso() && GRAFO.contains(x, y)) {
            digrafo.completarEntrada();
            return;
        }
        campo.atenderClick(x, y);
        Personaje p = digrafo.personajeEn(x, y);
        if (p == null) {
            for (int i = 0; i < personajes.tamanio(); i++) {
                if (celdaDeArbol(i).contains(x, y)) p = personajes.obtener(i);
            }
        }
        if (p != null) seleccionar(p);
    }

    @Override
    protected void actualizarEscena(double dt) {
        digrafo.actualizar(dt);
        if (confirmando) {
            tiempoDeConfirmacion += dt;
            digrafo.setOpacidadDeLineas(1 - Animacion.suave(Animacion.tramo(tiempoDeConfirmacion, 0, FIN_DEL_DIFUMINADO)));
            digrafo.setFotosOcultas(tiempoDeConfirmacion >= FIN_DEL_DIFUMINADO);
            if (tiempoDeConfirmacion >= finDelVuelo()) {
                juego.registrarDecisiones(ronda, marcas);
                irA(new EscenaArboles(sesion, juego, ronda, false));
            }
            return;
        }
        Entrada entrada = getEntrada();
        if (campo.actualizar(entrada, dt)) sugerencias = interprete.sugerencias(campo.getTexto());
        if (campo.confirmo(entrada)) aplicarDeduccion();
        if (campo.tieneFoco()) return;
        if (entrada.fuePresionada(KeyEvent.VK_V)) marcar(seleccionado, true);
        if (entrada.fuePresionada(KeyEvent.VK_M)) marcar(seleccionado, false);
        if (entrada.fuePresionada(KeyEvent.VK_RIGHT)) moverSeleccion(1);
        if (entrada.fuePresionada(KeyEvent.VK_LEFT)) moverSeleccion(-1);
    }

    @Override
    protected void dibujarEscena(Graphics2D g) {
        String subtitulo = "*Rumor del pasillo* (Markov " + sesion.getRumores().generador().getDescripcion()
                + ", no es una pista): «" + rumor.getTexto() + (rumor.fueCortadaPorLargo() ? "…" : "") + "»";
        encabezado(g, "Ronda " + (ronda + 1) + " de " + Juego.RONDAS, subtitulo, Estilo.TINTA, ancho() - 2 * MARGEN - 270);
        Personaje bajoElMouse = confirmando ? null : digrafo.personajeEn(mouseX(), mouseY());
        digrafo.dibujar(g, bajoElMouse);
        double visible = confirmando ? 1 - Animacion.suave(Animacion.tramo(tiempoDeConfirmacion, 0, FIN_DEL_DIFUMINADO)) : 1;
        if (visible > 0) {
            Graphics2D gv = (Graphics2D) g.create();
            gv.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) visible));
            dibujarDetalle(gv);
            dibujarDeduccion(gv);
            dibujarPie(gv);
            gv.dispose();
        }
        dibujarFranja(g);
        if (confirmando && tiempoDeConfirmacion >= FIN_DEL_DIFUMINADO) dibujarVuelos(g);
    }

    private void dibujarDetalle(Graphics2D g) {
        Tarjeta.dibujar(g, DETALLE);
        Personaje p = seleccionado;
        Afirmacion a = juego.getAfirmacion(ronda, p);
        int x = DETALLE.x + 16;
        int ancho = DETALLE.width - 32;
        int y = DETALLE.y + 14;
        Avatar.pintar(g, p, x, y, 52, false, marcas.de(p));
        g.setFont(Estilo.NOMBRE.deriveFont(24f));
        g.setColor(Estilo.colorDeNombre(p));
        g.drawString(p.getNombre(), x + 66, y + 26);
        Boolean marca = marcas.de(p);
        g.setFont(Estilo.CHICA_NEGRITA);
        g.setColor(marca == null ? Estilo.TINTA_SUAVE : Estilo.texto(Estilo.colorDeMarca(marca)));
        g.drawString(marca == null ? "Sin marcar" : "Lo marcaste: " + (marca ? "dice la verdad" : "miente"), x + 66, y + 46);
        y += Avatar.alto(52, false) + 14;

        Shape recorte = g.getClip();
        g.clipRect(DETALLE.x, y - 14, DETALLE.width, DETALLE.y + DETALLE.height - 112 - (y - 14));
        y = seccion(g, "Dice", "«" + a.getTexto(Estilo::nombre) + "»", Estilo.FRASE, x, y, ancho);
        y = seccion(g, "Qué implica", consecuencias(a), Estilo.CHICA, x, y, ancho);
        StringBuilder dicen = new StringBuilder();
        ListaDinamica<Afirmacion> frases = juego.getAfirmaciones(ronda);
        for (int i = 0; i < frases.tamanio(); i++) {
            Afirmacion otra = frases.obtener(i);
            if (!otra.nombraA(p)) continue;
            if (dicen.length() > 0) dicen.append('\n');
            dicen.append(Estilo.nombre(otra.getEmisor())).append(": «").append(otra.getTexto(Estilo::nombre)).append("»");
        }
        y = seccion(g, "Lo que dicen de " + p.getNombre(), dicen.length() == 0 ? "Nadie habla de " + Estilo.nombre(p) + " en esta ronda." : dicen.toString(),
                Estilo.CHICA, x, y, ancho);
        String aviso = pista == null ? null : pista.avisoPara(p);
        if (aviso != null) TextoRico.dibujar(g, aviso, Estilo.CHICA_NEGRITA, Estilo.texto(Estilo.ROJO), x, y, ancho);
        g.setClip(recorte);
        Tarjeta.titulo(g, "Marcalo (teclas V y M)", x, DETALLE.y + DETALLE.height - 100);
    }

    private static int seccion(Graphics2D g, String titulo, String texto, java.awt.Font fuente, int x, int y, int ancho) {
        Tarjeta.titulo(g, titulo, x, y);
        return y + 8 + TextoRico.dibujar(g, texto, fuente, Estilo.TINTA, x, y + 6, ancho) + 10;
    }

    private static String consecuencias(Afirmacion a) {
        String quien = Estilo.nombre(a.getEmisor());
        if (a.esComprobableConLasFotos()) {
            return "Se comprueba mirando las fotos. *Si es cierto, " + quien + " dice la verdad*; *si es falso, " + quien + " miente*.";
        }
        ListaDinamica<String> lista = a.getConsecuencias(Estilo::nombre);
        if (lista.estaVacia()) return "Esta frase no da pistas: un veraz y un mentiroso dirían lo mismo.";
        return "*Si " + quien + " dice la verdad:* " + lista.obtener(0) + ".\n*Si " + quien + " miente:* " + lista.obtener(1) + ".";
    }

    private void dibujarDeduccion(Graphics2D g) {
        g.setFont(Estilo.NEGRITA);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString("Tu deducción (trie)", MARGEN, CAMPO.y + 23);
        campo.dibujar(g);
        if (!sugerencias.estaVacia() && campo.tieneFoco()) {
            StringBuilder texto = new StringBuilder("Sugerencias: ");
            for (int i = 0; i < sugerencias.tamanio() && i < SUGERENCIAS_VISIBLES; i++) {
                if (i > 0) texto.append(" · ");
                texto.append(sugerencias.obtener(i));
            }
            g.setFont(Estilo.CHICA);
            g.setColor(Estilo.TINTA_SUAVE);
            g.drawString(texto.toString(), CAMPO.x + CAMPO.width + 112, CAMPO.y + 23);
        }
    }

    private void dibujarPie(Graphics2D g) {
        int total = personajes.tamanio();
        String contador = marcas.cantidad() == total
                ? "Marcaste a los " + total + ". Revisá que todo cierre y confirmá."
                : "Marcaste " + marcas.cantidad() + " de " + total + ".";
        int x = MARGEN + 152;
        g.setFont(Estilo.TEXTO);
        g.setColor(Estilo.TINTA_SUAVE);
        g.drawString(contador, x, Y_PIE + 14);
        if (!mensaje.isEmpty()) TextoRico.dibujar(g, mensaje, Estilo.CHICA_NEGRITA, colorDelMensaje, x, Y_PIE + 20, 560);
    }

    private Rectangle celdaDeArbol(int indice) {
        int separacion = 10;
        int ancho = (ancho() - 2 * MARGEN + 4 - (personajes.tamanio() - 1) * separacion) / personajes.tamanio();
        return new Rectangle(MARGEN - 2 + indice * (ancho + separacion), Y_FRANJA, ancho, ALTO_FRANJA);
    }

    private void dibujarFranja(Graphics2D g) {
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            Rectangle celda = celdaDeArbol(i);
            boolean elegido = p.equals(seleccionado) && !confirmando;
            Tarjeta.dibujar(g, celda, Estilo.TARJETA, elegido ? p.getColor() : Estilo.BORDE, elegido ? 3f : 1f);
            Rectangle zona = new Rectangle(celda.x, celda.y + 2, celda.width, celda.height - 22);
            boolean aterrizo = !confirmando || avanceDelVuelo(i) >= 0.82;
            boolean conFoto = confirmando && aterrizo;
            DibujoDeArbol.dibujar(g, zona, p, juego.getArbol(p), true, marcas.de(p), conFoto);
            g.setFont(Estilo.NOMBRE.deriveFont(elegido ? 16f : 14f));
            g.setColor(elegido ? Estilo.TINTA : Estilo.TINTA_SUAVE);
            int anchoNombre = g.getFontMetrics().stringWidth(p.getNombre());
            g.drawString(p.getNombre(), (int) (celda.getCenterX() - anchoNombre / 2.0), celda.y + celda.height - 6);
        }
    }

    private void dibujarVuelos(Graphics2D g) {
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            double avance = avanceDelVuelo(i);
            Rectangle foto = digrafo.rectanguloDeFoto(p);
            Point2D destino = DibujoDeArbol.centroDeRaiz(new Rectangle(celdaDeArbol(i).x, Y_FRANJA + 2, celdaDeArbol(i).width, ALTO_FRANJA - 22), true);
            double recorrido = Animacion.suave(Animacion.tramo(avance, 0, 0.82));
            Point2D origen = new Point2D.Double(foto.getCenterX(), foto.getCenterY());
            Point2D control = new Point2D.Double(destino.getX(), origen.getY());
            double u = 1 - recorrido;
            double x = u * u * origen.getX() + 2 * u * recorrido * control.getX() + recorrido * recorrido * destino.getX();
            double y = u * u * origen.getY() + 2 * u * recorrido * control.getY() + recorrido * recorrido * destino.getY();
            double ancho = Animacion.interpolar(foto.width, LADO_AL_ATERRIZAR, recorrido);
            double escala = ancho / foto.width;
            double opacidad = 1 - Animacion.tramo(avance, 0.78, 0.92);
            if (opacidad > 0) {
                Graphics2D gf = (Graphics2D) g.create();
                gf.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacidad));
                gf.translate(x, y);
                gf.scale(escala, escala);
                gf.translate(-foto.width / 2.0, -foto.height / 2.0);
                Avatar.pintar(gf, p, foto.width, false, marcas.de(p));
                gf.dispose();
            }
            double pulso = Animacion.tramo(avance, 0.8, 1);
            if (pulso > 0 && pulso < 1) {
                double radio = Animacion.interpolar(10, 34, Animacion.frenando(pulso));
                Graphics2D gp = (Graphics2D) g.create();
                gp.setColor(Estilo.conAlfa(p.getColor(), 0.67 * (1 - pulso)));
                gp.setStroke(new BasicStroke(3f));
                gp.draw(new Ellipse2D.Double(destino.getX() - radio, destino.getY() - radio, radio * 2, radio * 2));
                gp.dispose();
            }
        }
    }

    @Override
    public ListaDinamica<Personaje> getPersonajes() {
        return personajes;
    }

    @Override
    public ListaDinamica<Relacion> getRelaciones() {
        return juego.getGrafo(ronda).getRelaciones();
    }

    @Override
    public ListaDinamica<Personaje> getOrdenDeAparicion() {
        return ordenDeAparicion;
    }

    @Override
    public String getTextoDeGlobo(Personaje personaje) {
        return juego.getAfirmacion(ronda, personaje).getTexto(Estilo::nombre);
    }

    @Override
    public Boolean getMarca(Personaje personaje) {
        return marcas.de(personaje);
    }

    @Override
    public boolean tieneAviso(Personaje personaje) {
        return pista != null && pista.avisoPara(personaje) != null;
    }

    @Override
    public Personaje getSeleccionado() {
        return confirmando ? null : seleccionado;
    }

    @Override
    public ListaDinamica<Personaje> getRecorridoResaltado() {
        return pista == null ? new ListaDinamica<>() : pista.getRecorrido();
    }
}
