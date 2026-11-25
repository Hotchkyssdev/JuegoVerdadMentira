package juego.pantallas;

final class Textos {
    static final String RESUMEN =
            "*Cinco personajes.* Dos siempre dicen la verdad, dos siempre mienten y uno puede hacer las dos cosas.\n"
            + "*Tres rondas.* En cada una, todos dicen una frase. Marcá quién miente y quién dice la verdad: "
            + "hay una sola combinación que cierra.\n"
            + "*Para arrancar.* En la primera ronda alguien dice algo que se comprueba mirando las fotos.\n"
            + "*Para ganar.* Al final, elegí al único que cambió de una ronda a otra.";

    static final String REGLAS =
            "*Los personajes.* Hay cinco. Al empezar se sortea quién es quién: dos veraces, que siempre dicen la verdad; "
            + "dos mentirosos, que siempre mienten; y uno que puede hacer las dos cosas y en algún momento cambia.\n\n"
            + "*Cada ronda.* Cada personaje dice una frase. Si en esa ronda dice la verdad, la frase es cierta; si miente, es falsa. "
            + "Hacé clic en un personaje para ver qué implica su frase y marcalo con los botones del panel, "
            + "con las teclas *V* y *M*, o escribiendo tu deducción: «lisa y roz mienten, ferb dice la verdad».\n\n"
            + "*Las flechas.* Salen de quien habla y llegan a quien nombra: verde si dice que dice la verdad, "
            + "roja si dice que miente y gris si habla de algo que se ve en la foto.\n\n"
            + "*El árbol de cada personaje* está en la franja de abajo: cada nivel es una ronda, a la izquierda "
            + "mentiroso y a la derecha veraz. El juego no te dice si acertaste.\n\n"
            + "*Deshacer.* Podés suponer algo, seguir lo que implica y volver atrás con Deshacer.\n\n"
            + "*Pistas.* Tenés tres por partida y cada una cuesta puntos: marcan las frases que no cierran o te dicen "
            + "por quién seguir, con el camino más corto entre lo que ya marcaste y lo que falta.\n\n"
            + "*Al final* elegís quién es el que puede hacer las dos cosas. Si lo encontrás, ganás.";

    private Textos() {
    }
}
