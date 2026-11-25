package juego.reglas.personajes;

import estructuras.ListaDinamica;

import java.awt.Color;

public final class Elenco {
    private Elenco() {
    }

    public static ListaDinamica<Personaje> nuevo() {
        ListaDinamica<Personaje> personajes = new ListaDinamica<>();
        Personaje mcLovin = new Personaje("McLovin", new Color(0xF08A3E), "naranja", RasgoVisible.FLEQUILLO);
        mcLovin.agregarApodo("mc lovin");
        mcLovin.agregarApodo("mc");
        personajes.agregar(mcLovin);
        personajes.agregar(new Personaje("Roz", new Color(0x7FA3CC), "celeste", RasgoVisible.PELO_GRIS));
        personajes.agregar(new Personaje("Albert", new Color(0xA5ADB8), "gris", RasgoVisible.BARBA));
        personajes.agregar(new Personaje("Lisa", new Color(0xE3B456), "amarillo", RasgoVisible.COLLAR_DE_PERLAS));
        personajes.agregar(new Personaje("Ferb", new Color(0xA28BC9), "violeta", RasgoVisible.PELO_VERDE));
        return personajes;
    }

    public static ListaDinamica<String> nombres() {
        ListaDinamica<Personaje> personajes = nuevo();
        ListaDinamica<String> nombres = new ListaDinamica<>();
        for (int i = 0; i < personajes.tamanio(); i++) nombres.agregar(personajes.obtener(i).getNombre());
        return nombres;
    }
}
