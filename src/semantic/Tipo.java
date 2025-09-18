package semantic;

public enum Tipo {
    INT,
    FLOAT,
    STRING,
    BOOLEAN,
    VOID,
    UNKNOWN;

    public static boolean esCompatible(Tipo tipo1, Tipo tipo2) {
        if (tipo1 == UNKNOWN || tipo2 == UNKNOWN) {
            return true;
        }
        return tipo1 == tipo2;
    }

    public static Tipo fromString(String typeStr) {
        switch (typeStr.toLowerCase()) {
            case "int": return INT;
            case "float": return FLOAT;
            case "string": return STRING;
            case "boolean": return BOOLEAN;
            case "void": return VOID;
            default: return UNKNOWN;
        }
    }
}