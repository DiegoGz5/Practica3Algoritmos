package desdeCero;

// Por ahora cada ficha guarda solo el turno en que entró.
// Luego usaremos ese dato para calcular cuánto tardó en llegar al final.
public class ColaFichas {
    private static final int CAPACIDAD_INICIAL = 8;

    // El arreglo es la memoria de nuestra cola.
    private int[] turnosEntrada = new int[CAPACIDAD_INICIAL];

    // frente apunta a la ficha que debe salir primero.
    private int frente;

    // cantidad nos dice cuántos lugares del arreglo contienen fichas válidas.
    private int cantidad;

    // Agrega una ficha al final de la cola.
    public void encolar(int turnoEntrada) {
        // Si está llena, hacemos espacio antes de guardar la ficha nueva.
        if (cantidad == turnosEntrada.length) {
            ampliarArreglo();
        }

        // El final se calcula desde el frente más los elementos que ya hay.
        // El módulo (%) hace que el índice regrese a cero al llegar al límite.
        int fin = (frente + cantidad) % turnosEntrada.length;
        turnosEntrada[fin] = turnoEntrada;
        cantidad++;
    }

    // Saca la ficha más antigua y regresa el turno en que entró.
    public int desencolar() {
        if (estaVacia()) {
            throw new IllegalStateException("No se puede sacar una ficha de una cola vacía.");
        }

        int turnoEntrada = turnosEntrada[frente];

        // No movemos todas las fichas; solo avanzamos el frente una posición.
        frente = (frente + 1) % turnosEntrada.length;
        cantidad--;

        // Dejamos el frente en cero cuando ya no queda nada en la cola.
        if (cantidad == 0) {
            frente = 0;
        }

        return turnoEntrada;
    }

    public int tamano() {
        return cantidad;
    }

    public boolean estaVacia() {
        return cantidad == 0;
    }

    // Duplica el arreglo y copia las fichas respetando su orden actual.
    private void ampliarArreglo() {
        int[] arregloNuevo = new int[turnosEntrada.length * 2];

        for (int i = 0; i < cantidad; i++) {
            int indiceViejo = (frente + i) % turnosEntrada.length;
            arregloNuevo[i] = turnosEntrada[indiceViejo];
        }

        turnosEntrada = arregloNuevo;
        frente = 0;
    }
}
