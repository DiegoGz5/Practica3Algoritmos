import java.util.Random;

// Aquí guardamos lo básico de cada dado: su número, en qué estación está
// y qué número salió la última vez que lo lanzamos.
public class Dado {
    // Este número no cambia; sirve para distinguir los dados.
    private final int id;
    // Número de estación, del 1 al 10.
    private int estacion;
    // Resultado más reciente. En cero mientras todavía no se lanza.
    private int resultado;

    // Creamos el dado y le damos su estación inicial.
    public Dado(int id, int estacionInicial) {
        this.id = id;
        this.estacion = estacionInicial;
    }

    // Cambia el dado de estación. Si el número no está entre 1 y 10, avisamos del error.
    public void asignarA(int estacion) {
        if (estacion < 1 || estacion > 10) throw new IllegalArgumentException("Estación debe ser 1..10.");
        this.estacion = estacion;
    }

    // Saca un número aleatorio del 1 al 6 y lo guarda para mostrarlo en pantalla.
    public int lanzar(Random random) {
        resultado = random.nextInt(6) + 1;
        return resultado;
    }

    // Regresa el número que identifica este dado.
    public int getId() { return id; }

    // Regresa la estación donde está ahora.
    public int getEstacion() { return estacion; }

    // Regresa el último resultado; antes del primer lanzamiento vale cero.
    public int getResultado() { return resultado; }
}
