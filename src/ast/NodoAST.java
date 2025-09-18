package ast;

import java.util.List;

public class NodoAST {
    private String tipoNodo;
    private String nombre;
    private String tipo; // Para declaración
    private String tipoExpresion; // Para asignación
    private List<String> variablesUsadas; // Para operaciones
    private List<NodoAST> hijos;

    public NodoAST(String tipoNodo, String nombre) {
        this.tipoNodo = tipoNodo;
        this.nombre = nombre;
    }

    public NodoAST(String tipoNodo, String nombre, String tipo) {
        this.tipoNodo = tipoNodo;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getTipoNodo() {
        return tipoNodo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTipoExpresion() {
        return tipoExpresion;
    }

    public void setTipoExpresion(String tipoExpresion) {
        this.tipoExpresion = tipoExpresion;
    }

    public List<String> getVariablesUsadas() {
        return variablesUsadas;
    }

    public void setVariablesUsadas(List<String> variablesUsadas) {
        this.variablesUsadas = variablesUsadas;
    }

    public List<NodoAST> getHijos() {
        return hijos;
    }

    public void setHijos(List<NodoAST> hijos) {
        this.hijos = hijos;
    }
}
