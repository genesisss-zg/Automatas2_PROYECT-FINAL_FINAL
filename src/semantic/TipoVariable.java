package semantic;

public class TipoVariable {
    private Tipo tipoBase;
    private boolean esArreglo;
    private int dimensiones;

    public TipoVariable(Tipo tipoBase) {
        this.tipoBase = tipoBase;
        this.esArreglo = false;
        this.dimensiones = 0;
    }

    public TipoVariable(Tipo tipoBase, boolean esArreglo, int dimensiones) {
        this.tipoBase = tipoBase;
        this.esArreglo = esArreglo;
        this.dimensiones = dimensiones;
    }

    public Tipo getTipoBase() { return tipoBase; }
    public boolean isEsArreglo() { return esArreglo; }
    public int getDimensiones() { return dimensiones; }

    public boolean esCompatible(TipoVariable otro) {
        return tipoBase == otro.tipoBase && 
               esArreglo == otro.esArreglo && 
               dimensiones == otro.dimensiones;
    }
}