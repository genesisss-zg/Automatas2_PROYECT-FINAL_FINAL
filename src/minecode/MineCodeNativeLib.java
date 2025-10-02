package minecode;

import java.util.*;

public class MineCodeNativeLib {
    private static final Random random = new Random();

    // Funciones matemáticas
    public static MineCodeValue math_pow(List<MineCodeValue> args) {
        if (args.size() != 2) throw new RuntimeException("math_pow requiere 2 argumentos");
        double base = args.get(0).asEmerald();
        double exponent = args.get(1).asEmerald();
        return MineCodeValue.createEmerald(Math.pow(base, exponent));
    }

    public static MineCodeValue math_sqrt(List<MineCodeValue> args) {
        if (args.size() != 1) throw new RuntimeException("math_sqrt requiere 1 argumento");
        double value = args.get(0).asEmerald();
        return MineCodeValue.createEmerald(Math.sqrt(value));
    }

    public static MineCodeValue math_random(List<MineCodeValue> args) {
        return MineCodeValue.createEmerald(random.nextDouble());
    }

    // Funciones de cadena
    public static MineCodeValue string_length(List<MineCodeValue> args) {
        if (args.size() != 1) throw new RuntimeException("string_length requiere 1 argumento");
        String str = args.get(0).asObsidian();
        return MineCodeValue.createRedstone(str.length());
    }

    public static MineCodeValue string_upper(List<MineCodeValue> args) {
        if (args.size() != 1) throw new RuntimeException("string_upper requiere 1 argumento");
        String str = args.get(0).asObsidian();
        return MineCodeValue.createObsidian(str.toUpperCase());
    }

    public static MineCodeValue string_lower(List<MineCodeValue> args) {
        if (args.size() != 1) throw new RuntimeException("string_lower requiere 1 argumento");
        String str = args.get(0).asObsidian();
        return MineCodeValue.createObsidian(str.toLowerCase());
    }

    // Funciones de mundo MineCode
    public static MineCodeValue world_time(List<MineCodeValue> args) {
        return MineCodeValue.createRedstone((int) (System.currentTimeMillis() % 24000));
    }

    public static MineCodeValue world_weather(List<MineCodeValue> args) {
        String[] weathers = {"soleado", "lluvioso", "tormenta"};
        return MineCodeValue.createObsidian(weathers[random.nextInt(weathers.length)]);
    }

    // Registro de funciones nativas
    public static final Map<String, NativeFunction> NATIVE_FUNCTIONS = new HashMap<>();
    
    static {
        NATIVE_FUNCTIONS.put("math_pow", MineCodeNativeLib::math_pow);
        NATIVE_FUNCTIONS.put("math_sqrt", MineCodeNativeLib::math_sqrt);
        NATIVE_FUNCTIONS.put("math_random", MineCodeNativeLib::math_random);
        NATIVE_FUNCTIONS.put("string_length", MineCodeNativeLib::string_length);
        NATIVE_FUNCTIONS.put("string_upper", MineCodeNativeLib::string_upper);
        NATIVE_FUNCTIONS.put("string_lower", MineCodeNativeLib::string_lower);
        NATIVE_FUNCTIONS.put("world_time", MineCodeNativeLib::world_time);
        NATIVE_FUNCTIONS.put("world_weather", MineCodeNativeLib::world_weather);
    }

    @FunctionalInterface
    public interface NativeFunction {
        MineCodeValue execute(List<MineCodeValue> args);
    }
}