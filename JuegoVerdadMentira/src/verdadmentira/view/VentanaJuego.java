package verdadmentira.view;

import verdadmentira.domain.ArbolDecisiones;
import verdadmentira.domain.Personaje;
import verdadmentira.service.Juego;
import verdadmentira.view.component.Avatar;
import verdadmentira.view.component.Boton;
import verdadmentira.view.component.PanelArbol;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VentanaJuego extends JFrame {

    private final Map<Personaje, Boolean> marcas = new LinkedHashMap<>();
    private Timer animacion;
    private Juego juego;
    private int ronda;
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
        Boton empezar = new Boton("Empezar", Estilo.PRINCIPAL, true);
        empezar.addActionListener(e -> mostrarRonda());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        botones.setOpaque(false);
        botones.add(reglas);
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

    private void mostrarRonda() {
        pantallaActual = this::mostrarRonda;
        enInicio = false;
        partidaEnCurso = true;
        JPanel pantalla = pantalla("Ronda " + (ronda + 1) + " de " + Juego.RONDAS, null, Estilo.TINTA);
        PantallaRonda contenido = new PantallaRonda(juego, ronda, marcas,
                () -> {
                    juego.registrarDecisiones(ronda, marcas);
                    marcas.clear();
                    mostrarArboles();
                });
        pantalla.add(contenido, BorderLayout.CENTER);
        mostrar(pantalla);
    }

    private void mostrarArboles() {
        pantallaActual = this::mostrarArboles;
        boolean ultima = ronda == Juego.RONDAS - 1;
        JPanel pantalla = pantalla("Tus decisiones después de la ronda " + (ronda + 1),
                "Cada árbol es un personaje y cada nivel es una ronda: a la izquierda lo marcaste "
                        + "<b>mentiroso</b>, a la derecha <b>veraz</b>. El juego no te dice si acertaste.", Estilo.TINTA);

        pantalla.add(filaDeArboles(false), BorderLayout.CENTER);

        Boton seguir = new Boton(ultima ? "Elegir al que hace las dos cosas" : "Ir a la ronda " + (ronda + 2), Estilo.PRINCIPAL, true);
        seguir.addActionListener(e -> {
            if (ultima) {
                mostrarEleccion();
            } else {
                ronda++;
                mostrarRonda();
            }
        });
        pantalla.add(pie(null, seguir), BorderLayout.SOUTH);
        mostrar(pantalla);
    }

    private void mostrarEleccion() {
        pantallaActual = this::mostrarEleccion;
        JPanel pantalla = pantalla("¿Quién puede mentir y también decir la verdad?",
                "Es el único que cambió de una ronda a otra. Mirá tus árboles y elegilo.", Estilo.TINTA);
        pantalla.add(filaDeArboles(true), BorderLayout.CENTER);
        mostrar(pantalla);
    }

    private JPanel filaDeArboles(boolean conBotonDeEleccion) {
        JPanel columnas = new JPanel(new GridLayout(1, juego.getPersonajes().size(), 12, 0));
        columnas.setOpaque(false);
        for (Personaje p : juego.getPersonajes()) {
            JPanel columna = new JPanel(new BorderLayout(0, 10));
            columna.setOpaque(false);
            columna.add(tarjeta(PanelArbol.grande(p, juego.getArbol(p))), BorderLayout.CENTER);
            if (conBotonDeEleccion) {
                Boton elegir = new Boton("Es " + p.getNombre(), Estilo.colorDeNombre(p), false);
                elegir.addActionListener(e -> mostrarResultado(p));
                columna.add(elegir, BorderLayout.SOUTH);
            }
            columnas.add(columna);
        }
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(columnas, BorderLayout.NORTH);
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
            texto.setFont(Estilo.NEGRITA);
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
            mostrarRonda();
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
        setContentPane(pantalla);
        revalidate();
        repaint();
    }
}
