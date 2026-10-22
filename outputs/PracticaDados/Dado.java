import java.util.Random;

/** Dado de seis caras y la estación a la que está asignado. */
public class Dado {
    private final int id;
    private int estacion; // 1..10
    private int resultado;

    public Dado(int id, int estacionInicial) {
        this.id = id;
        this.estacion = estacionInicial;
    }

    public void asignarA(int estacion) {
        if (estacion < 1 || estacion > 10) throw new IllegalArgumentException("Estación debe ser 1..10.");
        this.estacion = estacion;
    }

    public int lanzar(Random random) {
        resultado = random.nextInt(6) + 1;
        return resultado;
    }

    public int getId() { return id; }
    public int getEstacion() { return estacion; }
    public int getResultado() { return resultado; }
}
