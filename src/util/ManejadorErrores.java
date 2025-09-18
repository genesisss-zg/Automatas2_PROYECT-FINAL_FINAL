package util;

import java.util.ArrayList;
import java.util.List;

public class ManejadorErrores {
    private List<ErrorSemantico> errores;

    public ManejadorErrores() {
        this.errores = new ArrayList<>();
    }

    public void agregarError(int linea, String mensaje, String tipo) {
        ErrorSemantico error = new ErrorSemantico(linea, mensaje, tipo);
        errores.add(error);
        System.err.println("ERROR [" + tipo + "] Línea " + linea + ": " + mensaje);
    }

    public void agregarError(ErrorSemantico error) {
        errores.add(error);
        System.err.println("ERROR [" + error.getTipo() + "] Línea " + 
                          error.getLinea() + ": " + error.getMensaje());
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public List<ErrorSemantico> getErrores() {
        return new ArrayList<>(errores);
    }

    public void limpiar() {
        errores.clear();
    }

    public int getCantidadErrores() {
        return errores.size();
    }
}