package juego;

import juego.pantallas.EscenaMenu;
import juego.pantallas.Sesion;
import juego.pantallas.VentanaEscalable;
import persistencia.Almacen;

public class Main {
    public static void main(String[] args) {
        Sesion sesion = new Sesion(new Almacen(args.length > 0 ? args[0] : Almacen.CARPETA_POR_DEFECTO));
        VentanaEscalable.abrir("Verdad o mentira", Sesion.ANCHO, Sesion.ALTO, new EscenaMenu(sesion));
    }
}
