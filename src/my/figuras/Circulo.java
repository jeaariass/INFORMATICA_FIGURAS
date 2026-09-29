package my.figuras;

/** Círculo definido por su centro y su radio. */
public class Circulo extends Figura {

    private Punto centro;
    private double radio;

    public Circulo(Punto centro, double radio) {
        super("Círculo");
        if (radio <= 0) {
            throw new IllegalArgumentException("El radio debe ser mayor que 0.");
        }
        this.centro = centro;
        this.radio = radio;
    }

    public Punto getCentro() {
        return centro;
    }

    public double getRadio() {
        return radio;
    }

    public double area() {
        return Math.PI * radio * radio;
    }

    public double perimetro() {
        return 2 * Math.PI * radio;
    }
}
