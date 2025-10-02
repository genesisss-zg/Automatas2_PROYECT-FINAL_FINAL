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
        System.out.println("PARSER: Starting parsing");
        
        while (currentToken.getType() != TokenType.EOF) {
            try {
                System.out.println("PARSER: Current token: " + currentToken.getType() + " - " + currentToken.getLexeme());
                ASTNode declaration = parseDeclaration();
                if (declaration != null) {
                    program.addDeclaration(declaration);
                    System.out.println("PARSER: Declaration added: " + declaration.getClass().getSimpleName());
                }
            } catch (Exception e) {
                System.err.println("PARSER ERROR: " + e.getMessage());
                synchronize();
            }
        }
        
        System.out.println("PARSER: Finished parsing");
        return program;
    }

    private ASTNode parseDeclaration() {
        if (currentToken.getType() == TokenType.ENCHANT_FUNC) {
            return parseFunctionDeclaration();
        } else if (currentToken.getType() == TokenType.VAR) {
            return parseVariableDeclaration();
        }
        return parseStatement();
    }

    private FunctionNode parseFunctionDeclaration() {
        expect(TokenType.ENCHANT_FUNC);
        String functionName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.LEFT_PAREN);
        
        FunctionNode function = new FunctionNode(currentToken.getLine(), functionName, "void");
        
        // Parse parameters
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                String paramName = expect(TokenType.IDENTIFIER).getLexeme();
                function.addParameter(new IdentifierNode(currentToken.getLine(), paramName));
            } while (match(TokenType.COMMA));
        }
        
        expect(TokenType.RIGHT_PAREN);
        
        // Tipo de retorno opcional
        String returnType = "void";
        if (match(TokenType.COLON)) {
            returnType = expect(TokenType.IDENTIFIER).getLexeme();
        }
        
        expect(TokenType.CRAFTING_TABLE);
        BlockNode body = parseBlock();
        function.setBody(body);
        
        return function;
    }

    private VariableDeclNode parseVariableDeclaration() {
        expect(TokenType.VAR);
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        
        String typeName = "auto";
        if (match(TokenType.COLON)) {
            typeName = expect(TokenType.IDENTIFIER).getLexeme();
        }
        
        ASTNode initialValue = null;
        if (match(TokenType.ASSIGN)) {
            initialValue = parseExpression();
        }
        
        // En MineCode, las declaraciones de variables pueden no terminar con ;
        if (match(TokenType.SEMICOLON)) {
            // Si hay ;, la consumimos, pero no es obligatoria
        }
        
        return new VariableDeclNode(currentToken.getLine(), varName, typeName, initialValue);
    }

    private BlockNode parseBlock() {
        BlockNode block = new BlockNode(currentToken.getLine());
        
        while (!check(TokenType.END_PORTAL) && !check(TokenType.EOF)) {
            ASTNode stmt = parseStatement();
            if (stmt != null) {
                block.addStatement(stmt);
                System.out.println("PARSER: Statement added to block: " + stmt.getClass().getSimpleName());
            }
        }
        
        expect(TokenType.END_PORTAL);
        return block;
    }

    private ASTNode parseStatement() {
        if (check(TokenType.REDSTONE_IF)) return parseIfStatement();
        if (check(TokenType.PISTON_LOOP)) return parseWhileStatement();
        if (check(TokenType.NETHER_RETURN)) return parseReturnStatement();
        if (check(TokenType.CRAFTING_TABLE)) return parseBlock();
        if (check(TokenType.PRINT)) return parsePrintStatement();
        
        if (check(TokenType.IDENTIFIER) && peekToken.getType() == TokenType.ASSIGN) {
            return parseAssignment();
        }
        
        return parseExpressionStatement();
    }

    private ASTNode parseIfStatement() {
        expect(TokenType.REDSTONE_IF);
        ASTNode condition = parseExpression();
        
        expect(TokenType.CRAFTING_TABLE);
        BlockNode thenBlock = parseBlock();
        IfNode ifNode = new IfNode(currentToken.getLine(), condition, thenBlock);
        
        if (match(TokenType.SLIME_ELSE)) {
            expect(TokenType.CRAFTING_TABLE);
            ifNode.setElseBlock(parseBlock());
        }
        
        return ifNode;
    }

    private ASTNode parseWhileStatement() {
        expect(TokenType.PISTON_LOOP);
        ASTNode condition = parseExpression();
        
        expect(TokenType.CRAFTING_TABLE);
        BlockNode body = parseBlock();
        return new WhileNode(currentToken.getLine(), condition, body);
    }

    private ASTNode parseReturnStatement() {
        expect(TokenType.NETHER_RETURN);
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.END_PORTAL) && !check(TokenType.CRAFTING_TABLE)) {
            value = parseExpression();
        }
        
        // En MineCode, return puede o no terminar con ;
        if (match(TokenType.SEMICOLON)) {
            // Si hay ;, la consumimos
        }
        
        return new ReturnNode(currentToken.getLine(), value);
    }

    private ASTNode parsePrintStatement() {
        expect(TokenType.PRINT);
        expect(TokenType.LEFT_PAREN);
        ASTNode value = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        
        // En MineCode, print puede o no terminar con ;
        if (match(TokenType.SEMICOLON)) {
            // Si hay ;, la consumimos, pero no es obligatoria
        }
        
        return new PrintNode(currentToken.getLine(), value);
    }

    private ASTNode parseAssignment() {
        String varName = expect(TokenType.IDENTIFIER).getLexeme();
        expect(TokenType.ASSIGN);
        ASTNode value = parseExpression();
        
        // En MineCode, las asignaciones pueden o no terminar con ;
        if (match(TokenType.SEMICOLON)) {
            // Si hay ;, la consumimos, pero no es obligatoria
        }
        
        return new AssignmentNode(currentToken.getLine(), varName, value);
    }

    private ASTNode parseExpressionStatement() {
        ASTNode expr = parseExpression();
        
        // En MineCode, las expresiones pueden o no terminar con ;
        if (match(TokenType.SEMICOLON)) {
            // Si hay ;, la consumimos, pero no es obligatoria
        }
        
        return new ExpressionStatementNode(currentToken.getLine(), expr);
    }

    private ASTNode parseExpression() {
        return parseBinaryExpression(0);
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
        if (match(TokenType.TORCH_ON)) {
            return new LiteralNode(currentToken.getLine(), true);
        }
        if (match(TokenType.TORCH_OFF)) {
            return new LiteralNode(currentToken.getLine(), false);
        }
        if (match(TokenType.IDENTIFIER)) {
            String identifierName = currentToken.getLexeme();
            if (check(TokenType.LEFT_PAREN)) {
                return parseFunctionCall(identifierName);
            }
            return new IdentifierNode(currentToken.getLine(), identifierName);
        }
        if (match(TokenType.LEFT_PAREN)) {
            ASTNode expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }
        
        throw new RuntimeException("Expresión inválida en línea " + currentToken.getLine() + 
                                 ", token: " + currentToken.getType() + " '" + currentToken.getLexeme() + "'");
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
        if (check(type)) {
            return nextToken();
        }
        throw new RuntimeException("Se esperaba " + type + ", se encontró " + currentToken.getType() + 
                                 " '" + currentToken.getLexeme() + "' en línea " + currentToken.getLine());
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
            // Puntos de sincronización para MineCode
            if (currentToken.getType() == TokenType.SEMICOLON) {
                nextToken();
                return;
            }
            
            // Nuevos puntos de sincronización para MineCode
            if (currentToken.getType() == TokenType.END_PORTAL ||
                currentToken.getType() == TokenType.CRAFTING_TABLE) {
                return;
            }
            
            // Inicios de declaración
            if (currentToken.getType() == TokenType.ENCHANT_FUNC || 
                currentToken.getType() == TokenType.VAR ||
                currentToken.getType() == TokenType.REDSTONE_IF ||
                currentToken.getType() == TokenType.PISTON_LOOP ||
                currentToken.getType() == TokenType.NETHER_RETURN ||
                currentToken.getType() == TokenType.PRINT) {
                return;
            }
            
            nextToken();
        }
    }
}