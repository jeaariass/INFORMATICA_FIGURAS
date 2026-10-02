package my.figuras;

import java.util.ArrayList;
import java.util.List;

/** Polígono convexo: 3 o más vértices en orden; el último se une con el primero. */
public class Poligono extends Figura {

    protected List<Punto> vertices;

    public Poligono(List<Punto> vertices) {
        this("Polígono", vertices);
    }

    /** Constructor para que las subclases (Triangulo) pongan su propio nombre. */
    protected Poligono(String nombre, List<Punto> vertices) {
        super(nombre);
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("Un polígono necesita mínimo 3 vértices.");
        }
        this.vertices = new ArrayList<>(vertices);
        if (!esConvexo()) {
            String motivo = vertices.size() == 3
                    ? "Los puntos están alineados y no forman un triángulo válido."
                    : "La figura no es convexa (algún vértice se dobla hacia adentro).";
            throw new IllegalArgumentException(motivo
                    + " Presiona \"Limpiar\" y vuelve a ingresar los puntos en orden"
                    + " (horario o antihorario) para formar una figura convexa.");
        }
    }

    public List<Punto> getVertices() {
        return vertices;
    }

    /**
     * Revisa si el polígono es convexo mirando, para cada tres vértices consecutivos,
     * hacia qué lado gira (producto cruz de los vectores de los dos lados). En un
     * polígono convexo todos los giros van en el mismo sentido; si en algún vértice
     * el giro cambia de sentido, hay una "entrada" y el polígono es cóncavo.
     * Si todos los productos cruz dan cero, los puntos están alineados (figura degenerada).
     */
    private boolean esConvexo() {
        int n = vertices.size();
        boolean hayGiroPositivo = false;
        boolean hayGiroNegativo = false;
        for (int i = 0; i < n; i++) {
            Punto a = vertices.get(i);
            Punto b = vertices.get((i + 1) % n);
            Punto c = vertices.get((i + 2) % n);
            double cruz = (b.getX() - a.getX()) * (c.getY() - b.getY())
                        - (b.getY() - a.getY()) * (c.getX() - b.getX());
            if (cruz > 0) {
                hayGiroPositivo = true;
            } else if (cruz < 0) {
                hayGiroNegativo = true;
            }
            if (hayGiroPositivo && hayGiroNegativo) {
                return false; // cambió el sentido del giro en algún vértice
            }
        }
        return hayGiroPositivo || hayGiroNegativo; // false si todos los giros dieron 0 (puntos alineados)
    }

    /** Área: suma de los triángulos formados en abanico desde el primer vértice. */
    public double area() {
        double total = 0;
        Punto v0 = vertices.get(0);
        for (int i = 1; i < vertices.size() - 1; i++) {
            total += new Triangulo(v0, vertices.get(i), vertices.get(i + 1)).area();
        }
        return total;
    }

    /** Perímetro: lados consecutivos más el lado de cierre (último -> primero). */
    public double perimetro() {
        double total = 0;
        for (int i = 0; i < vertices.size(); i++) {
            Punto actual = vertices.get(i);
            Punto siguiente = vertices.get((i + 1) % vertices.size());
            total += actual.distancia(siguiente);
        }
        return total;
    }
}
