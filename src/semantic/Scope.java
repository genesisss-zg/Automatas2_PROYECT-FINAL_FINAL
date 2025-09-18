package semantic;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private Map<String, Symbol> symbols;
    private Scope parent;

    public Scope(Scope parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;
    }

    public boolean declareSymbol(String name, Symbol symbol) {
        if (symbols.containsKey(name)) {
            return false;
        }
        symbols.put(name, symbol);
        return true;
    }

    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return null;
    }

    public boolean containsSymbol(String name) {
        return symbols.containsKey(name);
    }

    public Map<String, Symbol> getSymbols() {
        return new HashMap<>(symbols);
    }

    public Scope getParent() {
        return parent;
    }
}