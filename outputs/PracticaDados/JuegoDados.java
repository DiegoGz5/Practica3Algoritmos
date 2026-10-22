import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Simulación de consola de The Dice Game, versión 2 (reasignación de recursos). */
public class JuegoDados {
    private static final int NUM_ESTACIONES = 10;
    private static final int NUM_COLAS = NUM_ESTACIONES - 1;
    private final List<Estacion> estaciones = new ArrayList<>();
    private final List<ColaFichas> colas = new ArrayList<>();
    private final List<Dado> dados = new ArrayList<>();
    private final Random random = new Random();
    private long throughput;
    private int ronda;

    public JuegoDados() {
        for (int i = 1; i <= NUM_ESTACIONES; i++) estaciones.add(new Estacion(i));
        for (int i = 0; i < NUM_COLAS; i++) {
            ColaFichas cola = new ColaFichas();
            for (int ficha = 0; ficha < 4; ficha++) cola.encolar(1);
            colas.add(cola);
        }
        // Inicio: un dado en cada estación. En Game 2 se pueden reasignar antes de cada ronda.
        for (int i = 1; i <= NUM_ESTACIONES; i++) dados.add(new Dado(i, i));
    }

    private void reasignarDados(Scanner entrada) {
        System.out.println("Asigna cada uno de los 10 dados a una estación (1-10).");
        System.out.println("Pulsa Enter para conservar las asignaciones actuales.");
        for (Dado dado : dados) {
            System.out.printf("Dado %d (actualmente E%d): ", dado.getId(), dado.getEstacion());
            String linea = entrada.nextLine().trim();
            if (linea.isEmpty()) continue;
            try { dado.asignarA(Integer.parseInt(linea)); }
            catch (RuntimeException e) { System.out.println("Entrada inválida; se conserva E" + dado.getEstacion() + "."); }
        }
    }

    public void asignarDado(int idDado, int numeroEstacion) {
        dados.get(idDado - 1).asignarA(numeroEstacion);
    }

    /** Ejecuta una ronda y devuelve cuántas fichas llegaron a la salida en ella. */
    public int jugarRonda() {
        for (Estacion estacion : estaciones) estacion.reiniciarCapacidad();
        for (Dado dado : dados) {
            int resultado = dado.lanzar(random);
            estaciones.get(dado.getEstacion() - 1).agregarCapacidad(resultado);
        }

        // Calcula todos los movimientos con el estado previo; no se procesa lo recién recibido.
        int[] mover = new int[NUM_ESTACIONES];
        mover[0] = estaciones.get(0).getCapacidad(); // E1 introduce trabajo
        for (int i = 1; i < NUM_ESTACIONES; i++) {
            mover[i] = Math.min(estaciones.get(i).getCapacidad(), colas.get(i - 1).tamano());
        }

        // Primero se retira lo procesado; después se añade lo enviado desde la estación anterior.
        for (int i = 0; i < NUM_COLAS; i++) {
            for (int n = 0; n < mover[i + 1]; n++) colas.get(i).desencolar();
            for (int n = 0; n < mover[i]; n++) colas.get(i).encolar(1);
        }
        throughput += mover[NUM_ESTACIONES - 1];
        ronda++;
        return mover[NUM_ESTACIONES - 1];
    }

    public int getRonda() { return ronda; }
    public long getThroughput() { return throughput; }
    public int getCantidadCola(int indice) { return colas.get(indice).tamano(); }
    public int getCapacidadEstacion(int indice) { return estaciones.get(indice).getCapacidad(); }
    public int getEstacionDado(int indiceDado) { return dados.get(indiceDado).getEstacion(); }
    public int getResultadoDado(int indiceDado) { return dados.get(indiceDado).getResultado(); }

    public static void main(String[] args) {
        JuegoDados juego = new JuegoDados();
        try (Scanner entrada = new Scanner(System.in)) {
            System.out.println("The Dice Game — Juego 2: reasignación de recursos");
            System.out.print("¿Cuántas rondas quieres jugar? ");
            int rondas;
            try { rondas = Integer.parseInt(entrada.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Número inválido."); return; }
            if (rondas < 1) { System.out.println("Indica al menos una ronda."); return; }

            for (int i = 0; i < rondas; i++) {
                System.out.println("\n--- Preparación de ronda " + (i + 1) + " ---");
                juego.reasignarDados(entrada);
                int terminadas = juego.jugarRonda();
                System.out.printf("Ronda %d | terminadas: %d | throughput total: %d%n",
                        juego.getRonda(), terminadas, juego.getThroughput());
                for (int cola = 0; cola < NUM_COLAS; cola++)
                    System.out.printf("Cola %d (E%d→E%d): %d fichas%n", cola + 1, cola + 1, cola + 2,
                            juego.getCantidadCola(cola));
            }
        }
    }
}
