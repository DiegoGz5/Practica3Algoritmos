import java.util.Random;

/**
 * Representa uno de los diez dados de seis caras del juego.
 * El dado conserva su identificador, la estación a la que está asignado
 * y el resultado más reciente de su lanzamiento.
 */
public class Dado {
    /** Número fijo que identifica este dado en los controles de la interfaz. */
    private final int id;
    /** Estación asignada, numerada del 1 al 10 (a diferencia de los índices de arreglos). */
    private int estacion;
    /** Cara obtenida en el último lanzamiento; inicia en cero para indicar "sin lanzar". */
    private int resultado;

    /**
     * Crea un dado y lo coloca inicialmente en una estación.
     *
     * @param id identificador del dado, del 1 al 10
     * @param estacionInicial estación inicial asignada
     */
    public Dado(int id, int estacionInicial) {
        this.id = id;
        this.estacion = estacionInicial;
    }

    /**
     * Asigna el dado a una estación, rechazando números fuera del tablero.
     *
     * @param estacion número de estación, entre 1 y 10
     */
    public void asignarA(int estacion) {
        if (estacion < 1 || estacion > 10) throw new IllegalArgumentException("Estación debe ser 1..10.");
        this.estacion = estacion;
    }

    /**
     * Simula un lanzamiento uniforme de seis caras y recuerda su resultado.
     *
     * @param random generador aleatorio compartido por el modelo del juego
     * @return un número entre 1 y 6
     */
    public int lanzar(Random random) {
        resultado = random.nextInt(6) + 1;
        return resultado;
    }

    /** @return identificador del dado */
    public int getId() { return id; }

    /** @return estación actualmente asignada, del 1 al 10 */
    public int getEstacion() { return estacion; }

    /** @return último resultado lanzado, o cero si todavía no se ha lanzado */
    public int getResultado() { return resultado; }
}
