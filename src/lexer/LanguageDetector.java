package lexer;

public class LanguageDetector {
    public static boolean isJavaCode(String code) {
        String[] javaPatterns = {
            "public\\s+class", 
            "private\\s+", 
            "protected\\s+", 
            "import\\s+java", 
            "System\\.out\\.print", 
            "void\\s+main", 
            "new\\s+\\w+\\(",
            "package\\s+",
            "class\\s+\\w+\\s*\\{",
            "this\\.",
            "extends\\s+",
            "implements\\s+"
        };
        
        for (String pattern : javaPatterns) {
            if (code.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isSimpleLangCode(String code) {
        String[] simpleLangPatterns = {
            "var\\s+\\w+\\s*:", 
            "function\\s+\\w+\\s*\\(", 
            "print\\s*\\(", 
            ":\\s*int\\b", 
            ":\\s*float\\b", 
            ":\\s*string\\b",
            ":\\s*boolean\\b"
        };
        
        for (String pattern : simpleLangPatterns) {
            if (code.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }
}

//CLASE PARA ANALIZAR CODIGO JAVA O SIMPLELANG