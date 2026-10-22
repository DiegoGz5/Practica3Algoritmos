/** Recurso de procesamiento. Su capacidad es la suma de sus dados. */
public class Estacion {
    private final int numero;
    private int capacidad;

    public Estacion(int numero) { this.numero = numero; }
    public void agregarCapacidad(int valor) { capacidad += valor; }
    public int getNumero() { return numero; }
    public int getCapacidad() { return capacidad; }
    public void reiniciarCapacidad() { capacidad = 0; }
}
