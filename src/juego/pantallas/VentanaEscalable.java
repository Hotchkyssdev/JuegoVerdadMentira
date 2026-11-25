package juego.pantallas;

import juego.pantallas.componentes.estilo.Estilo;
import motor.Escena;
import motor.Motor;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VentanaEscalable {
    private static final int MAXIMO_DE_PASOS = 12;
    private static final int MILISEGUNDOS_POR_CUADRO = 8;
    private static final double ATRASO_MAXIMO = 0.25;

    private final Motor motor;
    private final int ancho;
    private final int alto;
    private final JFrame marco;
    private final JPanel panel;
    private final Timer reloj;
    private long ultimo;
    private double acumulado;

    public VentanaEscalable(String titulo, int ancho, int alto) {
        this.motor = new Motor(ancho, alto);
        this.ancho = ancho;
        this.alto = alto;
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Estilo.suavizar(g2);
                double escala = escala();
                g2.translate(margenX(escala), margenY(escala));
                g2.scale(escala, escala);
                g2.clipRect(0, 0, VentanaEscalable.this.ancho, VentanaEscalable.this.alto);
                motor.dibujar(g2);
                g2.dispose();
            }
        };
        panel.setBackground(Estilo.FONDO);
        panel.setPreferredSize(new Dimension(ancho, alto));
        panel.setFocusable(true);
        panel.addKeyListener(motor.getEntrada());
        MouseAdapter raton = new RatonEscalado();
        panel.addMouseListener(raton);
        panel.addMouseMotionListener(raton);

        marco = new JFrame(titulo);
        marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marco.add(panel);
        marco.pack();
        marco.setMinimumSize(new Dimension(ancho / 2, alto / 2));
        marco.setLocationRelativeTo(null);
        marco.setExtendedState(JFrame.MAXIMIZED_BOTH);
        reloj = new Timer(MILISEGUNDOS_POR_CUADRO, e -> avanzar());
    }

    public static void abrir(String titulo, int ancho, int alto, Escena inicial) {
        SwingUtilities.invokeLater(() -> new VentanaEscalable(titulo, ancho, alto).iniciar(inicial));
    }

    public void iniciar(Escena inicial) {
        motor.iniciar(inicial);
        marco.setVisible(true);
        panel.requestFocusInWindow();
        ultimo = System.nanoTime();
        reloj.start();
    }

    private double escala() {
        return Math.min(panel.getWidth() / (double) ancho, panel.getHeight() / (double) alto);
    }

    private double margenX(double escala) {
        return (panel.getWidth() - ancho * escala) / 2;
    }

    private double margenY(double escala) {
        return (panel.getHeight() - alto * escala) / 2;
    }

    private void avanzar() {
        long ahora = System.nanoTime();
        acumulado += Math.min((ahora - ultimo) / 1e9, ATRASO_MAXIMO);
        ultimo = ahora;
        int pasos = 0;
        while (acumulado >= Motor.PASO && pasos < MAXIMO_DE_PASOS) {
            motor.paso();
            acumulado -= Motor.PASO;
            pasos++;
        }
        if (pasos == MAXIMO_DE_PASOS) acumulado = 0;
        if (motor.estaTerminado()) {
            reloj.stop();
            marco.dispose();
            System.exit(0);
        }
        panel.repaint();
    }

    private class RatonEscalado extends MouseAdapter {
        private void mover(MouseEvent e) {
            double escala = escala();
            int x = (int) Math.round((e.getX() - margenX(escala)) / escala);
            int y = (int) Math.round((e.getY() - margenY(escala)) / escala);
            motor.getEntrada().moverMouse(x, y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1) return;
            panel.requestFocusInWindow();
            mover(e);
            motor.getEntrada().apretarMouse();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) motor.getEntrada().soltarMouse();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mover(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mover(e);
        }
    }
}
