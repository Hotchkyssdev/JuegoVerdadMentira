package juego.pantallas.componentes.dibujo;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public final class Trazos {
    private static final double PASO_DE_CURVA = 0.004;

    private Trazos() {
    }

    public static void lineaPunteada(Graphics2D g, double x1, double y1, double x2, double y2, double guion, double hueco) {
        double largo = Math.hypot(x2 - x1, y2 - y1);
        if (largo == 0) return;
        double dx = (x2 - x1) / largo;
        double dy = (y2 - y1) / largo;
        for (double recorrido = 0; recorrido < largo; recorrido += guion + hueco) {
            double fin = Math.min(largo, recorrido + guion);
            g.draw(new Line2D.Double(x1 + dx * recorrido, y1 + dy * recorrido, x1 + dx * fin, y1 + dy * fin));
        }
    }

    public static void circuloPunteado(Graphics2D g, double cx, double cy, double radio, int segmentos) {
        double paso = 360.0 / segmentos;
        for (int i = 0; i < segmentos; i++) {
            g.draw(new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, i * paso, paso / 2, Arc2D.OPEN));
        }
    }

    public static Point2D puntoDeCurva(Point2D p0, Point2D control, Point2D p2, double t) {
        double u = 1 - t;
        return new Point2D.Double(
                u * u * p0.getX() + 2 * u * t * control.getX() + t * t * p2.getX(),
                u * u * p0.getY() + 2 * u * t * control.getY() + t * t * p2.getY());
    }

    public static void curva(Graphics2D g, Point2D p0, Point2D control, Point2D p2, double desde, double hasta) {
        Path2D camino = new Path2D.Double();
        Point2D primero = puntoDeCurva(p0, control, p2, desde);
        camino.moveTo(primero.getX(), primero.getY());
        for (double t = desde; t < hasta; t += PASO_DE_CURVA * 2) {
            Point2D p = puntoDeCurva(p0, control, p2, t);
            camino.lineTo(p.getX(), p.getY());
        }
        Point2D ultimo = puntoDeCurva(p0, control, p2, hasta);
        camino.lineTo(ultimo.getX(), ultimo.getY());
        g.draw(camino);
    }

    public static void curvaPunteada(Graphics2D g, Point2D p0, Point2D control, Point2D p2, double desde, double hasta,
                                     double guion, double hueco) {
        double acumulado = 0;
        Point2D anterior = puntoDeCurva(p0, control, p2, desde);
        for (double t = desde + PASO_DE_CURVA; t <= hasta; t += PASO_DE_CURVA) {
            Point2D actual = puntoDeCurva(p0, control, p2, t);
            double tramo = anterior.distance(actual);
            if (acumulado % (guion + hueco) < guion) g.draw(new Line2D.Double(anterior, actual));
            acumulado += tramo;
            anterior = actual;
        }
    }

    public static void punta(Graphics2D g, Point2D desde, Point2D punta, double largo) {
        double angulo = Math.atan2(punta.getY() - desde.getY(), punta.getX() - desde.getX());
        double abertura = Math.toRadians(26);
        Path2D triangulo = new Path2D.Double();
        triangulo.moveTo(punta.getX(), punta.getY());
        triangulo.lineTo(punta.getX() - largo * Math.cos(angulo - abertura), punta.getY() - largo * Math.sin(angulo - abertura));
        triangulo.lineTo(punta.getX() - largo * Math.cos(angulo + abertura), punta.getY() - largo * Math.sin(angulo + abertura));
        triangulo.closePath();
        g.fill(triangulo);
    }
}
