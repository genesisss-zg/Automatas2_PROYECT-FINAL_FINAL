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
        if (scopes.size() <= 1) {
            throw new IllegalStateException("No se puede salir del scope global");
        }
        scopes.pop();
    }

    public Scope getCurrentScope() {
        return scopes.peek();
    }

    public boolean declareSymbol(String name, Symbol symbol) {
        return scopes.peek().declareSymbol(name, symbol);
    }

    public Symbol resolve(String name) {
        return scopes.peek().resolve(name);
    }

    public boolean containsSymbol(String name) {
        return scopes.peek().containsSymbol(name);
    }

    public Scope getGlobalScope() {
        return scopes.firstElement();
    }
}