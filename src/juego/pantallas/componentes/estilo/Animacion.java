package juego.pantallas.componentes.estilo;

public final class Animacion {
    private Animacion() {
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
