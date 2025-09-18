package util;

public class ErrorSemantico {
    private int linea;
    private String mensaje;
    private String tipo;

    public ErrorSemantico(int linea, String mensaje, String tipo) {
        this.linea = linea;
        this.mensaje = mensaje;
        this.tipo = tipo;
    }

    public int getLinea() { return linea; }
    public String getMensaje() { return mensaje; }
    public String getTipo() { return tipo; }

    @Override
    public String toString() {
        return "Línea " + linea + ": " + mensaje + " (" + tipo + ")";
    }
}