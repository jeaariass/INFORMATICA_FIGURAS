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
    }

    public List<Punto> getVertices() {
        return vertices;
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
