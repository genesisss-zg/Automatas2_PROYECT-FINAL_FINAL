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
        // PALABRAS CLAVE DE MINECODE
        keywords.put("crafting_table", TokenType.CRAFTING_TABLE);
        keywords.put("end_portal", TokenType.END_PORTAL);
        keywords.put("redstone_if", TokenType.REDSTONE_IF);
        keywords.put("slime_else", TokenType.SLIME_ELSE);
        keywords.put("piston_loop", TokenType.PISTON_LOOP);
        keywords.put("enchant_func", TokenType.ENCHANT_FUNC);
        keywords.put("nether_return", TokenType.NETHER_RETURN);
        keywords.put("torch_on", TokenType.TORCH_ON);
        keywords.put("torch_off", TokenType.TORCH_OFF);
        
        // PALABRAS CLAVE OPCIONALES
        keywords.put("var", TokenType.VAR);
        keywords.put("print", TokenType.PRINT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public Token nextToken() {
        skipWhitespace();
        start = current;

        if (isAtEnd()) {
            return makeToken(TokenType.EOF, "");
        }

        char c = source.charAt(current);
        current++;

        if (Character.isDigit(c)) {
            current--;
            return number();
        }
        if (Character.isLetter(c)) {
            current--;
            return identifier();
        }
        if (c == '"') {
            return string();
        }

        switch (c) {
            case '(': return makeToken(TokenType.LEFT_PAREN, null);
            case ')': return makeToken(TokenType.RIGHT_PAREN, null);
            case ',': return makeToken(TokenType.COMMA, null);
            case ';': return makeToken(TokenType.SEMICOLON, null);
            case ':': return makeToken(TokenType.COLON, null);
            
            case '+': return makeToken(TokenType.PLUS, null);
            case '-': 
                if (peek() == '>') {
                    current++;
                    return makeToken(TokenType.ASSIGN, null);
                }
                return makeToken(TokenType.MINUS, null);
            case '*': return makeToken(TokenType.MULTIPLY, null);
            case '/': 
                if (peek() == '/') {
                    current++;
                    while (peek() != '\n' && !isAtEnd()) {
                        current++;
                    }
                    return nextToken();
                }
                return makeToken(TokenType.DIVIDE, null);
            
            case '=': 
                if (peek() == '=') {
                    current++;
                    return makeToken(TokenType.EQUALS, null);
                }
                break;
                
            case '!': 
                if (peek() == '=') {
                    current++;
                    return makeToken(TokenType.NOT_EQUALS, null);
                }
                return makeToken(TokenType.NOT, null);
                
            case '<': 
                if (peek() == '=') {
                    current++;
                    return makeToken(TokenType.LESS_EQUAL, null);
                }
                return makeToken(TokenType.LESS, null);
                
            case '>': 
                if (peek() == '=') {
                    current++;
                    return makeToken(TokenType.GREATER_EQUAL, null);
                }
                return makeToken(TokenType.GREATER, null);
                
            case '&': 
                if (peek() == '&') {
                    current++;
                    return makeToken(TokenType.AND, null);
                }
                break;
                
            case '|': 
                if (peek() == '|') {
                    current++;
                    return makeToken(TokenType.OR, null);
                }
                break;
        }

        return makeToken(TokenType.ERROR, "");
    }

    private Token number() {
        while (Character.isDigit(peek())) {
            current++;
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            current++;
            while (Character.isDigit(peek())) {
                current++;
            }
        }

        String numberStr = source.substring(start, current);
        double value;
        if (numberStr.contains(".")) {
            value = Double.parseDouble(numberStr);
        } else {
            value = Integer.parseInt(numberStr);
        }
        return makeToken(TokenType.NUMBER, value);
    }

    private Token identifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            current++;
        }

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }
        return makeToken(type, text);
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            current++;
        }

        if (isAtEnd()) {
            return makeToken(TokenType.EOF, "");
        }

        current++;
        String value = source.substring(start + 1, current - 1);
        return makeToken(TokenType.STRING_LITERAL, value);
    }

    private Token makeToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        return new Token(type, lexeme, literal, line);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ': case '\r': case '\t': 
                    current++;
                    break;
                case '\n': 
                    line++;
                    current++;
                    break;
                default: 
                    return;
            }
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}