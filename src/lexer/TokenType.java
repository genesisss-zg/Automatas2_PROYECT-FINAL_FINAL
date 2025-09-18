package lexer;


public enum TokenType {
    // Palabras reservadas (agregar tipos)
    FUNCTION, VAR, IF, ELSE, WHILE, RETURN, PRINT,
    INT, FLOAT, STRING, BOOLEAN, VOID, // ← Tipos como palabras reservadas
    
    // Literales
    NUMBER, STRING_LITERAL, IDENTIFIER, TRUE, FALSE,
    
    // Operadores
    PLUS, MINUS, MULTIPLY, DIVIDE, ASSIGN, EQUALS, NOT_EQUALS,
    LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, AND, OR, NOT,
    
    // Símbolos
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, SEMICOLON, COLON,
    
    // Fin de archivo
    EOF
}