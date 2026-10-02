/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package my.figuras;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Ventana principal: se elige el tipo de figura, se agregan los puntos uno por uno
 * (el trazado se va dibujando) y con "Crear figura" se calculan los resultados.
 *
 * @author Jesus
 */
public class FigurasUI extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FigurasUI.class.getName());

    /** Puntos que el usuario ha agregado para la figura actual. */
    private final List<Punto> puntos = new ArrayList<>();

    /** Lo que se muestra en la lista "Puntos agregados". */
    private final DefaultListModel<String> modeloPuntos = new DefaultListModel<>();

    /** Figura creada con "Crear figura" (null mientras se están agregando puntos). */
    private Figura figuraActual;

    public FigurasUI() {
        initComponents();
        inicializar();
    }

    private void inicializar() {
        lstPuntos.setModel(modeloPuntos);
        dibujo().setPuntos(puntos);
        actualizarCamposSegunTipo();
    }

    /** El panel del formulario se crea como PanelDibujo (código de creación personalizado). */
    private PanelDibujo dibujo() {
        return (PanelDibujo) panelDibujo;
    }

    private String tipoSeleccionado() {
        return String.valueOf(cboTipo.getSelectedItem());
    }

    /** Activa el radio solo para el círculo y deja los resultados en blanco. */
    private void actualizarCamposSegunTipo() {
        boolean esCirculo = tipoSeleccionado().equals("Círculo");
        txtRadio.setEnabled(esCirculo);
        lblRadio.setEnabled(esCirculo);
        if (!esCirculo) {
            txtRadio.setText("");
        }
        limpiarResultados();
    }

    /** Máximo de puntos que admite cada tipo (Integer.MAX_VALUE = sin límite). */
    private int maximoPuntos() {
        return switch (tipoSeleccionado()) {
            case "Punto", "Círculo" -> 1;   // en el círculo el único punto es el centro
            default -> Integer.MAX_VALUE;   // Línea (mínimo 2) y Polígono (mínimo 3) sin tope
        };
    }

    private void agregarPunto() {
        if (figuraActual != null) {          // si ya había una figura creada, empieza una nueva
            limpiarPuntos();
        }
        if (puntos.size() >= maximoPuntos()) {
            JOptionPane.showMessageDialog(this, tipoSeleccionado() + " admite máximo " + maximoPuntos() + " punto(s).");
            return;
        }
        try {
            double x = leerNumero(txtX, "X");
            double y = leerNumero(txtY, "Y");
            Punto p = new Punto(x, y);
            puntos.add(p);
            modeloPuntos.addElement(puntos.size() + ". " + formatear(p));
            dibujo().repaint();              // el trazado se actualiza con cada punto
            txtX.setText("");
            txtY.setText("");
            txtX.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /**
     * Carga una "cartera de puntos" desde un archivo de texto: un punto por línea,
     * en formato x,y (con punto decimal, por ejemplo 3.5,-2). Reemplaza los puntos
     * que hubiera y, si el número de puntos es válido para el tipo seleccionado,
     * crea la figura de inmediato (con lo cual también se revisa la convexidad).
     */
    private void cargarPuntosDesdeArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Selecciona el archivo de puntos (una línea por punto, formato x,y)");
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        List<Punto> puntosLeidos = new ArrayList<>();
        try {
            List<String> lineas = Files.readAllLines(selector.getSelectedFile().toPath());
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) {
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length != 2) {
                    JOptionPane.showMessageDialog(this, "Línea " + (i + 1) + " inválida: \"" + linea
                            + "\". Usa el formato x,y (ejemplo: 3.5,-2).");
                    return;
                }
                try {
                    double x = Double.parseDouble(partes[0].trim());
                    double y = Double.parseDouble(partes[1].trim());
                    puntosLeidos.add(new Punto(x, y));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Línea " + (i + 1) + " inválida: \"" + linea
                            + "\". Los dos valores deben ser números (usa punto decimal, no coma).");
                    return;
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el archivo: " + ex.getMessage());
            return;
        }
        if (puntosLeidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El archivo no tiene puntos válidos.");
            return;
        }
        if (puntosLeidos.size() > maximoPuntos()) {
            JOptionPane.showMessageDialog(this, tipoSeleccionado() + " admite máximo " + maximoPuntos()
                    + " punto(s), y el archivo trae " + puntosLeidos.size() + ".");
            return;
        }
        limpiarPuntos();
        for (Punto p : puntosLeidos) {
            puntos.add(p);
            modeloPuntos.addElement(puntos.size() + ". " + formatear(p));
        }
        dibujo().repaint();
        crearFigura();
    }

    private void crearFigura() {
        try {
            figuraActual = switch (tipoSeleccionado()) {
                case "Punto" -> {
                    exigirPuntos(1);
                    yield puntos.get(0);
                }
                case "Línea" -> new Linea(puntos);
                // con exactamente 3 puntos, un "Polígono" ES un triángulo: se construye
                // el objeto Triangulo (misma validación de convexidad, área por Herón).
                case "Polígono" -> puntos.size() == 3
                        ? new Triangulo(puntos.get(0), puntos.get(1), puntos.get(2))
                        : new Poligono(puntos);
                case "Círculo" -> {
                    exigirPuntos(1);
                    yield new Circulo(puntos.get(0), leerNumero(txtRadio, "Radio"));
                }
                default -> throw new IllegalArgumentException("Tipo de figura no reconocido.");
            };
            mostrarResultados();
            dibujo().setFigura(figuraActual);
        } catch (IllegalArgumentException ex) { // incluye NumberFormatException
            figuraActual = null;
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void mostrarResultados() {
        limpiarResultados();
        if (figuraActual instanceof Poligono pol) {
            lblArea.setText("Área: " + String.format("%.2f", pol.area()));
            lblPerimetro.setText("Perímetro: " + String.format("%.2f", pol.perimetro()));
        } else if (figuraActual instanceof Circulo c) {
            lblArea.setText("Área: " + String.format("%.2f", c.area()));
            lblPerimetro.setText("Perímetro: " + String.format("%.2f", c.perimetro()));
        } else if (figuraActual instanceof Linea l) {
            lblLongitud.setText("Longitud: " + String.format("%.2f", l.longitud()));
        } else if (figuraActual instanceof Punto p) {
            lblLongitud.setText("Coordenadas: " + formatear(p));
        }
    }

    private void limpiarResultados() {
        lblArea.setText("Área: —");
        lblPerimetro.setText("Perímetro: —");
        lblLongitud.setText("Longitud: —");
    }

    private void limpiarPuntos() {
        puntos.clear();
        modeloPuntos.clear();
        figuraActual = null;
        dibujo().limpiar();
        limpiarResultados();
    }

    private void exigirPuntos(int cantidad) {
        if (puntos.size() != cantidad) {
            throw new IllegalArgumentException(tipoSeleccionado() + " necesita exactamente " + cantidad + " punto(s).");
        }
    }

    /** Lee un número de un campo de texto; acepta coma o punto decimal. */
    private double leerNumero(javax.swing.JTextField campo, String nombre) {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            throw new NumberFormatException("Escribe un valor para " + nombre + ".");
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("El valor de " + nombre + " no es un número válido.");
        }
    }

    /** (2, 3) en lugar de (2.0, 3.0). */
    private String formatear(Punto p) {
        return "(" + formatear(p.getX()) + ", " + formatear(p.getY()) + ")";
    }

    private String formatear(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelPrincipal = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblTipo = new javax.swing.JLabel();
        cboTipo = new javax.swing.JComboBox<>();
        lblDatos = new javax.swing.JLabel();
        lblX = new javax.swing.JLabel();
        txtX = new javax.swing.JTextField();
        lblY = new javax.swing.JLabel();
        txtY = new javax.swing.JTextField();
        lblRadio = new javax.swing.JLabel();
        txtRadio = new javax.swing.JTextField();
        btnAgregarPunto = new javax.swing.JButton();
        lblPuntos = new javax.swing.JLabel();
        scrollPuntos = new javax.swing.JScrollPane();
        lstPuntos = new javax.swing.JList<>();
        btnCargarArchivo = new javax.swing.JButton();
        btnCrearFigura = new javax.swing.JButton();
        lblDibujo = new javax.swing.JLabel();
        panelDibujo = new PanelDibujo();
        lblResultados = new javax.swing.JLabel();
        lblArea = new javax.swing.JLabel();
        lblPerimetro = new javax.swing.JLabel();
        lblLongitud = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Figuras Geométricas");

        panelPrincipal.setPreferredSize(new java.awt.Dimension(760, 600));
        panelPrincipal.setLayout(null);

        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitulo.setText("FIGURAS GEOMÉTRICAS");
        panelPrincipal.add(lblTitulo);
        lblTitulo.setBounds(20, 10, 720, 30);

        lblTipo.setText("Tipo de figura:");
        panelPrincipal.add(lblTipo);
        lblTipo.setBounds(20, 50, 110, 25);

        cboTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Punto", "Línea", "Polígono", "Círculo" }));
        cboTipo.addActionListener(this::cboTipoActionPerformed);
        panelPrincipal.add(cboTipo);
        cboTipo.setBounds(135, 50, 150, 25);

        lblDatos.setText("DATOS DE ENTRADA");
        panelPrincipal.add(lblDatos);
        lblDatos.setBounds(20, 90, 200, 20);

        lblX.setText("X:");
        panelPrincipal.add(lblX);
        lblX.setBounds(20, 120, 20, 25);
        panelPrincipal.add(txtX);
        txtX.setBounds(45, 120, 80, 25);

        lblY.setText("Y:");
        panelPrincipal.add(lblY);
        lblY.setBounds(20, 150, 20, 25);

        txtY.addActionListener(this::btnAgregarPuntoActionPerformed);
        panelPrincipal.add(txtY);
        txtY.setBounds(45, 150, 80, 25);

        lblRadio.setText("Radio:");
        panelPrincipal.add(lblRadio);
        lblRadio.setBounds(140, 120, 50, 25);
        panelPrincipal.add(txtRadio);
        txtRadio.setBounds(190, 120, 70, 25);

        btnAgregarPunto.setText("Agregar punto");
        btnAgregarPunto.addActionListener(this::btnAgregarPuntoActionPerformed);
        panelPrincipal.add(btnAgregarPunto);
        btnAgregarPunto.setBounds(70, 190, 150, 28);

        lblPuntos.setText("Puntos agregados:");
        panelPrincipal.add(lblPuntos);
        lblPuntos.setBounds(20, 225, 150, 20);

        scrollPuntos.setViewportView(lstPuntos);

        panelPrincipal.add(scrollPuntos);
        scrollPuntos.setBounds(20, 248, 240, 150);

        btnCargarArchivo.setText("Cargar archivo...");
        btnCargarArchivo.addActionListener(this::btnCargarArchivoActionPerformed);
        panelPrincipal.add(btnCargarArchivo);
        btnCargarArchivo.setBounds(20, 410, 150, 28);

        btnCrearFigura.setText("Crear figura");
        btnCrearFigura.addActionListener(this::btnCrearFiguraActionPerformed);
        panelPrincipal.add(btnCrearFigura);
        btnCrearFigura.setBounds(20, 448, 150, 28);

        lblDibujo.setText("ÁREA DE DIBUJO");
        panelPrincipal.add(lblDibujo);
        lblDibujo.setBounds(290, 90, 200, 20);

        panelDibujo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelDibujo.setLayout(null);
        panelPrincipal.add(panelDibujo);
        panelDibujo.setBounds(290, 115, 450, 325);

        lblResultados.setText("RESULTADOS");
        panelPrincipal.add(lblResultados);
        lblResultados.setBounds(20, 493, 200, 20);

        lblArea.setText("Área: —");
        panelPrincipal.add(lblArea);
        lblArea.setBounds(20, 518, 160, 20);

        lblPerimetro.setText("Perímetro: —");
        panelPrincipal.add(lblPerimetro);
        lblPerimetro.setBounds(190, 518, 180, 20);

        lblLongitud.setText("Longitud: —");
        panelPrincipal.add(lblLongitud);
        lblLongitud.setBounds(380, 518, 360, 20);

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        panelPrincipal.add(btnLimpiar);
        btnLimpiar.setBounds(20, 553, 100, 28);

        btnSalir.setText("Salir");
        btnSalir.addActionListener(this::btnSalirActionPerformed);
        panelPrincipal.add(btnSalir);
        btnSalir.setBounds(640, 553, 100, 28);

        getContentPane().add(panelPrincipal, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cboTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoActionPerformed
        limpiarPuntos();              // al cambiar de figura se empieza de cero
        actualizarCamposSegunTipo();
    }//GEN-LAST:event_cboTipoActionPerformed

    private void btnAgregarPuntoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarPuntoActionPerformed
        agregarPunto();
    }//GEN-LAST:event_btnAgregarPuntoActionPerformed

    private void btnCargarArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarArchivoActionPerformed
        cargarPuntosDesdeArchivo();
    }//GEN-LAST:event_btnCargarArchivoActionPerformed

    private void btnCrearFiguraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCrearFiguraActionPerformed
        crearFigura();
    }//GEN-LAST:event_btnCrearFiguraActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarPuntos();
        txtX.setText("");
        txtY.setText("");
        txtRadio.setText("");
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        dispose();
    }//GEN-LAST:event_btnSalirActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FigurasUI().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarPunto;
    private javax.swing.JButton btnCargarArchivo;
    private javax.swing.JButton btnCrearFigura;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JComboBox<String> cboTipo;
    private javax.swing.JLabel lblArea;
    private javax.swing.JLabel lblDatos;
    private javax.swing.JLabel lblDibujo;
    private javax.swing.JLabel lblLongitud;
    private javax.swing.JLabel lblPerimetro;
    private javax.swing.JLabel lblPuntos;
    private javax.swing.JLabel lblRadio;
    private javax.swing.JLabel lblResultados;
    private javax.swing.JLabel lblTipo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblX;
    private javax.swing.JLabel lblY;
    private javax.swing.JList<String> lstPuntos;
    private javax.swing.JPanel panelDibujo;
    private javax.swing.JPanel panelPrincipal;
    private javax.swing.JScrollPane scrollPuntos;
    private javax.swing.JTextField txtRadio;
    private javax.swing.JTextField txtX;
    private javax.swing.JTextField txtY;
    // End of variables declaration//GEN-END:variables
}
