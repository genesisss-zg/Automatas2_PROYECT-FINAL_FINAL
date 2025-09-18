package interpreter;

import ast.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Interpreter implements Evaluator {
    private Map<String, Object> variables;
    private Map<String, FunctionNode> functions;
    private Stack<Map<String, Object>> scopeStack;
    private int executionDepth;
    private static final int MAX_EXECUTION_DEPTH = 1000;

    public Interpreter() {
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(new HashMap<>()); // Scope global
        this.executionDepth = 0;
    }

    public void interpret(ProgramNode program) {
        // Registrar funciones primero
        for (ASTNode node : program.getDeclarations()) {
            if (node instanceof FunctionNode) {
                FunctionNode func = (FunctionNode) node;
                functions.put(func.getFunctionName(), func);
            }
        }

        // Ejecutar código global
        for (ASTNode node : program.getDeclarations()) {
            if (!(node instanceof FunctionNode)) {
                evaluate(node);
            }
        }
    }

    public Object evaluate(ASTNode node) {
        if (node instanceof AssignmentNode) return evaluate((AssignmentNode) node);
        if (node instanceof BinaryExpression) return evaluate((BinaryExpression) node);
        if (node instanceof BlockNode) return evaluate((BlockNode) node);
        if (node instanceof CallNode) return evaluate((CallNode) node);
        if (node instanceof ExpressionStatementNode) return evaluate((ExpressionStatementNode) node);
        if (node instanceof FunctionNode) return evaluate((FunctionNode) node);
        if (node instanceof IdentifierNode) return evaluate((IdentifierNode) node);
        if (node instanceof IfNode) return evaluate((IfNode) node);
        if (node instanceof LiteralNode) return evaluate((LiteralNode) node);
        if (node instanceof PrintNode) return evaluate((PrintNode) node);
        if (node instanceof ProgramNode) return evaluate((ProgramNode) node);
        if (node instanceof ReturnNode) return evaluate((ReturnNode) node);
        if (node instanceof TypeNode) return evaluate((TypeNode) node);
        if (node instanceof VariableDeclNode) return evaluate((VariableDeclNode) node);
        if (node instanceof WhileNode) return evaluate((WhileNode) node);
        return null;
    }

    private void checkExecutionDepth() {
        if (executionDepth++ > MAX_EXECUTION_DEPTH) {
            throw new RuntimeException("Profundidad de ejecución excedida");
        }
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return value != null;
    }

    @Override
    public Object evaluate(AssignmentNode node) {
        Object value = evaluate(node.getValue());
        scopeStack.peek().put(node.getVariableName(), value);
        return value;
    }

    @Override
    public Object evaluate(BinaryExpression node) {
        Object left = evaluate(node.getLeft());
        Object right = evaluate(node.getRight());

        // Verificar tipos
        if (!(left instanceof Number) || !(right instanceof Number)) {
            throw new RuntimeException("Operación numérica inválida con tipos no numéricos");
        }

        double leftNum = ((Number) left).doubleValue();
        double rightNum = ((Number) right).doubleValue();

        switch (node.getOperator()) {
            case "+": return leftNum + rightNum;
            case "-": return leftNum - rightNum;
            case "*": return leftNum * rightNum;
            case "/": 
                if (rightNum == 0) throw new RuntimeException("División por cero");
                return leftNum / rightNum;
            case "<": return leftNum < rightNum;
            case ">": return leftNum > rightNum;
            case "<=": return leftNum <= rightNum;
            case ">=": return leftNum >= rightNum;
            case "==": return left.equals(right);
            case "!=": return !left.equals(right);
            default: 
                throw new RuntimeException("Operador no soportado: " + node.getOperator());
        }
    }

    @Override
    public Object evaluate(BlockNode node) {
        scopeStack.push(new HashMap<>());
        Object result = null;
        for (ASTNode stmt : node.getStatements()) {
            result = evaluate(stmt);
        }
        scopeStack.pop();
        return result;
    }

    @Override
    public Object evaluate(CallNode node) {
        FunctionNode function = functions.get(node.getFunctionName());
        if (function == null) {
            throw new RuntimeException("Función no encontrada: " + node.getFunctionName());
        }

        // Guardar scope actual
        Map<String, Object> currentScope = scopeStack.peek();
        
        // Crear nuevo scope para la función
        scopeStack.push(new HashMap<>());
        
        // Ejecutar cuerpo de la función
        Object result = null;
        if (function.getBody() != null) {
            result = evaluate(function.getBody());
        }
        
        // Restaurar scope
        scopeStack.pop();
        scopeStack.push(currentScope);
        
        return result;
    }

    @Override
    public Object evaluate(ExpressionStatementNode node) {
        return evaluate(node.getExpression());
    }

    @Override
    public Object evaluate(FunctionNode node) {
        // Las funciones se registran pero no se ejecutan directamente
        functions.put(node.getFunctionName(), node);
        return null;
    }

    @Override
    public Object evaluate(IdentifierNode node) {
        // Buscar en scopes desde el más interno al más externo
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, Object> scope = scopeStack.get(i);
            if (scope.containsKey(node.getName())) {
                return scope.get(node.getName());
            }
        }
        throw new RuntimeException("Variable no definida: " + node.getName());
    }

    @Override
    public Object evaluate(IfNode node) {
        Object condition = evaluate(node.getCondition());
        if (isTruthy(condition)) {
            return evaluate(node.getThenBlock());
        } else if (node.getElseBlock() != null) {
            return evaluate(node.getElseBlock());
        }
        return null;
    }

    @Override
    public Object evaluate(LiteralNode node) {
        return node.getValue();
    }

    @Override
    public Object evaluate(PrintNode node) {
        Object value = evaluate(node.getValue());
        System.out.println(value);
        return value;
    }

    @Override
    public Object evaluate(ProgramNode node) {
        Object result = null;
        for (ASTNode declaration : node.getDeclarations()) {
            result = evaluate(declaration);
        }
        return result;
    }

    @Override
    public Object evaluate(ReturnNode node) {
        Object value = node.getValue() != null ? evaluate(node.getValue()) : null;
        throw new ReturnException(value);
    }

    @Override
    public Object evaluate(TypeNode node) {
        return null;
    }

    @Override
    public Object evaluate(VariableDeclNode node) {
        Object value = null;
        if (node.getInitialValue() != null) {
            value = evaluate(node.getInitialValue());
        }
        scopeStack.peek().put(node.getVariableName(), value);
        return value;
    }

    @Override
    public Object evaluate(WhileNode node) {
        Object result = null;
        while (true) {
            Object condition = evaluate(node.getCondition());
            if (!isTruthy(condition)) {
                break;
            }
            result = evaluate(node.getBody());
            
            // Control de profundidad para bucles infinitos
            checkExecutionDepth();
        }
        return result;
    }

    // Clase interna para manejar returns
    private static class ReturnException extends RuntimeException {
        private final Object value;
        
        public ReturnException(Object value) {
            this.value = value;
        }
        
        public Object getValue() {
            return value;
        }
    }
}