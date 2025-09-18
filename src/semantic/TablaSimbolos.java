package semantic;

import java.util.HashMap;

public class TablaSimbolos {
    private static class Simbolo {
        String tipo;
        boolean inicializado;

        Simbolo(String tipo, boolean inicializado) {
            this.tipo = tipo;
            this.inicializado = inicializado;
        }
    }

    private HashMap<String, Simbolo> tabla;

    public TablaSimbolos() {
        tabla = new HashMap<>();
    }

    public void declarar(String nombre, String tipo) throws Exception {
        if (tabla.containsKey(nombre)) {
            throw new Exception("Error: Variable '" + nombre + "' ya declarada.");
        }
        tabla.put(nombre, new Simbolo(tipo, false));
    }

    public void inicializar(String nombre) throws Exception {
        if (!tabla.containsKey(nombre)) {
            throw new Exception("Error: Variable '" + nombre + "' no declarada.");
        }
        tabla.get(nombre).inicializado = true;
    }

    public void usar(String nombre) throws Exception {
        if (!tabla.containsKey(nombre)) {
            throw new Exception("Error: Variable '" + nombre + "' no declarada.");
        }
        if (!tabla.get(nombre).inicializado) {
            throw new Exception("Error: Variable '" + nombre + "' no inicializada.");
        }
    }

    public String obtenerTipo(String nombre) throws Exception {
        if (!tabla.containsKey(nombre)) {
            throw new Exception("Error: Variable '" + nombre + "' no declarada.");
        }
        return tabla.get(nombre).tipo;
    }
}
