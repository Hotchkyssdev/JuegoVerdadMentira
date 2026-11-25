package juego.reglas;

public class DatoInvalidoException extends IllegalArgumentException {
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
