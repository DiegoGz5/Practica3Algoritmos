// Esta clase es una cola hecha con un arreglo. La ficha que entra primero
// es la primera que puede salir. Cada ficha guarda el turno en que entró.
public class ColaFichas {
    // La cola empieza con ocho espacios. Si se llena, el arreglo crece.
    private static final int CAPACIDAD_INICIAL = 8;

    // Aquí guardamos las fichas y el turno de entrada de cada una.
    private int[] fichas;
    // Posición de la ficha que saldrá primero.
    private int frente;
    // Cuántas fichas hay en la cola ahora.
    private int cantidad;

    // Creamos la cola vacía con ocho espacios.
    public ColaFichas() {
        fichas = new int[CAPACIDAD_INICIAL];
    }

    // Agrega una ficha al final. El % hace que el índice vuelva al inicio
    // cuando llega al final del arreglo.
    public void encolar(int ficha) {
        // Si ya no caben más fichas, hacemos más grande el arreglo primero.
        if (cantidad == fichas.length) ampliar();
        int fin = (frente + cantidad) % fichas.length;
        fichas[fin] = ficha;
        cantidad++;
    }

    // Saca la ficha que lleva más tiempo esperando y devuelve su turno de entrada.
    public int desencolar() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía.");
        int ficha = fichas[frente];
        // Avanzamos el frente. No hace falta recorrer ni mover las otras fichas.
        frente = (frente + 1) % fichas.length;
        cantidad--;
        // Si quedó vacía, la dejamos lista para volver a empezar desde el índice 0.
        if (cantidad == 0) frente = 0;
        return ficha;
    }

    // Devuelve cuántas fichas hay esperando.
    public int tamano() {
        return cantidad;
    }

    // Nos dice si ya no hay fichas que sacar.
    public boolean estaVacia() {
        return cantidad == 0;
    }

    // Crea un arreglo del doble de tamaño y copia las fichas en el mismo orden.
    // Después de copiarlas, el frente vuelve a quedar en el índice 0.
    private void ampliar() {
        int[] nuevoArreglo = new int[fichas.length * 2];
        for (int i = 0; i < cantidad; i++) {
            // Usamos % porque la cola podría estar repartida entre el final
            // y el inicio del arreglo viejo.
            nuevoArreglo[i] = fichas[(frente + i) % fichas.length];
        }
        fichas = nuevoArreglo;
        frente = 0;
    }
}
