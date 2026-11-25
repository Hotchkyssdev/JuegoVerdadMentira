package persistencia;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public final class Recursos {
    private Recursos() {
    }

    public static Font fuente(String rutaEnElClasspath) {
        try (InputStream entrada = Recursos.class.getResourceAsStream(rutaEnElClasspath)) {
            if (entrada == null) return null;
            return Font.createFont(Font.TRUETYPE_FONT, entrada);
        } catch (IOException | FontFormatException e) {
            return null;
        }
    }
}
