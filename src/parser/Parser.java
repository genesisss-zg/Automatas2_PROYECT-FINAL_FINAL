package parser;

import lexer.*;
import ast.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private Token peekToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        nextToken();
        nextToken();
    }

    public ProgramNode parse() {
        ProgramNode program = new ProgramNode();
        
        while (currentToken.getType() != TokenType.EOF) {
            try {
                ASTNode declaration = parseDeclaration();
                if (declaration != null) {
                    program.addDeclaration(declaration);
                }
            } catch (Exception e) {
                System.err.println("Error de parsing: " + e.getMessage());
                synchronize();
            }
        }
        
        return program;
    }

    
    private boolean isTypeToken(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || 
               type == TokenType.STRING || type == TokenType.BOOLEAN ||
               type == TokenType.VOID;
    }
    

    private ASTNode parseDeclaration() {
        if (currentToken.getType() == TokenType.FUNCTION) {
            return parseFunctionDeclaration();
        } else if (currentToken.getType() == TokenType.VAR) {
            return parseVariableDeclaration();
        }
        return parseStatement();
    }

    private FunctionNode parseFunctionDeclaration() {
        expect(TokenType.FUNCTION);
        String functionName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.LEFT_PAREN);
        
        FunctionNode function = new FunctionNode(currentToken.getLine(), functionName, "void");
        expect(TokenType.RIGHT_PAREN);
        expect(TokenType.LEFT_BRACE);
        
        BlockNode body = parseBlock();
        function.setBody(body);
        
        return function;
    }

    private VariableDeclNode parseVariableDeclaration() {
        expect(TokenType.VAR);
        
        // Obtener nombre de la variable
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        
        // Esperar dos puntos
        expect(TokenType.COLON);
        
        // Obtener tipo (ahora es palabra reservada)
        Token typeToken = currentToken;
        if (isTypeToken(typeToken.getType())) {
            nextToken();
            String typeName = typeToken.getLexeme();
            
            ASTNode initialValue = null;
            if (match(TokenType.ASSIGN)) {
                initialValue = parseExpression();
            }
            
            expect(TokenType.SEMICOLON);
            return new VariableDeclNode(typeToken.getLine(), varName, typeName, initialValue);
        } else {
            throw new RuntimeException("Se esperaba tipo de dato, se encontró: " + typeToken.getType());
        }
    }

    private BlockNode parseBlock() {
        BlockNode block = new BlockNode(currentToken.getLine());
        
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            ASTNode stmt = parseStatement();
            if (stmt != null) {
                block.addStatement(stmt);
            }
        }
        
        expect(TokenType.RIGHT_BRACE);
        return block;
    }

    private ASTNode parseStatement() {
        if (check(TokenType.IF)) return parseIfStatement();
        if (check(TokenType.WHILE)) return parseWhileStatement();
        if (check(TokenType.RETURN)) return parseReturnStatement();
        if (check(TokenType.LEFT_BRACE)) return parseBlock();
        if (check(TokenType.IDENTIFIER) && peekToken.getType() == TokenType.ASSIGN) {
            return parseAssignment();
        }
        return parseExpressionStatement();
    }

    private ASTNode parseIfStatement() {
        expect(TokenType.IF);
        expect(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        
        BlockNode thenBlock = parseBlock();
        IfNode ifNode = new IfNode(currentToken.getLine(), condition, thenBlock);
        
        if (match(TokenType.ELSE)) {
            ifNode.setElseBlock(parseBlock());
        }
        
        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        expect(TokenType.WHILE);
        expect(TokenType.LEFT_PAREN);
        ASTNode condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        
        BlockNode body = parseBlock();
        return new WhileNode(currentToken.getLine(), condition, body);
    }

    private ASTNode parseReturnStatement() {
        expect(TokenType.RETURN);
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        expect(TokenType.SEMICOLON);
        return new ReturnNode(currentToken.getLine(), value);
    }

    private ASTNode parseAssignment() {
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        expect(TokenType.SEMICOLON);
        return new AssignmentNode(currentToken.getLine(), varName, value);
    }

    private ASTNode parseExpressionStatement() {
        ASTNode expr = parseExpression();
        expect(TokenType.SEMICOLON);
        return new ExpressionStatementNode(currentToken.getLine(), expr);
    }

    private ASTNode parseExpression() {
        try {
            return parseBinaryExpression(0);
        } catch (Exception e) {
            // Recuperación de errores: sincronizar hasta el siguiente statement
            synchronize();
            throw new RuntimeException("Expresión inválida: " + e.getMessage());
        }
}
    private ASTNode parseBinaryExpression(int precedence) {
        ASTNode left = parsePrimary();
        
        while (true) {
            Token op = currentToken;
            int opPrecedence = getPrecedence(op.getType());
            if (opPrecedence <= precedence) break;
            
            nextToken();
            ASTNode right = parseBinaryExpression(opPrecedence);
            left = new BinaryExpression(op.getLine(), left, op.getLexeme(), right);
        }
        
        return left;
    }

    private ASTNode parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new LiteralNode(currentToken.getLine(), currentToken.getLiteral());
        }
        if (match(TokenType.STRING_LITERAL)) {
            return new LiteralNode(currentToken.getLine(), currentToken.getLiteral());
        }
        if (match(TokenType.IDENTIFIER)) {
            if (check(TokenType.LEFT_PAREN)) {
                return parseFunctionCall(currentToken.getLexeme());
            }
            return new IdentifierNode(currentToken.getLine(), currentToken.getLexeme());
        }
        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }
        
        throw new RuntimeException("Expresión inválida");
    }

    private ASTNode parseFunctionCall(String functionName) {
        CallNode call = new CallNode(currentToken.getLine(), functionName);
        expect(TokenType.LEFT_PAREN);
        
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                call.addArgument(parseExpression());
            } while (match(TokenType.COMMA));
        }
        
        expect(TokenType.RIGHT_PAREN);
        return call;
    }

    private int getPrecedence(TokenType type) {
        switch (type) {
            case OR: return 1;
            case AND: return 2;
            case EQUALS: case NOT_EQUALS: return 3;
            case LESS: case GREATER: case LESS_EQUAL: case GREATER_EQUAL: return 4;
            case PLUS: case MINUS: return 5;
            case MULTIPLY: case DIVIDE: return 6;
            default: return 0;
        }
    }

    private Token expect(TokenType type) {
        if (currentToken.getType() == type) {
            return nextToken();
        }
        throw new RuntimeException("Se esperaba " + type + ", se encontró " + currentToken.getType());
    }

    private boolean check(TokenType type) {
        return currentToken.getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            nextToken();
            return true;
        }
        return false;
    }

    private Token nextToken() {
        Token prev = currentToken;
        currentToken = peekToken;
        peekToken = lexer.nextToken();
        return prev;
    }

    private void synchronize() {
        while (currentToken.getType() != TokenType.EOF) {
            if (currentToken.getType() == TokenType.SEMICOLON) {
                nextToken();
                return;
            }
            
            // Si encontramos el inicio de una nueva declaración, paramos
            if (currentToken.getType() == TokenType.FUNCTION || 
                currentToken.getType() == TokenType.VAR ||
                currentToken.getType() == TokenType.IF ||
                currentToken.getType() == TokenType.WHILE ||
                currentToken.getType() == TokenType.RETURN) {
                return;
            }
            
            nextToken();
        }
    }
}