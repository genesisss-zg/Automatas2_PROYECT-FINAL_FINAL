package semantic;

import ast.*;
import util.ManejadorErrores;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer implements ASTVisitor {
    private ScopeManager scopeManager;
    private ManejadorErrores manejadorErrores;
    private String currentFunction;
    private Tipo currentReturnType;
    private Map<String, Boolean> analyzedFunctions;

    public SemanticAnalyzer() {
        this.scopeManager = new ScopeManager();
        this.manejadorErrores = new ManejadorErrores();
        this.currentFunction = "global";
        this.currentReturnType = Tipo.VOID;
        this.analyzedFunctions = new HashMap<>();
    }

    public void analyze(ProgramNode program) {
        try {
            // Primera pasada: declarar todas las funciones
            for (ASTNode declaration : program.getDeclarations()) {
                if (declaration instanceof FunctionNode) {
                    FunctionNode func = (FunctionNode) declaration;
                    Symbol funcSymbol = new Symbol(
                        func.getFunctionName(),
                        Tipo.fromString(func.getReturnType()),
                        null,
                        true
                    );
                    scopeManager.declareSymbol(func.getFunctionName(), funcSymbol);
                }
            }
            
            // Segunda pasada: analizar cuerpos
            for (ASTNode declaration : program.getDeclarations()) {
                declaration.accept(this);
            }
            
        } catch (Exception e) {
            manejadorErrores.agregarError(0, "Error durante análisis semántico: " + e.getMessage(), "Semántico");
        }
    }

    public ManejadorErrores getManejadorErrores() {
        return manejadorErrores;
    }

    @Override
    public void visit(ProgramNode node) {
        scopeManager.enterScope();
        for (ASTNode child : node.getDeclarations()) {
            child.accept(this);
        }
        scopeManager.exitScope();
    }

    @Override
    public void visit(FunctionNode node) {
        String functionName = node.getFunctionName();
        
        // Evitar re-analizar funciones ya procesadas
        if (analyzedFunctions.containsKey(functionName)) {
            return;
        }
        analyzedFunctions.put(functionName, true);

        String previousFunction = currentFunction;
        Tipo previousReturnType = currentReturnType;
        
        currentFunction = functionName;
        currentReturnType = Tipo.fromString(node.getReturnType());
        
        scopeManager.enterScope();
        
        // Analizar parámetros
        for (ASTNode param : node.getParameters()) {
            param.accept(this);
        }
        
        // Analizar cuerpo
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }
        
        scopeManager.exitScope();
        
        currentFunction = previousFunction;
        currentReturnType = previousReturnType;
    }

    @Override
    public void visit(VariableDeclNode node) {
        String varName = node.getVariableName();
        
        if (scopeManager.containsSymbol(varName)) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' ya declarada", "Semántico");
            return;
        }

        Symbol varSymbol = new Symbol(
            varName,
            Tipo.fromString(node.getType()),
            null,
            false
        );
        
        scopeManager.declareSymbol(varName, varSymbol);

        if (node.getInitialValue() != null) {
            node.getInitialValue().accept(this);
        }
    }

    @Override
    public void visit(AssignmentNode node) {
        String varName = node.getVariableName();
        
        // Verificar que la variable existe
        Symbol symbol = scopeManager.resolve(varName);
        if (symbol == null) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' no declarada", "Semántico");
            return;
        }

        // Verificar que no sea una función
        if (symbol.isFunction()) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "'" + varName + "' es una función, no una variable", "Semántico");
            return;
        }

        // Verificar el valor asignado
        node.getValue().accept(this);
    }

    @Override
    public void visit(ReturnNode node) {
        // Verificar que estamos dentro de una función
        if ("global".equals(currentFunction)) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Return fuera de función", "Semántico");
            return;
        }

        // Verificar tipo de retorno
        if (node.getValue() != null) {
            node.getValue().accept(this);
        } else if (currentReturnType != Tipo.VOID) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Función debe retornar un valor", "Semántico");
        }
    }

    @Override
    public void visit(BinaryExpression node) {
        // Solo visitar los operandos, NO tratar de resolver el operador
        node.getLeft().accept(this);
        node.getRight().accept(this);
        
        // Aquí podrías agregar verificación de tipos en el futuro
    }

    @Override
    public void visit(BlockNode node) {
        scopeManager.enterScope();
        for (ASTNode statement : node.getStatements()) {
            statement.accept(this);
        }
        scopeManager.exitScope();
    }

    @Override
    public void visit(CallNode node) {
        String functionName = node.getFunctionName();
        
        // Verificar que la función existe
        Symbol symbol = scopeManager.resolve(functionName);
        if (symbol == null) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Función '" + functionName + "' no declarada", "Semántico");
            return;
        }

        // Verificar que sea una función
        if (!symbol.isFunction()) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "'" + functionName + "' no es una función", "Semántico");
            return;
        }

        // Verificar argumentos
        for (ASTNode arg : node.getArguments()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        // Solo visitar la expresión
        node.getExpression().accept(this);
    }

    @Override
    public void visit(IdentifierNode node) {
        String varName = node.getName();
        
        // No verificar operadores o símbolos especiales
        if (isOperatorOrSpecialSymbol(varName)) {
            return;
        }
        
        // Verificar que la variable existe
        Symbol symbol = scopeManager.resolve(varName);
        if (symbol == null) {
            manejadorErrores.agregarError(node.getLineNumber(), 
                "Variable '" + varName + "' no declarada", "Semántico");
        }
    }

    @Override
    public void visit(IfNode node) {
        node.getCondition().accept(this);
        node.getThenBlock().accept(this);
        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }
    }

    @Override
    public void visit(LiteralNode node) {
        // Los literales no requieren verificación semántica
    }

    @Override
    public void visit(PrintNode node) {
        // Verificar que el valor a imprimir existe
        node.getValue().accept(this);
    }

    @Override
    public void visit(TypeNode node) {
        // Los nodos de tipo no requieren verificación semántica
    }

    @Override
    public void visit(WhileNode node) {
        node.getCondition().accept(this);
        node.getBody().accept(this);
    }

    // Método auxiliar para identificar operadores y símbolos especiales
    private boolean isOperatorOrSpecialSymbol(String name) {
        return name.equals("+") || name.equals("-") || name.equals("*") || 
               name.equals("/") || name.equals("=") || name.equals("==") ||
               name.equals("!=") || name.equals("<") || name.equals(">") ||
               name.equals("<=") || name.equals(">=") || name.equals("&&") ||
               name.equals("||") || name.equals("!") || name.equals(";") ||
               name.equals(":") || name.equals(",") || name.equals("(") ||
               name.equals(")") || name.equals("{") || name.equals("}") ||
               name.equals("'") || name.equals("\"");
    }
}