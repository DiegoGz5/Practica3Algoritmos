// Una estación representa uno de los pasos de la línea de producción.
// Su capacidad es la suma de los dados que tiene en el turno actual.
public class Estacion {
    // Número que vemos en el tablero, del 1 al 10.
    private final int numero;
    // Aquí vamos sumando lo que salió en los dados este turno.
    private int capacidad;

    // Creamos la estación con su número.
    public Estacion(int numero) { this.numero = numero; }

    // Si hay varios dados aquí, vamos sumando sus resultados.
    public void agregarCapacidad(int valor) { capacidad += valor; }

    // Devuelve el número de estación.
    public int getNumero() { return numero; }

    // Devuelve la capacidad que juntó en este turno.
    public int getCapacidad() { return capacidad; }

    // Al empezar otra ronda borramos la capacidad anterior, pero los dados siguen aquí.
    public void reiniciarCapacidad() { capacidad = 0; }
}
