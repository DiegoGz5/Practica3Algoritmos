/**
 * Cola FIFO de fichas implementada únicamente con un arreglo circular.
 * FIFO significa "primero en entrar, primero en salir": una ficha se agrega
 * al final y se retira desde el frente. En esta práctica cada entero guarda
 * el turno en que la ficha entró al sistema.
 */
public class ColaFichas {
    /** Espacio reservado inicialmente; el arreglo crece si se llena. */
    private static final int CAPACIDAD_INICIAL = 8;

    /** Arreglo que contiene los datos de la cola. */
    private int[] fichas;
    /** Índice del elemento que se retirará en la siguiente llamada a desencolar. */
    private int frente;
    /** Número de elementos válidos que hay actualmente en el arreglo circular. */
    private int cantidad;

    /** Crea una cola vacía con capacidad inicial de ocho fichas. */
    public ColaFichas() {
        fichas = new int[CAPACIDAD_INICIAL];
    }

    /**
     * Agrega una ficha al final lógico de la cola.
     * Si frente + cantidad rebasa el último índice, el módulo hace que el
     * índice regrese al inicio del arreglo y reutilice el espacio circular.
     *
     * @param ficha dato asociado a la ficha (en este juego, su turno de entrada)
     */
    public void encolar(int ficha) {
        // No se puede distinguir entre lleno y vacío si se ocupa el arreglo entero;
        // por eso se duplica antes de insertar cuando cantidad == capacidad.
        if (cantidad == fichas.length) ampliar();
        int fin = (frente + cantidad) % fichas.length;
        fichas[fin] = ficha;
        cantidad++;
    }

    /**
     * Retira y devuelve el dato de la ficha que lleva más tiempo en la cola.
     *
     * @return dato guardado con la ficha ubicada al frente
     * @throws IllegalStateException si se intenta retirar de una cola vacía
     */
    public int desencolar() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía.");
        int ficha = fichas[frente];
        // Avanzar circularmente evita mover todos los demás elementos.
        frente = (frente + 1) % fichas.length;
        cantidad--;
        // Normalizar el frente facilita las inserciones cuando vuelve a quedar vacía.
        if (cantidad == 0) frente = 0;
        return ficha;
    }

    /** @return número de fichas que hay dentro de la cola */
    public int tamano() {
        return cantidad;
    }

    /** @return true cuando no hay fichas para retirar */
    public boolean estaVacia() {
        return cantidad == 0;
    }

    /**
     * Duplica la capacidad y copia los elementos desde el frente en orden FIFO.
     * Al copiar, la cola queda "desenrollada": su primer elemento pasa al índice 0.
     */
    private void ampliar() {
        int[] nuevoArreglo = new int[fichas.length * 2];
        for (int i = 0; i < cantidad; i++) {
            // El módulo también permite copiar correctamente cuando los datos
            // originales ocupan el final y el inicio del arreglo circular.
            nuevoArreglo[i] = fichas[(frente + i) % fichas.length];
        }
        fichas = nuevoArreglo;
        frente = 0;
    }
}
