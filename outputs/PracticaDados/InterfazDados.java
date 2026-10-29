import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Ventana principal del juego. Presenta el circuito, las colas visibles,
 * los personajes alienígenas y los controles para reasignar los diez dados.
 * La lógica de turnos está en JuegoDados; esta clase se encarga de la vista.
 */
public class InterfazDados extends JFrame {
    /** Paleta común del fondo, las tarjetas y los controles. */
    private static final Color FONDO = new Color(20, 31, 48);
    private static final Color TARJETA = new Color(34, 49, 69);
    private static final Color ACENTO = new Color(83, 220, 190);
    private static final Color TEXTO = new Color(238, 243, 250);
    /** Un color distinto para cada alien del tablero. */
    private static final Color[] CAMISETAS = {
            new Color(91, 220, 112), new Color(175, 104, 236), new Color(64, 201, 220),
            new Color(247, 147, 54), new Color(244, 104, 180), new Color(112, 226, 186),
            new Color(102, 157, 255), new Color(209, 230, 70), new Color(66, 190, 180),
            new Color(238, 105, 112)
    };

    /** Modelo que conserva el estado del juego y calcula los turnos. */
    private final JuegoDados juego = new JuegoDados();
    /** Selector de estación de cada dado, uno por dado. */
    private final JComboBox<String>[] asignaciones;
    /** Mensaje corto con el resultado de la ronda más reciente. */
    private final JLabel estado = new JLabel("Acomoda los dados y lanza la primera ronda", SwingConstants.CENTER);
    /** Ronda actual y throughput acumulado que se muestran en la cabecera. */
    private final JLabel marcador = new JLabel("RONDA 0     •     THROUGHPUT 0", SwingConstants.CENTER);
    /** Componente que dibuja el circuito completo. */
    private final TableroVisual tablero = new TableroVisual();
    /** Botón único de turno; se desactiva al llegar a veinte rondas. */
    private final BotonJuego lanzar = new BotonJuego("LANZAR RONDA");
    /** Se conserva la ventana para que permanezca viva después de mostrarla. */
    private GraficasFinales graficasFinales;

    /** Construye la ventana, el tablero y los controles para reasignar los dados. */
    @SuppressWarnings("unchecked")
    public InterfazDados() {
        super("DADOS EN CADENA | Juego 2");
        asignaciones = (JComboBox<String>[]) new JComboBox<?>[10];
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 760));
        setSize(1120, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        // La interfaz se divide en cabecera, tablero central y controles inferiores.
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 22, 14, 22));
        add(contenido, BorderLayout.CENTER);

        JLabel titulo = new JLabel("DADOS EN CADENA", SwingConstants.CENTER);
        titulo.setForeground(TEXTO);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 25));
        marcador.setForeground(ACENTO);
        marcador.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel cabecera = new JPanel(new GridLayout(2, 1, 3, 3));
        cabecera.setOpaque(false);
        cabecera.add(titulo);
        cabecera.add(marcador);
        contenido.add(cabecera, BorderLayout.NORTH);

        // Se usa un área desplazable horizontal por si la ventana es más estrecha.
        tablero.setPreferredSize(new Dimension(1060, 500));
        JScrollPane areaTablero = new JScrollPane(tablero,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        areaTablero.setBorder(null);
        areaTablero.setOpaque(false);
        areaTablero.getViewport().setOpaque(false);
        contenido.add(areaTablero, BorderLayout.CENTER);

        JPanel controles = new JPanel(new BorderLayout(8, 8));
        controles.setOpaque(false);
        JLabel etiquetaDados = new JLabel("REASIGNA LOS DADOS ANTES DE CADA LANZAMIENTO");
        etiquetaDados.setForeground(ACENTO);
        etiquetaDados.setFont(new Font("SansSerif", Font.BOLD, 12));
        controles.add(etiquetaDados, BorderLayout.NORTH);

        // Dos filas de cinco selectores: cada uno representa un dado numerado.
        JPanel panelDados = new JPanel(new GridLayout(2, 5, 8, 7));
        panelDados.setOpaque(false);
        for (int i = 0; i < 10; i++) {
            JPanel control = new JPanel(new BorderLayout(4, 4));
            control.setOpaque(true);
            control.setBackground(TARJETA);
            control.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            JLabel nombre = new JLabel("Dado " + (i + 1));
            nombre.setForeground(TEXTO);
            JComboBox<String> selector = new JComboBox<>();
            for (int e = 1; e <= 10; e++) selector.addItem("Estación " + e);
            selector.setSelectedIndex(i);
            final int dado = i + 1;
            selector.addActionListener(event -> {
                // Actualiza el modelo al instante para que el siguiente turno
                // use la asignación elegida; repaint refleja el dado en el tablero.
                juego.asignarDado(dado, selector.getSelectedIndex() + 1);
                tablero.repaint();
            });
            asignaciones[i] = selector;
            control.add(nombre, BorderLayout.NORTH);
            control.add(selector, BorderLayout.CENTER);
            panelDados.add(control);
        }
        controles.add(panelDados, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 7));
        pie.setOpaque(false);
        estado.setForeground(TEXTO);
        lanzar.addActionListener(event -> lanzarRonda());
        pie.add(estado, BorderLayout.NORTH);
        pie.add(lanzar, BorderLayout.SOUTH);
        controles.add(pie, BorderLayout.SOUTH);
        contenido.add(controles, BorderLayout.SOUTH);
    }

    /** Ejecuta un turno, actualiza los indicadores y abre las gráficas al turno 20. */
    private void lanzarRonda() {
        int terminadas = juego.jugarRonda();
        estado.setText("Ronda " + juego.getRonda() + ": salieron " + terminadas + " fichas");
        marcador.setText("RONDA " + juego.getRonda() + "     •     THROUGHPUT " + juego.getThroughput());
        tablero.repaint();
        if (juego.getRonda() == JuegoDados.MAX_TURNOS) {
            // La partida termina aquí: bloquea cambios y presenta el resumen final.
            estado.setText("Partida terminada: resultados de los 20 turnos");
            lanzar.setEnabled(false);
            for (JComboBox<String> asignacion : asignaciones) asignacion.setEnabled(false);
            graficasFinales = new GraficasFinales(juego);
            graficasFinales.setVisible(true);
        }
    }

    /** Inicia Swing en su hilo de eventos, que es donde debe crearse la interfaz. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazDados().setVisible(true));
    }

    /**
     * Panel del tablero. Calcula posiciones en forma de serpiente: E1..E5 arriba,
     * E6..E10 abajo en sentido inverso. El dibujo se vuelve a crear en cada repaint.
     */
    private class TableroVisual extends JPanel {
        /** Diámetro de cada ficha que se representa dentro de una cola. */
        private static final int TAMANO_FICHA = 10;
        /** Tope visual por cola para que muchas fichas no invadan el tablero. */
        private static final int MAX_FICHAS_DIBUJADAS = 20;

        /** Prepara el color y el borde del lienzo. */
        TableroVisual() {
            setOpaque(true);
            setBackground(new Color(25, 39, 57));
            setBorder(BorderFactory.createLineBorder(new Color(59, 79, 103)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Graphics2D habilita antialiasing para bordes suaves y formas redondeadas.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Cinco columnas por fila, con el regreso E5→E6 vertical en el extremo derecho.
            int left = 85;
            int right = getWidth() - 85;
            int step = (right - left) / 4;
            int top = 100;
            int bottom = getHeight() - 100;
            int[] xs = new int[10];
            int[] ys = new int[10];
            for (int i = 0; i < 5; i++) {
                // Los primeros cinco índices son la fila superior, de izquierda a derecha.
                xs[i] = left + step * i;
                ys[i] = top;
                // Los siguientes cinco son la fila inferior, en sentido inverso.
                xs[i + 5] = right - step * i;
                ys[i + 5] = bottom;
            }

            dibujarRuta(g2, xs, ys);
            // Primero se dibujan las conexiones y colas, luego las estaciones encima.
            for (int i = 0; i < 9; i++) dibujarCola(g2, i, xs, ys);
            dibujarFlechas(g2, xs, ys);
            for (int i = 0; i < 10; i++) dibujarEstacion(g2, i, xs[i], ys[i]);
            dibujarRecursosFinales(g2, left - 57, (top + bottom) / 2 - 22, 128, 102);

            g2.setColor(new Color(170, 187, 207));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("ENTRADA", 18, top - 82);
            g2.drawString("SALIDA", 18, bottom + 80);
            g2.dispose();
        }

        /** Dibuja los nueve tramos que conectan las estaciones en orden. */
        private void dibujarRuta(Graphics2D g, int[] xs, int[] ys) {
            g.setColor(new Color(48, 68, 88));
            g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 9; i++) {
                int x1 = xs[i], y1 = ys[i], x2 = xs[i + 1], y2 = ys[i + 1];
                if (y1 == y2) {
                    // Se dejan 75 píxeles libres junto a cada tarjeta de estación.
                    int dir = x2 > x1 ? 1 : -1;
                    g.drawLine(x1 + dir * 75, y1, x2 - dir * 75, y2);
                } else {
                    // Único tramo entre filas: E5→E6, recto y vertical.
                    int dir = y2 > y1 ? 1 : -1;
                    g.drawLine(x1, y1 + dir * 75, x2, y2 - dir * 75);
                }
            }
        }

        /** Dibuja una flecha en el centro de cada tramo para indicar el flujo FIFO. */
        private void dibujarFlechas(Graphics2D g, int[] xs, int[] ys) {
            for (int i = 0; i < 9; i++) {
                int x1 = xs[i], y1 = ys[i], x2 = xs[i + 1], y2 = ys[i + 1];
                if (y1 == y2) {
                    int dir = x2 > x1 ? 1 : -1;
                    dibujarFlecha(g, (x1 + x2) / 2 + dir * 42, y1, dir, 0);
                } else {
                    int dir = y2 > y1 ? 1 : -1;
                    dibujarFlecha(g, x1, (y1 + y2) / 2 + dir * 65, 0, dir);
                }
            }
        }

        /** Pinta una punta de flecha horizontal o vertical según dx/dy. */
        private void dibujarFlecha(Graphics2D g, int x, int y, int dx, int dy) {
            g.setColor(ACENTO);
            int[] px = {x, x - 7 * dx, x - 7 * dx};
            int[] py = {y, y - 4, y + 4};
            if (dy != 0) {
                px = new int[]{x, x - 4, x + 4};
                py = new int[]{y, y - 7 * dy, y - 7 * dy};
            }
            g.fillPolygon(px, py, 3);
        }

        /** Dibuja el contenedor de una cola, sus fichas visibles y su cantidad total. */
        private void dibujarCola(Graphics2D g, int indice, int[] xs, int[] ys) {
            int cx = (xs[indice] + xs[indice + 1]) / 2;
            int cy = (ys[indice] + ys[indice + 1]) / 2;
            int total = juego.getCantidadCola(indice);
            // El número indica el total aunque se limite cuántas bolitas se dibujan.
            int visibles = Math.min(total, MAX_FICHAS_DIBUJADAS);
            g.setColor(new Color(22, 31, 43, 225));
            g.fillRoundRect(cx - 37, cy - 40, 74, 80, 14, 14);
            g.setColor(new Color(103, 125, 147));
            g.drawRoundRect(cx - 37, cy - 40, 74, 80, 14, 14);
            for (int n = 0; n < visibles; n++) {
                // Las fichas se acomodan en una cuadrícula de cinco columnas.
                int col = n % 5;
                int fila = n / 5;
                int x = cx - 31 + col * 13;
                int y = cy - 16 + fila * 13;
                g.setColor(new Color(250, 197, 89));
                g.fillOval(x, y, TAMANO_FICHA, TAMANO_FICHA);
                g.setColor(new Color(255, 228, 159));
                g.drawOval(x, y, TAMANO_FICHA, TAMANO_FICHA);
            }
            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            String texto = "Q" + (indice + 1) + " · " + total;
            g.drawString(texto, cx - g.getFontMetrics().stringWidth(texto) / 2, cy - 26);
        }

        /** Dibuja una tarjeta de estación, alien, capacidad y dados asignados. */
        private void dibujarEstacion(Graphics2D g, int indice, int cx, int cy) {
            // cx/cy son el centro; x/y representan la esquina superior izquierda.
            int w = 136, h = 150;
            int x = cx - w / 2, y = cy - h / 2;
            g.setColor(new Color(13, 22, 35, 190));
            g.fillRoundRect(x + 3, y + 5, w, h, 18, 18);
            g.setColor(TARJETA);
            g.fillRoundRect(x, y, w, h, 18, 18);
            g.setColor(new Color(71, 96, 120));
            g.drawRoundRect(x, y, w, h, 18, 18);

            dibujarPersonaje(g, indice, x + 28, y + 17);

            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString("E" + (indice + 1), x + 8, y + 130);
            g.setColor(ACENTO);
            g.drawString("CAP. " + juego.getCapacidadEstacion(indice), x + 8, y + 144);
            dibujarDados(g, indice, x + 95, y + 15);
        }

        /**
         * Dibuja cada ficha terminada dentro de un depósito independiente en
         * el espacio libre entre las filas. La cantidad del encabezado coincide
         * con el total de fichas que almacena el modelo.
         */
        private void dibujarRecursosFinales(Graphics2D g, int x, int y, int ancho, int alto) {
            int total = juego.getCantidadRecursosFinales();
            g.setColor(new Color(34, 49, 69));
            g.fillRoundRect(x, y, ancho, alto, 10, 10);
            g.setColor(new Color(103, 125, 147));
            g.drawRoundRect(x, y, ancho, alto, 10, 10);
            g.setColor(TEXTO);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString("RECURSOS FINALES", x + 8, y + 15);
            g.setColor(ACENTO);
            String cantidad = Integer.toString(total);
            g.drawString(cantidad, x + ancho - 8 - g.getFontMetrics().stringWidth(cantidad), y + 15);

            if (total == 0) return;
            // Se usan hasta nueve columnas; el número de filas crece con la salida.
            // El diámetro se reduce si hace falta para mantener todas las fichas dentro.
            int columnas = Math.min(total, 9);
            int filas = (total + columnas - 1) / columnas;
            int espacioX = Math.max(1, (ancho - 16) / columnas);
            int espacioY = Math.max(1, (alto - 25) / filas);
            int diametro = Math.max(2, Math.min(10, Math.min(espacioX - 2, espacioY - 2)));
            g.setColor(new Color(250, 197, 89));
            for (int i = 0; i < total; i++) {
                // Cada índice representa una ficha terminada; se calcula su fila y columna.
                int col = i % columnas;
                int fila = i / columnas;
                int px = x + 8 + col * espacioX + Math.max(0, (espacioX - diametro) / 2);
                int py = y + 21 + fila * espacioY + Math.max(0, (espacioY - diametro) / 2);
                g.fillOval(px, py, diametro, diametro);
                g.setColor(new Color(255, 228, 159));
                g.drawOval(px, py, diametro, diametro);
                g.setColor(new Color(250, 197, 89));
            }
        }

        /** Dibuja la cabeza alienígena de la estación indicada. */
        private void dibujarPersonaje(Graphics2D g, int indice, int cx, int y) {
            Color alien = CAMISETAS[indice];
            Color sombra = alien.darker();

            // Silueta ovalada con frente amplia y barbilla afinada, como la referencia.
            Path2D cabeza = new Path2D.Double();
            // moveTo inicia la forma; curveTo define los lados curvos y la punta inferior.
            cabeza.moveTo(cx, y + 2);
            cabeza.curveTo(cx + 18, y + 1, cx + 25, y + 16, cx + 23, y + 34);
            cabeza.curveTo(cx + 21, y + 49, cx + 10, y + 62, cx, y + 68);
            cabeza.curveTo(cx - 10, y + 62, cx - 21, y + 49, cx - 23, y + 34);
            cabeza.curveTo(cx - 25, y + 16, cx - 18, y + 1, cx, y + 2);
            cabeza.closePath();
            g.setColor(sombra);
            g.translate(0, 2);
            g.fill(cabeza);
            g.translate(0, -2);
            g.setColor(alien);
            g.fill(cabeza);
            g.setColor(alien.brighter());
            g.setStroke(new BasicStroke(1.5f));
            g.draw(cabeza);

            // Los ojos son completamente negros, sin pupilas ni rasgos humanos.
            dibujarOjoAlien(g, cx - 10, y + 37, -0.25);
            dibujarOjoAlien(g, cx + 10, y + 37, 0.25);
            g.setColor(new Color(37, 48, 58));
            g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(cx - 7, y + 51, 14, 7, 200, 140);

        }

        /** Pinta un ojo negro y ovalado, girado levemente hacia el exterior. */
        private void dibujarOjoAlien(Graphics2D g, int cx, int cy, double angulo) {
            Graphics2D ojo = (Graphics2D) g.create();
            // Se gira cada ojo alrededor de su centro para que apunte hacia afuera.
            ojo.rotate(angulo, cx, cy);
            ojo.setColor(new Color(18, 25, 32));
            ojo.fillOval(cx - 8, cy - 13, 16, 26);
            ojo.dispose();
        }

        /** Busca qué dados están asignados a la estación y dibuja sus resultados. */
        private void dibujarDados(Graphics2D g, int estacion, int cx, int y) {
            int[] indices = new int[10];
            int cuenta = 0;
            // Guarda los índices de los dados asignados; puede haber varios dados en una estación.
            for (int d = 0; d < 10; d++) if (juego.getEstacionDado(d) == estacion + 1) indices[cuenta++] = d;
            int die = 26, gap = 2, porFila = 3;
            int filas = Math.max(1, (cuenta + porFila - 1) / porFila);
            for (int n = 0; n < cuenta; n++) {
                int fila = n / porFila;
                int columna = n % porFila;
                int x = cx - 40 + columna * (die + gap);
                dibujarDado(g, x, y + fila * (die + gap), die, juego.getResultadoDado(indices[n]));
            }
        }

        /** Dibuja un dado blanco y los puntos correspondientes a su valor (1..6). */
        private void dibujarDado(Graphics2D g, int x, int y, int size, int valor) {
            g.setColor(new Color(6, 11, 18, 120));
            g.fillRoundRect(x + 2, y + 2, size, size, 6, 6);
            g.setColor(new Color(246, 248, 250));
            g.fillRoundRect(x, y, size, size, 6, 6);
            g.setColor(new Color(190, 202, 214));
            g.drawRoundRect(x, y, size, size, 6, 6);
            if (valor <= 0) {
                // Antes del primer turno no hay valor numérico, así que se pinta un signo ?.
                g.setColor(new Color(34, 50, 68));
                g.setFont(new Font("SansSerif", Font.BOLD, size - 7));
                String pregunta = "?";
                g.drawString(pregunta, x + (size - g.getFontMetrics().stringWidth(pregunta)) / 2,
                        y + (size + g.getFontMetrics().getAscent()) / 2 - 2);
                return;
            }
            g.setColor(new Color(34, 50, 68));
            int[][] puntos = {{4, 4}, {size - 6, size - 6}, {size - 6, 4}, {4, size - 6},
                    {4, size / 2 - 1}, {size - 6, size / 2 - 1}, {size / 2 - 1, size / 2 - 1}};
            // Cada subarreglo indica qué posiciones de puntos dibujar para cada cara del dado.
            int[][] indices = {{6}, {0, 1}, {0, 6, 1}, {0, 2, 3, 1}, {0, 2, 6, 3, 1}, {0, 2, 4, 5, 3, 1}};
            for (int punto : indices[Math.min(valor, 6) - 1])
                g.fillOval(x + puntos[punto][0], y + puntos[punto][1], 3, 3);
        }
    }

    /** Botón con el color de acento usado para iniciar cada turno. */
    private static class BotonJuego extends javax.swing.JButton {
        BotonJuego(String texto) {
            super(texto);
            setForeground(new Color(14, 35, 48));
            setBackground(ACENTO);
            setFont(new Font("SansSerif", Font.BOLD, 15));
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        }
    }
}
