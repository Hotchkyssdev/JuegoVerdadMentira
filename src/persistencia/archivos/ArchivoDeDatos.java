package persistencia.archivos;

import estructuras.ListaDinamica;

public interface ArchivoDeDatos<T> {
    String getNombre();

    T cargar(ListaDinamica<String> avisos);

    boolean guardar(T datos, ListaDinamica<String> avisos);

    T datosIniciales();

    boolean existe();
}
