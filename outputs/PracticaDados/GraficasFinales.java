import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Ventana de resultados que dibuja las cuatro métricas guardadas por JuegoDados.
 * Se usan paneles Swing propios, sin bibliotecas de gráficas externas, para
 * que el proyecto siga siendo fácil de abrir en IntelliJ.
 */
public class GraficasFinales extends JFrame {
    /** Colores compartidos por las pestañas para conservar el estilo del tablero. */
    private static final Color FONDO = new Color(20, 31, 48);
    private static final Color PANEL = new Color(29, 43, 62);
    private static final Color TEXTO = new Color(238, 243, 250);
    private static final Color AZUL = new Color(52, 174, 239);
    private static final Color VERDE = new Color(83, 220, 190);
    /** El modelo entrega los datos; esta clase solo selecciona y dibuja series. */
    private final JuegoDados juego;

    /** Construye una pestaña para cada métrica solicitada. */
    public GraficasFinales(JuegoDados juego) {
        super("Resultados de la partida | 20 turnos");
        this.juego = juego;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 670);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);

        // JTabbedPane deja explorar cada gráfica sin encogerlas todas en una pantalla.
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Activity", crearActividad());
        pestañas.addTab("Throughput", new Grafica("Throughput", "Turn", "Finished resources", TipoGrafica.THROUGHPUT));
        pestañas.addTab("Number in system", new Grafica("Number in system", "Turn", "Resources in queues", TipoGrafica.SISTEMA));
        pestañas.addTab("Time in system", new Grafica("Time in system", "Order of arrival", "Turns", TipoGrafica.TIEMPO));
        pestañas.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(pestañas);
    }

    /** Crea la pestaña de actividad e incluye un selector de estación. */
    private JPanel crearActividad() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JComboBox<String> selector = new JComboBox<>();
        // La opción final suma la actividad de las diez estaciones.
        for (int i = 1; i <= 10; i++) selector.addItem("Station " + i);
        selector.addItem("All stations");
        selector.setSelectedIndex(0);
        Grafica grafica = new Grafica("Activity", "Turn", "Activity", TipoGrafica.ACTIVIDAD);
        selector.addActionListener(event -> grafica.setEstacionSeleccionada(selector.getSelectedIndex()));
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setOpaque(false);
        encabezado.add(selector, BorderLayout.WEST);
        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(grafica, BorderLayout.CENTER);
        return panel;
    }

    /** Indica a un mismo panel qué serie debe consultar y qué escala usar. */
    private enum TipoGrafica { ACTIVIDAD, THROUGHPUT, SISTEMA, TIEMPO }

    /** Panel que calcula los valores visibles y dibuja barras, ejes y leyendas. */
    private class Grafica extends JPanel {
        /** Texto de encabezado y de los ejes para esta métrica. */
        private final String titulo;
        private final String ejeX;
        private final String ejeY;
        /** Tipo de datos que se consultan en JuegoDados. */
        private final TipoGrafica tipo;
        /** Índice 0..9 para una estación; 10 representa "All stations". */
        private int estacionSeleccionada;

        /** Crea un panel gráfico para la métrica indicada. */
        Grafica(String titulo, String ejeX, String ejeY, TipoGrafica tipo) {
            this.titulo = titulo;
            this.ejeX = ejeX;
            this.ejeY = ejeY;
            this.tipo = tipo;
            setOpaque(true);
            setBackground(PANEL);
            setPreferredSize(new Dimension(940, 530));
        }

        /** Actualiza la estación visible y solicita un nuevo pintado de Swing. */
        void setEstacionSeleccionada(int estacion) {
            estacionSeleccionada = estacion;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            // Se crea una copia para que los cambios de color, giro y trazo no
            // alteren el contexto gráfico que Swing comparte con otros componentes.
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = getWidth(), alto = getHeight();
            // Márgenes reservados para números del eje Y, categorías y títulos.
            int izquierda = 76, derecha = ancho - 28, arriba = 48, abajo = alto - 74;
            int graficaAncho = derecha - izquierda, graficaAlto = abajo - arriba;

            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString(titulo, izquierda, 30);

            int cantidad = cantidadPuntos();
            int maximo = maximoValor();
            if (cantidad == 0) {
                // Time in system puede no tener puntos si ninguna ficha alcanzó la salida.
                dibujarCuadricula(g, izquierda, arriba, graficaAncho, graficaAlto, 1);
                g.setColor(TEXTO);
                g.setFont(new Font("SansSerif", Font.PLAIN, 15));
                g.drawString("No hubo recursos terminados durante esta partida.", izquierda + 35, arriba + graficaAlto / 2);
            } else {
                dibujarCuadricula(g, izquierda, arriba, graficaAncho, graficaAlto, maximo);
                dibujarBarras(g, izquierda, arriba, graficaAncho, graficaAlto, cantidad, maximo);
                dibujarEtiquetasX(g, izquierda, derecha, abajo, cantidad);
            }
            dibujarEtiquetaEjeY(g, arriba, abajo);
            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(ejeX, izquierda + (graficaAncho - fm.stringWidth(ejeX)) / 2, alto - 20);
            g.dispose();
        }

        /** Dibuja cinco intervalos horizontales y sus valores numéricos. */
        private void dibujarCuadricula(Graphics2D g, int x, int y, int ancho, int alto, int maximo) {
            int divisiones = 5;
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= divisiones; i++) {
                int yy = y + alto - (alto * i / divisiones);
                // Convierte la posición de la línea (de abajo hacia arriba) en valor.
                int valor = (maximo * i + divisiones - 1) / divisiones;
                g.setColor(new Color(78, 94, 114));
                g.setStroke(new BasicStroke(i == 0 ? 1.5f : 1f));
                g.drawLine(x, yy, x + ancho, yy);
                g.setColor(new Color(190, 202, 216));
                String etiqueta = Integer.toString(valor);
                g.drawString(etiqueta, x - 12 - g.getFontMetrics().stringWidth(etiqueta), yy + 4);
            }
        }

        /** Dibuja una barra por turno, o dos barras agrupadas para la actividad. */
        private void dibujarBarras(Graphics2D g, int x, int y, int ancho, int alto, int cantidad, int maximo) {
            double anchoGrupo = (double) ancho / cantidad;
            for (int i = 0; i < cantidad; i++) {
                int primero = valorPrimario(i);
                int xGrupo = x + (int) Math.round(i * anchoGrupo);
                if (tipo == TipoGrafica.ACTIVIDAD) {
                    // Verde = recursos movidos; azul = capacidad de los dados ("rolled").
                    int segundo = valorSecundario(i);
                    int anchoBarra = Math.max(2, (int) (anchoGrupo * 0.32));
                    dibujarBarra(g, xGrupo + (int) (anchoGrupo * 0.16), y, alto, maximo,
                            primero, anchoBarra, VERDE);
                    dibujarBarra(g, xGrupo + (int) (anchoGrupo * 0.54), y, alto, maximo,
                            segundo, anchoBarra, AZUL);
                } else {
                    int anchoBarra = Math.max(2, (int) (anchoGrupo * 0.72));
                    dibujarBarra(g, xGrupo + (int) (anchoGrupo * 0.14), y, alto, maximo,
                            primero, anchoBarra, AZUL);
                }
            }
            if (tipo == TipoGrafica.ACTIVIDAD) dibujarLeyenda(g, x + ancho - 150, 20);
        }

        /** Escala un valor a píxeles de altura y pinta el rectángulo de la barra. */
        private void dibujarBarra(Graphics2D g, int x, int y, int alto, int maximo, int valor,
                                  int anchoBarra, Color color) {
            int altura = (int) Math.round((double) valor / maximo * alto);
            g.setColor(color);
            g.fillRoundRect(x, y + alto - altura, anchoBarra, altura, 5, 5);
        }

        /** Explica los colores de las dos series de Activity. */
        private void dibujarLeyenda(Graphics2D g, int x, int y) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(VERDE);
            g.fillRect(x, y - 8, 10, 10);
            g.setColor(TEXTO);
            g.drawString("Moved", x + 15, y);
            g.setColor(AZUL);
            g.fillRect(x + 68, y - 8, 10, 10);
            g.setColor(TEXTO);
            g.drawString("Rolled", x + 83, y);
        }

        /** Pone el número de turno o el orden de salida bajo cada barra. */
        private void dibujarEtiquetasX(Graphics2D g, int izquierda, int derecha, int abajo, int cantidad) {
            g.setColor(new Color(190, 202, 216));
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            int saltar = cantidad > 24 ? 2 : 1;
            for (int i = 0; i < cantidad; i += saltar) {
                int centro = izquierda + (int) (((double) i + 0.5) * (derecha - izquierda) / cantidad);
                String texto = Integer.toString(etiquetaPunto(i));
                g.drawString(texto, centro - g.getFontMetrics().stringWidth(texto) / 2, abajo + 15);
            }
        }

        /** Dibuja el nombre vertical del eje Y usando una copia rotada del contexto. */
        private void dibujarEtiquetaEjeY(Graphics2D g, int arriba, int abajo) {
            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            Graphics2D rotado = (Graphics2D) g.create();
            rotado.rotate(-Math.PI / 2);
            FontMetrics fm = rotado.getFontMetrics();
            rotado.drawString(ejeY, -(arriba + abajo + fm.stringWidth(ejeY)) / 2, 18);
            rotado.dispose();
        }

        /** Número de barras: WIP tiene el estado inicial más veinte turnos. */
        private int cantidadPuntos() {
            if (tipo == TipoGrafica.SISTEMA) return JuegoDados.MAX_TURNOS + 1;
            if (tipo == TipoGrafica.TIEMPO) return juego.getCantidadRecursosFinales();
            return JuegoDados.MAX_TURNOS;
        }

        /** Convierte el índice interno a la etiqueta visible en el eje horizontal. */
        private int etiquetaPunto(int indice) {
            return tipo == TipoGrafica.SISTEMA ? indice : indice + 1;
        }

        /** Obtiene el valor principal de la métrica para una barra. */
        private int valorPrimario(int indice) {
            switch (tipo) {
                // Throughput se guarda acumulado: cada barra no es solo lo que
                // salió ese turno, sino todo lo que ha salido hasta ese turno.
                case THROUGHPUT: return juego.getThroughputEnTurno(indice + 1);
                // Number in system incluye el estado inicial (índice 0) y luego
                // el inventario que permanece en las nueve colas al final de cada turno.
                case SISTEMA: return juego.getNumeroEnSistema(indice);
                // Time in system usa el orden en que las fichas llegan a la salida.
                case TIEMPO: return juego.getTiempoEnSistema(indice);
                // En Activity la serie principal es el número de fichas movidas.
                default: return actividad(indice, false);
            }
        }

        /** En Activity la segunda serie es la capacidad lanzada; otras gráficas no la usan. */
        private int valorSecundario(int indice) {
            return actividad(indice, true);
        }

        /** Suma la serie de una estación o de todas para el turno indicado. */
        private int actividad(int indiceTurno, boolean lanzada) {
            int valor = 0;
            // Las estaciones del arreglo son 0..9, pero la opción 10 del combo
            // significa sumar las diez estaciones.
            int inicio = estacionSeleccionada == 10 ? 0 : estacionSeleccionada;
            int fin = estacionSeleccionada == 10 ? 10 : estacionSeleccionada + 1;
            for (int estacion = inicio; estacion < fin; estacion++) {
                valor += lanzada ? juego.getActividadLanzada(indiceTurno + 1, estacion)
                        : juego.getActividadMovida(indiceTurno + 1, estacion);
            }
            return valor;
        }

        /** Busca el valor mayor para escalar todas las barras sin recortarlas. */
        private int maximoValor() {
            int maximo = 1;
            for (int i = 0; i < cantidadPuntos(); i++) {
                maximo = Math.max(maximo, valorPrimario(i));
                if (tipo == TipoGrafica.ACTIVIDAD) maximo = Math.max(maximo, valorSecundario(i));
            }
            return maximo;
        }
    }
}
