JUEGO DE LA VERDAD Y LA MENTIRA - JAVA SWING
Requisitos: JDK 17 o superior. No requiere librerias externas.

Ejecucion:
- Abrir la carpeta src en IntelliJ IDEA / Eclipse / VS Code y ejecutar Main.java.
- O desde una terminal dentro de src:
  javac *.java
  java Main

Clases:
Personaje: representa a cada participante y su tipo.
Afirmacion: contiene la afirmacion de un personaje sobre otro.
GrafoPersonajes: guarda las relaciones dirigidas entre personajes.
ArbolDecisiones: almacena las elecciones del jugador en un arbol binario.
PanelArbol: dibuja el arbol con ramas izquierdas rojas y derechas verdes.
Juego: asigna roles y genera las tres rondas.
VentanaJuego: interfaz grafica Swing.
Main: inicia la aplicacion.

Reglas:
- Se asignan aleatoriamente dos veraces, dos mentirosos y un personaje Ambos.
- Los tipos fijos permanecen iguales durante las tres rondas.
- El personaje Ambos puede decir verdad o mentir en cada ronda.
- Cada personaje afirma algo sobre otro.
- Los veraces realizan afirmaciones verdaderas y los mentirosos afirmaciones falsas.
- No se muestra si las respuestas del jugador son correctas hasta el resultado final.

Nota:
La primera version usa avatares con iniciales en lugar de fotografias, para que
el juego funcione sin recursos externos. Se pueden agregar imagenes despues.
