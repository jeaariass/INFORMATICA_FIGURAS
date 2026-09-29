package my.figuras;

/** Punto en coordenadas cartesianas 2D. */
public class Punto extends Figura {

    private double x;
    private double y;

    public Punto(double x, double y) {
        super("Punto");
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /** Distancia euclidiana entre este punto y otro. */
    public double distancia(Punto otro) {
        double dx = otro.x - this.x;
        double dy = otro.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
