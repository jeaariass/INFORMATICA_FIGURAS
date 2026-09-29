package my.figuras;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panel donde se dibujan los puntos a medida que se agregan y, al final, la figura creada.
 * La escala se ajusta sola para que todos los puntos quepan en el panel.
 */
public class PanelDibujo extends JPanel {

    private static final int MARGEN = 30;

    private List<Punto> puntos = new ArrayList<>();
    private Figura figura;

    // Transformación coordenadas cartesianas -> píxeles (se recalcula en cada repintado)
    private double escala;
    private double minX;
    private double minY;
    private double desplazamientoX;
    private double desplazamientoY;

    public PanelDibujo() {
        setBackground(Color.WHITE);
    }

    /** Puntos que se van agregando (trazado en progreso). */
    public void setPuntos(List<Punto> puntos) {
        this.puntos = puntos;
        repaint();
    }

    /** Figura ya creada; null si todavía no se ha creado. */
    public void setFigura(Figura figura) {
        this.figura = figura;
        repaint();
    }

    public void limpiar() {
        figura = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        calcularEscala();
        dibujarEjes(g2);

        if (figura == null) {
            dibujarTrazadoEnProgreso(g2);
        } else {
            dibujarFigura(g2);
        }
        g2.dispose();
    }

    // ---------------------------------------------------------------- escala

    private void calcularEscala() {
        double xMin = 0, xMax = 0, yMin = 0, yMax = 0; // el origen siempre queda a la vista
        for (Punto p : puntos) {
            xMin = Math.min(xMin, p.getX());
            xMax = Math.max(xMax, p.getX());
            yMin = Math.min(yMin, p.getY());
            yMax = Math.max(yMax, p.getY());
        }
        if (figura instanceof Circulo c) {
            xMin = Math.min(xMin, c.getCentro().getX() - c.getRadio());
            xMax = Math.max(xMax, c.getCentro().getX() + c.getRadio());
            yMin = Math.min(yMin, c.getCentro().getY() - c.getRadio());
            yMax = Math.max(yMax, c.getCentro().getY() + c.getRadio());
        }
        double ancho = Math.max(xMax - xMin, 1);
        double alto = Math.max(yMax - yMin, 1);
        double anchoUtil = getWidth() - 2.0 * MARGEN;
        double altoUtil = getHeight() - 2.0 * MARGEN;

        escala = Math.min(anchoUtil / ancho, altoUtil / alto); // misma escala en X y Y
        minX = xMin;
        minY = yMin;
        // centra el dibujo dentro del panel
        desplazamientoX = MARGEN + (anchoUtil - ancho * escala) / 2;
        desplazamientoY = MARGEN + (altoUtil - alto * escala) / 2;
    }

    private double aPixelX(double x) {
        return desplazamientoX + (x - minX) * escala;
    }

    /** En pantalla la Y crece hacia abajo, por eso se invierte. */
    private double aPixelY(double y) {
        return getHeight() - (desplazamientoY + (y - minY) * escala);
    }

    // ---------------------------------------------------------------- dibujo

    private void dibujarEjes(Graphics2D g2) {
        g2.setColor(new Color(200, 200, 200));
        int ox = (int) Math.round(aPixelX(0));
        int oy = (int) Math.round(aPixelY(0));
        g2.drawLine(0, oy, getWidth(), oy);   // eje X
        g2.drawLine(ox, 0, ox, getHeight());  // eje Y
        g2.drawString("0", ox + 3, oy - 3);
    }

    private void dibujarTrazadoEnProgreso(Graphics2D g2) {
        g2.setColor(new Color(30, 90, 200));
        g2.setStroke(new BasicStroke(2));
        for (int i = 0; i < puntos.size() - 1; i++) {
            dibujarSegmento(g2, puntos.get(i), puntos.get(i + 1));
        }
        dibujarPuntos(g2, puntos);
    }

    private void dibujarFigura(Graphics2D g2) {
        if (figura instanceof Poligono pol) {
            Path2D forma = new Path2D.Double();
            List<Punto> v = pol.getVertices();
            forma.moveTo(aPixelX(v.get(0).getX()), aPixelY(v.get(0).getY()));
            for (int i = 1; i < v.size(); i++) {
                forma.lineTo(aPixelX(v.get(i).getX()), aPixelY(v.get(i).getY()));
            }
            forma.closePath(); // une el último punto con el primero
            rellenarYContornear(g2, forma);
            dibujarPuntos(g2, v);
        } else if (figura instanceof Circulo c) {
            double r = c.getRadio() * escala;
            double cx = aPixelX(c.getCentro().getX());
            double cy = aPixelY(c.getCentro().getY());
            rellenarYContornear(g2, new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
            dibujarPuntos(g2, List.of(c.getCentro()));
        } else if (figura instanceof Linea l) {
            g2.setColor(new Color(30, 90, 200));
            g2.setStroke(new BasicStroke(2));
            List<Punto> p = l.getPuntos();
            for (int i = 0; i < p.size() - 1; i++) {
                dibujarSegmento(g2, p.get(i), p.get(i + 1));
            }
            dibujarPuntos(g2, p);
        } else if (figura instanceof Punto p) {
            dibujarPuntos(g2, List.of(p));
        }
    }

    private void rellenarYContornear(Graphics2D g2, java.awt.Shape forma) {
        g2.setColor(new Color(30, 90, 200, 60));
        g2.fill(forma);
        g2.setColor(new Color(30, 90, 200));
        g2.setStroke(new BasicStroke(2));
        g2.draw(forma);
    }

    private void dibujarSegmento(Graphics2D g2, Punto a, Punto b) {
        g2.drawLine((int) Math.round(aPixelX(a.getX())), (int) Math.round(aPixelY(a.getY())),
                    (int) Math.round(aPixelX(b.getX())), (int) Math.round(aPixelY(b.getY())));
    }

    private void dibujarPuntos(Graphics2D g2, List<Punto> lista) {
        for (int i = 0; i < lista.size(); i++) {
            int px = (int) Math.round(aPixelX(lista.get(i).getX()));
            int py = (int) Math.round(aPixelY(lista.get(i).getY()));
            g2.setColor(new Color(200, 40, 40));
            g2.fillOval(px - 4, py - 4, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.valueOf(i + 1), px + 6, py - 6);
        }
    }
}
