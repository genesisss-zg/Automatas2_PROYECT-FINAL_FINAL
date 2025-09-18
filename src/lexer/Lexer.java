package lexer;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    
    private static final Map<String, TokenType> keywords;


    static {
        keywords = new HashMap<>();
        // Palabras reservadas
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("var", TokenType.VAR);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("print", TokenType.PRINT);
        
        // Tipos de datos (agregados)
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("string", TokenType.STRING);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("void", TokenType.VOID);
        
        // Valores booleanos
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        
        // NUEVAS KEYWORDS PARA JAVA
       // keywords.put("public", TokenType.PUBLIC);
        //keywords.put("private", TokenType.PRIVATE);
        //keywords.put("protected", TokenType.PROTECTED);
        //keywords.put("class", TokenType.CLASS);
        //keywords.put("import", TokenType.IMPORT);
        //keywords.put("package", TokenType.PACKAGE);
        //keywords.put("static", TokenType.STATIC);
        //keywords.put("final", TokenType.FINAL);
        //keywords.put("this", TokenType.THIS);
        //keywords.put("new", TokenType.NEW);
        //keywords.put("extends", TokenType.EXTENDS);
        //keywords.put("implements", TokenType.IMPLEMENTS);
        //keywords.put("abstract", TokenType.ABSTRACT);
        //keywords.put("interface", TokenType.INTERFACE);
        //keywords.put("try", TokenType.TRY);
        //keywords.put("catch", TokenType.CATCH);
        //keywords.put("finally", TokenType.FINALLY);
        //keywords.put("throw", TokenType.THROW);
        //keywords.put("throws", TokenType.THROWS);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public Token nextToken() {
        skipWhitespace();
        start = current;

        if (isAtEnd()) return makeToken(TokenType.EOF, "");

        char c = advance();

        if (Character.isDigit(c)) return number();
        if (Character.isLetter(c)) return identifier();
        if (c == '"') return string();

        switch (c) {
            case '(': return makeToken(TokenType.LEFT_PAREN, null);
            case ')': return makeToken(TokenType.RIGHT_PAREN, null);
            case '{': return makeToken(TokenType.LEFT_BRACE, null);
            case '}': return makeToken(TokenType.RIGHT_BRACE, null);
            case ',': return makeToken(TokenType.COMMA, null);
            case ';': return makeToken(TokenType.SEMICOLON, null);
            case ':': return makeToken(TokenType.COLON, null);
            
            case '+': return makeToken(TokenType.PLUS, null);
            case '-': return makeToken(TokenType.MINUS, null);
            case '*': return makeToken(TokenType.MULTIPLY, null);
            case '/': return makeToken(TokenType.DIVIDE, null);
            
            case '=': return match('=') ? makeToken(TokenType.EQUALS, null) : makeToken(TokenType.ASSIGN, null);
            case '!': return match('=') ? makeToken(TokenType.NOT_EQUALS, null) : makeToken(TokenType.NOT, null);
            case '<': return match('=') ? makeToken(TokenType.LESS_EQUAL, null) : makeToken(TokenType.LESS, null);
            case '>': return match('=') ? makeToken(TokenType.GREATER_EQUAL, null) : makeToken(TokenType.GREATER, null);
            
            case '&': return match('&') ? makeToken(TokenType.AND, null) : null;
            case '|': return match('|') ? makeToken(TokenType.OR, null) : null;
        }

        return makeToken(TokenType.EOF, "");
    }

    private Token number() {
        while (Character.isDigit(peek())) advance();

        if (peek() == '.' && Character.isDigit(peekNext())) {
            advance();
            while (Character.isDigit(peek())) advance();
        }

        String numberStr = source.substring(start, current);
        double value = Double.parseDouble(numberStr);
        return makeToken(TokenType.NUMBER, value);
    }

    private Token identifier() {
        while (Character.isLetterOrDigit(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;

        return makeToken(type, text);
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) return makeToken(TokenType.EOF, "");

        advance(); // Consume the closing "
        String value = source.substring(start + 1, current - 1);
        return makeToken(TokenType.STRING_LITERAL, value);
    }

    private Token makeToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        return new Token(type, lexeme, literal, line);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ': case '\r': case '\t': advance(); break;
                case '\n': line++; advance(); break;
                case '/': 
                    if (peekNext() == '/') {
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else {
                        return;
                    }
                    break;
                default: return;
            }
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}