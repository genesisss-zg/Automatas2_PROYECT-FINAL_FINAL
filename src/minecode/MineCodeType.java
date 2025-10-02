package minecode;

public enum MineCodeType {
    // Tipos básicos del sistema
    REDSTONE("redstone", "int"),    // Enteros (nivel de redstone)
    EMERALD("emerald", "float"),    // Decimales (esmeraldas)
    OBSIDIAN("obsidian", "string"), // Texto (obsidiana)
    NETHER("nether", "boolean"),    // Booleanos (nether)
    ENDER("ender", "array"),        // Arrays (ender)
    VOID("void", "void"),           // Vacío
    UNKNOWN("unknown", "unknown");  // Desconocido

    private final String mineCodeName;
    private final String internalName;

    MineCodeType(String mineCodeName, String internalName) {
        this.mineCodeName = mineCodeName;
        this.internalName = internalName;
    }

    public String getMineCodeName() { return mineCodeName; }
    public String getInternalName() { return internalName; }

    public static MineCodeType fromString(String typeName) {
        for (MineCodeType type : values()) {
            if (type.mineCodeName.equals(typeName) || type.internalName.equals(typeName)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean isCompatibleWith(MineCodeType other) {
        if (this == UNKNOWN || other == UNKNOWN) return true;
        return this == other;
    }

    public static boolean canAssign(MineCodeType variableType, MineCodeValue value) {
        if (variableType == UNKNOWN) return true;
        return variableType == value.getType();
    }
}