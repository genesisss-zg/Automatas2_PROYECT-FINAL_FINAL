package semantic;

import java.util.Stack;

public class ScopeManager {
    private Stack<Scope> scopes;

    public ScopeManager() {
        this.scopes = new Stack<>();
        enterScope(); // Scope global
    }

    public void enterScope() {
        Scope parent = scopes.isEmpty() ? null : scopes.peek();
        scopes.push(new Scope(parent));
    }

    public void exitScope() {
        if (scopes.size() > 1) {
            scopes.pop();
        }
    }

    public Scope getCurrentScope() {
        return scopes.peek();
    }

    public boolean declareSymbol(String name, Symbol symbol) {
        return scopes.peek().declareSymbol(name, symbol);
    }

    public Symbol resolve(String name) {
        // No intentar resolver operadores o símbolos especiales
        if (isOperatorOrSpecialSymbol(name)) {
            return null; // Los operadores no son variables
        }
        return scopes.peek().resolve(name);
    }

    public boolean containsSymbol(String name) {
        if (isOperatorOrSpecialSymbol(name)) {
            return false; // Los operadores no son variables
        }
        return scopes.peek().containsSymbol(name);
    }

    private boolean isOperatorOrSpecialSymbol(String name) {
        // Lista de operadores y símbolos que NO son variables
        return name.equals("+") || name.equals("-") || name.equals("*") || 
               name.equals("/") || name.equals("=") || name.equals("==") ||
               name.equals("!=") || name.equals("<") || name.equals(">") ||
               name.equals("<=") || name.equals(">=") || name.equals("&&") ||
               name.equals("||") || name.equals("!") || name.equals(";") ||
               name.equals(":") || name.equals(",") || name.equals("(") ||
               name.equals(")") || name.equals("{") || name.equals("}");
    }

    public Scope getGlobalScope() {
        return scopes.firstElement();
    }
}