package persistencia.archivos;

import estructuras.ListaDinamica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Base de los archivos de datos del juego. Todos son de texto en UTF-8, con
 * un dato por línea. Las líneas vacías y las que empiezan con «#» se
 * ignoran, así que se pueden usar como comentarios dentro del archivo.
 * Antes de sobrescribir un archivo se copia al lado con extensión «.bak».
 * Si el archivo no existe, se usan los datos iniciales y se lo crea; si una
 * línea está dañada, se avisa con su número y se sigue con la siguiente.
 */
public abstract class ArchivoDeTexto<T> implements ArchivoDeDatos<T> {
    private static final String CODIFICACION = "UTF-8";
    private static final String EXTENSION_DE_COPIA = ".bak";
    private static final String COMENTARIO = "#";

    private final File carpeta;
    private final String nombre;

    protected ArchivoDeTexto(File carpeta, String nombre) {
        if (carpeta == null) throw new IllegalArgumentException("Falta la carpeta de datos");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Falta el nombre del archivo");
        this.carpeta = carpeta;
        this.nombre = nombre;
    }

    protected abstract T interpretar(ListaDinamica<LineaLeida> lineas, ListaDinamica<String> avisos);

    protected abstract ListaDinamica<String> escribir(T datos);

    protected abstract String encabezado();

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean existe() {
        return getArchivo().exists();
    }

    public File getArchivo() {
        return new File(carpeta, nombre);
    }

    @Override
    public T cargar(ListaDinamica<String> avisos) {
        File archivo = getArchivo();
        if (!archivo.exists()) {
            avisos.agregar("No estaba " + nombre + ": se creó con los datos iniciales.");
            T iniciales = datosIniciales();
            guardar(iniciales, avisos);
            return iniciales;
        }
        ListaDinamica<LineaLeida> lineas = new ListaDinamica<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), CODIFICACION))) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                String limpia = linea.trim();
                if (limpia.isEmpty() || limpia.startsWith(COMENTARIO)) continue;
                lineas.agregar(new LineaLeida(numero, limpia));
            }
        } catch (IOException e) {
            avisos.agregar("No se pudo leer " + nombre + " (" + e.getMessage() + "): se usan los datos iniciales.");
            return datosIniciales();
        }
        return interpretar(lineas, avisos);
    }

    @Override
    public boolean guardar(T datos, ListaDinamica<String> avisos) {
        File archivo = getArchivo();
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            avisos.agregar("No se pudo crear la carpeta " + carpeta.getPath() + ": no se guardó " + nombre + ".");
            return false;
        }
        try {
            if (archivo.exists()) copiar(archivo, new File(carpeta, nombre + EXTENSION_DE_COPIA));
        } catch (IOException e) {
            avisos.agregar("No se pudo hacer la copia de " + nombre + " (" + e.getMessage() + "): no se sobrescribió.");
            return false;
        }
        try (BufferedWriter escritor = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), CODIFICACION))) {
            ListaDinamica<String> encabezado = lineasDe(encabezado());
            for (int i = 0; i < encabezado.tamanio(); i++) {
                escritor.write(COMENTARIO + " " + encabezado.obtener(i));
                escritor.newLine();
            }
            ListaDinamica<String> lineas = escribir(datos);
            for (int i = 0; i < lineas.tamanio(); i++) {
                escritor.write(lineas.obtener(i));
                escritor.newLine();
            }
            return true;
        } catch (IOException e) {
            avisos.agregar("No se pudo guardar " + nombre + " (" + e.getMessage() + ").");
            return false;
        }
    }

    private static void copiar(File origen, File destino) throws IOException {
        try (InputStream entrada = new FileInputStream(origen); OutputStream salida = new FileOutputStream(destino)) {
            int leido;
            while ((leido = entrada.read()) != -1) salida.write(leido);
        }
    }

    protected static ListaDinamica<String> lineasDe(String texto) {
        ListaDinamica<String> lineas = new ListaDinamica<>();
        int inicio = 0;
        while (inicio <= texto.length()) {
            int fin = texto.indexOf('\n', inicio);
            if (fin < 0) fin = texto.length();
            String linea = texto.substring(inicio, fin).trim();
            if (!linea.isEmpty()) lineas.agregar(linea);
            inicio = fin + 1;
        }
        return lineas;
    }

    protected void avisarLineaDanada(ListaDinamica<String> avisos, LineaLeida linea, String motivo) {
        avisos.agregar(nombre + ", línea " + linea.getNumero() + ": " + motivo + " Se ignoró.");
    }

    protected static int entero(String texto, String campo) throws LineaDanadaException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new LineaDanadaException(campo + " tiene que ser un número entero y dice «" + texto.trim() + "».");
        }
    }
}
