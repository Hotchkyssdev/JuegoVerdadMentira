JUEGO DE LA VERDAD Y LA MENTIRA - JAVA SWING
Requisitos: JDK 17 o superior. No requiere librerias externas.

Ejecucion:
- Abrir la carpeta src en IntelliJ IDEA / Eclipse / VS Code y ejecutar verdadmentira/Main.java.
- O desde una terminal dentro de src:
  javac -encoding UTF-8 -d out $(find . -name "*.java")
  cp -r verdadmentira/view/fotos out/verdadmentira/view/
  java -cp out verdadmentira.Main

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

Fotos:
Cada personaje tiene su foto en verdadmentira/view/fotos/<nombre sin tildes>.png
(360x360, con el fondo del color del personaje). Avatar las carga desde el
classpath; si falta alguna, muestra el recuadro con el color liso.
Las frases que se comprueban mirando las fotos hablan del color del fondo o de
un rasgo visible (RasgoVisible), así que si se cambia una foto hay que revisar
que el rasgo siga a la vista.

Animaciones:
view/animacion tiene el motor (Animacion: un Timer de ~60 cuadros por segundo
con curvas de aceleración) y CapaDeTransicion, el glass pane donde se dibuja
lo que viaja entre pantallas y que bloquea los clics mientras dura.
- Al empezar una ronda, PanelDigrafo hace aparecer las fotos y después recorre
  la cadena de quién habla de quién: el globo de cada personaje y su flecha,
  que crece hasta el nombrado. Un clic la completa.
- Al confirmar, PantallaRonda difumina flechas, globos y el panel de detalle,
  y cada foto vuela a la raíz de su árbol. VentanaJuego registra la ronda y
  hace subir los árboles al centro de la pantalla de resumen.

Versión web:
La carpeta web/ publica el mismo juego en el navegador con CheerpJ 5.0, que
ejecuta la aplicación Swing con Java 21 sobre WebAssembly. index.html carga
CheerpJ desde su CDN y corre web/verdad-o-mentira.jar.
- Armar el .jar (no se versiona):  bash web/construir.sh
- Probar en local (hace falta un servidor con peticiones por rango):
  npx http-server web -c-1
- Publicar en Vercel:  cd web && vercel deploy --prod
Segoe Print no existe en el navegador: los nombres usan Kalam
(view/fuentes, licencia SIL OFL en OFL.txt), que el juego carga si Segoe
Print no está instalada.

