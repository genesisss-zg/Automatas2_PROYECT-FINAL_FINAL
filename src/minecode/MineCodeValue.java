package minecode;

public class MineCodeValue {
    private final MineCodeType type;
    private final Object value;

    public MineCodeValue(MineCodeType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public MineCodeType getType() { return type; }
    public Object getValue() { return value; }

    // Métodos de conversión
    public int asRedstone() { return ((Number) value).intValue(); }
    public double asEmerald() { return ((Number) value).doubleValue(); }
    public String asObsidian() { return value.toString(); }
    public boolean asNether() { return (Boolean) value; }

    public static MineCodeValue createRedstone(int value) {
        return new MineCodeValue(MineCodeType.REDSTONE, value);
    }

    public static MineCodeValue createEmerald(double value) {
        return new MineCodeValue(MineCodeType.EMERALD, value);
    }

    public static MineCodeValue createObsidian(String value) {
        return new MineCodeValue(MineCodeType.OBSIDIAN, value);
    }

    public static MineCodeValue createNether(boolean value) {
        return new MineCodeValue(MineCodeType.NETHER, value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}