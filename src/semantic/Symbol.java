package semantic;

public class Symbol {
    private String name;
    private Tipo tipo;
    private Object value;
    private boolean isFunction;

    public Symbol(String name, Tipo tipo, Object value, boolean isFunction) {
        this.name = name;
        this.tipo = tipo;
        this.value = value;
        this.isFunction = isFunction;
    }

    public String getName() { return name; }
    public Tipo getTipo() { return tipo; }
    public Object getValue() { return value; }
    public boolean isFunction() { return isFunction; }

    public void setValue(Object value) { this.value = value; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
}