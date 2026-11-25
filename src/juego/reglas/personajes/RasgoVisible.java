package juego.reglas.personajes;

public enum RasgoVisible {
    FLEQUILLO("tiene flequillo"),
    PELO_GRIS("tiene el pelo gris"),
    BARBA("tiene barba"),
    COLLAR_DE_PERLAS("tiene un collar de perlas"),
    PELO_VERDE("tiene el pelo verde");

    private final String descripcion;

    RasgoVisible(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
