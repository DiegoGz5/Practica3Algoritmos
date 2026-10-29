# Practica 3 — Algoritmos y Estructuras de Datos

Simulación en Java de **The Dice Game, juego 2**, con reasignación de recursos entre 10 estaciones.

## Ejecutar en IntelliJ IDEA

1. Abre la carpeta `Practica3AlgoritmosYE` como proyecto.
2. Abre `outputs/PracticaDados/InterfazDados.java`.
3. Ejecuta su método `main` para iniciar la interfaz gráfica.

El juego usa arreglos para guardar dados, estaciones y colas. `ColaFichas.java` implementa la cola FIFO con un arreglo circular dinámico; no utiliza nodos enlazados ni `Queue`/`Stack` de Java.
La partida gráfica está limitada a 20 turnos. Al terminar, muestra las gráficas de actividad (movido y lanzado), throughput acumulado, número en el sistema y tiempo en el sistema.

El proyecto usa Java Swing y no requiere dependencias externas.
