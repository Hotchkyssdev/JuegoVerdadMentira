package verdadmentira.view;

import verdadmentira.domain.Afirmacion;
import verdadmentira.service.Juego;
import verdadmentira.view.component.PanelArbol;
import verdadmentira.domain.Personaje;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class VentanaJuego extends JFrame {
    private final Juego juego = new Juego();
    private int ronda = 0;
    private final Map<String, Boolean> selecciones = new LinkedHashMap<>();
    private final JPanel panelPersonajes = new JPanel(new GridLayout(0,1,6,6));
    private final JLabel titulo = new JLabel("", SwingConstants.CENTER);
    private final JTextArea afirmaciones = new JTextArea();

    public VentanaJuego() {
        setTitle("El juego de la verdad y la mentira");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950,700); setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        titulo.setFont(new Font("SansSerif",Font.BOLD,25));
        titulo.setBorder(new EmptyBorder(15,5,5,5)); add(titulo,BorderLayout.NORTH);

        afirmaciones.setEditable(false); afirmaciones.setFont(new Font("SansSerif",Font.PLAIN,17));
        afirmaciones.setLineWrap(true); afirmaciones.setWrapStyleWord(true);
        JPanel centro = new JPanel(new GridLayout(1,2,10,10));
        JScrollPane sp1 = new JScrollPane(afirmaciones);
        sp1.setBorder(BorderFactory.createTitledBorder("Afirmaciones"));
        panelPersonajes.setBackground(Color.WHITE);
        JScrollPane sp2 = new JScrollPane(panelPersonajes);
        sp2.setBorder(BorderFactory.createTitledBorder("Marcá quién miente y quién dice la verdad"));
        centro.add(sp1); centro.add(sp2); add(centro,BorderLayout.CENTER);

        JButton confirmar = new JButton("Confirmar ronda");
        confirmar.addActionListener(e -> confirmarRonda());
        JPanel abajo = new JPanel(); abajo.add(confirmar); add(abajo,BorderLayout.SOUTH);
        mostrarRonda();
    }

    private void mostrarRonda() {
        selecciones.clear();
        titulo.setText("RONDA " + (ronda+1) + " DE 3");
        StringBuilder texto = new StringBuilder();
        for (Afirmacion a : juego.getAfirmaciones(ronda))
            texto.append("• ").append(a.getTexto()).append("\n\n");
        afirmaciones.setText(texto.toString());

        panelPersonajes.removeAll();
        for (Personaje p : juego.getPersonajes()) {
            JPanel fila = new JPanel(new BorderLayout(8,5));
            fila.setBackground(Color.WHITE); fila.setBorder(new EmptyBorder(8,8,8,8));
            JLabel avatar = new JLabel(p.getNombre().substring(0,1),SwingConstants.CENTER);
            avatar.setOpaque(true); avatar.setBackground(new Color(45,65,95));
            avatar.setForeground(Color.WHITE); avatar.setFont(new Font("SansSerif",Font.BOLD,20));
            avatar.setPreferredSize(new Dimension(42,42)); fila.add(avatar,BorderLayout.WEST);
            fila.add(new JLabel(p.getNombre()),BorderLayout.CENTER);

            JPanel botones = new JPanel(new GridLayout(1,2,5,0));
            JToggleButton mentiroso = new JToggleButton("Mentiroso");
            JToggleButton veraz = new JToggleButton("Veraz");
            ButtonGroup grupo = new ButtonGroup(); grupo.add(mentiroso); grupo.add(veraz);
            mentiroso.setBackground(new Color(255,220,220));
            veraz.setBackground(new Color(215,245,220));
            mentiroso.addActionListener(e -> selecciones.put(p.getNombre(),false));
            veraz.addActionListener(e -> selecciones.put(p.getNombre(),true));
            botones.add(mentiroso); botones.add(veraz); fila.add(botones,BorderLayout.SOUTH);
            panelPersonajes.add(fila);
        }
        panelPersonajes.revalidate(); panelPersonajes.repaint();
    }

    private void confirmarRonda() {
        if (selecciones.size()!=5) {
            JOptionPane.showMessageDialog(this,"Tenés que elegir una opción para los cinco personajes.");
            return;
        }
        for (Personaje p:juego.getPersonajes())
            juego.registrarDecision(ronda,p,selecciones.get(p.getNombre()));
        juego.construirArbol(ronda);

        PanelArbol dibujo = new PanelArbol();
        dibujo.setRaiz(juego.getArbol(ronda).getRaiz());
        JTextArea recorrido = new JTextArea(String.join("\n",juego.getArbol(ronda).obtenerRecorrido()));
        recorrido.setEditable(false);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Árbol de tus decisiones - ronda "+(ronda+1),SwingConstants.CENTER),BorderLayout.NORTH);
        panel.add(dibujo,BorderLayout.CENTER); panel.add(new JScrollPane(recorrido),BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this,panel,"Decisiones guardadas",JOptionPane.PLAIN_MESSAGE);

        ronda++;
        if (ronda<3) mostrarRonda(); else resultadoFinal();
    }

    private void resultadoFinal() {
        String[] nombres=juego.getPersonajes().stream().map(Personaje::getNombre).toArray(String[]::new);
        String elegido=(String)JOptionPane.showInputDialog(this,
                "¿Quién puede decir tanto la verdad como mentiras?","Elección final",
                JOptionPane.QUESTION_MESSAGE,null,nombres,nombres[0]);
        if (elegido==null) elegido="";
        Personaje ambos=juego.getPersonajeAmbos();
        StringBuilder sb=new StringBuilder();
        sb.append(elegido.equals(ambos.getNombre())?"¡GANASTE!":"PERDISTE").append("\n");
        sb.append("El personaje especial era: ").append(ambos.getNombre()).append("\n\n");
        sb.append("COMPARACIÓN DE LAS TRES RONDAS\n");
        for(int r=0;r<3;r++) {
            sb.append("\nRONDA ").append(r+1).append("\n");
            for(Personaje p:juego.getPersonajes()) {
                boolean eleccion=juego.getDecisiones(r).get(p.getNombre());
                boolean real=juego.comportamientoReal(r,p);
                sb.append(p.getNombre()).append(": elegiste ")
                  .append(eleccion?"VERAZ":"MENTIROSO")
                  .append(" | era ").append(real?"VERAZ":"MENTIROSO")
                  .append(eleccion==real?" [CORRECTO]":" [INCORRECTO]")
                  .append(" | tipo: ").append(p.descripcionReal()).append("\n");
            }
        }
        JTextArea area=new JTextArea(sb.toString(),25,65);
        area.setEditable(false); area.setFont(new Font("Monospaced",Font.PLAIN,13));
        JOptionPane.showMessageDialog(this,new JScrollPane(area),"Resultado final",JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}