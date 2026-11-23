package verdadmentira.view;

import verdadmentira.domain.Afirmacion;
import verdadmentira.domain.GrafoPersonajes;
import verdadmentira.domain.Personaje;
import verdadmentira.service.Juego;
import verdadmentira.view.component.Avatar;
import verdadmentira.view.component.Boton;
import verdadmentira.view.component.Burbuja;
import verdadmentira.view.component.IconoDePeligro;
import verdadmentira.view.component.PanelArbol;
import verdadmentira.view.component.PanelDigrafo;
import verdadmentira.view.style.Estilo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

public class PantallaRonda extends JPanel {
    private static final int ANCHO_DETALLE = 350;
    private static final int ANCHO_TEXTO_DETALLE = 255;

    private final Juego juego;
    private final int ronda;
    private final Map<Personaje, Boolean> marcas;
    private final Deque<Map<Personaje, Boolean>> historial = new ArrayDeque<>();
    private final Boton deshacer = new Boton("Deshacer", Estilo.TINTA_SUAVE, false);
    private final Boton borrarMarcas = new Boton("Deshacer todo", IconoDePeligro.AMARILLO, false);
    private final Map<Personaje, String> avisos = new HashMap<>();
    private final Map<Personaje, Afirmacion> afirmaciones = new LinkedHashMap<>();
    private final Map<Personaje, PanelArbol> arboles = new HashMap<>();
    private final Map<Personaje, JPanel> celdasDeArbol = new HashMap<>();
    private final Map<Personaje, JLabel> nombresDeArbol = new HashMap<>();
    private final IntSupplier pistasRestantes;
    private final Runnable gastarPista;
    private final PanelDigrafo digrafo;
    private final JPanel detalle = new PanelQueSigueElAncho();
    private final JLabel contador = new JLabel();
    private final JLabel mensaje = new JLabel(" ");
    private final Boton confirmar = new Boton("Confirmar ronda", Estilo.PRINCIPAL, true);
    private final Boton pista;
    private Personaje seleccionado;

    public PantallaRonda(Juego juego, int ronda, Map<Personaje, Boolean> marcas,
                         IntSupplier pistasRestantes,
                         Runnable gastarPista, Runnable alConfirmar) {
        super(new BorderLayout(0, 12));
        this.juego = juego;
        this.ronda = ronda;
        this.marcas = marcas;
        this.pistasRestantes = pistasRestantes;
        this.gastarPista = gastarPista;
        setOpaque(false);
        for (Afirmacion a : juego.getAfirmaciones(ronda)) afirmaciones.put(a.getEmisor(), a);
        seleccionado = afirmaciones.values().stream()
                .filter(Afirmacion::esHechoVisible)
                .map(Afirmacion::getEmisor)
                .filter(p -> !marcas.containsKey(p))
                .findFirst()
                .orElseGet(() -> getPersonajes().stream().filter(p -> !marcas.containsKey(p))
                        .findFirst().orElse(getPersonajes().get(0)));

        digrafo = new PanelDigrafo(this);
        detalle.setOpaque(false);
        detalle.setLayout(new BoxLayout(detalle, BoxLayout.Y_AXIS));
        JScrollPane desplazable = new JScrollPane(detalle);
        desplazable.setBorder(null);
        desplazable.setOpaque(false);
        desplazable.getViewport().setOpaque(false);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        JPanel tarjetaDetalle = tarjeta(desplazable);
        tarjetaDetalle.setPreferredSize(new Dimension(ANCHO_DETALLE, 0));

        JPanel centro = new JPanel(new BorderLayout(16, 0));
        centro.setOpaque(false);
        centro.add(digrafo, BorderLayout.CENTER);
        centro.add(tarjetaDetalle, BorderLayout.EAST);
        add(centro, BorderLayout.CENTER);

        JPanel franja = new JPanel(new GridLayout(1, getPersonajes().size(), 10, 0));
        franja.setOpaque(false);
        for (Personaje p : getPersonajes()) franja.add(celdaDeArbol(p));

        contador.setFont(Estilo.TEXTO);
        contador.setForeground(Estilo.TINTA_SUAVE);
        mensaje.setFont(Estilo.NEGRITA);
        borrarMarcas.setColorDeTexto(Estilo.TINTA);
        borrarMarcas.setIconoAlFinal(new IconoDePeligro(18));
        JPanel textosDelPie = new JPanel();
        textosDelPie.setOpaque(false);
        textosDelPie.setLayout(new BoxLayout(textosDelPie, BoxLayout.Y_AXIS));
        textosDelPie.add(contador);
        textosDelPie.add(mensaje);
        JPanel izquierda = new JPanel(new BorderLayout(16, 0));
        izquierda.setOpaque(false);
        JPanel contenedorBorrar = new JPanel(new GridBagLayout());
        contenedorBorrar.setOpaque(false);
        contenedorBorrar.add(borrarMarcas);
        izquierda.add(contenedorBorrar, BorderLayout.WEST);
        izquierda.add(textosDelPie, BorderLayout.CENTER);

        pista = new Boton(textoDePista(), Estilo.TINTA_SUAVE, false);
        pista.addActionListener(e -> usarPista());
        deshacer.addActionListener(e -> deshacer());
        borrarMarcas.addActionListener(e -> borrarMarcas());
        confirmar.addActionListener(e -> alConfirmar.run());
        JPanel herramientas = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        herramientas.setOpaque(false);
        herramientas.add(pista);
        digrafo.setHerramientas(herramientas);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(deshacer);
        botones.add(confirmar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(izquierda, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);

        JPanel sur = new JPanel();
        sur.setOpaque(false);
        sur.setLayout(new BoxLayout(sur, BoxLayout.Y_AXIS));
        franja.setAlignmentX(Component.LEFT_ALIGNMENT);
        pie.setAlignmentX(Component.LEFT_ALIGNMENT);
        sur.add(franja);
        sur.add(Box.createVerticalStrut(12));
        sur.add(pie);
        add(sur, BorderLayout.SOUTH);

        refrescar();
    }

    public List<Personaje> getPersonajes() {
        return juego.getPersonajes();
    }

    public Afirmacion getAfirmacion(Personaje p) {
        return afirmaciones.get(p);
    }

    public GrafoPersonajes getGrafo() {
        return juego.getGrafo(ronda);
    }

    public Boolean getMarca(Personaje p) {
        return marcas.get(p);
    }

    public String getAviso(Personaje p) {
        return avisos.get(p);
    }

    public Personaje getSeleccionado() {
        return seleccionado;
    }

    public void seleccionar(Personaje p) {
        seleccionado = p;
        refrescar();
    }

    private void marcar(Personaje p, boolean veraz) {
        historial.push(new HashMap<>(marcas));
        if (Objects.equals(marcas.get(p), veraz)) marcas.remove(p);
        else marcas.put(p, veraz);
        limpiarPista();
        refrescar();
    }

    private void deshacer() {
        if (historial.isEmpty()) return;
        marcas.clear();
        marcas.putAll(historial.pop());
        limpiarPista();
        refrescar();
    }

    private void borrarMarcas() {
        if (marcas.isEmpty()) return;
        historial.push(new HashMap<>(marcas));
        marcas.clear();
        limpiarPista();
        refrescar();
    }

    private void limpiarPista() {
        avisos.clear();
        mensaje.setText(" ");
    }

    private void moverSeleccion(int paso) {
        List<Personaje> personajes = getPersonajes();
        int indice = personajes.indexOf(seleccionado);
        seleccionar(personajes.get(Math.floorMod(indice + paso, personajes.size())));
    }

    private void refrescar() {
        int total = getPersonajes().size();
        contador.setText(marcas.size() == total
                ? "Marcaste a los " + total + ". Revisá que todo cierre y confirmá."
                : "Marcaste " + marcas.size() + " de " + total + ".");
        deshacer.setEnabled(!historial.isEmpty());
        borrarMarcas.setEnabled(!marcas.isEmpty());
        confirmar.setEnabled(marcas.size() == total);
        pista.setText(textoDePista());
        pista.setEnabled(pistasRestantes.getAsInt() > 0);
        pista.revalidate();
        for (Personaje p : getPersonajes()) {
            arboles.get(p).setPendiente(getMarca(p));
            boolean elegido = p == seleccionado;
            JLabel nombreDeArbol = nombresDeArbol.get(p);
            nombreDeArbol.setFont(Estilo.NOMBRE.deriveFont(elegido ? 18f : 15f));
            nombreDeArbol.setForeground(elegido ? Estilo.TINTA : Estilo.TINTA_SUAVE);
            celdasDeArbol.get(p).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(elegido ? p.getColor() : Estilo.BORDE, elegido ? 3 : 1),
                    new EmptyBorder(elegido ? 4 : 6, 6, elegido ? 4 : 6, 6)));
        }
        armarDetalle();
        digrafo.repaint();
    }

    private JPanel celdaDeArbol(Personaje p) {
        PanelArbol arbol = PanelArbol.compacto(p, juego.getArbol(p));
        arboles.put(p, arbol);
        JLabel nombre = new JLabel(p.getNombre(), JLabel.CENTER);
        nombre.setFont(Estilo.NOMBRE.deriveFont(13f));
        nombre.setForeground(Estilo.TINTA_SUAVE);
        nombresDeArbol.put(p, nombre);
        JPanel celda = new JPanel(new BorderLayout(0, 2));
        celda.setBackground(Estilo.TARJETA);
        celda.add(arbol, BorderLayout.CENTER);
        celda.add(nombre, BorderLayout.SOUTH);
        celda.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        celda.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionar(p);
            }
        });
        celdasDeArbol.put(p, celda);
        return celda;
    }

    private void armarDetalle() {
        detalle.removeAll();
        Personaje p = seleccionado;
        Afirmacion a = afirmaciones.get(p);

        Avatar avatar = new Avatar(p, 58, false);
        avatar.setMarca(getMarca(p));
        JLabel nombre = new JLabel(p.getNombre());
        nombre.setFont(Estilo.NOMBRE.deriveFont(26f));
        nombre.setForeground(Estilo.colorDeNombre(p));
        Boolean marca = getMarca(p);
        String queDice = Boolean.TRUE.equals(marca) ? "dice la verdad" : "miente";
        String textoDeEstado;
        if (marca == null) textoDeEstado = "Sin marcar";
        else textoDeEstado = "Lo marcaste: " + queDice;
        JLabel estado = new JLabel(textoDeEstado);
        estado.setFont(Estilo.CHICA_NEGRITA);
        estado.setForeground(marca == null ? Estilo.TINTA_SUAVE : Estilo.texto(Estilo.colorDeMarca(marca)));
        JPanel textosCabecera = new JPanel();
        textosCabecera.setOpaque(false);
        textosCabecera.setLayout(new BoxLayout(textosCabecera, BoxLayout.Y_AXIS));
        textosCabecera.add(Box.createVerticalGlue());
        textosCabecera.add(nombre);
        textosCabecera.add(estado);
        textosCabecera.add(Box.createVerticalGlue());
        JPanel contenedorAvatar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        contenedorAvatar.setOpaque(false);
        contenedorAvatar.add(avatar);
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.add(contenedorAvatar, BorderLayout.WEST);
        cabecera.add(textosCabecera, BorderLayout.CENTER);
        limitarAlto(cabecera, Math.max(Avatar.alto(58, false), textosCabecera.getPreferredSize().height));
        agregar(cabecera, 12);

        agregar(titulo("Dice"), 2);
        agregar(texto("«" + a.getTexto(Estilo::nombreHtml) + "»", Estilo.FRASE), 12);

        agregar(titulo("Qué implica"), 2);
        agregar(texto(Burbuja.consecuenciasHtml(a), Estilo.TEXTO), 12);

        agregar(titulo("Lo que dicen de " + p.getNombre()), 2);
        StringBuilder dicen = new StringBuilder();
        for (Afirmacion otra : afirmaciones.values()) {
            if (otra.getEmisor() == p) continue;
            boolean loNombra = otra.getReferencias().stream().anyMatch(r -> r.objetivo() == p);
            if (loNombra) {
                dicen.append(Estilo.nombreHtml(otra.getEmisor())).append(": «")
                        .append(otra.getTexto(Estilo::nombreHtml)).append("»<br>");
            }
        }
        agregar(texto(dicen.length() == 0 ? "Nadie habla de " + p.getNombre() + " en esta ronda." : dicen.toString(), Estilo.TEXTO), 12);

        String aviso = avisos.get(p);
        if (aviso != null) {
            JLabel etiquetaAviso = texto(aviso, Estilo.NEGRITA);
            etiquetaAviso.setForeground(Estilo.texto(Estilo.ROJO));
            agregar(etiquetaAviso, 12);
        }

        agregar(titulo("Marcalo"), 4);
        Boton verdad = new Boton("Dice la verdad", Estilo.VERDE, false);
        verdad.setSeleccionado(Boolean.TRUE.equals(marca));
        verdad.addActionListener(e -> marcar(p, true));
        Boton miente = new Boton("Miente", Estilo.ROJO, false);
        miente.setSeleccionado(Boolean.FALSE.equals(marca));
        miente.addActionListener(e -> marcar(p, false));
        JPanel marcar = new JPanel(new GridLayout(1, 2, 8, 0));
        marcar.setOpaque(false);
        marcar.add(verdad);
        marcar.add(miente);
        limitarAlto(marcar, 42);
        agregar(marcar, 14);

        Boton anterior = new Boton("Anterior", Estilo.TINTA_SUAVE, false);
        anterior.addActionListener(e -> moverSeleccion(-1));
        Boton siguiente = new Boton("Siguiente", Estilo.TINTA_SUAVE, false);
        siguiente.addActionListener(e -> moverSeleccion(1));
        JPanel navegar = new JPanel(new GridLayout(1, 2, 8, 0));
        navegar.setOpaque(false);
        navegar.add(anterior);
        navegar.add(siguiente);
        limitarAlto(navegar, 38);
        agregar(navegar, 0);

        detalle.revalidate();
        detalle.repaint();
    }

    private void agregar(JComponent componente, int espacioDespues) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        detalle.add(componente);
        if (espacioDespues > 0) detalle.add(Box.createVerticalStrut(espacioDespues));
    }

    private static void limitarAlto(JComponent componente, int alto) {
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        componente.setPreferredSize(new Dimension(100, alto));
    }

    private static class PanelQueSigueElAncho extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto.toUpperCase());
        etiqueta.setFont(Estilo.CHICA_NEGRITA);
        etiqueta.setForeground(Estilo.TINTA_SUAVE);
        return etiqueta;
    }

    private static JLabel texto(String html, Font fuente) {
        JLabel etiqueta = new JLabel("<html><div style='width:" + ANCHO_TEXTO_DETALLE + "px'>" + html + "</div></html>");
        etiqueta.setFont(fuente);
        etiqueta.setForeground(Estilo.TINTA);
        return etiqueta;
    }

    private String textoDePista() {
        return "Pista (" + pistasRestantes.getAsInt() + ")";
    }

    private void usarPista() {
        Map<Personaje, Boolean> todas = marcas;
        if (todas.isEmpty()) {
            mostrarMensaje("Marcá al menos a alguien antes de pedir una pista.", Estilo.TINTA);
            return;
        }
        avisos.clear();
        int revisadas = 0;
        for (Afirmacion a : afirmaciones.values()) {
            Personaje emisor = a.getEmisor();
            boolean sePuedeRevisar = todas.containsKey(emisor)
                    && a.getReferencias().stream().allMatch(r -> todas.containsKey(r.objetivo()));
            if (!sePuedeRevisar) continue;
            revisadas++;
            if (a.esVerdadera(todas) != todas.get(emisor)) {
                avisos.put(emisor, "No cierra con lo que marcaste: si " + emisor.getNombre()
                        + (todas.get(emisor) ? " dice la verdad, su frase tendría que ser cierta." : " miente, su frase tendría que ser falsa."));
            }
        }
        if (revisadas == 0) {
            mostrarMensaje("Todavía no hay frases para revisar: marcá también a quienes nombran. No se gastó la pista.", Estilo.TINTA);
            return;
        }
        gastarPista.run();
        if (avisos.isEmpty()) {
            mostrarMensaje("Lo que marcaste hasta ahora no se contradice (" + revisadas + " frases revisadas).", Estilo.texto(Estilo.VERDE));
        } else {
            seleccionado = avisos.keySet().stream().findFirst().orElse(seleccionado);
            mostrarMensaje(avisos.size() == 1
                    ? "Hay una frase que no cierra con lo que marcaste: su globo está en rojo."
                    : "Hay " + avisos.size() + " frases que no cierran con lo que marcaste: sus globos están en rojo.", Estilo.texto(Estilo.ROJO));
        }
        refrescar();
    }

    private void mostrarMensaje(String texto, Color color) {
        mensaje.setText(texto);
        mensaje.setForeground(color);
    }

    private static JPanel tarjeta(JComponent contenido) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Estilo.TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                new EmptyBorder(14, 16, 14, 12)));
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }
}
