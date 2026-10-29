import java.util.Random;

// Esta clase se encarga de las reglas y de guardar el estado de la partida.
// Hay 10 estaciones, 10 dados y 9 colas entre ellas. La interfaz consulta aquí
// los datos que necesita para mostrar el tablero y las gráficas.
public class JuegoDados {
    // Estos valores no cambian durante la partida.
    private static final int NUM_ESTACIONES = 10;
    // Una cola va entre cada par de estaciones, por eso son nueve.
    private static final int NUM_COLAS = NUM_ESTACIONES - 1;
    // La partida termina después de veinte turnos.
    public static final int MAX_TURNOS = 20;
    // En los arreglos usamos índices 0..9; en el tablero se ven como estaciones 1..10.
    private final Estacion[] estaciones = new Estacion[NUM_ESTACIONES];
    // colas[0] está entre E1 y E2, colas[1] entre E2 y E3, y así hasta E9-E10.
    private final ColaFichas[] colas = new ColaFichas[NUM_COLAS];
    // dados[0] es el dado 1, dados[1] es el dado 2, etc.
    private final Dado[] dados = new Dado[NUM_ESTACIONES];
    // Se comparte el mismo generador para sacar resultados aleatorios.
    private final Random random = new Random();
    // Estos arreglos guardan los datos que después se usan en la gráfica Activity.
    private final int[][] actividadMovida = new int[MAX_TURNOS][NUM_ESTACIONES];
    private final int[][] actividadLanzada = new int[MAX_TURNOS][NUM_ESTACIONES];
    // throughputPorTurno guarda el total acumulado al terminar cada ronda.
    private final int[] throughputPorTurno = new int[MAX_TURNOS];
    // La posición 0 guarda el estado inicial; las posiciones 1..20 son cada turno.
    private final int[] numeroEnSistemaPorTurno = new int[MAX_TURNOS + 1];
    // Guardamos cuánto tardaron en llegar a E10 las fichas que entraron durante la partida.
    private int[] tiemposEnSistema = new int[16];
    // Cuántas fichas ya salieron por E10, incluidas las que estaban desde el inicio.
    private int cantidadRecursosFinales;
    // Las fichas iniciales no tienen una ronda de entrada conocida para medir su tiempo.
    private int cantidadTiemposEnSistema;
    // Total de fichas que ya salieron por E10.
    private long throughput;
    // Cuántas rondas se han jugado hasta el momento.
    private int ronda;

    // Preparamos las estaciones, las colas, las fichas iniciales y los dados.
    public JuegoDados() {
        for (int i = 0; i < NUM_ESTACIONES; i++) estaciones[i] = new Estacion(i + 1);
        for (int i = 0; i < NUM_COLAS; i++) {
            ColaFichas cola = new ColaFichas();
            // Ponemos cuatro fichas en cada cola: son 36 en total y ya están desde el inicio.
            for (int ficha = 0; ficha < 4; ficha++) cola.encolar(0);
            colas[i] = cola;
        }
        // Al principio cada dado está en su estación correspondiente. Luego se pueden mover.
        for (int i = 0; i < NUM_ESTACIONES; i++) dados[i] = new Dado(i + 1, i + 1);
        numeroEnSistemaPorTurno[0] = contarFichasEnColas();
    }

    // La interfaz llama a este método cuando se cambia un selector de dados.
    public void asignarDado(int idDado, int numeroEstacion) {
        // El dado 1 está en el índice 0 del arreglo, por eso restamos uno.
        dados[idDado - 1].asignarA(numeroEstacion);
    }

    // Juega una ronda y regresa cuántas fichas salieron por la estación 10.
    // Primero se calcula todo y luego se mueven las fichas, así una ficha no
    // puede pasar por dos estaciones en una misma ronda.
    public int jugarRonda() {
        if (ronda >= MAX_TURNOS) throw new IllegalStateException("La partida termina después de 20 turnos.");
        int turnoActual = ronda + 1;
        // 1. Quitamos la capacidad de la ronda pasada.
        for (Estacion estacion : estaciones) estacion.reiniciarCapacidad();
        // 2. Lanzamos los dados y sumamos sus resultados en la estación asignada.
        for (Dado dado : dados) {
            int resultado = dado.lanzar(random);
            estaciones[dado.getEstacion() - 1].agregarCapacidad(resultado);
        }

        // 3. Calculamos cuántas fichas movería cada estación, sin tocar las colas aún.
        // E1 mete fichas nuevas. Las demás solo pueden mover las que ya tienen esperando.
        int[] mover = new int[NUM_ESTACIONES];
        mover[0] = estaciones[0].getCapacidad(); // E1 introduce trabajo
        for (int i = 1; i < NUM_ESTACIONES; i++) {
            mover[i] = Math.min(estaciones[i].getCapacidad(), colas[i - 1].tamano());
        }

        // 4. Sacamos las fichas que sí se procesaron. También guardamos cuándo entraron,
        // para no perder ese dato mientras pasan de una cola a otra.
        int[][] transferencias = new int[NUM_ESTACIONES][];
        transferencias[0] = new int[mover[0]];
        for (int n = 0; n < mover[0]; n++) transferencias[0][n] = turnoActual;
        for (int estacion = 1; estacion < NUM_ESTACIONES; estacion++) {
            int[] lote = new int[mover[estacion]];
            for (int n = 0; n < lote.length; n++) lote[n] = colas[estacion - 1].desencolar();
            if (estacion == NUM_ESTACIONES - 1) {
                cantidadRecursosFinales += lote.length;
                // Las fichas iniciales tienen turnoEntrada 0, así que no las graficamos:
                // no sabemos cuánto tiempo llevaban en el sistema antes de empezar.
                for (int turnoEntrada : lote) {
                    if (turnoEntrada > 0) guardarTiempoEnSistema(turnoActual - turnoEntrada);
                }
            } else {
                transferencias[estacion] = lote;
            }
        }

        // 5. Ahora sí ponemos cada lote en la siguiente cola. Como todos los movimientos
        // se calcularon antes, las fichas recién llegadas esperan hasta la siguiente ronda.
        for (int cola = 0; cola < NUM_COLAS; cola++) {
            for (int turnoEntrada : transferencias[cola]) colas[cola].encolar(turnoEntrada);
        }
        // 6. Guardamos lo que movió cada estación y cuánto le permitieron sus dados.
        for (int estacion = 0; estacion < NUM_ESTACIONES; estacion++) {
            actividadMovida[ronda][estacion] = mover[estacion];
            actividadLanzada[ronda][estacion] = estaciones[estacion].getCapacidad();
        }
        throughput += mover[NUM_ESTACIONES - 1];
        ronda++;
        // Guardamos los valores que van a necesitar las gráficas al final de la partida.
        throughputPorTurno[ronda - 1] = (int) throughput;
        numeroEnSistemaPorTurno[ronda] = contarFichasEnColas();
        return mover[NUM_ESTACIONES - 1];
    }

    // Anota el tiempo de una ficha que llegó a la salida. Si ya no cabe,
    // hacemos un arreglo más grande y copiamos lo que teníamos.
    private void guardarTiempoEnSistema(int tiempo) {
        if (cantidadTiemposEnSistema == tiemposEnSistema.length) {
            int[] ampliado = new int[tiemposEnSistema.length * 2];
            for (int i = 0; i < tiemposEnSistema.length; i++) ampliado[i] = tiemposEnSistema[i];
            tiemposEnSistema = ampliado;
        }
        tiemposEnSistema[cantidadTiemposEnSistema++] = tiempo;
    }

    private int contarFichasEnColas() {
        int total = 0;
        // Las fichas que ya llegaron al final no cuentan como fichas en proceso.
        for (ColaFichas cola : colas) total += cola.tamano();
        return total;
    }

    // Métodos get para que la interfaz y las gráficas puedan consultar los datos.
    public int getRonda() { return ronda; }
    public long getThroughput() { return throughput; }
    public int getCantidadCola(int indice) { return colas[indice].tamano(); }
    public int getCantidadRecursosFinales() { return cantidadRecursosFinales; }
    public int getCantidadTiemposEnSistema() { return cantidadTiemposEnSistema; }
    public int getTiempoEnSistema(int indice) { return tiemposEnSistema[indice]; }
    public int getNumeroEnSistema(int turno) { return numeroEnSistemaPorTurno[turno]; }
    public int getThroughputEnTurno(int turno) { return throughputPorTurno[turno - 1]; }
    public int getActividadMovida(int turno, int estacion) { return actividadMovida[turno - 1][estacion]; }
    public int getActividadLanzada(int turno, int estacion) { return actividadLanzada[turno - 1][estacion]; }
    public int getCapacidadEstacion(int indice) { return estaciones[indice].getCapacidad(); }
    public int getEstacionDado(int indiceDado) { return dados[indiceDado].getEstacion(); }
    public int getResultadoDado(int indiceDado) { return dados[indiceDado].getResultado(); }

}
