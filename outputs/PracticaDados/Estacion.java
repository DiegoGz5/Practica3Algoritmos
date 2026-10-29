/**
 * Representa una etapa o recurso de procesamiento de la línea.
 * Su capacidad de un turno es la suma de los resultados de todos los dados
 * asignados a ella; reiniciar la capacidad no cambia sus dados asignados.
 */
public class Estacion {
    /** Número visible de la estación (1..10). */
    private final int numero;
    /** Capacidad acumulada para el turno actual. */
    private int capacidad;

    /** Crea una estación identificada por su número. */
    public Estacion(int numero) { this.numero = numero; }

    /** Suma el resultado de un dado a la capacidad de este turno. */
    public void agregarCapacidad(int valor) { capacidad += valor; }

    /** @return número de estación */
    public int getNumero() { return numero; }

    /** @return suma de los resultados de sus dados en el turno actual */
    public int getCapacidad() { return capacidad; }

    /** Comienza la siguiente ronda borrando solo la capacidad del turno anterior. */
    public void reiniciarCapacidad() { capacidad = 0; }
}
