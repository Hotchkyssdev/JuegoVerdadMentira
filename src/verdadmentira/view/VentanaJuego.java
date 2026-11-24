package verdadmentira.view;

import verdadmentira.domain.Afirmacion;
import verdadmentira.domain.ArbolDecisiones;
import verdadmentira.domain.Personaje;
import verdadmentira.service.Juego;
import verdadmentira.view.animacion.Animacion;
import verdadmentira.view.animacion.CapaDeTransicion;
import verdadmentira.view.component.Avatar;
import verdadmentira.view.component.Boton;
import verdadmentira.view.component.Burbuja;
import verdadmentira.view.component.PanelArbol;
import verdadmentira.view.component.PanelTransparente;
import verdadmentira.view.style.Estilo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VentanaJuego extends JFrame {
    private static final int PISTAS_POR_PARTIDA = 3;

    private static final int DURACION_DE_LA_SUBIDA = 950;
    private static final int ESCALONADO_DE_LA_SUBIDA = 70;

    private record SalidaDeRonda(BufferedImage pantalla, Rectangle ubicacionDePantalla,
                                 Map<Personaje, Rectangle> celdas, Map<Personaje, BufferedImage> imagenesDeCeldas) { }

    private record PasoDePractica(String texto, Boolean mcLovin, Boolean roz, Boolean lisa, boolean contradiccion) { }

    private final Map<Personaje, Boolean> marcas = new LinkedHashMap<>();
    private Timer animacion;
    private Juego juego;
    private int ronda;
    private int pistasRestantes;
    private Runnable pantallaActual;
    private boolean enInicio;
    private boolean partidaEnCurso;

    public VentanaJuego() {
        setTitle("Verdad o mentira");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Rectangle pantallaDisponible = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1280, pantallaDisponible.width), Math.min(860, pantallaDisponible.height));
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        if (pantallaDisponible.height < 860) setExtendedState(MAXIMIZED_BOTH);
        nuevaPartida();
    }

    private void nuevaPartida() {
        reiniciar();
        mostrarInicio();
    }

    private void reiniciar() {
        juego = new Juego();
        ronda = 0;
        pistasRestantes = PISTAS_POR_PARTIDA;
        marcas.clear();
    }

    private void volverAlInicio() {
        if (partidaEnCurso) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "Vas a dejar esta partida y empezar una nueva. ¿Volver al inicio?",
                    "Volver al inicio", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) return;
        }
        nuevaPartida();
    }

    private void mostrarReglas() {
        JPanel contenido = tarjeta(Estilo.reglas());
        JScrollPane desplazable = new JScrollPane(contenido);
        desplazable.setBorder(null);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        Dimension ideal = contenido.getPreferredSize();
        int altoMaximo = (int) (getHeight() * 0.7);
        desplazable.setPreferredSize(new Dimension(ideal.width + 24, Math.min(ideal.height + 4, altoMaximo)));
        JOptionPane.showMessageDialog(this, desplazable, "Cómo se juega", JOptionPane.PLAIN_MESSAGE);
    }

    private void cambiarTema() {
        Estilo.usarTemaOscuro(!Estilo.esOscuro());
        pantallaActual.run();
    }

    private void mostrarInicio() {
        pantallaActual = this::mostrarInicio;
        enInicio = true;
        partidaEnCurso = false;
        JPanel pantalla = new JPanel(new BorderLayout());
        pantalla.setBackground(Estilo.FONDO);
        pantalla.setBorder(new EmptyBorder(18, 28, 16, 28));

        Boton tema = new Boton(Estilo.esOscuro() ? "Modo claro" : "Modo oscuro", Estilo.TINTA_SUAVE, false);
        tema.addActionListener(e -> cambiarTema());
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        arriba.setOpaque(false);
        arriba.add(tema);
        pantalla.add(arriba, BorderLayout.NORTH);

        JLabel titulo = new JLabel("Verdad o mentira", SwingConstants.CENTER);
        titulo.setFont(Estilo.TITULO.deriveFont(44f));
        titulo.setForeground(Estilo.TINTA);
        JLabel subtitulo = new JLabel("Un juego de lógica: descubrí quién miente y quién es capaz de hacer las dos cosas.", SwingConstants.CENTER);
        subtitulo.setFont(Estilo.TEXTO.deriveFont(17f));
        subtitulo.setForeground(Estilo.TINTA_SUAVE);

        JPanel fotos = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        fotos.setOpaque(false);
        for (Personaje p : juego.getPersonajes()) fotos.add(new Avatar(p, 112, true));

        JLabel resumen = Estilo.resumen();
        JPanel tarjetaResumen = tarjeta(resumen);
        JPanel contenedorResumen = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contenedorResumen.setOpaque(false);
        contenedorResumen.add(tarjetaResumen);

        Boton reglas = new Boton("Cómo se juega", Estilo.TINTA_SUAVE, false);
        reglas.addActionListener(e -> mostrarReglas());
        Boton practicar = new Boton("Practicar primero", Estilo.TINTA_SUAVE, false);
        practicar.addActionListener(e -> mostrarPractica(0));
        Boton empezar = new Boton("Empezar", Estilo.PRINCIPAL, true);
        empezar.addActionListener(e -> mostrarRonda(true));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        botones.add(reglas);
        botones.add(practicar);
        botones.add(empezar);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{titulo, subtitulo, fotos, contenedorResumen, botones}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        titulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, titulo.getPreferredSize().height));
        subtitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, subtitulo.getPreferredSize().height));
        centro.add(Box.createVerticalGlue());
        centro.add(titulo);
        centro.add(Box.createVerticalStrut(6));
        centro.add(subtitulo);
        centro.add(Box.createVerticalStrut(24));
        centro.add(fotos);
        centro.add(Box.createVerticalStrut(24));
        centro.add(contenedorResumen);
        centro.add(Box.createVerticalStrut(24));
        centro.add(botones);
        centro.add(Box.createVerticalGlue());
        pantalla.add(centro, BorderLayout.CENTER);
        mostrar(pantalla);
    }

    private void mostrarPractica(int paso) {
        pantallaActual = () -> mostrarPractica(paso);
        enInicio = false;
        List<Personaje> personajes = juego.getPersonajes();
        Personaje mcLovin = personajes.get(0);
        Personaje roz = personajes.get(1);
        Personaje lisa = personajes.get(3);
        String m = Estilo.nombreHtml(mcLovin);
        String r = Estilo.nombreHtml(roz);
        String l = Estilo.nombreHtml(lisa);
        PasoDePractica[] pasos = {
                new PasoDePractica("Acá hay tres personajes y cada uno dice una frase. Hay una sola forma de marcarlos "
                        + "en la que todas las frases cierran. Vamos a encontrarla paso a paso.", null, null, null, false),
                new PasoDePractica("Probemos una suposición: <b>" + m + " miente</b>.", false, null, null, false),
                new PasoDePractica("Si " + m + " miente, su frase es falsa. Dijo «" + r + " dice la verdad», "
                        + "así que <b>" + r + " miente</b>.", false, false, null, false),
                new PasoDePractica(r + " miente, así que su frase también es falsa. Dijo «" + l + " miente», "
                        + "entonces <b>" + l + " dice la verdad</b>.", false, false, true, false),
                new PasoDePractica(l + " dice la verdad, así que su frase es cierta: «" + m + " dice la verdad, pero " + r + " no». "
                        + "¡Pero habíamos supuesto que " + m + " miente! Es una <b>contradicción</b>: la suposición estaba mal.",
                        false, false, true, true),
                new PasoDePractica("Entonces <b>" + m + " dice la verdad</b>. Su frase es cierta, así que "
                        + "<b>" + r + " dice la verdad</b>.", true, true, null, false),
                new PasoDePractica(r + " dice la verdad, así que «" + l + " miente» es cierto: <b>" + l + " miente</b>.",
                        true, true, false, false),
                new PasoDePractica("Comprobemos: " + l + " miente, así que su frase tiene que ser falsa. «" + m + " dice la verdad, pero "
                        + r + " no» es falsa, porque " + r + " dice la verdad. <b>Todo cierra.</b><br><br>"
                        + "En el juego son cinco personajes y tres rondas, y uno de ellos cambia de una ronda a otra: "
                        + "encontrarlo es el objetivo.", true, true, false, false)
        };
        PasoDePractica actual = pasos[paso];

        JPanel pantalla = pantalla("Práctica", "Paso " + (paso + 1) + " de " + pasos.length
                + ". Así se razona una ronda: se supone algo y se sigue hasta que todo cierre o aparezca una contradicción.", Estilo.TINTA);

        Afirmacion fraseDeMcLovin = new Afirmacion(mcLovin, List.of(new Afirmacion.Referencia(roz, true)));
        Afirmacion fraseDeRoz = new Afirmacion(roz, List.of(new Afirmacion.Referencia(lisa, false)));
        Afirmacion fraseDeLisa = new Afirmacion(lisa, List.of(
                new Afirmacion.Referencia(mcLovin, true), new Afirmacion.Referencia(roz, false)));

        JPanel columnas = new JPanel(new GridLayout(1, 3, 24, 0));
        columnas.setOpaque(false);
        Afirmacion[] frases = {fraseDeMcLovin, fraseDeRoz, fraseDeLisa};
        Boolean[] marcasDelPaso = {actual.mcLovin(), actual.roz(), actual.lisa()};
        for (int i = 0; i < frases.length; i++) {
            Burbuja burbuja = new Burbuja(frases[i]);
            if (actual.contradiccion() && frases[i] == fraseDeLisa) burbuja.setAviso("Esto contradice lo que supusimos.");
            Avatar avatar = new Avatar(frases[i].getEmisor(), 110, true);
            avatar.setMarca(marcasDelPaso[i]);
            JPanel contenedorAvatar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            contenedorAvatar.setOpaque(false);
            contenedorAvatar.add(avatar);
            JPanel columna = new JPanel(new BorderLayout(0, 4));
            columna.setOpaque(false);
            columna.add(burbuja, BorderLayout.NORTH);
            columna.add(contenedorAvatar, BorderLayout.CENTER);
            columnas.add(columna);
        }

        JLabel explicacion = new JLabel("<html><div style='width:900px'>" + actual.texto() + "</div></html>");
        explicacion.setFont(Estilo.FRASE);
        explicacion.setForeground(Estilo.TINTA);

        JPanel centro = new JPanel(new BorderLayout(0, 24));
        centro.setOpaque(false);
        centro.add(columnas, BorderLayout.NORTH);
        centro.add(tarjeta(explicacion), BorderLayout.CENTER);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(centro, BorderLayout.NORTH);
        pantalla.add(envoltorio, BorderLayout.CENTER);

        Boton anterior = new Boton("Anterior", Estilo.TINTA_SUAVE, false);
        anterior.setEnabled(paso > 0);
        anterior.addActionListener(e -> mostrarPractica(paso - 1));
        boolean ultimo = paso == pasos.length - 1;
        Boton siguiente = new Boton(ultimo ? "Empezar a jugar" : "Siguiente", Estilo.PRINCIPAL, true);
        siguiente.addActionListener(e -> {
            if (ultimo) mostrarRonda(true);
            else mostrarPractica(paso + 1);
        });
        pantalla.add(pie(null, anterior, siguiente), BorderLayout.SOUTH);
        mostrar(pantalla);
    }

    private void mostrarRonda(boolean conEntrada) {
        pantallaActual = () -> mostrarRonda(false);
        enInicio = false;
        partidaEnCurso = true;
        JPanel pantalla = pantalla("Ronda " + (ronda + 1) + " de " + Juego.RONDAS, null, Estilo.TINTA);
        PantallaRonda[] contenido = new PantallaRonda[1];
        contenido[0] = new PantallaRonda(juego, ronda, marcas,
                () -> pistasRestantes,
                () -> pistasRestantes--,
                () -> contenido[0].animarConfirmacion(() -> {
                    juego.registrarDecisiones(ronda, marcas);
                    marcas.clear();
                    mostrarArboles(salidaDe(contenido[0]));
                }));
        pantalla.add(contenido[0], BorderLayout.CENTER);
        mostrar(pantalla);
        if (conEntrada) contenido[0].animarEntrada();
    }

    private SalidaDeRonda salidaDe(PantallaRonda contenido) {
        CapaDeTransicion capa = CapaDeTransicion.de(getRootPane());
        Map<Personaje, Rectangle> celdas = new HashMap<>();
        Map<Personaje, BufferedImage> imagenes = new HashMap<>();
        contenido.getCeldasDeArbol().forEach((p, celda) -> {
            celdas.put(p, capa.ubicacionDe(celda));
            imagenes.put(p, capa.fotografiar(celda));
        });
        JComponent pantalla = (JComponent) getContentPane();
        return new SalidaDeRonda(contenido.fotografiarSinCeldas(capa, pantalla), capa.ubicacionDe(pantalla), celdas, imagenes);
    }

    private void mostrarArboles(SalidaDeRonda salida) {
        pantallaActual = () -> mostrarArboles(null);
        boolean ultima = ronda == Juego.RONDAS - 1;
        JPanel pantalla = pantalla("Tus decisiones después de la ronda " + (ronda + 1),
                "Cada árbol es un personaje y cada nivel es una ronda: a la izquierda lo marcaste "
                        + "<b>mentiroso</b>, a la derecha <b>veraz</b>. El juego no te dice si acertaste.", Estilo.TINTA);

        Map<Personaje, PanelTransparente> tarjetas = new HashMap<>();
        pantalla.add(filaDeArboles(false, tarjetas), BorderLayout.CENTER);

        Boton seguir = new Boton(ultima ? "Elegir al que hace las dos cosas" : "Ir a la ronda " + (ronda + 2), Estilo.PRINCIPAL, true);
        seguir.addActionListener(e -> {
            if (ultima) {
                mostrarEleccion();
            } else {
                ronda++;
                mostrarRonda(true);
            }
        });
        pantalla.add(pie(null, seguir), BorderLayout.SOUTH);
        mostrar(pantalla);
        if (salida != null) animarSubida(salida, tarjetas);
    }

    private void animarSubida(SalidaDeRonda salida, Map<Personaje, PanelTransparente> tarjetas) {
        validate();
        CapaDeTransicion capa = CapaDeTransicion.de(getRootPane());
        Map<Personaje, Rectangle> destinos = new HashMap<>();
        Map<Personaje, BufferedImage> imagenesDestino = new HashMap<>();
        tarjetas.forEach((p, tarjeta) -> {
            destinos.put(p, capa.ubicacionDe(tarjeta));
            imagenesDestino.put(p, capa.fotografiar((Component) tarjeta.getComponent(0)));
            tarjeta.setOpacidad(0);
        });
        List<Personaje> personajes = juego.getPersonajes();
        double duracion = DURACION_DE_LA_SUBIDA + (personajes.size() - 1) * ESCALONADO_DE_LA_SUBIDA;
        Animacion.iniciar((int) duracion, t -> {
            double tiempo = t * duracion;
            capa.mostrar(g -> {
                double anterior = 1 - Animacion.suave(Animacion.tramo(t, 0, 0.55));
                if (anterior > 0) dibujarImagen(g, salida.pantalla(), salida.ubicacionDePantalla(), anterior, false);
                for (int i = 0; i < personajes.size(); i++) {
                    Personaje p = personajes.get(i);
                    double inicio = i * ESCALONADO_DE_LA_SUBIDA;
                    double avance = Animacion.suave(Animacion.tramo(tiempo, inicio, inicio + DURACION_DE_LA_SUBIDA));
                    Rectangle desde = salida.celdas().get(p);
                    Rectangle hasta = destinos.get(p);
                    Rectangle actual = new Rectangle(
                            (int) Math.round(Animacion.interpolar(desde.x, hasta.x, avance)),
                            (int) Math.round(Animacion.interpolar(desde.y, hasta.y, avance)),
                            (int) Math.round(Animacion.interpolar(desde.width, hasta.width, avance)),
                            (int) Math.round(Animacion.interpolar(desde.height, hasta.height, avance)));
                    g.setColor(Estilo.TARJETA);
                    g.fill(actual);
                    g.setColor(Estilo.BORDE);
                    g.draw(new Rectangle(actual.x, actual.y, actual.width - 1, actual.height - 1));
                    dibujarImagen(g, salida.imagenesDeCeldas().get(p), actual, 1 - Animacion.tramo(avance, 0, 0.5), true);
                    dibujarImagen(g, imagenesDestino.get(p), actual, Animacion.tramo(avance, 0.35, 1), true);
                }
            });
        }, () -> {
            tarjetas.values().forEach(tarjeta -> tarjeta.setOpacidad(1));
            capa.terminar();
        });
    }

    private static void dibujarImagen(Graphics2D g, BufferedImage imagen, Rectangle zona, double opacidad, boolean ajustar) {
        if (opacidad <= 0 || imagen == null) return;
        Graphics2D gi = (Graphics2D) g.create();
        gi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(1, opacidad)));
        gi.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (!ajustar) {
            gi.drawImage(imagen, zona.x, zona.y, zona.width, zona.height, null);
        } else {
            double proporcion = (double) imagen.getWidth() / imagen.getHeight();
            double ancho = zona.width;
            double alto = ancho / proporcion;
            if (alto > zona.height) {
                alto = zona.height;
                ancho = alto * proporcion;
            }
            gi.drawImage(imagen, (int) Math.round(zona.getCenterX() - ancho / 2), (int) Math.round(zona.getCenterY() - alto / 2),
                    (int) Math.round(ancho), (int) Math.round(alto), null);
        }
        gi.dispose();
    }

    private void mostrarEleccion() {
        pantallaActual = this::mostrarEleccion;
        JPanel pantalla = pantalla("¿Quién puede mentir y también decir la verdad?",
                "Es el único que cambió de una ronda a otra. Mirá tus árboles y elegilo.", Estilo.TINTA);
        pantalla.add(filaDeArboles(true, null), BorderLayout.CENTER);
        mostrar(pantalla);
    }

    private JPanel filaDeArboles(boolean conBotonDeEleccion, Map<Personaje, PanelTransparente> tarjetas) {
        JPanel columnas = new JPanel(new GridLayout(1, juego.getPersonajes().size(), 12, 0));
        columnas.setOpaque(false);
        for (Personaje p : juego.getPersonajes()) {
            JPanel columna = new JPanel(new BorderLayout(0, 10));
            columna.setOpaque(false);
            PanelTransparente tarjeta = new PanelTransparente(tarjeta(PanelArbol.grande(p, juego.getArbol(p))));
            if (tarjetas != null) tarjetas.put(p, tarjeta);
            columna.add(tarjeta, BorderLayout.CENTER);
            if (conBotonDeEleccion) {
                Boton elegir = new Boton("Es " + p.getNombre(), Estilo.colorDeNombre(p), false);
                elegir.addActionListener(e -> mostrarResultado(p));
                columna.add(elegir, BorderLayout.SOUTH);
            }
            columnas.add(columna);
        }
        if (conBotonDeEleccion) {
            JPanel centro = new JPanel(new BorderLayout());
            centro.setOpaque(false);
            centro.add(columnas, BorderLayout.NORTH);
            return centro;
        }
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.weightx = 1;
        centro.add(columnas, restricciones);
        return centro;
    }

    private void mostrarResultado(Personaje elegido) {
        pantallaActual = () -> mostrarResultado(elegido);
        partidaEnCurso = false;
        Personaje ambos = juego.getPersonajeAmbos();
        boolean gano = elegido == ambos;
        String detalle = gano
                ? "Lo encontraste: el que podía hacer las dos cosas era " + Estilo.nombreHtml(ambos) + "."
                : "Elegiste a " + Estilo.nombreHtml(elegido) + ", pero el que podía hacer las dos cosas era " + Estilo.nombreHtml(ambos) + ".";
        JPanel pantalla = pantalla(gano ? "¡Ganaste!" : "Perdiste", detalle, Estilo.texto(gano ? Estilo.VERDE : Estilo.ROJO));

        ArbolDecisiones loQueHizo = new ArbolDecisiones(ambos.getNombre());
        for (int r = 0; r < Juego.RONDAS; r++) loQueHizo.agregarRonda(juego.dijoLaVerdad(r, ambos));
        JPanel comparacion = new JPanel(new GridLayout(1, 2, 12, 0));
        comparacion.setOpaque(false);
        comparacion.add(arbolConTitulo("Lo que marcaste", PanelArbol.grande(ambos, juego.getArbol(ambos))));
        comparacion.add(arbolConTitulo("Lo que hizo en realidad", PanelArbol.grande(ambos, loQueHizo)));

        JPanel tabla = new JPanel(new GridLayout(0, 2 + Juego.RONDAS, 2, 2));
        tabla.setBackground(Estilo.BORDE);
        tabla.setBorder(BorderFactory.createLineBorder(Estilo.BORDE, 2));
        tabla.add(encabezado("Personaje"));
        tabla.add(encabezado("Qué era"));
        for (int r = 0; r < Juego.RONDAS; r++) tabla.add(encabezado("Ronda " + (r + 1)));

        List<List<Runnable>> revelaciones = new ArrayList<>();
        for (int r = 0; r < Juego.RONDAS; r++) revelaciones.add(new ArrayList<>());
        int[] aciertos = new int[Juego.RONDAS];
        for (Personaje p : juego.getPersonajes()) {
            JPanel nombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            nombre.setBackground(Estilo.TARJETA);
            nombre.add(new Avatar(p, 40, false));
            JLabel texto = new JLabel(p.getNombre());
            texto.setFont(Estilo.NOMBRE);
            texto.setForeground(Estilo.TINTA);
            nombre.add(texto);
            tabla.add(nombre);
            tabla.add(celda(p.descripcionReal(), p == ambos ? Estilo.AMARILLO_CLARO : Estilo.TARJETA));
            for (int r = 0; r < Juego.RONDAS; r++) {
                boolean marcado = juego.getDecision(r, p);
                boolean real = juego.dijoLaVerdad(r, p);
                if (marcado == real) aciertos[r]++;
                String contenido = "Marcaste: <b>" + comoTexto(marcado) + "</b><br>Era: <b>" + comoTexto(real) + "</b>";
                JLabel celda = celda("?", Estilo.TARJETA);
                celda.setForeground(Estilo.TINTA_SUAVE);
                tabla.add(celda);
                Color fondo = marcado == real ? Estilo.VERDE_CLARO : Estilo.ROJO_CLARO;
                revelaciones.get(r).add(() -> {
                    celda.setText("<html><div style='text-align:center'>" + contenido + "</div></html>");
                    celda.setForeground(Estilo.TINTA);
                    celda.setBackground(fondo);
                });
            }
        }
        tabla.add(encabezado("Aciertos"));
        tabla.add(encabezado(""));
        for (int r = 0; r < Juego.RONDAS; r++) {
            JLabel total = encabezado("?");
            int cantidad = aciertos[r];
            revelaciones.get(r).add(() -> total.setText(cantidad + " de " + juego.getPersonajes().size()));
            tabla.add(total);
        }

        JPanel envoltorioTabla = new JPanel(new BorderLayout());
        envoltorioTabla.setOpaque(false);
        envoltorioTabla.add(tabla, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(18, 0));
        centro.setOpaque(false);
        JPanel envoltorioComparacion = new JPanel(new BorderLayout());
        envoltorioComparacion.setOpaque(false);
        envoltorioComparacion.add(comparacion, BorderLayout.NORTH);
        envoltorioComparacion.setPreferredSize(new Dimension(460, 0));
        centro.add(envoltorioComparacion, BorderLayout.WEST);
        centro.add(envoltorioTabla, BorderLayout.CENTER);
        pantalla.add(centro, BorderLayout.CENTER);

        Boton inicio = new Boton("Volver al inicio", Estilo.TINTA_SUAVE, false);
        inicio.addActionListener(e -> nuevaPartida());
        Boton otra = new Boton("Jugar de nuevo", Estilo.PRINCIPAL, true);
        otra.addActionListener(e -> {
            reiniciar();
            mostrarRonda(true);
        });
        pantalla.add(pie(null, inicio, otra), BorderLayout.SOUTH);
        mostrar(pantalla);

        int[] siguiente = {0};
        animacion = new Timer(750, e -> {
            if (siguiente[0] >= revelaciones.size()) {
                ((Timer) e.getSource()).stop();
                return;
            }
            revelaciones.get(siguiente[0]).forEach(Runnable::run);
            siguiente[0]++;
        });
        animacion.setInitialDelay(500);
        animacion.start();
    }

    private static JPanel arbolConTitulo(String titulo, PanelArbol arbol) {
        JLabel etiqueta = new JLabel(titulo, SwingConstants.CENTER);
        etiqueta.setFont(Estilo.NEGRITA);
        etiqueta.setForeground(Estilo.TINTA_SUAVE);
        JPanel columna = new JPanel(new BorderLayout(0, 6));
        columna.setOpaque(false);
        columna.add(etiqueta, BorderLayout.NORTH);
        columna.add(tarjeta(arbol), BorderLayout.CENTER);
        return columna;
    }

    private static String comoTexto(boolean diceVerdad) {
        return diceVerdad ? "dice la verdad" : "miente";
    }

    private JPanel pantalla(String titulo, String subtitulo, Color colorTitulo) {
        JPanel pantalla = new JPanel(new BorderLayout(0, 14));
        pantalla.setBackground(Estilo.FONDO);
        pantalla.setBorder(new EmptyBorder(18, 28, 16, 28));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(titulo);
        t.setFont(Estilo.TITULO);
        t.setForeground(colorTitulo);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(t);
        if (subtitulo != null) {
            JLabel s = new JLabel("<html>" + subtitulo + "</html>");
            s.setFont(Estilo.TEXTO);
            s.setForeground(Estilo.TINTA_SUAVE);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            textos.add(Box.createVerticalStrut(6));
            textos.add(s);
        }

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        if (!enInicio) {
            Boton inicio = new Boton("Inicio", Estilo.TINTA_SUAVE, false);
            inicio.addActionListener(e -> volverAlInicio());
            botones.add(inicio);
            Boton reglas = new Boton("Cómo se juega", Estilo.TINTA_SUAVE, false);
            reglas.addActionListener(e -> mostrarReglas());
            botones.add(reglas);
        }
        Boton tema = new Boton(Estilo.esOscuro() ? "Modo claro" : "Modo oscuro", Estilo.TINTA_SUAVE, false);
        tema.addActionListener(e -> cambiarTema());
        botones.add(tema);

        JPanel cabecera = new JPanel(new BorderLayout(16, 0));
        cabecera.setOpaque(false);
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(botones, BorderLayout.EAST);
        pantalla.add(cabecera, BorderLayout.NORTH);
        return pantalla;
    }

    private static JPanel tarjeta(JComponent contenido) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Estilo.TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                new EmptyBorder(14, 16, 14, 16)));
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    private static JPanel pie(JComponent izquierda, JComponent... botones) {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        if (izquierda != null) pie.add(izquierda, BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        for (JComponent b : botones) derecha.add(b);
        pie.add(derecha, BorderLayout.EAST);
        return pie;
    }

    private static JLabel encabezado(String texto) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(Estilo.ENCABEZADO);
        etiqueta.setForeground(Estilo.textoSobre(Estilo.ENCABEZADO));
        etiqueta.setFont(Estilo.NEGRITA);
        etiqueta.setBorder(new EmptyBorder(8, 8, 8, 8));
        return etiqueta;
    }

    private static JLabel celda(String html, Color fondo) {
        JLabel etiqueta = new JLabel("<html><div style='text-align:center'>" + html + "</div></html>", SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(fondo);
        etiqueta.setForeground(Estilo.TINTA);
        etiqueta.setFont(Estilo.TEXTO);
        etiqueta.setBorder(new EmptyBorder(6, 8, 6, 8));
        return etiqueta;
    }

    private void mostrar(JComponent pantalla) {
        if (animacion != null) animacion.stop();
        CapaDeTransicion.quitar(getRootPane());
        setContentPane(pantalla);
        revalidate();
        repaint();
    }
}
