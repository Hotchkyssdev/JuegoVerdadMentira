package verdadmentira.view.animacion;

import javax.swing.Timer;
import java.util.function.DoubleConsumer;

public final class Animacion {
    private static final int MILISEGUNDOS_POR_CUADRO = 15;

    private final Timer temporizador;
    private final long duracionEnNanos;
    private final DoubleConsumer alAvanzar;
    private final Runnable alTerminar;
    private long inicio;
    private boolean terminada;

    private Animacion(int duracionEnMilisegundos, DoubleConsumer alAvanzar, Runnable alTerminar) {
        this.duracionEnNanos = Math.max(1, duracionEnMilisegundos) * 1_000_000L;
        this.alAvanzar = alAvanzar;
        this.alTerminar = alTerminar;
        temporizador = new Timer(MILISEGUNDOS_POR_CUADRO, e -> avanzar());
        temporizador.setCoalesce(true);
    }

    public static Animacion iniciar(int duracionEnMilisegundos, DoubleConsumer alAvanzar, Runnable alTerminar) {
        Animacion animacion = new Animacion(duracionEnMilisegundos, alAvanzar, alTerminar);
        animacion.inicio = System.nanoTime();
        animacion.alAvanzar.accept(0);
        animacion.temporizador.start();
        return animacion;
    }

    public boolean estaCorriendo() {
        return !terminada;
    }

    public void completar() {
        if (terminada) return;
        terminar();
    }

    public void cancelar() {
        terminada = true;
        temporizador.stop();
    }

    private void avanzar() {
        if (terminada) return;
        double progreso = (System.nanoTime() - inicio) / (double) duracionEnNanos;
        if (progreso >= 1) {
            terminar();
            return;
        }
        alAvanzar.accept(progreso);
    }

    private void terminar() {
        terminada = true;
        temporizador.stop();
        alAvanzar.accept(1);
        if (alTerminar != null) alTerminar.run();
    }

    public static double tramo(double progreso, double desde, double hasta) {
        if (progreso <= desde) return 0;
        if (progreso >= hasta) return 1;
        return (progreso - desde) / (hasta - desde);
    }

    public static double suave(double t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    public static double frenando(double t) {
        return 1 - Math.pow(1 - t, 3);
    }

    public static double conRebote(double t) {
        double c1 = 1.4;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    public static double interpolar(double desde, double hasta, double t) {
        return desde + (hasta - desde) * t;
    }
}
