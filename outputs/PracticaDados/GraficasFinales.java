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

// Esta ventana muestra los resultados al terminar la partida. Las gráficas
// están dibujadas con Swing, así que no necesitamos instalar otra biblioteca.
public class GraficasFinales extends JFrame {
    // Usamos colores parecidos a los del tablero.
    private static final Color FONDO = new Color(20, 31, 48);
    private static final Color PANEL = new Color(29, 43, 62);
    private static final Color TEXTO = new Color(238, 243, 250);
    private static final Color AZUL = new Color(52, 174, 239);
    private static final Color VERDE = new Color(83, 220, 190);
    // Los números vienen de JuegoDados; aquí solo los mostramos.
    private final JuegoDados juego;

    // Creamos una pestaña para cada gráfica.
    public GraficasFinales(JuegoDados juego) {
        super("Resultados de la partida | 20 turnos");
        this.juego = juego;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 670);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);

        // Las pestañas permiten ver una gráfica a la vez.
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Activity", crearActividad());
        pestañas.addTab("Throughput", new Grafica("Throughput", "Turn", "Finished resources", TipoGrafica.THROUGHPUT));
        pestañas.addTab("Number in system", new Grafica("Number in system", "Turn", "Resources in queues", TipoGrafica.SISTEMA));
        pestañas.addTab("Time in system", new Grafica("Time in system", "Order of arrival", "Turns", TipoGrafica.TIEMPO));
        pestañas.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(pestañas);
    }

    // Activity tiene un selector para escoger una estación o verlas todas juntas.
    private JPanel crearActividad() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JComboBox<String> selector = new JComboBox<>();
        // Agregamos las estaciones y al final una opción para ver el total.
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

    // Le dice al panel qué tipo de gráfica tiene que dibujar.
    private enum TipoGrafica { ACTIVIDAD, THROUGHPUT, SISTEMA, TIEMPO }

    // Este panel se encarga de dibujar las barras y los ejes.
    private class Grafica extends JPanel {
        // Títulos que cambian según la gráfica.
        private final String titulo;
        private final String ejeX;
        private final String ejeY;
        // Sirve para saber qué datos pedirle al juego.
        private final TipoGrafica tipo;
        // 0..9 es una estación; 10 significa que se suman todas.
        private int estacionSeleccionada;

        // Guarda los títulos y el tipo de gráfica para usarlos al dibujar.
        Grafica(String titulo, String ejeX, String ejeY, TipoGrafica tipo) {
            this.titulo = titulo;
            this.ejeX = ejeX;
            this.ejeY = ejeY;
            this.tipo = tipo;
            setOpaque(true);
            setBackground(PANEL);
            setPreferredSize(new Dimension(940, 530));
        }

        // Cuando se cambia la estación, volvemos a dibujar la gráfica.
        void setEstacionSeleccionada(int estacion) {
            estacionSeleccionada = estacion;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            // Hacemos una copia del lápiz de dibujo para no cambiar el de Swing.
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = getWidth(), alto = getHeight();
            // Dejamos espacio a los lados para los números y nombres de los ejes.
            int izquierda = 76, derecha = ancho - 28, arriba = 48, abajo = alto - 74;
            int graficaAncho = derecha - izquierda, graficaAlto = abajo - arriba;

            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString(titulo, izquierda, 30);

            int cantidad = cantidadPuntos();
            int maximo = maximoValor();
            if (cantidad == 0) {
                // Si ninguna ficha llegó al final, no hay tiempos que graficar.
                dibujarCuadricula(g, izquierda, arriba, graficaAncho, graficaAlto, 1);
                g.setColor(TEXTO);
                g.setFont(new Font("SansSerif", Font.PLAIN, 15));
                g.drawString("No hay tiempos registrados para recursos ingresados durante la partida.",
                        izquierda + 20, arriba + graficaAlto / 2);
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

        // Dibuja las líneas horizontales y los números que sirven de escala.
        private void dibujarCuadricula(Graphics2D g, int x, int y, int ancho, int alto, int maximo) {
            int divisiones = 5;
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= divisiones; i++) {
                int yy = y + alto - (alto * i / divisiones);
                // Calculamos qué número corresponde a cada línea de la cuadrícula.
                int valor = (maximo * i + divisiones - 1) / divisiones;
                g.setColor(new Color(78, 94, 114));
                g.setStroke(new BasicStroke(i == 0 ? 1.5f : 1f));
                g.drawLine(x, yy, x + ancho, yy);
                g.setColor(new Color(190, 202, 216));
                String etiqueta = Integer.toString(valor);
                g.drawString(etiqueta, x - 12 - g.getFontMetrics().stringWidth(etiqueta), yy + 4);
            }
        }

        // Dibuja una barra por dato. Activity lleva dos: movido y capacidad lanzada.
        private void dibujarBarras(Graphics2D g, int x, int y, int ancho, int alto, int cantidad, int maximo) {
            double anchoGrupo = (double) ancho / cantidad;
            for (int i = 0; i < cantidad; i++) {
                int primero = valorPrimario(i);
                int xGrupo = x + (int) Math.round(i * anchoGrupo);
                if (tipo == TipoGrafica.ACTIVIDAD) {
                    // Verde muestra cuántas fichas se movieron; azul, cuánto dieron los dados.
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

        // Pasamos el número a píxeles para saber qué tan alta debe ser la barra.
        private void dibujarBarra(Graphics2D g, int x, int y, int alto, int maximo, int valor,
                                  int anchoBarra, Color color) {
            int altura = (int) Math.round((double) valor / maximo * alto);
            g.setColor(color);
            g.fillRoundRect(x, y + alto - altura, anchoBarra, altura, 5, 5);
        }

        // La leyenda ayuda a recordar qué significa cada color en Activity.
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

        // Escribe debajo de las barras el turno o el orden en que salió cada ficha.
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

        // Giramos el nombre para que quede escrito junto al eje vertical.
        private void dibujarEtiquetaEjeY(Graphics2D g, int arriba, int abajo) {
            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            Graphics2D rotado = (Graphics2D) g.create();
            rotado.rotate(-Math.PI / 2);
            FontMetrics fm = rotado.getFontMetrics();
            rotado.drawString(ejeY, -(arriba + abajo + fm.stringWidth(ejeY)) / 2, 18);
            rotado.dispose();
        }

        // Number in system tiene 21 datos: empieza en 0 y luego muestra los 20 turnos.
        private int cantidadPuntos() {
            if (tipo == TipoGrafica.SISTEMA) return JuegoDados.MAX_TURNOS + 1;
            if (tipo == TipoGrafica.TIEMPO) return juego.getCantidadTiemposEnSistema();
            return JuegoDados.MAX_TURNOS;
        }

        // Los arreglos empiezan en cero, pero en pantalla los turnos empiezan en uno.
        private int etiquetaPunto(int indice) {
            return tipo == TipoGrafica.SISTEMA ? indice : indice + 1;
        }

        // Aquí escogemos qué dato usar como altura de la barra.
        private int valorPrimario(int indice) {
            switch (tipo) {
                // Throughput va creciendo con las fichas que ya salieron hasta ese turno.
                case THROUGHPUT: return juego.getThroughputEnTurno(indice + 1);
                // Contamos las fichas que siguen en las nueve colas.
                case SISTEMA: return juego.getNumeroEnSistema(indice);
                // Cada barra es una ficha que llegó a la salida y muestra cuánto tardó.
                case TIEMPO: return juego.getTiempoEnSistema(indice);
                // La primera barra de Activity es lo que movió la estación.
                default: return actividad(indice, false);
            }
        }

        // La segunda barra de Activity muestra la capacidad que dieron sus dados.
        private int valorSecundario(int indice) {
            return actividad(indice, true);
        }

        // Busca el dato de una estación; si se eligió "todas", suma las diez.
        private int actividad(int indiceTurno, boolean lanzada) {
            int valor = 0;
            // En el combo, el 10 no es una estación: significa "All stations".
            int inicio = estacionSeleccionada == 10 ? 0 : estacionSeleccionada;
            int fin = estacionSeleccionada == 10 ? 10 : estacionSeleccionada + 1;
            for (int estacion = inicio; estacion < fin; estacion++) {
                valor += lanzada ? juego.getActividadLanzada(indiceTurno + 1, estacion)
                        : juego.getActividadMovida(indiceTurno + 1, estacion);
            }
            return valor;
        }

        // Encontramos el dato más grande para que todas las barras quepan en la gráfica.
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
