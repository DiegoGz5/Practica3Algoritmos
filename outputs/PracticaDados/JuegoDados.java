import java.util.Random;
import java.util.Scanner;

/**
 * Modelo principal de The Dice Game 2: diez estaciones, diez dados y nueve
 * colas entre estaciones. Aquí vive la lógica del juego y los datos que usan
 * tanto la interfaz gráfica como la ventana de resultados. Las estaciones,
 * dados y colas se guardan en arreglos; cada ColaFichas también está hecha con
 * un arreglo circular.
 */
public class JuegoDados {
    /** Cantidad fija de estaciones del circuito. */
    private static final int NUM_ESTACIONES = 10;
    /** Entre diez estaciones hay nueve espacios de espera: E1→E2, ..., E9→E10. */
    private static final int NUM_COLAS = NUM_ESTACIONES - 1;
    /** Duración de cada partida y tamaño de las tablas de actividad por turno. */
    public static final int MAX_TURNOS = 20;
    /** Las estaciones se indexan como [0..9], aunque se muestran como 1..10. */
    private final Estacion[] estaciones = new Estacion[NUM_ESTACIONES];
    /** Cola i guarda fichas que esperan pasar de estación i+1 a i+2. */
    private final ColaFichas[] colas = new ColaFichas[NUM_COLAS];
    /** Dado i corresponde al dado con identificador i+1. */
    private final Dado[] dados = new Dado[NUM_ESTACIONES];
    /** Generador usado para todos los lanzamientos de la partida. */
    private final Random random = new Random();
    /** Recursos procesados por estación y turno (índices turno-1 y estación-1). */
    private final int[][] actividadMovida = new int[MAX_TURNOS][NUM_ESTACIONES];
    /** Capacidad lanzada por estación y turno; equivale a la suma de sus dados. */
    private final int[][] actividadLanzada = new int[MAX_TURNOS][NUM_ESTACIONES];
    /** Throughput acumulado al final de cada turno (las salidas de la estación 10). */
    private final int[] throughputPorTurno = new int[MAX_TURNOS];
    /** Fichas en las nueve colas: posición 0 es el inicio, posiciones 1..20 los turnos. */
    private final int[] numeroEnSistemaPorTurno = new int[MAX_TURNOS + 1];
    /** Una entrada por ficha terminada; guarda su duración en turnos dentro del sistema. */
    private int[] tiemposEnSistema = new int[16];
    /** Cantidad de entradas válidas en tiemposEnSistema y número total de fichas terminadas. */
    private int cantidadRecursosFinales;
    /** Total de fichas que llegaron a la salida a lo largo de la partida. */
    private long throughput;
    /** Número de turnos completados hasta ahora, desde 0 hasta MAX_TURNOS. */
    private int ronda;

    /**
     * Prepara la línea. Cada una de las nueve colas empieza con cuatro fichas
     * (36 en total); las fichas iniciales tienen turno de entrada 0. Cada dado
     * comienza asignado a la estación con el mismo número.
     */
    public JuegoDados() {
        for (int i = 0; i < NUM_ESTACIONES; i++) estaciones[i] = new Estacion(i + 1);
        for (int i = 0; i < NUM_COLAS; i++) {
            ColaFichas cola = new ColaFichas();
            // Las 36 fichas iniciales ya están en el sistema al comenzar el turno 0.
            for (int ficha = 0; ficha < 4; ficha++) cola.encolar(0);
            colas[i] = cola;
        }
        // Inicio: un dado en cada estación. En Game 2 se pueden reasignar antes de cada ronda.
        for (int i = 0; i < NUM_ESTACIONES; i++) dados[i] = new Dado(i + 1, i + 1);
        numeroEnSistemaPorTurno[0] = contarFichasEnColas();
    }

    /** Pide en consola una estación nueva para cada dado; Enter conserva la actual. */
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
        // El identificador empieza en 1, pero el arreglo empieza en el índice 0.
        dados[idDado - 1].asignarA(numeroEstacion);
    }

    /**
     * Ejecuta un turno completo y devuelve cuántas fichas llegaron a la salida.
     * El orden de las fases es importante: primero se lanzan los dados, luego
     * se calcula cuánto puede procesar cada estación usando las colas del
     * inicio del turno, y finalmente se aplican todas las transferencias.
     * Así una ficha no puede avanzar dos estaciones en un mismo turno.
     *
     * @return fichas que procesó la estación 10 en este turno
     * @throws IllegalStateException si ya se completaron los veinte turnos
     */
    public int jugarRonda() {
        if (ronda >= MAX_TURNOS) throw new IllegalStateException("La partida termina después de 20 turnos.");
        int turnoActual = ronda + 1;
        // 1. La capacidad corresponde solo a este turno; se borran los valores anteriores.
        for (Estacion estacion : estaciones) estacion.reiniciarCapacidad();
        // 2. Cada dado se lanza y su valor suma capacidad a su estación asignada.
        for (Dado dado : dados) {
            int resultado = dado.lanzar(random);
            estaciones[dado.getEstacion() - 1].agregarCapacidad(resultado);
        }

        // 3. Calcula los movimientos sin modificar ninguna cola todavía.
        // E1 es el proveedor de entrada y genera mover[0] fichas nuevas.
        // Las estaciones E2..E10 no pueden mover más fichas que las disponibles
        // en su cola de entrada, aunque sus dados den una capacidad mayor.
        int[] mover = new int[NUM_ESTACIONES];
        mover[0] = estaciones[0].getCapacidad(); // E1 introduce trabajo
        for (int i = 1; i < NUM_ESTACIONES; i++) {
            mover[i] = Math.min(estaciones[i].getCapacidad(), colas[i - 1].tamano());
        }

        // 4. Retira de cada cola las fichas procesadas y guarda sus turnos de entrada.
        // transferencias[i] es el lote que la estación i+1 enviará a la cola i.
        // La última estación no tiene cola siguiente: registra las fichas como terminadas.
        int[][] transferencias = new int[NUM_ESTACIONES][];
        transferencias[0] = new int[mover[0]];
        for (int n = 0; n < mover[0]; n++) transferencias[0][n] = turnoActual;
        for (int estacion = 1; estacion < NUM_ESTACIONES; estacion++) {
            int[] lote = new int[mover[estacion]];
            for (int n = 0; n < lote.length; n++) lote[n] = colas[estacion - 1].desencolar();
            if (estacion == NUM_ESTACIONES - 1) {
                for (int turnoEntrada : lote) guardarTiempoEnSistema(turnoActual - turnoEntrada);
            } else {
                transferencias[estacion] = lote;
            }
        }

        // 5. Una vez retiradas todas las fichas antiguas, se depositan los lotes
        // en sus colas siguientes. Esto mantiene simultáneas las transferencias.
        for (int cola = 0; cola < NUM_COLAS; cola++) {
            for (int turnoEntrada : transferencias[cola]) colas[cola].encolar(turnoEntrada);
        }
        // 6. Guarda las dos medidas de actividad: capacidad lanzada y fichas movidas.
        for (int estacion = 0; estacion < NUM_ESTACIONES; estacion++) {
            actividadMovida[ronda][estacion] = mover[estacion];
            actividadLanzada[ronda][estacion] = estaciones[estacion].getCapacidad();
        }
        throughput += mover[NUM_ESTACIONES - 1];
        ronda++;
        // Las gráficas usan valores acumulados o inventario al cierre de cada turno.
        throughputPorTurno[ronda - 1] = (int) throughput;
        numeroEnSistemaPorTurno[ronda] = contarFichasEnColas();
        return mover[NUM_ESTACIONES - 1];
    }

    /**
     * Conserva el tiempo de una ficha terminada. Si el arreglo se llena, crea
     * otro del doble de capacidad y copia los tiempos anteriores.
     */
    private void guardarTiempoEnSistema(int tiempo) {
        if (cantidadRecursosFinales == tiemposEnSistema.length) {
            int[] ampliado = new int[tiemposEnSistema.length * 2];
            for (int i = 0; i < tiemposEnSistema.length; i++) ampliado[i] = tiemposEnSistema[i];
            tiemposEnSistema = ampliado;
        }
        tiemposEnSistema[cantidadRecursosFinales++] = tiempo;
    }

    private int contarFichasEnColas() {
        int total = 0;
        // Las fichas ya terminadas están en salida y no cuentan como WIP.
        for (ColaFichas cola : colas) total += cola.tamano();
        return total;
    }

    /** @return turnos completados */
    public int getRonda() { return ronda; }
    /** @return throughput final acumulado de la partida */
    public long getThroughput() { return throughput; }
    /** @return fichas que actualmente esperan en la cola indicada (índice 0..8) */
    public int getCantidadCola(int indice) { return colas[indice].tamano(); }
    /** @return número de fichas que han llegado a la salida */
    public int getCantidadRecursosFinales() { return cantidadRecursosFinales; }
    /** @return duración de la ficha terminada en el orden de llegada indicado */
    public int getTiempoEnSistema(int indice) { return tiemposEnSistema[indice]; }
    /** @param turno instante de medición, de 0 (inicio) a 20 */
    public int getNumeroEnSistema(int turno) { return numeroEnSistemaPorTurno[turno]; }
    /** @param turno turno de juego, de 1 a 20 */
    public int getThroughputEnTurno(int turno) { return throughputPorTurno[turno - 1]; }
    /** @param turno turno de juego (1..20), @param estacion índice de estación (0..9) */
    public int getActividadMovida(int turno, int estacion) { return actividadMovida[turno - 1][estacion]; }
    /** @param turno turno de juego (1..20), @param estacion índice de estación (0..9) */
    public int getActividadLanzada(int turno, int estacion) { return actividadLanzada[turno - 1][estacion]; }
    /** @param indice índice de estación (0..9) */
    public int getCapacidadEstacion(int indice) { return estaciones[indice].getCapacidad(); }
    /** @param indiceDado índice de dado (0..9), no identificador visible */
    public int getEstacionDado(int indiceDado) { return dados[indiceDado].getEstacion(); }
    /** @param indiceDado índice de dado (0..9) */
    public int getResultadoDado(int indiceDado) { return dados[indiceDado].getResultado(); }

    /** Punto de entrada para ejecutar la versión de consola del modelo. */
    public static void main(String[] args) {
        JuegoDados juego = new JuegoDados();
        try (Scanner entrada = new Scanner(System.in)) {
            System.out.println("The Dice Game — Juego 2: reasignación de recursos");
            System.out.print("¿Cuántas rondas quieres jugar? ");
            int rondas;
            // Se valida que la versión de consola tampoco exceda el máximo de veinte.
            try { rondas = Integer.parseInt(entrada.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Número inválido."); return; }
            if (rondas < 1 || rondas > MAX_TURNOS) {
                System.out.println("Indica entre 1 y " + MAX_TURNOS + " rondas."); return;
            }

            for (int i = 0; i < rondas; i++) {
                System.out.println("\n--- Preparación de ronda " + (i + 1) + " ---");
                // En cada ronda el usuario puede mover cada dado o presionar Enter.
                juego.reasignarDados(entrada);
                int terminadas = juego.jugarRonda();
                System.out.printf("Ronda %d | terminadas: %d | throughput total: %d%n",
                        juego.getRonda(), terminadas, juego.getThroughput());
                // Muestra inventario pendiente de cada tramo después del movimiento.
                for (int cola = 0; cola < NUM_COLAS; cola++)
                    System.out.printf("Cola %d (E%d→E%d): %d fichas%n", cola + 1, cola + 1, cola + 2,
                            juego.getCantidadCola(cola));
            }
        }
    }
}
