package juego.pantallas.componentes.dibujo;

import estructuras.Diccionario;
import estructuras.ListaDinamica;
import juego.pantallas.componentes.estilo.Animacion;
import juego.pantallas.componentes.estilo.Estilo;
import juego.pantallas.componentes.estilo.TextoRico;
import juego.reglas.personajes.Personaje;
import juego.reglas.ronda.Relacion;
import juego.reglas.ronda.TipoDeRelacion;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class DibujoDelDigrafo {
    private static final int MARGEN = 10;
    private static final int SEPARACION_GLOBO = 12;
    private static final int COLA = 10;
    private static final int ALTO_NOMBRE = 30;
    private static final double RADIO_DEL_HALO = 0.78;
    private static final double ESCALONADO_DE_FOTOS = 110;
    private static final double DURACION_DE_FOTO = 520;
    private static final double INICIO_DE_LA_CADENA = 700;
    private static final double PASO_DE_LA_CADENA = 420;
    private static final double DURACION_DE_FLECHA = 640;
    private static final double DURACION_DE_GLOBO = 320;
    private static final double DURACION_DEL_REALCE = 420;

    private enum Estado { APAGADA, NORMAL, ENTRANTE, SALIENTE }

    private enum Lado { ARRIBA, DERECHA, IZQUIERDA }

    private final DatosDelDigrafo datos;
    private final Rectangle zona = new Rectangle();
    private final Diccionario<Personaje, Rectangle> fotos = new Diccionario<>();
    private final Diccionario<Personaje, Rectangle> globos = new Diccionario<>();
    private final Diccionario<Personaje, Lado> lados = new Diccionario<>();
    private boolean ubicacionesVigentes;
    private double tiempo = Double.MAX_VALUE;
    private double opacidadDeLineas = 1;
    private boolean fotosOcultas;

    public DibujoDelDigrafo(DatosDelDigrafo datos) {
        this.datos = datos;
    }

    public void ubicar(int x, int y, int ancho, int alto) {
        zona.setBounds(x, y, ancho, alto);
        ubicacionesVigentes = false;
    }

    public void animarEntrada() {
        tiempo = 0;
    }

    public void actualizar(double dt) {
        if (tiempo < duracionDeLaEntrada()) tiempo += dt * 1000;
    }

    public boolean entradaEnCurso() {
        return tiempo < duracionDeLaEntrada();
    }

    public void completarEntrada() {
        tiempo = Double.MAX_VALUE;
    }

    public void setOpacidadDeLineas(double opacidad) {
        opacidadDeLineas = Math.max(0, Math.min(1, opacidad));
    }

    public void setFotosOcultas(boolean ocultas) {
        fotosOcultas = ocultas;
    }

    public void refrescarGlobos() {
        ubicacionesVigentes = false;
    }

    public Rectangle rectanguloDeFoto(Personaje p) {
        Rectangle foto = fotos.obtener(p);
        if (foto == null) return new Rectangle();
        return new Rectangle(foto.x, foto.y, foto.width, Avatar.alto(foto.width, false));
    }

    public Personaje personajeEn(int x, int y) {
        ListaDinamica<Personaje> personajes = datos.getPersonajes();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            Rectangle foto = fotos.obtener(p);
            Rectangle globo = globos.obtener(p);
            if (foto != null && (foto.contains(x, y) || globo.contains(x, y))) return p;
        }
        return null;
    }

    private double duracionDeLaEntrada() {
        return finDeLasFlechas() + DURACION_DEL_REALCE;
    }

    private double finDeLasFlechas() {
        return INICIO_DE_LA_CADENA + (datos.getPersonajes().tamanio() - 1) * PASO_DE_LA_CADENA + DURACION_DE_FLECHA;
    }

    private double avance(double desde, double duracion) {
        return Animacion.tramo(tiempo, desde, desde + duracion);
    }

    private double momentoDe(Personaje p) {
        return INICIO_DE_LA_CADENA + Math.max(0, datos.getOrdenDeAparicion().indiceDe(p)) * PASO_DE_LA_CADENA;
    }

    private static Graphics2D conOpacidad(Graphics2D g, double opacidad) {
        Graphics2D copia = (Graphics2D) g.create();
        copia.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, Math.min(1, opacidad))));
        return copia;
    }

    private void calcularUbicaciones(Graphics2D g) {
        if (ubicacionesVigentes) return;
        ubicacionesVigentes = true;
        fotos.vaciar();
        globos.vaciar();
        lados.vaciar();
        int w = zona.width;
        int h = zona.height;
        int anchoFoto = (int) Math.max(54, Math.min(80, Math.min(w, h) * 0.15));
        int altoFoto = Avatar.alto(anchoFoto, false) + ALTO_NOMBRE;
        int anchoGlobo = (int) Math.max(140, Math.min(205, w * 0.24));
        int anchoTexto = anchoGlobo - 26;
        ListaDinamica<Personaje> personajes = datos.getPersonajes();
        Diccionario<Personaje, Rectangle> tamanios = new Diccionario<>();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            String texto = datos.getTextoDeGlobo(p);
            int ancho = Math.min(anchoTexto, TextoRico.anchoNatural(g, texto, Estilo.TEXTO) + 4);
            int alto = TextoRico.alto(g, texto, Estilo.TEXTO, ancho);
            tamanios.poner(p, new Rectangle(0, 0, ancho + 26, alto + 16));
        }
        int altoGloboDeArriba = tamanios.obtener(personajes.obtener(0)).height + COLA;
        int margenInferior = (int) Math.ceil(Math.max(anchoFoto, altoFoto) * RADIO_DEL_HALO - altoFoto / 2.0) + MARGEN;
        double radioY = (h - MARGEN - margenInferior - altoFoto - SEPARACION_GLOBO - altoGloboDeArriba) / (1 + Math.sin(Math.toRadians(54)));
        radioY = Math.max(40, radioY);
        double cy = zona.y + MARGEN + altoGloboDeArriba + SEPARACION_GLOBO + altoFoto / 2.0 + radioY;
        double radioX = (w / 2.0 - MARGEN - anchoFoto / 2.0 - SEPARACION_GLOBO - COLA - anchoGlobo) / Math.cos(Math.toRadians(18));
        radioX = Math.max(60, Math.min(radioX, radioY * 2.4));
        double cx = zona.getCenterX();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            double angulo = Math.toRadians(-90 + i * 360.0 / personajes.tamanio());
            int x = (int) Math.round(cx + radioX * Math.cos(angulo) - anchoFoto / 2.0);
            int y = (int) Math.round(cy + radioY * Math.sin(angulo) - altoFoto / 2.0);
            Rectangle foto = new Rectangle(x, y, anchoFoto, altoFoto);
            fotos.poner(p, foto);
            Rectangle tamanio = tamanios.obtener(p);
            Lado lado;
            Rectangle globo;
            if (Math.abs(Math.cos(angulo)) < 0.3) {
                lado = Lado.ARRIBA;
                globo = new Rectangle((int) foto.getCenterX() - tamanio.width / 2, foto.y - SEPARACION_GLOBO - tamanio.height - COLA + 4, tamanio.width, tamanio.height);
            } else if (Math.cos(angulo) > 0) {
                lado = Lado.DERECHA;
                globo = new Rectangle(foto.x + foto.width + SEPARACION_GLOBO + COLA - 4, (int) foto.getCenterY() - tamanio.height / 2, tamanio.width, tamanio.height);
            } else {
                lado = Lado.IZQUIERDA;
                globo = new Rectangle(foto.x - SEPARACION_GLOBO - COLA + 4 - tamanio.width, (int) foto.getCenterY() - tamanio.height / 2, tamanio.width, tamanio.height);
            }
            globo.x = Math.max(zona.x, Math.min(globo.x, zona.x + w - globo.width - 1));
            globo.y = Math.max(zona.y, Math.min(globo.y, zona.y + h - globo.height - 1));
            globos.poner(p, globo);
            lados.poner(p, lado);
        }
    }

    public void dibujar(Graphics2D g, Personaje bajoElMouse) {
        Graphics2D g2 = (Graphics2D) g.create();
        Estilo.suavizar(g2);
        calcularUbicaciones(g2);
        double realce = avance(finDeLasFlechas(), DURACION_DEL_REALCE);
        Personaje seleccionado = datos.getSeleccionado();
        ListaDinamica<Relacion> relaciones = datos.getRelaciones();
        relaciones.ordenar((a, b) -> Integer.compare(estadoDe(a, seleccionado).ordinal(), estadoDe(b, seleccionado).ordinal()));
        for (int i = 0; i < relaciones.tamanio(); i++) {
            Relacion r = relaciones.obtener(i);
            double crecimiento = Animacion.suave(avance(momentoDe(r.getOrigen()), DURACION_DE_FLECHA));
            if (crecimiento > 0) dibujarArista(g2, r, estadoDe(r, seleccionado), crecimiento, Animacion.suave(realce));
        }
        ListaDinamica<Personaje> personajes = datos.getPersonajes();
        ListaDinamica<Personaje> resaltados = datos.getRecorridoResaltado();
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            Rectangle foto = fotos.obtener(p);
            if (p.equals(seleccionado) || p.equals(bajoElMouse)) {
                double radio = Math.max(foto.width, foto.height) * RADIO_DEL_HALO * Animacion.interpolar(0.6, 1, Animacion.frenando(realce));
                Graphics2D gh = conOpacidad(g2, realce * opacidadDeLineas);
                gh.setColor(Estilo.conAlfa(p.getColor(), p.equals(seleccionado) ? 0.35 : 0.18));
                gh.fill(new Ellipse2D.Double(foto.getCenterX() - radio, foto.getCenterY() - radio, radio * 2, radio * 2));
                gh.dispose();
            }
        }
        for (int i = 0; i < personajes.tamanio(); i++) {
            Personaje p = personajes.obtener(i);
            Rectangle foto = fotos.obtener(p);
            double aparicion = avance(i * ESCALONADO_DE_FOTOS, DURACION_DE_FOTO);
            if (aparicion <= 0) continue;
            if (!fotosOcultas) dibujarFoto(g2, p, foto, aparicion);
            int orden = resaltados.indiceDe(p);
            if (orden >= 0 && opacidadDeLineas > 0) dibujarPasoDePista(g2, foto, orden + 1);
            Graphics2D gn = conOpacidad(g2, Animacion.frenando(aparicion) * opacidadDeLineas);
            dibujarNombre(gn, p, foto, p.equals(seleccionado) && realce > 0.5);
            gn.dispose();
            double globo = Animacion.frenando(avance(momentoDe(p), DURACION_DE_GLOBO));
            if (globo > 0) {
                Graphics2D gg = conOpacidad(g2, globo * opacidadDeLineas);
                gg.translate(0, (1 - globo) * 10);
                dibujarGlobo(gg, p, p.equals(seleccionado) && realce > 0.5);
                gg.dispose();
            }
        }
        g2.dispose();
    }

    private void dibujarPasoDePista(Graphics2D g, Rectangle foto, int numero) {
        int lado = 22;
        int x = foto.x + foto.width - lado / 2;
        int y = foto.y - lado / 2;
        g.setColor(Estilo.AMARILLO);
        g.fill(new Ellipse2D.Double(x, y, lado, lado));
        g.setColor(Estilo.textoSobre(Estilo.AMARILLO));
        g.setFont(Estilo.CHICA_NEGRITA);
        FontMetrics medidas = g.getFontMetrics();
        String texto = String.valueOf(numero);
        g.drawString(texto, x + (lado - medidas.stringWidth(texto)) / 2, y + (lado + medidas.getAscent()) / 2 - 2);
    }

    private void dibujarFoto(Graphics2D g, Personaje p, Rectangle foto, double aparicion) {
        int alto = Avatar.alto(foto.width, false);
        double escala = Animacion.interpolar(0.55, 1, Animacion.conRebote(aparicion));
        Graphics2D gf = conOpacidad(g, Animacion.frenando(Math.min(1, aparicion * 1.6)));
        gf.translate(foto.getCenterX(), foto.y + alto / 2.0);
        gf.scale(escala, escala);
        gf.translate(-foto.width / 2.0, -alto / 2.0);
        Avatar.pintar(gf, p, foto.width, false, datos.getMarca(p));
        gf.dispose();
    }

    private void dibujarNombre(Graphics2D g, Personaje p, Rectangle foto, boolean seleccionado) {
        g.setFont(Estilo.NOMBRE.deriveFont(seleccionado ? 20f : 16f));
        FontMetrics medidas = g.getFontMetrics();
        String nombre = p.getNombre();
        int anchoTexto = medidas.stringWidth(nombre);
        double cx = foto.getCenterX();
        int base = foto.y + foto.height - ALTO_NOMBRE + 6 + (ALTO_NOMBRE - 6 + medidas.getAscent() - medidas.getDescent()) / 2;
        if (seleccionado) {
            int ancho = anchoTexto + 22;
            int alto = medidas.getHeight() + 4;
            g.setColor(p.getColor());
            g.fillRoundRect((int) (cx - ancho / 2.0), base - medidas.getAscent() - 2, ancho, alto, alto, alto);
            g.setColor(Estilo.textoSobre(p.getColor()));
        } else {
            g.setColor(Estilo.TINTA);
        }
        g.drawString(nombre, (float) (cx - anchoTexto / 2.0), base);
    }

    private void dibujarGlobo(Graphics2D g, Personaje p, boolean seleccionado) {
        Rectangle globo = globos.obtener(p);
        Rectangle foto = fotos.obtener(p);
        Area forma = new Area(new RoundRectangle2D.Double(globo.x, globo.y, globo.width, globo.height, 18, 18));
        Path2D cola = new Path2D.Double();
        Lado lado = lados.obtener(p);
        if (lado == Lado.ARRIBA) {
            double x = foto.getCenterX();
            cola.moveTo(x - 9, globo.getMaxY() - 2);
            cola.lineTo(x, foto.y - 2);
            cola.lineTo(x + 9, globo.getMaxY() - 2);
        } else if (lado == Lado.DERECHA) {
            double y = globo.getCenterY();
            cola.moveTo(globo.x + 2, y - 9);
            cola.lineTo(foto.getMaxX() + 2, foto.getCenterY());
            cola.lineTo(globo.x + 2, y + 9);
        } else {
            double y = globo.getCenterY();
            cola.moveTo(globo.getMaxX() - 2, y - 9);
            cola.lineTo(foto.x - 2, foto.getCenterY());
            cola.lineTo(globo.getMaxX() - 2, y + 9);
        }
        cola.closePath();
        forma.add(new Area(cola));
        g.setColor(Estilo.TARJETA);
        g.fill(forma);
        boolean conAviso = datos.tieneAviso(p);
        g.setColor(conAviso ? Estilo.texto(Estilo.ROJO) : seleccionado ? p.getColor() : Estilo.BORDE);
        g.setStroke(new BasicStroke(conAviso || seleccionado ? 2.6f : 1.4f));
        g.draw(forma);
        TextoRico.dibujar(g, datos.getTextoDeGlobo(p), Estilo.TEXTO, Estilo.TINTA, globo.x + 13, globo.y + 8, globo.width - 22);
    }

    private static Estado estadoDe(Relacion r, Personaje seleccionado) {
        if (seleccionado == null) return Estado.NORMAL;
        if (r.getOrigen().equals(seleccionado)) return Estado.SALIENTE;
        if (r.getDestino().equals(seleccionado)) return Estado.ENTRANTE;
        return Estado.APAGADA;
    }

    private void dibujarArista(Graphics2D g, Relacion r, Estado estado, double crecimiento, double realce) {
        Color color = r.getTipo() == TipoDeRelacion.DICE_QUE_DICE_LA_VERDAD ? Estilo.texto(Estilo.VERDE)
                : r.getTipo() == TipoDeRelacion.DICE_QUE_MIENTE ? Estilo.texto(Estilo.ROJO) : Estilo.TINTA_SUAVE;
        double opacidadFinal = estado == Estado.APAGADA ? 0.16 : estado == Estado.NORMAL ? 0.85 : 1;
        float grosorFinal = estado == Estado.SALIENTE ? 4.5f : estado == Estado.ENTRANTE ? 3f : 2.4f;
        double opacidad = Animacion.interpolar(0.9, opacidadFinal, realce) * opacidadDeLineas;
        if (opacidad <= 0) return;
        float grosor = (float) Animacion.interpolar(2.8, grosorFinal, realce);
        Graphics2D ga = conOpacidad(g, opacidad);
        ga.setColor(color);
        ga.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Rectangle desde = agrandar(fotos.obtener(r.getOrigen()), 5);
        if (r.esBucle()) {
            dibujarBucle(ga, desde, crecimiento, grosor);
            ga.dispose();
            return;
        }
        Rectangle hasta = agrandar(fotos.obtener(r.getDestino()), 7);
        Point2D p0 = new Point2D.Double(desde.getCenterX(), desde.getCenterY());
        Point2D p2 = new Point2D.Double(hasta.getCenterX(), hasta.getCenterY());
        double dx = p2.getX() - p0.getX();
        double dy = p2.getY() - p0.getY();
        double largo = Math.hypot(dx, dy);
        double curva = Math.min(45, largo * 0.12);
        Point2D control = new Point2D.Double((p0.getX() + p2.getX()) / 2 - dy / largo * curva, (p0.getY() + p2.getY()) / 2 + dx / largo * curva);
        double inicio = 0;
        while (inicio < 1 && desde.contains(Trazos.puntoDeCurva(p0, control, p2, inicio))) inicio += 0.005;
        double finCompleto = 1;
        while (finCompleto > inicio && hasta.contains(Trazos.puntoDeCurva(p0, control, p2, finCompleto))) finCompleto -= 0.005;
        double fin = inicio + (finCompleto - inicio) * crecimiento;
        if (estado == Estado.ENTRANTE && realce > 0.5) Trazos.curvaPunteada(ga, p0, control, p2, inicio, fin, 7, 6);
        else Trazos.curva(ga, p0, control, p2, inicio, fin);
        double largoDePunta = (8 + grosor * 1.6) * Animacion.tramo(crecimiento, 0, 0.15);
        if (largoDePunta > 0) {
            Trazos.punta(ga, Trazos.puntoDeCurva(p0, control, p2, Math.max(inicio, fin - 0.03)), Trazos.puntoDeCurva(p0, control, p2, fin), largoDePunta);
        }
        ga.dispose();
    }

    private void dibujarBucle(Graphics2D g, Rectangle foto, double crecimiento, float grosor) {
        double radio = Math.max(24, foto.width * 0.36);
        boolean haciaLaDerecha = foto.getCenterX() <= zona.getCenterX();
        double cx = haciaLaDerecha ? foto.getMaxX() : foto.getMinX();
        double cy = foto.getMinY();
        double inicio = haciaLaDerecha ? 270 : 0;
        double recorrido = 270 * crecimiento;
        g.draw(new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, inicio, recorrido, Arc2D.OPEN));
        double largoDePunta = (8 + grosor * 1.6) * Animacion.tramo(crecimiento, 0, 0.15);
        if (largoDePunta <= 0) return;
        Trazos.punta(g, puntoDelArco(cx, cy, radio, inicio + recorrido - 12), puntoDelArco(cx, cy, radio, inicio + recorrido), largoDePunta);
    }

    private static Point2D puntoDelArco(double cx, double cy, double radio, double grados) {
        double angulo = Math.toRadians(grados);
        return new Point2D.Double(cx + radio * Math.cos(angulo), cy - radio * Math.sin(angulo));
    }

    private static Rectangle agrandar(Rectangle r, int margen) {
        return new Rectangle(r.x - margen, r.y - margen, r.width + 2 * margen, r.height + 2 * margen);
    }
}
