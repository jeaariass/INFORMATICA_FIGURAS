package my.figuras;

import java.util.ArrayList;
import java.util.List;

/** Línea o polilínea: 2 o más puntos unidos en el orden en que se ingresan. */
public class Linea extends Figura {

    private List<Punto> puntos;

    public Linea(List<Punto> puntos) {
        super("Línea");
        if (puntos.size() < 2) {
            throw new IllegalArgumentException("Una línea necesita mínimo 2 puntos.");
        }
        this.puntos = new ArrayList<>(puntos);
    }

    public List<Punto> getPuntos() {
        return puntos;
    }

    /** Suma de las distancias entre cada punto y el siguiente. */
    public double longitud() {
        double total = 0;
        for (int i = 0; i < puntos.size() - 1; i++) {
            total += puntos.get(i).distancia(puntos.get(i + 1));
        }
        return total;
    }
}
