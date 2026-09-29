package my.figuras;

import java.util.List;

/** Triángulo: polígono de exactamente 3 vértices. */
public class Triangulo extends Poligono {

    public Triangulo(Punto a, Punto b, Punto c) {
        super("Triángulo", List.of(a, b, c));
    }

    /** Área con la fórmula de Herón. */
    @Override
    public double area() {
        double a = vertices.get(0).distancia(vertices.get(1));
        double b = vertices.get(1).distancia(vertices.get(2));
        double c = vertices.get(2).distancia(vertices.get(0));
        double s = (a + b + c) / 2;
        double producto = s * (s - a) * (s - b) * (s - c);
        return Math.sqrt(Math.max(0, producto)); // max evita NaN por redondeo si los puntos son colineales
    }
}
