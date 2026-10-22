/** Cola FIFO de fichas, implementada con nodos enlazados simples. */
public class ColaFichas {
    private static class Nodo {
        private final int ficha;
        private Nodo siguiente;

        private Nodo(int ficha) { this.ficha = ficha; }
    }

    private Nodo frente;
    private Nodo fin;
    private int tamano;

    public void encolar(int ficha) {
        Nodo nuevo = new Nodo(ficha);
        if (fin == null) frente = nuevo;
        else fin.siguiente = nuevo;
        fin = nuevo;
        tamano++;
    }

    public int desencolar() {
        if (frente == null) throw new IllegalStateException("La cola está vacía.");
        int valor = frente.ficha;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamano--;
        return valor;
    }

    public int tamano() { return tamano; }
}
